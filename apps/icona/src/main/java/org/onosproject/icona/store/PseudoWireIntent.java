package org.onosproject.icona.store;

import java.util.Date;
import java.util.Optional;

import org.onlab.packet.MacAddress;
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
    
    private MacAddress macSrc;
    private MacAddress macDst;

    private boolean isEgress;
    private boolean isIngress;

    private PathInstallationStatus installationStatus;
    private Date lastStatusUpdate;

    private IntentId intentId;

    public PseudoWireIntent(String dstCluster, String srcDpid, long srcPort,
                            String dstDpid, long dstPort, MacAddress macSrc,
                            MacAddress macDst,
                            PathInstallationStatus installationStatus, boolean isIngress, boolean isEgress) {
        this(dstCluster,
             new ConnectPoint(DeviceId.deviceId(srcDpid),
                                          PortNumber.portNumber(srcPort)),
             new ConnectPoint(DeviceId.deviceId(dstDpid),
                              PortNumber.portNumber(dstPort)),
                              macSrc,
                              macDst,
             installationStatus, isIngress, isEgress);

    }

    public PseudoWireIntent(String dstClusterName, ConnectPoint src,
                            ConnectPoint dst, MacAddress macSrc,
                            MacAddress macDst,
                            PathInstallationStatus installationStatus,
                            boolean isIngress,
                            boolean isEgress) {
        this.dstClusterName = dstClusterName;
        this.src = src;
        this.dst = dst;
        this.macSrc = macSrc;
        this.macDst = macDst;
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

    

    public MacAddress macSrc() {
        return macSrc;
    }

    public void macSrc(MacAddress macSrc) {
        this.macSrc = macSrc;
    }

    public MacAddress macDst() {
        return macDst;
    }

    public void macDst(MacAddress macDst) {
        this.macDst = macDst;
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





}
