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
import org.onosproject.net.intent.IntentService;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.LeadershipService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.icona.BFSTree;
import org.onosproject.icona.IconaConfigService;
import org.onosproject.icona.IconaService;
import org.onosproject.icona.InterClusterPath;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentReplayType;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentRequestType;
import org.onosproject.icona.channel.inter.InterChannelService;
import org.onosproject.icona.channel.intra.IconaIntraEvent;
import org.onosproject.icona.channel.intra.IntraChannelService;
import org.onosproject.icona.channel.intra.IntraPseudoWireElement;
import org.onosproject.icona.store.Cluster;
import org.onosproject.icona.store.EndPoint;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.InterLink;
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
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.PointToPointIntent;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkService;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;

@Component(immediate = true)
@Service
public class IconaManager implements IconaService {

    private final Logger log = getLogger(getClass());

    // ICONA interval to send HELLO
    private static int helloInterval = 1000;
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
    protected LeadershipService leadershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterService clusterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IconaStoreService iconaStoreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected InterChannelService interChannelService;
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IconaConfigService iconaConfigService;

    @Activate
    public void activate() {
        log.info("Starting icona manager");
        deviceService.addListener(new ManageDevices());
        linkService.addListener(new ManageLinks());
        mgmtThread = new MgmtHandler();
        mgmtThread.start();

        leadershipService.runForLeadership(iconaConfigService.getIconaLeaderPath());
        loadStartUp();

    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
        mgmtThread.interrupt();
    }

