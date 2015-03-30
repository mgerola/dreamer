package org.onosproject.icona.channel.intra;

import org.onlab.packet.MacAddress;
import org.onosproject.icona.channel.intra.IntraPseudoWireElement.IntentUpdateType;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;

public interface IntraChannelService {

    public void remIntraPseudoWire(IconaIntraEvent event);

    void intraPseudoWire(ConnectPoint src, ConnectPoint dst, MacAddress macSrc, MacAddress macDst,
                         TrafficSelector selector, TrafficTreatment treatment, IntentUpdateType intentUpdateType);


}
