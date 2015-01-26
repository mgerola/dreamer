package org.onosproject.icona.channel.intra;

import java.util.Optional;

import org.onosproject.icona.channel.intra.IntraPseudoWireElement.IntentUpdateType;
import org.onosproject.net.ConnectPoint;

public interface IntraChannelService {

    // TODO: create a generic list of PW parameters
    public void addIntraPseudoWire(ConnectPoint src, ConnectPoint dst,
                                   IntentUpdateType intentUpdateType,
                                   Optional<Integer> ingressLabel,
                                   Optional<Integer> egressLabel);

    public void remIntraPseudoWire(IconaIntraEvent event);
}
