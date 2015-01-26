package org.onosproject.icona.impl;

import static org.slf4j.LoggerFactory.getLogger;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.icona.IconaConfigService;
import org.slf4j.Logger;


@Component(immediate = true)
@Service
public class IconaConfigLoader implements IconaConfigService {

    private final Logger log = getLogger(getClass());
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;
    
    private ApplicationId appId;
    
    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.icona");
        log.info("Started with Application ID {}", appId.id());

    }
    
    //TODO: fixME
    private String clusterName = "DREAMER";

    private String iconaLeaderPath = "ICONA";
    
    
    @Override
    public String getClusterName() {
        return clusterName;
    }

    @Override
    public String getIconaLeaderPath() {
        return iconaLeaderPath;
    }

    @Override
    public ApplicationId getApplicationId() {
        return appId;
    }

}
