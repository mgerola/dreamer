package org.onosproject.icona.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.security.Key;
import java.util.Optional;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.MacAddress;
import org.onlab.packet.MplsLabel;
import org.onosproject.icona.IconaConfigService;
import org.onosproject.icona.IconaPseudoWireService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.IngressMplsBackupIntent;
import org.onosproject.net.intent.IntentId;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.MplsIntent;
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

    @Override
    public IntentId installPseudoWireIntent(ConnectPoint ingress,
                                        ConnectPoint egress, MacAddress srcMac, MacAddress dstMac) {

        TrafficSelector selec = DefaultTrafficSelector.builder().matchEthSrc(srcMac).matchEthDst(dstMac).build();
        log.info("ingress {}, egress {}, srcMac {}, dstMac {}", ingress, egress, srcMac, dstMac);
        PointToPointIntent intent = PointToPointIntent.builder()
                                .appId(iconaConfigService.getApplicationId())
                                .selector(selec)
                                .ingressPoint(ingress)
                                .egressPoint(egress)
                                .build();
        intentService.submit(intent);
        return intent.id();

    }

}
