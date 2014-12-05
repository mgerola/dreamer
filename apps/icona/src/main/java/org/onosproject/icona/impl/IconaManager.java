package org.onosproject.icona.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Date;
import org.slf4j.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.icona.IconaService;
import org.onosproject.icona.channel.InterChannelService;
import org.onosproject.icona.channel.IntraChannelService;
import org.onosproject.icona.channel.impl.IntraChannel;
import org.onosproject.icona.store.Cluster;
import org.onosproject.icona.store.EndPoint;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.InterLink;
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
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkService;

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
    private static String clusterName = "DREAMER";
    private static IntraChannelService intraChannelService;
    private static MgmtHandler mgmtThread;

    private ApplicationId appId;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IconaStoreService storeService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected InterChannelService interChannelService;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.icona");
        log.info("Started with Application ID {}", appId.id());

        deviceService.addListener(new ManageDevices());
        linkService.addListener(new ManageLinks());
        mgmtThread = new MgmtHandler();
        mgmtThread.start();

        intraChannelService = new IntraChannel(storeService);

        loadStartUp();

    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
        mgmtThread.interrupt();
    }

    private void loadStartUp() {
        storeService.addCluster(new Cluster(getCusterName(), new Date()));

        for (Device device : deviceService.getDevices()) {
            for (Port devPort : deviceService.getPorts(device.id())) {
                if (!devPort.number().equals(PortNumber.LOCAL)
                        && devPort.number() != null) {
                    interChannelService.addEndPointEvent(clusterName,
                                                         device.id(),
                                                         devPort.number());
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
        log.info("ClusterName {}", storeService.getCluster(remoteclusterName));

        if (mastershipService.getLocalRole(localId) == MastershipRole.MASTER) {
            if (!remoteclusterName.isEmpty() && remoteclusterName != null
                    && storeService.getCluster(remoteclusterName) != null) {
                if (storeService.getInterLink(localId, localPort) == null) {
                    interChannelService.addInterLinkEvent(clusterName,
                                                          remoteclusterName,
                                                          localId, localPort,
                                                          remoteId, remotePort);

                    for (EndPoint endPoint : storeService.getEndPoints(localId)) {
                        interChannelService.remEndPointEvent(endPoint
                                .getClusterName(), endPoint.getId(), endPoint
                                .getPort());
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

    @Override
    public String getCusterName() {
        return clusterName;
    }

    class ManageLinks implements LinkListener {

        @Override
        public void event(LinkEvent event) {
            if (MastershipRole.MASTER == mastershipService.getLocalRole(event
                    .subject().src().deviceId())) {
                DeviceId srcId = event.subject().src().deviceId();
                PortNumber srcPort = event.subject().src().port();

                if (storeService.getEndPoint(srcId, srcPort) != null) {
                    interChannelService.remEndPointEvent(clusterName, srcId,
                                                         srcPort);

                }
            }

            if (MastershipRole.MASTER == mastershipService.getLocalRole(event
                    .subject().dst().deviceId())) {
                DeviceId dstId = event.subject().dst().deviceId();
                PortNumber dstPort = event.subject().dst().port();

                if (storeService.getEndPoint(dstId, dstPort) != null) {
                    interChannelService.remEndPointEvent(clusterName, dstId,
                                                         dstPort);

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
                    for (InterLink interlink : storeService.getInterLinks(id)) {
                        interChannelService
                                .remInterLinkEvent(interlink
                                                           .getSrcClusterName(),
                                                   interlink
                                                           .getSrcClusterName(),
                                                   interlink.getSrcId(),
                                                   interlink.getSrcPort(),
                                                   interlink.getDstId(),
                                                   interlink.getSrcPort());
                    }
                    for (EndPoint endPoint : storeService.getEndPoints(id)) {
                        interChannelService.remEndPointEvent(endPoint
                                .getClusterName(), endPoint.getId(), endPoint
                                .getPort());
                    }
                    break;

                case PORT_ADDED:
                    if (!event.port().number().equals(PortNumber.LOCAL)
                            && storeService.getInterLink(id, event.port()
                                    .number()) == null
                            && !isLink(id, event.port().number())) {
                        interChannelService.addEndPointEvent(clusterName, id,
                                                             event.port()
                                                                     .number());
                    }
                    break;

                case PORT_REMOVED:
                    // If it was a IL or an EP, an "IL or EP remove" is
                    // published.
                    if (storeService.getInterLink(id, event.port().number()) != null) {
                        InterLink interLink = storeService
                                .getInterLink(id, event.port().number());
                        interChannelService
                                .remInterLinkEvent(interLink
                                                           .getSrcClusterName(),
                                                   interLink
                                                           .getDstClusterName(),
                                                   interLink.getSrcId(),
                                                   interLink.getSrcPort(),
                                                   interLink.getDstId(),
                                                   interLink.getDstPort());

                    }

                    if (storeService.getEndPoint(id, event.port().number()) != null) {
                        EndPoint endPointEvent = storeService
                                .getEndPoint(id, event.port().number());

                        interChannelService
                                .remEndPointEvent(endPointEvent
                                        .getClusterName(), endPointEvent
                                        .getId(), endPointEvent.getPort());
                    }
                    break;

                case PORT_UPDATED:
                    // If the port is not up and an IL exists, an "IL remove" is
                    // published.
                    if (!event.port().isEnabled()) {
                        if (storeService
                                .getInterLink(id, event.port().number()) != null) {

                            InterLink interLink = storeService
                                    .getInterLink(id, event.port().number());
                            interChannelService
                                    .remInterLinkEvent(interLink
                                                               .getSrcClusterName(),
                                                       interLink
                                                               .getDstClusterName(),
                                                       interLink.getSrcId(),
                                                       interLink.getSrcPort(),
                                                       interLink.getDstId(),
                                                       interLink.getDstPort());
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
                    interChannelService.helloManagement(new Date(),
                                                        getCusterName());
                    Collection<Cluster> oldCluster = storeService
                            .remOldCluster(helloInterval * deadOccurence);
                    if (!oldCluster.isEmpty()) {
                        for (Cluster cluster : oldCluster) {
                            log.warn("Cluster {} is down: no HELLO received in the last {} milliseconds",
                                     cluster.getClusterName(), helloInterval
                                             * deadOccurence);
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
}
