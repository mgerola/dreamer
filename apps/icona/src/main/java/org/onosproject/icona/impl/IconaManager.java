package org.onosproject.icona.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.MacAddress;
import org.onosproject.icona.BFSTree;
import org.onosproject.icona.IconaConfigService;
import org.onosproject.icona.IconaService;
import org.onosproject.icona.InterClusterPath;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentReplayType;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentRequestType;
import org.onosproject.icona.channel.inter.InterChannelService;
import org.onosproject.icona.channel.intra.IconaIntraEvent;
import org.onosproject.icona.channel.intra.IntraChannelService;
import org.onosproject.icona.channel.intra.IntraPseudoWireElement;
import org.onosproject.icona.channel.intra.IntraPseudoWireElement.IntentUpdateType;
import org.onosproject.icona.store.Cluster;
import org.onosproject.icona.store.EndPoint;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.InterLink;
import org.onosproject.icona.store.MasterPseudoWire;
import org.onosproject.icona.store.PseudoWire;
import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
import org.onosproject.icona.store.PseudoWireIntent;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.MastershipRole;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkService;

import static com.google.common.base.Preconditions.checkNotNull;

@Component(immediate = true)
@Service
public class IconaManager implements IconaService {

    private final Logger log = getLogger(getClass());
    // TODO: manage the dealy: hazelcast migrate distributed register
    // and with low delay we have troubles...
    // ICONA interval to send HELLO
    private static int helloInterval = 3000;
    // If ICONA does not receive HELLO packets from another cluster for a period
    // of time
    // longer than the HELLO interval multiplied by
    // DEAD_OCCURRENCE, the cluster is considered dead.
    private static short deadOccurence = 3;

    private static IntraChannelService intraChannelService;
    private static MgmtHandler mgmtThread;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IconaStoreService iconaStoreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected InterChannelService interChannelService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IconaConfigService iconaConfigService;

    private class MgmtHandler extends Thread {

        @Override
        public void run() {
            // TODO: to be changed: add new hello is equal to update. I do not
            // need to remove previousHello and maybe I can use a simpler index

            // TODO: manage mastership! We cannot use the swtiches master...

            try {
                while (!isInterrupted()) {
                    if (iconaConfigService.isLeader()) {

                        // log.info("Sono icona-leader");
                        interChannelService
                                .helloManagement(new Date(), iconaConfigService
                                        .getClusterName());
                        Collection<Cluster> oldCluster = iconaStoreService
                                .getOldCluster(helloInterval * deadOccurence);
                        if (!oldCluster.isEmpty()) {
                            for (Cluster cluster : oldCluster) {
                                for (EndPoint endPoint : cluster.getEndPoints()) {
                                    interChannelService
                                            .remEndPointEvent(endPoint);
                                }
                                for (InterLink interLink : cluster
                                        .getInterLinks()) {
                                    interChannelService
                                            .remInterLinkEvent(interLink
                                                                       .srcClusterName(),
                                                               interLink
                                                                       .dstClusterName(),
                                                               interLink.src(),
                                                               interLink.dst());
                                }

                                interChannelService.remCluster(cluster
                                        .getClusterName());

                                log.warn("Cluster {} is down: no HELLO received in the last {} milliseconds",
                                         cluster.getClusterName(),
                                         helloInterval * deadOccurence);
                            }
                        }
                    }
                }
                Thread.sleep(helloInterval);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted", e);

            }
        }
    }

    @Activate
    public void activate() {
        log.info("Starting ICONA manager: Cluster {}", iconaConfigService.getClusterName());
        loadStartUp();
        deviceService.addListener(new ManageDevices());
        linkService.addListener(new ManageLinks());
        mgmtThread = new MgmtHandler();
        mgmtThread.start();



    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
        mgmtThread.interrupt();
    }

