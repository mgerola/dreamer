package org.onosproject.icona.impl;

import static org.slf4j.LoggerFactory.getLogger;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.ApplicationId;
import org.onosproject.icona.IconaConfigService;
import org.onosproject.icona.IconaPseudoWireService;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentReplayType;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentRequestType;
import org.onosproject.icona.channel.inter.InterChannelService;
import org.onosproject.icona.impl.IconaManager.ManageLinks;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.PseudoWireIntent;
import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.PointToPointIntent;
import org.slf4j.Logger;

@Service
@Component(immediate = true)
public class IconaPseudoWireManager implements IconaPseudoWireService {
    
    private final Logger log = getLogger(getClass());
    
     @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;
    
     @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IconaConfigService iconaConfigService;
    

    
    @Activate
    public void activate() {
        log.info("Starting Pseudo Wire Manager");

    }
    
    @Deactivate
    public void deactivate() {
        log.info("Stopped");
    }

    public void installPseudoWireIntent(ConnectPoint ingress,
                                        ConnectPoint egress) {

        TrafficSelector selec = DefaultTrafficSelector.builder().build();
        TrafficTreatment treatment = DefaultTrafficTreatment.builder().build();

        //TODO: to be changed with mpls intent!
        intentService.submit(new PointToPointIntent(iconaConfigService.getApplicationId(), selec, treatment,
                                                    ingress, egress));
    }
    
}
