package org.onosproject.icona.channel.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.util.Date;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.LeadershipService;
import org.onosproject.icona.IconaConfigService;
import org.onosproject.icona.IconaPseudoWireService;
import org.onosproject.icona.IconaService;
import org.onosproject.icona.InterClusterPath;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentReplayType;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentRequestType;
import org.onosproject.icona.channel.inter.IconaManagementEvent;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent;
import org.onosproject.icona.channel.inter.IconaTopologyEvent;
import org.onosproject.icona.channel.inter.InterChannelService;
import org.onosproject.icona.channel.inter.InterEndPointElement;
import org.onosproject.icona.channel.inter.InterLinkElement;
import org.onosproject.icona.channel.inter.IconaManagementEvent.MessageType;
import org.onosproject.icona.channel.inter.InterPseudoWireElement;
import org.onosproject.icona.impl.IconaManager;
import org.onosproject.icona.store.Cluster;
import org.onosproject.icona.store.EndPoint;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.MasterPseudoWire;
import org.onosproject.icona.store.PseudoWire;
import org.onosproject.icona.store.PseudoWireIntent;
import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
import org.onosproject.icona.utils.BitSetIndex;
import org.onosproject.icona.utils.BitSetIndex.IndexType;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.topology.PathService;
import org.slf4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@Component(immediate = true)
@Service
public class InterChannelManager implements InterChannelService {

    private final Logger log = getLogger(getClass());
    private Config interHazelcastConfig;
    private HazelcastInstance interHazelcastInstance;
    private final String ICONA_INTER_HAZELCAST_CONFIG = "conf/hazelcast-icona-inter.xml";

    // Topology channel
    private static IMap<byte[], IconaTopologyEvent> topologyChannel;
    public static final String ICONA_TOPOLOGY_CHANNEL_NAME = "icona.topology";

    // Intent channel
    private static IMap<byte[], IconaPseudoWireIntentEvent> pseudoWireChannel;
    public static final String ICONA_INTENT_CHANNEL_NAME = "icona.intent";
    // Management channel
    private IMap<String, IconaManagementEvent> mgmtChannel;
    public static final String ICONA_MGMT_CHANNEL_NAME = "icona.mgmt";
    public static BitSetIndex mgmtEventCounter = null;
    private IconaManagementEvent oldHello;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IconaStoreService iconaStoreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LeadershipService leadershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterService clusterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IconaConfigService iconaConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IconaPseudoWireService iconaPseudoWireService;
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PathService pathService;

