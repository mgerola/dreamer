package org.onosproject.icona.channel.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.util.Optional;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.LeadershipService;
import org.onosproject.icona.channel.IconaIntraEvent;
import org.onosproject.icona.channel.IntraChannelService;
import org.onosproject.icona.channel.IntraPseudoWireElement;
import org.onosproject.icona.channel.IntraPseudoWireElement.IntentUpdateType;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.net.ConnectPoint;
import org.slf4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@Component(immediate = true)
@Service
public class IntraChannelManager implements IntraChannelService {

    private final Logger log = getLogger(getClass());
    private Config intraHazelcastConfig;
    private HazelcastInstance intraHazelcastInstance;
    private final String ICONA_INTRA_HAZELCAST_CONFIG = "conf/hazelcast-icona-intra.xml";

    // Intent and Interlink event to be notify to the master
    private IMap<byte[], IconaIntraEvent> intraEventChannel;
    public static final String ICONA_PW_CHANNEL_NAME = "icona.intra";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterService clusterService;
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LeadershipService leadershipService;
    
        @Activate
        public void activate() {
        
        try {
            intraHazelcastConfig = new FileSystemXmlConfig(ICONA_INTRA_HAZELCAST_CONFIG);
        } catch (FileNotFoundException e) {
            log.error("Error opening fall back Hazelcast XML configuration. " + "File not found: "
                    + ICONA_INTRA_HAZELCAST_CONFIG, e);
            e.printStackTrace();
            intraHazelcastConfig = new Config();
        }
        intraHazelcastConfig.setInstanceName("ICONA-INTRA");

        // TODO: check why it is needed...
        ClassLoader classLoader = this.getClass().getClassLoader();
        this.intraHazelcastConfig.setClassLoader(classLoader);

        this.intraHazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(intraHazelcastConfig);
        
        this.intraEventChannel = intraHazelcastInstance.getMap(ICONA_PW_CHANNEL_NAME);
        this.intraEventChannel.addEntryListener(new IconaPwListener(leadershipService),
                                          true);
        
        //TODO: create a listener
        
        }

    @Override
    public void addIntraPW(ConnectPoint src, ConnectPoint dst, IntentUpdateType intentUpdateType, Integer ingressLabel,
                           Integer egressLabel){
        
        IntraPseudoWireElement pw = new IntraPseudoWireElement(src, dst, IntentUpdateType.INSTALL, ingressLabel, egressLabel);
        IconaIntraEvent intraEvent = new IconaIntraEvent(pw, clusterService.getLocalNode().id());
        intraEventChannel.put(intraEvent.getID(), intraEvent);
    }
}

