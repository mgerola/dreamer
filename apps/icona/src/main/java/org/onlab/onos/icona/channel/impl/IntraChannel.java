package org.onlab.onos.icona.channel.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;

import org.onlab.onos.icona.IconaIntraEvent;
import org.slf4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class IntraChannel {

    private final Logger log = getLogger(getClass());
    private Config intraHazelcastConfig;
    private HazelcastInstance intraHazelcastInstance;
    public static final String ICONA_INTRA_HAZELCAST_CONFIG = "conf/hazelcast-icona-intra.xml";

    // Intent and Interlink event to be notify to the master
    private static IMap<byte[], IconaIntraEvent> intraEventChannel;
    public static final String ICONA_PW_CHANNEL_NAME = "icona.intra";

    public IntraChannel() {
        try {
            intraHazelcastConfig = new FileSystemXmlConfig(
                                                           ICONA_INTRA_HAZELCAST_CONFIG);
        } catch (FileNotFoundException e) {
            log.error("Error opening fall back Hazelcast XML configuration. "
                    + "File not found: " + ICONA_INTRA_HAZELCAST_CONFIG, e);
            e.printStackTrace();
            intraHazelcastConfig = new Config();
        }
        intraHazelcastConfig.setInstanceName("ICONA-INTRA");

        //TODO: check why it is needed...
        ClassLoader classLoader = this.getClass().getClassLoader();
        this.intraHazelcastConfig.setClassLoader(classLoader);

        intraHazelcastInstance = Hazelcast
                .getOrCreateHazelcastInstance(intraHazelcastConfig);
    }
}