    @Activate
    public void activate() {
        log.info("Starting inter channel!");

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

        this.mgmtChannel = interHazelcastInstance
                .getMap(ICONA_MGMT_CHANNEL_NAME);
        this.mgmtChannel
                .addEntryListener(new IconaMgmtListener(iconaStoreService),
                                  true);
        InterChannelManager.mgmtEventCounter = new BitSetIndex(
                                                               IndexType.MGMT_CHANNEL);
        
        loadMgmt();
        
        
        InterChannelManager.topologyChannel = interHazelcastInstance
                .getMap(ICONA_TOPOLOGY_CHANNEL_NAME);
        InterChannelManager.topologyChannel
                .addEntryListener(new IconaTopologyListener(iconaStoreService, iconaConfigService),
                                  true);
        loadTopology();
        
        InterChannelManager.pseudoWireChannel = interHazelcastInstance
                .getMap(ICONA_INTENT_CHANNEL_NAME);
        InterChannelManager.pseudoWireChannel
                .addEntryListener(new IconaPseudoWireIntentListener(
                                                                    leadershipService,
                                                                    clusterService,
                                                                    iconaConfigService,
                                                                    iconaStoreService,
                                                                    this,
                                                                    iconaPseudoWireService, 
                                                                    pathService),
                                  true);


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
                    iconaStoreService
                            .addInterLink(event.getClusterName(),
                                          interLinkEvent.getRemoteClusterName(),
                                          interLinkEvent.getLocalId(),
                                          interLinkEvent.getLocalPort(),
                                          interLinkEvent.getRemoteId(),
                                          interLinkEvent.getRemotePort());
                }
                InterEndPointElement endPointEvent = event
                        .getEntryPointElement();
                if (endPointEvent != null) {
                    log.info("Load InterLink {}", interLinkEvent);
                    iconaStoreService
                            .addEndpoint(event.getClusterName(),
                                         endPointEvent.getDpid(),
                                         endPointEvent.getPortNumber());
                }
            }
        }

    }

    private void loadMgmt() {
        //Add my cluster
        iconaStoreService.addCluster(new Cluster(iconaConfigService
                                                 .getClusterName(), new Date()));
        
        if (mgmtChannel.values() != null) {
            for (IconaManagementEvent event : mgmtChannel.values()) {
                if (iconaStoreService.getCluster(event.getClusterName()) != null) {
                    iconaStoreService.getCluster(event.getClusterName())
                            .setLastSeen(event.getTimeStamp());
                    continue;
                }
                iconaStoreService
                        .addCluster(new Cluster(event.getClusterName(), event
                                .getTimeStamp()));
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
                                  ConnectPoint src, ConnectPoint dst) {

        InterLinkElement interLinkEvent = new InterLinkElement(dstClusterName,
                                                               src, dst);
        IconaTopologyEvent iLEvent = new IconaTopologyEvent(interLinkEvent,
                                                            srcClusterName);
        topologyChannel.put(iLEvent.getID(), iLEvent);

    }

    @Override
    public void remInterLinkEvent(String srcClusterName, String dstClusterName,
                                  ConnectPoint src, ConnectPoint dst) {
        InterLinkElement interLinkEvent = new InterLinkElement(dstClusterName,
                                                               src, dst);
        IconaTopologyEvent iLEvent = new IconaTopologyEvent(interLinkEvent,
                                                            srcClusterName);
        topologyChannel.remove(iLEvent.getID());

    }

    @Override
    public void addEndPointEvent(String clusterName, ConnectPoint cp) {
        InterEndPointElement endPointEvent = new InterEndPointElement(cp);
        IconaTopologyEvent ePEvent = new IconaTopologyEvent(endPointEvent,
                                                            clusterName);
        log.info("Publishing EntryPoint added: {}", endPointEvent.toString());
        topologyChannel.put(ePEvent.getID(), ePEvent);

    }

    @Override
    public void remEndPointEvent(EndPoint endPoint) {
        InterEndPointElement endPointEvent = new InterEndPointElement(endPoint);
        IconaTopologyEvent ePEvent = new IconaTopologyEvent(
                                                            endPointEvent,
                                                            endPoint.clusterName());
        log.info("Publishing EntryPoint removal: {}", endPointEvent.toString());
        topologyChannel.remove(ePEvent.getID());

    }

    @Override
    public void addCluster(String ClusterName) {
        IconaTopologyEvent clusterEvent = new IconaTopologyEvent(ClusterName);
        topologyChannel.put(clusterEvent.getID(), clusterEvent);
    }

    @Override
    public void remCluster(String ClusterName) {
        IconaTopologyEvent clusterEvent = new IconaTopologyEvent(ClusterName);
        topologyChannel.remove(clusterEvent.getID());
    }
    
    @Override
    public void addPseudoWireEvent(ConnectPoint src, ConnectPoint dst, String clusterName, PathInstallationStatus pwStatus, String pseudoWireId) {
        IconaTopologyEvent event = new IconaTopologyEvent(new InterPseudoWireElement(src,dst, pwStatus, pseudoWireId), clusterName);
        topologyChannel.put(event.getID(), event);
        
    }
    
    @Override
    public void addPseudoWireEvent(PseudoWire pw) {
        IconaTopologyEvent event = new IconaTopologyEvent(
                                                          new InterPseudoWireElement(
                                                                                     pw.getSrcEndPoint(),
                                                                                     pw.getDstEndPoint(),
                                                                                     pw.getPwStatus(),
                                                                                     pw.getPseudoWireId()),
                                                          pw.getClusterMaster());
        topologyChannel.put(event.getID(), event);
    }

    // TODO: implement!
    @Override
    public void remPseudoWireEvent(PseudoWire pw) {
        IconaTopologyEvent event = new IconaTopologyEvent(
                                                          new InterPseudoWireElement(
                                                                                     pw.getSrcEndPoint(),
                                                                                     pw.getDstEndPoint(),
                                                                                     pw.getPwStatus(),
                                                                                     pw.getPseudoWireId()),
                                                          pw.getClusterMaster());
        topologyChannel.remove(event.getID());
        
    }

    @Override
    public IconaPseudoWireIntentEvent addPseudoWireIntentEvent(String clustrLeader,
                                                         String pseudoWireId,
                                                         PseudoWireIntent pseudoWireIntent,
                                                         IntentRequestType intentRequestType,
                                                         IntentReplayType intentReplayType) {
        IconaPseudoWireIntentEvent event = new IconaPseudoWireIntentEvent(
                                                                          clustrLeader,
                                                                          pseudoWireId,
                                                                          pseudoWireIntent,
                                                                          intentRequestType,
                                                                          intentReplayType);

        pseudoWireChannel.put(event.getID(), event);
        return event;

    }

    @Override
    public void addPseudoWireIntentEvent(IconaPseudoWireIntentEvent intentEvent) {
        pseudoWireChannel.put(intentEvent.getID(), intentEvent);

    }

    @Override
    public void remIntentEvent(IconaPseudoWireIntentEvent intentEvent) {
        pseudoWireChannel.remove(intentEvent.getID());
    }
}