    private void loadStartUp() {
        interChannelService.addCluster(iconaConfigService.getClusterName());
        for (Device device : deviceService.getDevices()) {
            for (Port devPort : deviceService.getPorts(device.id())) {
                if (!devPort.number().equals(PortNumber.LOCAL)
                        && devPort.number() != null
                        && !isLink(device.id(), devPort.number())
                        && iconaStoreService.getInterLinks(device.id())
                                .isEmpty()) {
                    interChannelService
                            .addEndPointEvent(iconaConfigService
                                                      .getClusterName(),
                                              new ConnectPoint(device.id(),
                                                               devPort.number()));
                }
            }
        }
    }

    @Override
    public void handleELLDP(String remoteclusterName, DeviceId localId,
                            PortNumber localPort, DeviceId remoteId,
                            PortNumber remotePort) {

        log.info("Received ELLDP from cluster {}: local switch DPID {} and port {} "
                         + "and remote switch DPID {} and port {}",
                 remoteclusterName, localId, localPort, remoteId, remotePort);
        // Publish a new "IL add" and if an EPs exits, an "EP remove" is
        // published

        // TODO: some of these check sould be handle in the linkdiscorvery...
        if (mastershipService.getLocalRole(localId) == MastershipRole.MASTER) {
            if (!remoteclusterName.isEmpty() && remoteclusterName != null
                    && iconaStoreService.getCluster(remoteclusterName) != null
                    && localId != null) {
                if (!iconaStoreService.getInterLink(localId, localPort)
                        .isPresent()) {
                    interChannelService
                            .addInterLinkEvent(iconaConfigService
                                                       .getClusterName(),
                                               remoteclusterName,
                                               new ConnectPoint(localId,
                                                                localPort),
                                               new ConnectPoint(remoteId,
                                                                remotePort));

                    for (EndPoint endPoint : iconaStoreService
                            .getEndPoints(localId)) {
                        interChannelService.remEndPointEvent(endPoint);
                    }
                    // TODO: this is the inverse IL, but should be removed from
                    // the other cluster!
                    // if (!storeService.getEndPoints(remoteId).isEmpty()) {
                    // for (EndPoint endPoint :
                    // storeService.getEndPoints(remoteId)) {
                    // interChannelService.remEndPointEvent(endPoint.getClusterName(),
                    // endPoint.getId(), endPoint.getPort());
                    // }
                    // }

                }
            } else {
                log.debug("Received an LLDP from another ONOS cluster {} not actually registered with ICONA",
                          remoteclusterName);
            }
        }

    }

    class ManageLinks implements LinkListener {

        @Override
        public void event(LinkEvent event) {
            if (MastershipRole.MASTER == mastershipService.getLocalRole(event
                    .subject().src().deviceId())) {
                DeviceId srcId = event.subject().src().deviceId();
                PortNumber srcPort = event.subject().src().port();

                iconaStoreService.getEndPoint(srcId, srcPort)
                        .ifPresent(endpoint -> interChannelService
                                           .remEndPointEvent(endpoint));
            }

            if (MastershipRole.MASTER == mastershipService.getLocalRole(event
                    .subject().dst().deviceId())) {
                DeviceId dstId = event.subject().dst().deviceId();
                PortNumber dstPort = event.subject().dst().port();

                iconaStoreService.getEndPoint(dstId, dstPort)
                        .ifPresent(endpoint -> interChannelService
                                           .remEndPointEvent(endpoint));

            }
        }
    }

