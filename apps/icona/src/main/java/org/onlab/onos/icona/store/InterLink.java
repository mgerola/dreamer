package org.onlab.onos.icona.store;

import org.onlab.onos.net.DeviceId;
import org.onlab.onos.net.PortNumber;

public class InterLink {

    private String srcClusterName;
    private String dstClusterName;

    private DeviceId srcDpid;
    private PortNumber srcPort;
    private DeviceId dstDpid;
    private PortNumber dstPort;


    public InterLink(String srcClusterName, String dstClusterName, String srcDpid,
             Long srcPort, String dstDpid, Long dstPort) {
        this.srcDpid = DeviceId.deviceId(srcDpid);
        this.srcPort = PortNumber.portNumber(srcPort);
        this.srcClusterName = srcClusterName;

        this.dstDpid = DeviceId.deviceId(dstDpid);
        this.dstPort = PortNumber.portNumber(dstPort);
        this.dstClusterName = dstClusterName;
    }

    public DeviceId getSrcDpid() {
        return srcDpid;
    }

    public PortNumber getSrcPort() {
        return srcPort;
    }

    public DeviceId getDstDpid() {
        return dstDpid;
    }

    public PortNumber getDstPort() {
        return dstPort;
    }

    public String getSrcClusterName() {
        return srcClusterName;
    }

    public String getDstClusterName() {
        return dstClusterName;
    }


    @Override
    public String toString() {
        return "InterLink [localClusterName=" + srcClusterName + ", remoteClusterName="
                + dstClusterName + ", localDpid=" + srcDpid + ", localPort="
                + srcPort + ", remoteDpid=" + dstDpid + ", remotePort=" + dstPort
                + "]";
    }

}