    private void loadStartUp() {
        iconaStoreService.addCluster(new Cluster(iconaConfigService.getClusterName(), new Date()));
        interChannelService.addCluster(iconaConfigService.getClusterName());
        for (Device device : deviceService.getDevices()) {
            for (Port devPort : deviceService.getPorts(device.id())) {
                if (!devPort.number().equals(PortNumber.LOCAL)
                        && devPort.number() != null
                        && !isLink(device.id(), devPort.number())
                        && iconaStoreService.getInterLinks(device.id())
                                .isEmpty()) {
                    interChannelService
                            .addEndPointEvent(iconaConfigService.getClusterName(),
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
        log.info("ClusterName {}",
                 iconaStoreService.getCluster(remoteclusterName));

        if (mastershipService.getLocalRole(localId) == MastershipRole.MASTER) {
            if (!remoteclusterName.isEmpty() && remoteclusterName != null
                    && iconaStoreService.getCluster(remoteclusterName) != null) {
                if (iconaStoreService.getInterLink(localId, localPort) == null) {
                    interChannelService
                            .addInterLinkEvent(iconaConfigService.getClusterName(),
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

                if (iconaStoreService.getEndPoint(srcId, srcPort) != null) {
                    interChannelService.remEndPointEvent(iconaStoreService
                            .getEndPoint(srcId, srcPort));

                }
            }

            if (MastershipRole.MASTER == mastershipService.getLocalRole(event
                    .subject().dst().deviceId())) {
                DeviceId dstId = event.subject().dst().deviceId();
                PortNumber dstPort = event.subject().dst().port();

                if (iconaStoreService.getEndPoint(dstId, dstPort) != null) {
                    interChannelService.remEndPointEvent(iconaStoreService
                            .getEndPoint(dstId, dstPort));

                }

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
                        interChannelService
                                .addEndPointEvent(iconaConfigService.getClusterName(),
                                                  new ConnectPoint(id, event
                                                          .port().number()));
                    }
                    break;

                case PORT_REMOVED:
                    // If it was a IL or an EP, an "IL or EP remove" is
                    // published.
                    if (iconaStoreService.getInterLink(id, event.port()
                            .number()) != null) {
                        InterLink interLink = iconaStoreService
                                .getInterLink(id, event.port().number());
                        interChannelService
                                .remInterLinkEvent(interLink.srcClusterName(),
                                                   interLink.dstClusterName(),
                                                   interLink.src(),
                                                   interLink.dst());

                    }

                    if (iconaStoreService
                            .getEndPoint(id, event.port().number()) != null) {
                        EndPoint endPointEvent = iconaStoreService
                                .getEndPoint(id, event.port().number());

                        interChannelService.remEndPointEvent(endPointEvent);
                    }
                    break;

                case PORT_UPDATED:
                    // If the port is not up and an IL exists, an "IL remove" is
                    // published.
                    if (!event.port().isEnabled()) {
                        if (iconaStoreService.getInterLink(id, event.port()
                                .number()) != null) {

                            InterLink interLink = iconaStoreService
                                    .getInterLink(id, event.port().number());
                            interChannelService
                                    .remInterLinkEvent(interLink
                                                               .srcClusterName(),
                                                       interLink
                                                               .dstClusterName(),
                                                       interLink.src(),
                                                       interLink.dst());
                        }

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

    private class MgmtHandler extends Thread {

        @Override
        public void run() {
            // TODO: to be changed: add new hello is equal to update. I do not
            // need to remove previousHello and maybe I can use a simpler index

            // TODO: manage mastership! We cannot use the swtiches master...
            while (!isInterrupted()) {
                try {
                    // log.info("IconaLeader {}",
                    // leadershipService.getLeader(iconaLeaderPath));
                    if (leadershipService.getLeader(iconaConfigService.getIconaLeaderPath()) != null
                            && clusterService
                                    .getLocalNode()
                                    .id()
                                    .equals(leadershipService
                                                    .getLeader(iconaConfigService.getIconaLeaderPath()))) {

                        //log.info("Sono icona-leader");
                        interChannelService.helloManagement(new Date(),
                                                            iconaConfigService.getClusterName());
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
                    Thread.sleep(helloInterval);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
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
        EndPoint srcEndPoint = checkNotNull(iconaStoreService
                                                    .getEndPoint(DeviceId.deviceId(intraPW
                                                                         .srcId()),
                                                                 PortNumber
                                                                         .portNumber(intraPW
                                                                                 .srcPort())),
                                            "Source EndPoint does not exists");
        EndPoint dstEndPoint = checkNotNull(iconaStoreService
                                                    .getEndPoint(DeviceId.deviceId(intraPW
                                                                         .dstId()),
                                                                 PortNumber
                                                                         .portNumber(intraPW
                                                                                 .dstPort())),
                                            "Destination EndPoint does not exists");

        // TODO: publish the PW on the topology channel
        PseudoWire pw = new PseudoWire(srcEndPoint, dstEndPoint);
        checkArgument(iconaStoreService.addPseudoWire(pw));

        if (leadershipService.getLeader(iconaConfigService.getIconaLeaderPath()) != null
                && clusterService.getLocalNode().id()
                        .equals(leadershipService.getLeader(iconaConfigService.getIconaLeaderPath()))) {

            BFSTree geoTree = new BFSTree(
                                          iconaStoreService.getCluster(srcEndPoint
                                                  .clusterName()),
                                          iconaStoreService);
            Cluster dstCluster = iconaStoreService.getCluster(dstEndPoint
                    .clusterName());
            InterClusterPath interClusterPath = geoTree.getPath(dstCluster);

            // EPs are in the same cluster
            if (interClusterPath.getInterlinks().isEmpty()) {
                pw.addPseudoWireIntent(srcEndPoint, dstEndPoint,
                                       srcEndPoint.clusterName(),
                                       PathInstallationStatus.RECEIVED);

                // TODO: local intent, but can be remote...
                // Installation procedure...
            } else {

                // The list stores all the ILs in the "inverse"
                // order: from destination to source.
                List<InterLink> interLinks = interClusterPath.getInterlinks();

                // SrcEndPoint to last interLink
                pw.addPseudoWireIntent(srcEndPoint,
                                       interLinks.get(interLinks.size() - 1)
                                               .src(), srcEndPoint
                                               .clusterName(),
                                       PathInstallationStatus.RECEIVED);

                // DstEndPoint to first interlink
                pw.addPseudoWireIntent(interLinks.get(0).dst(), dstEndPoint,
                                       dstEndPoint.clusterName(),
                                       PathInstallationStatus.RECEIVED);

                // Interlinks in the middle
                for (int i = interLinks.size() - 1; i > 0; i--) {
                    pw.addPseudoWireIntent(interLinks.get(i).dst(), interLinks
                            .get(i - 1).src(), interLinks.get(i)
                            .dstClusterName(), PathInstallationStatus.RECEIVED);

                }
            }

            // Send the intents to the channel
            for (PseudoWireIntent pseudoWireIntent : pw.getIntents()) {
                interChannelService.addPseudoWireEvent(iconaConfigService.getClusterName(),
                                                  pw.getPseudoWireId(),
                                                  pseudoWireIntent,
                                                  IntentRequestType.RESERVE,
                                                  IntentReplayType.EMPTY);

            }

            pw.setPwStatus(PathInstallationStatus.INITIALIZED);
            log.info("PseudoWire sent to inter channel");
        }
    }
}
