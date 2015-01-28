package org.onosproject.icona.store;

import java.util.Date;
import java.util.Optional;

import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public class PseudoWireIntent {

    private String dstClusterName;

    private ConnectPoint src;

    private ConnectPoint dst;

    private Optional<Integer> ingressLabel;
    private Optional<Integer> egressLabel;

    private PathInstallationStatus installationStatus;
    private Date lastStatusUpdate;

    public PseudoWireIntent(String dstCluster, String srcDpid, long srcPort,
                            String dstDpid, long dstPort, Integer ingressLabel,
                            Integer egressLabel,
                            PathInstallationStatus installationStatus) {
        this(dstCluster, 
             new ConnectPoint(DeviceId.deviceId(srcDpid),
                                          PortNumber.portNumber(srcPort)),
             new ConnectPoint(DeviceId.deviceId(dstDpid),
                              PortNumber.portNumber(dstPort)), 
             ingressLabel,
             egressLabel, 
             installationStatus);

    }

    public PseudoWireIntent(String dstClusterName, ConnectPoint src,
                            ConnectPoint dst, Integer ingressLabel,
                            Integer egressLabel,
                            PathInstallationStatus installationStatus) {
        this.dstClusterName = dstClusterName;
        this.src = src;
        this.dst = dst;
        this.ingressLabel = Optional.ofNullable(ingressLabel);
        this.egressLabel = Optional.ofNullable(ingressLabel);
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

    public Optional<Integer> ingressLabel() {
        return ingressLabel;
    }

    public void ingressLabel(Optional<Integer> ingressLabel) {
        this.ingressLabel = ingressLabel;
    }

    public Optional<Integer> egressLabel() {
        return egressLabel;
    }

    public void egressLabel(Optional<Integer> egressLabel) {
        this.egressLabel = egressLabel;
    }

    @Override
    public String toString() {
        return "IconaIntent [dstClusterName=" + dstClusterName + ", src=" + src
                + ", dst=" + dst + ", installationStatus=" + installationStatus
                + ", lastStatusUpdate=" + lastStatusUpdate + "]";
    }
}
