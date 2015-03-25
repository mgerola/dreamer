package org.onosproject.icona.store;

import java.util.Date;
import java.util.Optional;

import org.onlab.packet.MplsLabel;
import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.intent.IntentId;

public class PseudoWireIntent {

    private String dstClusterName;

    private ConnectPoint src;

    private ConnectPoint dst;

    private Optional<MplsLabel> ingressLabel;
    private Optional<MplsLabel> egressLabel;

    private boolean isEgress;
    private boolean isIngress;

    private PathInstallationStatus installationStatus;
    private Date lastStatusUpdate;

    private IntentId intentId;

    public PseudoWireIntent(String dstCluster, String srcDpid, long srcPort,
                            String dstDpid, long dstPort, Integer ingressLabel,
                            Integer egressLabel,
                            PathInstallationStatus installationStatus, boolean isIngress, boolean isEgress) {
        this(dstCluster,
             new ConnectPoint(DeviceId.deviceId(srcDpid),
                                          PortNumber.portNumber(srcPort)),
             new ConnectPoint(DeviceId.deviceId(dstDpid),
                              PortNumber.portNumber(dstPort)),
             ingressLabel,
             egressLabel,
             installationStatus, isIngress, isEgress);

    }

    public PseudoWireIntent(String dstClusterName, ConnectPoint src,
                            ConnectPoint dst, Integer ingressLabel,
                            Integer egressLabel,
                            PathInstallationStatus installationStatus,
                            boolean isIngress,
                            boolean isEgress) {
        this.dstClusterName = dstClusterName;
        this.src = src;
        this.dst = dst;
        if(ingressLabel != null){
        this.ingressLabel = Optional.ofNullable(MplsLabel.mplsLabel(ingressLabel));
        }else{
            this.ingressLabel = Optional.empty();
        }
        if(egressLabel !=null){
        this.egressLabel = Optional.ofNullable(MplsLabel.mplsLabel(egressLabel));
        }else{
            this.egressLabel = Optional.empty();
        }
        this.installationStatus = installationStatus;
        this.lastStatusUpdate = new Date();
        this.isEgress = isEgress;
        this.isIngress = isIngress;


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

    public Optional<MplsLabel> ingressLabel() {
        return ingressLabel;
    }

    public void ingressLabel(MplsLabel ingressLabel) {
        this.ingressLabel = Optional.ofNullable(ingressLabel);
    }

    public void ingressLabel(Optional<MplsLabel> ingressLabel) {
        this.ingressLabel = ingressLabel;
    }

    public Optional<MplsLabel> egressLabel() {
        return egressLabel;
    }

    public void egressLabel(Optional<MplsLabel> egressLabel) {
        this.egressLabel = egressLabel;
    }

    public void egressLabel(MplsLabel egressLabel) {
        this.egressLabel = Optional.ofNullable(egressLabel);
    }

    public boolean isEgress() {
        return isEgress;
    }

    public Boolean isIngress() {
        return isIngress;
    }

    public IntentId intentId() {
        return intentId;
    }

    public void intentId(IntentId intentId){
        this.intentId = intentId;
    }

    @Override
    public String toString() {
        return "PseudoWireIntent [dstClusterName=" + dstClusterName + ", src="
                + src + ", dst=" + dst + ", ingressLabel=" + ingressLabel
                + ", egressLabel=" + egressLabel + ", isEgress=" + isEgress
                + ", isIngress=" + isIngress + ", installationStatus="
                + installationStatus + ", lastStatusUpdate=" + lastStatusUpdate
                + ", intentId=" + intentId + "]";
    }




}
