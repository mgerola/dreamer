package org.onosproject.icona.channel;

import org.onosproject.icona.channel.IntraPseudoWireElement.IntentUpdateType;
import org.onosproject.net.ConnectPoint;

public interface IntraChannelService {

    // TODO: create a generic list of PW parameters
    public void addIntraPW(ConnectPoint src, ConnectPoint dst, IntentUpdateType intentUpdateType, Integer ingressLabel,
                           Integer egressLabel);

}
