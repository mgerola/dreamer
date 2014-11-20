package org.onlab.onos.icona.store;

import java.util.Date;

import org.onlab.onos.icona.store.PseudoWire.PathInstallationStatus;



public class IconaIntent {

    private String dstClusterName;

    private long srcDpid;
    private int srcPort;

    private long dstDpid;
    private int dstPort;

    private PathInstallationStatus installationStatus;
    private Date lastStatusUpdate;

    private String srcMacAddress;
    private String dstMacAddress;

    public IconaIntent(String dstCluster, long srcDpid, int srcPort, long dstDpid,
            int dstPort, PathInstallationStatus installationStatus, String srcMacAddress,
            String dstMacAddress) {
        this.dstClusterName = dstCluster;
        this.srcDpid = srcDpid;
        this.srcPort = srcPort;
        this.dstDpid = dstDpid;
        this.dstPort = dstPort;
        this.installationStatus = installationStatus;
        this.lastStatusUpdate = new Date();

        this.srcMacAddress = srcMacAddress;
        this.dstMacAddress = dstMacAddress;

    }

    public IconaIntent(EndPoint srcEndPoint, EndPoint dstEndPoint,
            PathInstallationStatus installationStatus, String srcMacAddress,
            String dstMacAddress) {
        this.dstClusterName = srcEndPoint.getClusterName();
        this.srcDpid = srcEndPoint.getDpid();
        this.srcPort = srcEndPoint.getPort();
        this.dstDpid = dstEndPoint.getDpid();
        this.dstPort = dstEndPoint.getPort();
        this.installationStatus = installationStatus;
        this.lastStatusUpdate = new Date();

        this.srcMacAddress = srcMacAddress;
        this.dstMacAddress = dstMacAddress;
    }

    public long getSrcDpid() {
        return srcDpid;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public long getDstDpid() {
        return dstDpid;
    }

    public int getDstPort() {
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
        return "IconaIntent [dstClusterName=" + dstClusterName + ", srcDpid=" + srcDpid
                + ", srcPort=" + srcPort + ", dstDpid=" + dstDpid + ", dstPort="
                + dstPort + ", installationStatus=" + installationStatus
                + ", lastStatusUpdate=" + lastStatusUpdate + "]";
    }

}
