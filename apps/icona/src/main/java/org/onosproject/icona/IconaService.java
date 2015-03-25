package org.onosproject.icona;

import org.onosproject.icona.channel.intra.IconaIntraEvent;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public interface IconaService {

     void handleELLDP(String remoteclusterName, DeviceId localDpid,
                     PortNumber localPort, DeviceId remoteDpid,
                     PortNumber remotePort);

     void handlePseudoWire(IconaIntraEvent event);





}
