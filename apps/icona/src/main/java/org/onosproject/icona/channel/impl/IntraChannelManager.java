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
import org.onosproject.icona.IconaService;
import org.onosproject.icona.channel.intra.IconaIntraEvent;
import org.onosproject.icona.channel.intra.IntraChannelService;
import org.onosproject.icona.channel.intra.IntraPseudoWireElement;
import org.onosproject.icona.channel.intra.IntraPseudoWireElement.IntentUpdateType;
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
    protected IconaService iconaService;



        @Activate
        public void activate() {

            log.info("Starting intra channel!");

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
        this.intraEventChannel.addEntryListener(new IconaPseudoWireListener(iconaService, this), true);

        //TODO: create a listener

        }

    @Override
    public void addIntraPseudoWire(ConnectPoint src, ConnectPoint dst, IntentUpdateType intentUpdateType, Optional<Integer> ingressLabel,
                                   Optional<Integer> egressLabel){

        IntraPseudoWireElement pw = new IntraPseudoWireElement(src, dst, IntentUpdateType.INSTALL, ingressLabel, egressLabel);
        IconaIntraEvent intraEvent = new IconaIntraEvent(pw, clusterService.getLocalNode().id());
        intraEventChannel.put(intraEvent.getID(), intraEvent);
    }

    @Override
    public void remIntraPseudoWire(IconaIntraEvent event) {
        intraEventChannel.remove(event.getID());

    }
}

