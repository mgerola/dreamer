package org.onosproject.icona;

import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public interface IconaService {

    public String getCusterName();

    void handleELLDP(String remoteclusterName, DeviceId localDpid,
                     PortNumber localPort, DeviceId remoteDpid,
                     PortNumber remotePort);
}
