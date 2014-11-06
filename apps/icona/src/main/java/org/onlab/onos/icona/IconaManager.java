package org.onlab.onos.icona;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import org.slf4j.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.onos.core.ApplicationId;
import org.onlab.onos.core.CoreService;
import org.onlab.onos.net.device.DeviceService;
import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@Component(immediate = true)
@Service
public class IconaManager implements IconaService {

    private static final Logger log = getLogger(IconaManager.class);

    private Config interHazelcastConfig;
    private HazelcastInstance interHazelcastInstance;
    public static final String ICONA_INTER_HAZELCAST_CONFIG = "conf/hazelcast-icona-inter.xml";

    private Config intraHazelcastConfig;
    private HazelcastInstance intraHazelcastInstance;
    public static final String ICONA_INTRA_HAZELCAST_CONFIG = "conf/hazelcast-icona-intra.xml";

    // Inter channels
    // Topology channel
    private static IMap<byte[], IconaTopologyEvent> topologyChannel;
    public static final String ICONA_TOPOLOGY_CHANNEL_NAME = "icona.topology";

    // Intent channel
    private static IMap<byte[], IntentEvent> intentChannel;
    public static final String ICONA_INTENT_CHANNEL_NAME = "icona.intent";
    // Management channel
    private IMap<String, ManagementEvent> mgmtChannel;

    // Intra channel
    // Intent and Interlink event to be notify to the master
    private static IMap<byte[], IconaIntraEvent> intraEventChannel;
    public static final String ICONA_PW_CHANNEL_NAME = "icona.intra";

    // ICONA interval to send HELLO
    private static int helloInterval = 1000;
    // If ICONA does not receive HELLO packets from another cluster for a period
    // of time
    // longer than the HELLO interval multiplied by
    // DEAD_OCCURRENCE, the cluster is considered dead.

    private ApplicationId appId;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onlab.onos.icona");
        log.info("Started with Application ID {}", appId.id());
        interHazelcastConfig = setHazelcastConfig("ICONA-INTER",
                                                  ICONA_INTER_HAZELCAST_CONFIG);

        intraHazelcastConfig = setHazelcastConfig("ICONA-INTRA",
                                                  ICONA_INTRA_HAZELCAST_CONFIG);

    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
    }

    private Config setHazelcastConfig(String instanceName, String iconaConfig) {
        try {
            intraHazelcastConfig = new FileSystemXmlConfig(iconaConfig);
        } catch (FileNotFoundException e) {
            log.error("Error opening fall back Hazelcast XML configuration. "
                    + "File not found: " + iconaConfig, e);
            e.printStackTrace();
            intraHazelcastConfig = new Config();
        }
        intraHazelcastConfig.setInstanceName(instanceName);
        return interHazelcastConfig;
    }

    @Override
    public void handleELLDP(String remoteclusterName, String localDpid,
                            long localPort, String remoteDpid, long remotePort) {

        log.info("Received LLDP from cluster {}: local switch DPID {} and port {} "
                + "and remote switch DPID {} and port {}",
                 remoteclusterName, localDpid, localPort, remoteDpid,
                 remotePort);
    }
}
