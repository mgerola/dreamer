package org.onosproject.icona.store;

import static com.google.common.base.Preconditions.checkNotNull;

import org.onosproject.icona.InterClusterPath;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.intent.IntentId;

public class PseudoWire {

    private EndPoint srcEndPoint;
    private EndPoint dstEndPoint;
    private String clusterMaster;
    private String pseudoWireId;
    private IntentId intentId;
    private PathInstallationStatus pwStatus;
    
    public enum PathInstallationStatus {
        RECEIVED, INITIALIZED, RESERVED, COMMITTED, INSTALLED,

    }

    
    public PseudoWire(EndPoint srcEndPoint, EndPoint dstEndPoint, String clusterMaster, PathInstallationStatus pwStatus) {
    checkNotNull(srcEndPoint);
    checkNotNull(dstEndPoint);
    
    this.dstEndPoint = dstEndPoint;
    this.srcEndPoint = srcEndPoint;
    this.clusterMaster = clusterMaster;
    this.pseudoWireId = srcEndPoint.deviceId() + "/" + srcEndPoint.port()
            + "-" + dstEndPoint.deviceId() + "/" + dstEndPoint.port();
    this.pwStatus = pwStatus;
    }
    
    public EndPoint getSrcEndPoint() {
        return srcEndPoint;
    }

    public EndPoint getDstEndPoint() {
        return dstEndPoint;
    }

    public String getPseudoWireId() {
        return pseudoWireId;
    }
    
    public void setIntentId(IntentId intentId){
        this.intentId = intentId;
    }
    
    public IntentId getIntentId() {
        return intentId;
    }

    public PathInstallationStatus getPwStatus() {
        return pwStatus;
    }

    public void setPwStatus(PathInstallationStatus pwStatus) {
        this.pwStatus = pwStatus;
    }
    
    public String getClusterMaster() {
        return clusterMaster;
    }

    @Override
    public String toString() {
        return "PseudoWire [srcEndPoint=" + srcEndPoint + ", dstEndPoint="
                + dstEndPoint + ", clusterMaster=" + clusterMaster
                + ", pseudoWireId=" + pseudoWireId + ", pwStatus=" + pwStatus
                + "]";
    }    
}
