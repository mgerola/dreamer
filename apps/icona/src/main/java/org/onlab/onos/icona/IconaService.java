package org.onlab.onos.icona;

import org.onlab.onos.net.DeviceId;
import org.onlab.onos.net.PortNumber;

public interface IconaService {

    public String getCusterName();

    void handleELLDP(String remoteclusterName, DeviceId localDpid,
                     PortNumber localPort, DeviceId remoteDpid,
                     PortNumber remotePort);
}
