package org.onosproject.icona.channel.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.util.Date;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.icona.IconaIntentListener;
import org.onosproject.icona.IconaIntentEvent;
import org.onosproject.icona.channel.EndPointElement;
import org.onosproject.icona.channel.IconaManagementEvent;
import org.onosproject.icona.channel.IconaTopologyEvent;
import org.onosproject.icona.channel.InterChannelService;
import org.onosproject.icona.channel.InterLinkElement;
import org.onosproject.icona.channel.IconaManagementEvent.MessageType;
import org.onosproject.icona.store.Cluster;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.utils.BitSetIndex;
import org.onosproject.icona.utils.BitSetIndex.IndexType;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.slf4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@Component(immediate = true)
@Service
public class InterChannelManager implements InterChannelService {

    private final Logger log = getLogger(getClass());
    private Config interHazelcastConfig;
    private HazelcastInstance interHazelcastInstance;
    public static final String ICONA_INTER_HAZELCAST_CONFIG = "conf/hazelcast-icona-inter.xml";

    // Topology channel
    private static IMap<byte[], IconaTopologyEvent> topologyChannel;
    public static final String ICONA_TOPOLOGY_CHANNEL_NAME = "icona.topology";

    // Intent channel
    private static IMap<byte[], IconaIntentEvent> intentChannel;
    public static final String ICONA_INTENT_CHANNEL_NAME = "icona.intent";
    // Management channel
    private IMap<String, IconaManagementEvent> mgmtChannel;
    public static final String ICONA_MGMT_CHANNEL_NAME = "icona.mgmt";
    public static BitSetIndex mgmtEventCounter = null;
    private IconaManagementEvent oldHello;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IconaStoreService storeService;

    @Activate
    public void activate() {

        try {
            this.interHazelcastConfig = new FileSystemXmlConfig(
                                                                ICONA_INTER_HAZELCAST_CONFIG);
        } catch (FileNotFoundException e) {
            log.error("Error opening fall back Hazelcast XML configuration. "
                    + "File not found: " + ICONA_INTER_HAZELCAST_CONFIG, e);
            e.printStackTrace();
            this.interHazelcastConfig = new Config();
        }

        this.interHazelcastConfig.setInstanceName("ICONA-INTER");

        // TODO: check why it is needed...
        ClassLoader classLoader = this.getClass().getClassLoader();
        this.interHazelcastConfig.setClassLoader(classLoader);

        interHazelcastInstance = Hazelcast
                .getOrCreateHazelcastInstance(interHazelcastConfig);

        InterChannelManager.topologyChannel = interHazelcastInstance
                .getMap(ICONA_TOPOLOGY_CHANNEL_NAME);
        InterChannelManager.topologyChannel
                .addEntryListener(new IconaTopologyListener(storeService), true);

        InterChannelManager.intentChannel = interHazelcastInstance
                .getMap(ICONA_INTENT_CHANNEL_NAME);
        InterChannelManager.intentChannel
                .addEntryListener(new IconaIntentListener(), true);

        this.mgmtChannel = interHazelcastInstance
                .getMap(ICONA_MGMT_CHANNEL_NAME);
        this.mgmtChannel.addEntryListener(new IconaMgmtListener(storeService),
                                          true);
        InterChannelManager.mgmtEventCounter = new BitSetIndex(
                                                               IndexType.MGMT_CHANNEL);

        loadMgmt();
        loadTopology();

    }

    public void deactivate() {
        interHazelcastInstance.shutdown();

    }

    private void loadTopology() {
        // TODO: load EPs
        if (topologyChannel.values() != null) {
            for (IconaTopologyEvent event : topologyChannel.values()) {
                InterLinkElement interLinkEvent = event.getInterLinkElement();
                if (interLinkEvent != null) {

                    log.info("Load InterLink {}", interLinkEvent);
                    storeService.addInterLink(event.getClusterName(),
                                              interLinkEvent
                                                      .getRemoteClusterName(),
                                              interLinkEvent.getLocalId(),
                                              interLinkEvent.getLocalPort(),
                                              interLinkEvent.getRemoteId(),
                                              interLinkEvent.getRemotePort());
                }
                EndPointElement endPointEvent = event.getEntryPointElement();
                if (endPointEvent != null) {
                    log.info("Load InterLink {}", interLinkEvent);
                    storeService.addEndpoint(event.getClusterName(),
                                             endPointEvent.getDpid(),
                                             endPointEvent.getPortNumber());
                }
            }
        }

    }

    private void loadMgmt() {
        if (mgmtChannel.values() != null) {
            for (IconaManagementEvent event : mgmtChannel.values()) {
                if (storeService.getCluster(event.getClusterName()) != null) {
                    storeService.getCluster(event.getClusterName())
                            .setLastSeen(event.getTimeStamp());
                    continue;
                }
                storeService.addCluster(new Cluster(event.getClusterName(),
                                                    event.getTimeStamp()));
            }
        }

    }

    @Override
    public void helloManagement(Date date, String clusterName) {
        IconaManagementEvent hello = new IconaManagementEvent(
                                                              clusterName,
                                                              MessageType.HELLO,
                                                              mgmtEventCounter
                                                                      .getNewIndex());

        mgmtChannel.put(hello.getid(), hello);
        if (oldHello != null) {
            // log.debug("Removing previous HELLO sent {}",
            // previousHello.getid());
            if (mgmtChannel.remove(oldHello.getid(), oldHello)) {
                mgmtEventCounter.releaseIndex(oldHello.getCounter());
            }
        }
        oldHello = hello;

    }

    @Override
    public void addInterLinkEvent(String srcClusterName, String dstClusterName,
                                  DeviceId srcId, PortNumber srcPort,
                                  DeviceId dstId, PortNumber dstPort) {

        InterLinkElement interLinkEvent = new InterLinkElement(dstClusterName,
                                                               srcId, srcPort,
                                                               dstId, dstPort);
        IconaTopologyEvent iLEvent = new IconaTopologyEvent(interLinkEvent,
                                                            srcClusterName);
        topologyChannel.put(iLEvent.getID(), iLEvent);

    }

    @Override
    public void remInterLinkEvent(String srcClusterName, String dstClusterName,
                                  DeviceId srcId, PortNumber srcPort,
                                  DeviceId dstId, PortNumber dstPort) {
        InterLinkElement interLinkEvent = new InterLinkElement(dstClusterName,
                                                               srcId, srcPort,
                                                               dstId, dstPort);
        IconaTopologyEvent iLEvent = new IconaTopologyEvent(interLinkEvent,
                                                            srcClusterName);
        topologyChannel.remove(iLEvent.getID());

    }

    @Override
    public void addEndPointEvent(String clusterName, DeviceId id,
                                 PortNumber port) {
        EndPointElement endPointEvent = new EndPointElement(id, port);
        IconaTopologyEvent ePEvent = new IconaTopologyEvent(endPointEvent,
                                                            clusterName);
        log.info("Publishing EntryPoint added: {}", endPointEvent.toString());
        topologyChannel.put(ePEvent.getID(), ePEvent);

    }

    @Override
    public void remEndPointEvent(String clusterName, DeviceId id,
                                 PortNumber port) {
        EndPointElement endPointEvent = new EndPointElement(id, port);
        IconaTopologyEvent ePEvent = new IconaTopologyEvent(endPointEvent,
                                                            clusterName);
        log.info("Publishing EntryPoint removal: {}", endPointEvent.toString());
        topologyChannel.remove(ePEvent.getID());

    }

}
