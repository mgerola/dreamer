package org.onlab.onos.icona;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.onos.core.ApplicationId;
import org.onlab.onos.core.CoreService;
import org.onlab.onos.icona.channel.impl.InterChannel;
import org.onlab.onos.icona.channel.impl.InterChannelService;
import org.onlab.onos.icona.store.Cluster;
import org.onlab.onos.icona.store.impl.IconaStore;
import org.onlab.onos.icona.store.impl.IconaStoreService;
import org.onlab.onos.mastership.MastershipService;
import org.onlab.onos.net.DeviceId;
import org.onlab.onos.net.MastershipRole;
import org.onlab.onos.net.device.DeviceEvent;
import org.onlab.onos.net.device.DeviceListener;
import org.onlab.onos.net.device.DeviceService;

@Component(immediate = true)
@Service(value = IconaService.class)
public class IconaManager implements IconaService, DeviceListener {

    private final Logger log = getLogger(getClass());

    // ICONA interval to send HELLO
    private static int helloInterval = 1000;
    // If ICONA does not receive HELLO packets from another cluster for a period
    // of time
    // longer than the HELLO interval multiplied by
    // DEAD_OCCURRENCE, the cluster is considered dead.
    private static short deadOccurence = 3;
    private static String clusterName = "DREAMER";
    private static InterChannelService interChannelService;
    private static IconaStoreService storeService;

    private ApplicationId appId;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onlab.onos.icona");
        log.info("Started with Application ID {}", appId.id());
        deviceService.addListener(this);
        storeService = new IconaStore();
        interChannelService = new InterChannel(storeService);
        new MgmtHandler().start();

        storeService.addCluster(new Cluster(getCusterName(), new Date()));

    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
    }

    @Override
    public void handleELLDP(String remoteclusterName, DeviceId localId,
                            long localPort, DeviceId remoteId, long remotePort) {

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
                    manageInterLinkAdded(remoteclusterName, localId, localPort,
                                         remoteId, remotePort);
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

    @Override
    public void event(DeviceEvent event) {
        switch (event.type()) {
        case DEVICE_ADDED:
            if (MastershipRole.MASTER == mastershipService.getLocalRole(event
                    .subject().id())) {

            } else {

            }
            break;
        // case DEVICE_AVAILABILITY_CHANGED:
        // break;
        // case DEVICE_MASTERSHIP_CHANGED:
        // break;
        case DEVICE_REMOVED:
            break;
        // case DEVICE_SUSPENDED:
        // break;
        // case DEVICE_UPDATED:
        // break;
        case PORT_ADDED:
            break;
        case PORT_REMOVED:
            break;
        case PORT_UPDATED:

            break;
        default:
            break;

        }
    }

    class MgmtHandler extends Thread {

        @Override
        public void run() {
            // TODO: to be changed: add new hello is equal to update. I do not
            // need to remove previousHello and maybe I can use a simpler index

            // TODO: manage mastership! We cannot use the swtiches master...
            while (true) {
                try {
                    interChannelService.helloManagement(new Date(),
                                                        getCusterName());
                    List<Cluster> oldCluster = storeService
                            .remOldCluster(helloInterval * deadOccurence);
                    if (oldCluster != null) {
                        for (Cluster cluster : oldCluster) {
                            log.warn("Cluster {} is down: no HELLO received in the last {} milliseconds",
                                     cluster.getClusterName(), helloInterval
                                             * deadOccurence);
                        }
                        // TODO: clean all info of the cluster: destroy
                        // ILs between this cluster and
                        // the dead one, remove all EPs.
                    }

                    Thread.sleep(helloInterval);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
    }

    private void manageInterLinkAdded(String dstClusterName, DeviceId localId,
                                      long srcPort, DeviceId remoteId,
                                      long dstPort) {

        interChannelService.addInterLinkEvent(clusterName, dstClusterName,
                                              localId, srcPort, remoteId,
                                              dstPort);
    }
}
