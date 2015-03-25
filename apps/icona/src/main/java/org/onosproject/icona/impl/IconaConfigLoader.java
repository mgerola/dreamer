package org.onosproject.icona.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.LeadershipService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.icona.IconaConfig;
import org.onosproject.icona.IconaConfigService;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;


@Component(immediate = true)
@Service
public class IconaConfigLoader implements IconaConfigService {

    private final Logger log = getLogger(getClass());

    private static final String CONFIG_DIR = "conf/";
    private static final String DEFAULT_CONFIG_FILE = "config-cluster.json";
    private String configFileName = DEFAULT_CONFIG_FILE;

    private String clusterName = "DREAMER";

    private String iconaLeaderPath = "ICONA";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LeadershipService leadershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterService clusterService;

    private ApplicationId appId;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.icona");
        log.info("Started with Application ID {}", appId.id());

        IconaConfig config = readIconaConfig();
        if (config != null) {
            applyIconaConfig(config);
        } else{
            log.warn("Impossible loading ICONA configuration: use default setting");
        }

        leadershipService.runForLeadership(iconaLeaderPath);
    }

    private void applyIconaConfig(IconaConfig config) {
        this.clusterName = config.clusterName();

    }

    private IconaConfig readIconaConfig() {
        File configFile = new File(CONFIG_DIR, configFileName);
        ObjectMapper mapper = new ObjectMapper();

        try {
            log.info("Loading config: {}", configFile.getAbsolutePath());
            IconaConfig config =
                    mapper.readValue(configFile, IconaConfig.class);

            return config;
        } catch (FileNotFoundException e) {
            log.warn("Configuration file not found: {}", configFileName);
        } catch (IOException e) {
            log.error("Error loading configuration", e);
        }

        return null;
    }

    @Override
    public String getClusterName() {
        return clusterName;
    }

    @Override
    public ApplicationId getApplicationId() {
        return appId;
    }

    @Override
    public boolean isLeader() {

        // if null should be the only one!
        // TODO: to be verified
        if (leadershipService.getLeader(iconaLeaderPath) == null) {
            return true;
        }
        if (clusterService.getLocalNode().id()
                .equals(leadershipService.getLeader(iconaLeaderPath))) {
            return true;
        }
        return false;
    }

}
