package org.onlab.onos.icona;

import org.onlab.onos.net.DeviceId;

public interface IconaService {


    public String getCusterName();

    void handleELLDP(String remoteclusterName, DeviceId localDpid, long localPort,
                     DeviceId remoteDpid, long remotePort);
}
