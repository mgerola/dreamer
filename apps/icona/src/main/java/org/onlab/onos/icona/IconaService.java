package org.onlab.onos.icona;

public interface IconaService {

    void handleELLDP(String remoteclusterName, String localDpid,
                     long localPort, String remoteDpid, long remotePort);

}
