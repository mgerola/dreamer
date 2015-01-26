package org.onosproject.icona;

import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent;
import org.onosproject.net.ConnectPoint;

public interface IconaPseudoWireService {
    
    void installPseudoWireIntent(ConnectPoint ingress, ConnectPoint egresss);

}
