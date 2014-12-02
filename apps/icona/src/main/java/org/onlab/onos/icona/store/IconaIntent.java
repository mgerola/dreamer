package org.onlab.onos.icona.store;

import java.util.Date;

import org.onlab.onos.icona.store.PseudoWire.PathInstallationStatus;
import org.onlab.onos.net.DeviceId;
import org.onlab.onos.net.PortNumber;

public class IconaIntent {

    private String dstClusterName;

    private DeviceId srcId;
    private PortNumber srcPort;

    private DeviceId dstId;
    private PortNumber dstPort;

    private PathInstallationStatus installationStatus;
    private Date lastStatusUpdate;

    private String srcMacAddress;
    private String dstMacAddress;

    public IconaIntent(String dstCluster, String srcDpid, long srcPort,
                       String dstDpid, long dstPort,
                       PathInstallationStatus installationStatus,
                       String srcMacAddress, String dstMacAddress) {
        this.dstClusterName = dstCluster;
        this.srcId = DeviceId.deviceId(srcDpid);
        this.srcPort = PortNumber.portNumber(srcPort);
        this.dstId = DeviceId.deviceId(dstDpid);
        this.dstPort = PortNumber.portNumber(dstPort);
        this.installationStatus = installationStatus;
        this.lastStatusUpdate = new Date();

        this.srcMacAddress = srcMacAddress;
        this.dstMacAddress = dstMacAddress;

    }

    // public IconaIntent(EndPoint srcEndPoint, EndPoint dstEndPoint,
    // PathInstallationStatus installationStatus, String srcMacAddress,
    // String dstMacAddress) {
    // this.dstClusterName = srcEndPoint.getClusterName();
    // this.srcDpid = srcEndPoint.getDpid();
    // this.srcPort = srcEndPoint.getPort();
    // this.dstDpid = dstEndPoint.getDpid();
    // this.dstPort = dstEndPoint.getPort();
    // this.installationStatus = installationStatus;
    // this.lastStatusUpdate = new Date();
    //
    // this.srcMacAddress = srcMacAddress;
    // this.dstMacAddress = dstMacAddress;
    // }

    public DeviceId getSrcId() {
        return srcId;
    }

    public PortNumber getSrcPort() {
        return srcPort;
    }

    public DeviceId getDstId() {
        return dstId;
    }

    public PortNumber getDstPort() {
        return dstPort;
    }

    public Date getLastStatusUpdate() {
        return lastStatusUpdate;
    }

    public String getSrcMacAddress() {
        return srcMacAddress;
    }

    public String getDstMacAddress() {
        return dstMacAddress;
    }

    public String getDstClusterName() {
        return this.dstClusterName;
    }

    public PathInstallationStatus getInstallationStatus() {
        return installationStatus;
    }

    public void setInstallationStatus(PathInstallationStatus installationStatus) {
        this.installationStatus = installationStatus;
        this.lastStatusUpdate = new Date();
    }

    @Override
    public String toString() {
        return "IconaIntent [dstClusterName=" + dstClusterName + ", srcDpid="
                + srcId + ", srcPort=" + srcPort + ", dstDpid=" + dstId
                + ", dstPort=" + dstPort + ", installationStatus="
                + installationStatus + ", lastStatusUpdate=" + lastStatusUpdate
                + "]";
    }

}
