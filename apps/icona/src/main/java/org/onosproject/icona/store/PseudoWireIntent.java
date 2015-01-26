package org.onosproject.icona.store;

import java.util.Date;

import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;


public class PseudoWireIntent {

    private String dstClusterName;

    private ConnectPoint src;

    private ConnectPoint dst;

    private PathInstallationStatus installationStatus;
    private Date lastStatusUpdate;


    public PseudoWireIntent(String dstCluster, String srcDpid, long srcPort, String dstDpid, long dstPort,
                       PathInstallationStatus installationStatus, String srcMacAddress, String dstMacAddress) {
        this.dstClusterName = dstCluster;
        this.src = new ConnectPoint(DeviceId.deviceId(srcDpid), PortNumber.portNumber(srcPort));
        this.dst = new ConnectPoint(DeviceId.deviceId(dstDpid), PortNumber.portNumber(dstPort));
        this.installationStatus = installationStatus;
        this.lastStatusUpdate = new Date();

    }

     public PseudoWireIntent(ConnectPoint src, ConnectPoint dst, String dstClusterName,
     PathInstallationStatus installationStatus) {
     this.dstClusterName = dstClusterName;
     this.src = src;
     this.dst = dst;
     this.installationStatus = installationStatus;
     this.lastStatusUpdate = new Date();

     }

    public ConnectPoint src() {
        return src;
    }

    public ConnectPoint dst() {
        return dst;
    }

    public Date lastStatusUpdate() {
        return lastStatusUpdate;
    }

    public String dstClusterName() {
        return this.dstClusterName;
    }

    public PathInstallationStatus installationStatus() {
        return installationStatus;
    }

    public void installationStatus(PathInstallationStatus installationStatus) {
        this.installationStatus = installationStatus;
        this.lastStatusUpdate = new Date();
    }

    @Override
    public String toString() {
        return "IconaIntent [dstClusterName=" + dstClusterName + ", src=" + src + ", dst=" + dst
                + ", installationStatus=" + installationStatus + ", lastStatusUpdate=" + lastStatusUpdate + "]";
    }
}