    private class ManageDevices implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {
            if (MastershipRole.MASTER == mastershipService.getLocalRole(event
                    .subject().id())) {
                DeviceId id = event.subject().id();

                switch (event.type()) {

                case DEVICE_ADDED:
                    log.info("Ports {}", deviceService.getPorts(id));
                    // TODO: needed?
                    // for (Port devPort : deviceService.getPorts(id)) {
                    // if (devPort.number() != PortNumber.LOCAL &&
                    // devPort.number() != null) {
                    // interChannelService
                    // .addEndPointEvent(clusterName, id,
                    // devPort.number());
                    // }
                    // }
                    break;

                case DEVICE_REMOVED:
                    for (InterLink interLink : iconaStoreService
                            .getInterLinks(id)) {
                        interChannelService
                                .remInterLinkEvent(interLink.srcClusterName(),
                                                   interLink.dstClusterName(),
                                                   interLink.src(),
                                                   interLink.dst());
                    }
                    for (EndPoint endPoint : iconaStoreService.getEndPoints(id)) {
                        interChannelService.remEndPointEvent(endPoint);
                    }
                    break;

                case PORT_ADDED:
                    if (!event.port().number().equals(PortNumber.LOCAL)
                            && iconaStoreService.getInterLinks(id).isEmpty()
                            && !isLink(id, event.port().number())) {
                        interChannelService.addEndPointEvent(iconaConfigService
                                .getClusterName(), new ConnectPoint(id, event
                                .port().number()));
                    }
                    break;

                case PORT_REMOVED:
                    // If it was a IL or an EP, an "IL or EP remove" is
                    // published.

                    iconaStoreService
                            .getInterLink(id, event.port().number())
                            .ifPresent(interLink -> interChannelService.remInterLinkEvent(interLink
                                                                                                  .srcClusterName(),
                                                                                          interLink
                                                                                                  .dstClusterName(),
                                                                                          interLink
                                                                                                  .src(),
                                                                                          interLink
                                                                                                  .dst()));

                    iconaStoreService.getEndPoint(id, event.port().number())
                            .ifPresent(endpoint -> interChannelService
                                               .remEndPointEvent(endpoint));

                    break;

                case PORT_UPDATED:
                    // If the port is not up and an IL exists, an "IL remove" is
                    // published.
                    if (!event.port().isEnabled()) {

                        Collection<PseudoWireIntent> localIntents = iconaStoreService.getLocalIntents(new ConnectPoint(id, event.port().number()));
                        if(localIntents != null) {
                            log.info("We need to reroute all the intents!");
                        }

                        iconaStoreService
                                .getInterLink(id, event.port().number())
                                .ifPresent(interLink -> interChannelService.remInterLinkEvent(interLink
                                                                                                      .srcClusterName(),
                                                                                              interLink
                                                                                                      .dstClusterName(),
                                                                                              interLink
                                                                                                      .src(),
                                                                                              interLink
                                                                                                      .dst()));

                    }
                    break;

                // TODO: do we need these cases?
                // case DEVICE_AVAILABILITY_CHANGED:
                // break;
                // case DEVICE_MASTERSHIP_CHANGED:
                // break;
                // case DEVICE_SUSPENDED:
                // break;
                // case DEVICE_UPDATED:
                // break;
                default:
                    break;

                }
            }
        }
    }

    private Boolean isLink(DeviceId id, PortNumber port) {
        if (linkService.getIngressLinks(new ConnectPoint(id, port)).isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    public void handlePseudoWire(IconaIntraEvent event) {

        IntraPseudoWireElement intraPW = event.intraPseudoWireElement();

        if(event.intraPseudoWireElement().intentUpdateType() == IntentUpdateType.INSTALL) {


        EndPoint srcEndPoint = iconaStoreService
                .getEndPoint(DeviceId.deviceId(intraPW.srcId()),
                             PortNumber.portNumber(intraPW.srcPort()))
                .orElseThrow(() -> new NullPointerException(
                                                            "Source EndPoint does not exists"));

        EndPoint dstEndPoint = iconaStoreService
                .getEndPoint(DeviceId.deviceId(intraPW.dstId()),
                             PortNumber.portNumber(intraPW.dstPort()))
                .orElseThrow(() -> new NullPointerException(
                                                            "Destination EndPoint does not exists"));
        
        MacAddress srcMac = MacAddress.valueOf(intraPW.macSrc());
        MacAddress dstMac = MacAddress.valueOf(intraPW.macDst());
        
        // TODO: Fix trafficSelector and TrafficTratement
        MasterPseudoWire pw = new MasterPseudoWire(srcEndPoint, dstEndPoint,
                                                   iconaConfigService
                                                           .getClusterName(),
                                                   DefaultTrafficSelector
                                                           .builder().build(),
                                                   DefaultTrafficTreatment
                                                           .builder().build());
        iconaStoreService.addMasterPseudoWire(pw);

        if (iconaConfigService.isLeader()) {

            // Publishing PW on the interchannel
            interChannelService
                    .addPseudoWireEvent(srcEndPoint, dstEndPoint,
                                        iconaConfigService.getClusterName(),
                                        PathInstallationStatus.RECEIVED, pw.getPseudoWireId());

            BFSTree geoTree = new BFSTree(
                                          iconaStoreService.getCluster(srcEndPoint
                                                  .clusterName()),
                                          iconaStoreService, null);

            Cluster dstCluster = iconaStoreService.getCluster(dstEndPoint
                    .clusterName());
            InterClusterPath interClusterPath = geoTree.getPath(dstCluster);
            checkNotNull(interClusterPath.getInterlinks());
            String ils = "";
            for (InterLink il : interClusterPath.getInterlinks()){

                ils = ils + " " + il.toString();
            }
            log.info("ILs PATH: {}", ils);
            pw.setInterClusterPath(interClusterPath);



            // EPs are in the same cluster
            if (interClusterPath.getInterlinks().isEmpty()) {
                pw.addPseudoWireIntent(srcEndPoint, dstEndPoint,
                                       srcEndPoint.clusterName(), srcMac , dstMac,
                                       PathInstallationStatus.RECEIVED, true,
                                       true);

                // Installation procedure...
            } else {

                // The list stores all the ILs in the "inverse"
                // order: from destination to source.
                List<InterLink> interLinks = interClusterPath.getInterlinks();

                pw.addPseudoWireIntent(srcEndPoint,
                                       interLinks.get(interLinks.size() - 1)
                                               .src(), srcEndPoint
                                               .clusterName(), srcMac, dstMac,
                                       PathInstallationStatus.RECEIVED, true,
                                       false);

                pw.addPseudoWireIntent(interLinks.get(0).dst(), dstEndPoint,
                                       dstEndPoint.clusterName(), srcMac, dstMac,
                                       PathInstallationStatus.RECEIVED, false,
                                       true);

                // Interlinks in the middle
                for (int i = interLinks.size() - 1; i > 0; i--) {
                    pw.addPseudoWireIntent(interLinks.get(i).dst(), interLinks
                                                   .get(i - 1).src(),
                                           interLinks.get(i).dstClusterName(),
                                           srcMac , dstMac,
                                           PathInstallationStatus.RECEIVED,
                                           false, false);

                }
            }

            // Send the intents to the channel
            for (PseudoWireIntent pseudoWireIntent : pw.getIntents()) {
                interChannelService
                        .addPseudoWireIntentEvent(iconaConfigService
                                                          .getClusterName(), pw
                                                          .getPseudoWireId(),
                                                  pseudoWireIntent,
                                                  IntentRequestType.RESERVE,
                                                  IntentReplayType.EMPTY);

            }


            pw.setPwStatus(PathInstallationStatus.INITIALIZED);
        }
        } else if (event.intraPseudoWireElement().intentUpdateType() == IntentUpdateType.DELETE) {
            ConnectPoint srcCP = new ConnectPoint(DeviceId.deviceId(intraPW.srcId()),
                    PortNumber.portNumber(intraPW.srcPort()));
            ConnectPoint dstCP = new ConnectPoint(DeviceId.deviceId(intraPW.srcId()),
                                              PortNumber.portNumber(intraPW.srcPort()));


            PseudoWire pw = iconaStoreService.getPseudoWire(iconaStoreService.getPseudoWireId(srcCP, dstCP));
            if (pw == null) {
                pw = iconaStoreService.getMasterPseudoWire(iconaStoreService.getPseudoWireId(srcCP, dstCP));
                if (pw == null) {
                    log.warn("Pseudowire does not exist: {}");
                    return;
                }
            }
            interChannelService.addPseudoWireEvent(pw);
        }
    }
}
