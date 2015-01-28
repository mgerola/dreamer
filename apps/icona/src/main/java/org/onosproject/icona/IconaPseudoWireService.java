package org.onosproject.icona;

import java.util.Optional;

import org.onlab.packet.MplsLabel;
import org.onosproject.net.ConnectPoint;

public interface IconaPseudoWireService {

    void installPseudoWireIntent(ConnectPoint ingress, Optional<MplsLabel> ingressLabel,
                                 ConnectPoint egress,
                                 Optional<MplsLabel> egressLabel);

}
