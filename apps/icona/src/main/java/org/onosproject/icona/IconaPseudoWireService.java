package org.onosproject.icona;

import java.util.Optional;

import org.onlab.packet.MacAddress;
import org.onlab.packet.MplsLabel;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.intent.IntentId;

public interface IconaPseudoWireService {


    IntentId installPseudoWireIntent(ConnectPoint ingress, ConnectPoint egress, MacAddress srcMac,
                                     MacAddress dstMac);

}
