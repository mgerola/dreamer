package org.onlab.onos.icona.channel.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.util.Date;

import org.onlab.onos.icona.IconaIntentListener;
import org.onlab.onos.icona.IconaIntentEvent;
import org.onlab.onos.icona.IconaService;
import org.onlab.onos.icona.channel.IconaManagementEvent;
import org.onlab.onos.icona.channel.IconaTopologyEvent;
import org.onlab.onos.icona.channel.InterLinkElement;
import org.onlab.onos.icona.channel.IconaManagementEvent.MessageType;
import org.onlab.onos.icona.store.Cluster;
import org.onlab.onos.icona.store.impl.IconaStoreService;
import org.onlab.onos.icona.utils.BitSetIndex;
import org.onlab.onos.icona.utils.BitSetIndex.IndexType;
import org.onlab.onos.net.DeviceId;
import org.slf4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class InterChannel implements InterChannelService {

    private final Logger log = getLogger(getClass());
    private Config interHazelcastConfig;
    private HazelcastInstance interHazelcastInstance;
    public static final String ICONA_INTER_HAZELCAST_CONFIG = "conf/hazelcast-icona-inter.xml";
    private IconaStoreService storeService;

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
   

    public InterChannel(IconaStoreService storeService) {
        this.storeService = storeService;
        
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

        InterChannel.topologyChannel = interHazelcastInstance
                .getMap(ICONA_TOPOLOGY_CHANNEL_NAME);
        InterChannel.topologyChannel
                .addEntryListener(new IconaTopologyListener(storeService), true);

        InterChannel.intentChannel = interHazelcastInstance
                .getMap(ICONA_INTENT_CHANNEL_NAME);
        InterChannel.intentChannel.addEntryListener(new IconaIntentListener(),
                                                    true);

        this.mgmtChannel = interHazelcastInstance
                .getMap(ICONA_MGMT_CHANNEL_NAME);
        this.mgmtChannel.addEntryListener(new IconaMgmtListener(storeService),
                                          true);
        InterChannel.mgmtEventCounter = new BitSetIndex(IndexType.MGMT_CHANNEL);
        
        loadMgmt();
        loadTopology();

    }

    private void loadTopology() {
        //TODO: load EPs
        if (topologyChannel.values() != null) {
            for (IconaTopologyEvent event : topologyChannel.values()) {
                InterLinkElement interLinkEvent = event.getInterLinkElement();
                if (interLinkEvent != null) {

                    log.info("Load InterLink {}", interLinkEvent);
                    storeService.addInterLink(event.getClusterName(),
                                              interLinkEvent.getRemoteClusterName(),
                                              interLinkEvent.getLocalId(),
                                              interLinkEvent.getLocalPort(),
                                              interLinkEvent.getRemoteId(),
                                              interLinkEvent.getRemotePort());
                    log.info("InterLinks {}", storeService.getInterLinks());
                } 
            }
        }
        
    }

    private void loadMgmt() {
        if (mgmtChannel.values() != null) {
            for (IconaManagementEvent event : mgmtChannel.values()) {
                if (storeService.getCluster(
                        event.getClusterName()) != null) {
                    storeService.getCluster(event.getClusterName())
                            .setLastSeen(event.getTimeStamp());
                    continue;
                }
                storeService.addCluster(
                        new Cluster(event.getClusterName(), event.getTimeStamp()));
            }
        }
        
    }

    @Override
    public void addInterLinkEvent(String srcClusterName, String dstClusterName,
                                  DeviceId srcId, long srcPort, DeviceId dstId,
                                  long dstPort) {

        InterLinkElement interLinkEvent = new InterLinkElement(dstClusterName,
                                                               srcId, srcPort,
                                                               dstId, dstPort);
        IconaTopologyEvent iLEvent = new IconaTopologyEvent(interLinkEvent,
                                                            srcClusterName);
        topologyChannel.put(iLEvent.getID(), iLEvent);

    }

    @Override
    public void remInterLinkEvent(IconaTopologyEvent topologyEvent) {
        // TODO Auto-generated method stub

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
            if (mgmtChannel.remove(oldHello.getid(),
                                   oldHello)) {
                mgmtEventCounter.releaseIndex(oldHello
                        .getCounter());
            }
        }
        oldHello = hello;

    }

}
