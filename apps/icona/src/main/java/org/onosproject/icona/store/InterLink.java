package org.onosproject.icona.store;

import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public class InterLink {

    private String srcClusterName;
    private String dstClusterName;

    private ConnectPoint src;
    private ConnectPoint dst;

    public InterLink(String srcClusterName, String dstClusterName,
                     String srcDpid, Long srcPort, String dstDpid, Long dstPort) {
        this.src = new ConnectPoint(DeviceId.deviceId(srcDpid), PortNumber.portNumber(srcPort));
        this.srcClusterName = srcClusterName;

        this.dst = new ConnectPoint(DeviceId.deviceId(dstDpid), PortNumber.portNumber(dstPort));
        this.dstClusterName = dstClusterName;
    }

    public ConnectPoint src(){
        return this.src;
    }

    public ConnectPoint dst(){
        return this.dst;
    }

    public String srcClusterName() {
        return srcClusterName;
    }

    public String dstClusterName() {
        return dstClusterName;
    }


}
