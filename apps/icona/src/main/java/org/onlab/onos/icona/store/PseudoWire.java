package org.onlab.onos.icona.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PseudoWire {
    private EndPoint srcEndPoint;
    private EndPoint dstEndPoint;
    private String pseudoWireId;
    private PathInstallationStatus pwStatus;
    // private Map<String, PathInstallationStatus> clusterIntentStatusMap;
    private Map<String, IconaIntent> clusterIntentMap;


    // private InterClusterPath interClusterPath;

    public enum PathInstallationStatus{
        RECEIVED,
        INITIALIZED,
        RESERVED,
        COMMITTED,
        INSTALLED,

    }

    public PseudoWire(EndPoint srcEndPoint, EndPoint dstEndPoint) {
        this.dstEndPoint = dstEndPoint;
        this.srcEndPoint = srcEndPoint;
        this.pseudoWireId = srcEndPoint.getDpid() + "/" + srcEndPoint.getPort() + "-"
                + dstEndPoint.getDpid() + "/" + dstEndPoint.getPort();
        this.pwStatus = PathInstallationStatus.RECEIVED;
        this.clusterIntentMap = new HashMap<String, IconaIntent>();
    }

    public PathInstallationStatus getPwStatus() {
        return pwStatus;
    }

    public void setPwStatus(PathInstallationStatus pwStatus) {
        this.pwStatus = pwStatus;
    }

    public void setIntentStatus(String clusterName,
            PathInstallationStatus installationStatus) {
        if (clusterIntentMap.get(clusterName) != null) {
            clusterIntentMap.get(clusterName).setInstallationStatus(installationStatus);
        }
    }

    public void addIntent(IconaIntent intent) {
        clusterIntentMap.put(intent.getDstClusterName(), intent);
    }

    public Collection<IconaIntent> getIntents() {
        return clusterIntentMap.values();
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

    @Override
    public String toString() {
        return "PseudoWire [srcEndPoint=" + srcEndPoint + ", dstEndPoint=" + dstEndPoint
                + ", pseudoWireId=" + pseudoWireId + ", pwStatus=" + pwStatus
                + ", clusterIntentMap=" + clusterIntentMap + "]";
    }

}
