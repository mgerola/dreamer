package org.onosproject.icona.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


import org.onosproject.net.ConnectPoint;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;

import static com.google.common.base.Preconditions.checkNotNull;

public class PseudoWire {
    private EndPoint srcEndPoint;
    private EndPoint dstEndPoint;
    private String pseudoWireId;
    private PathInstallationStatus pwStatus;
    private Map<String, PseudoWireIntent> clusterIntentMap;

    private TrafficSelector selector;
    private TrafficTreatment treatment;

    // private InterClusterPath interClusterPath;
    // TODO: ingressLabel and egressLabel

    public enum PathInstallationStatus {
        RECEIVED, INITIALIZED, RESERVED, COMMITTED, INSTALLED,

    }

    public PseudoWire(EndPoint srcEndPoint, EndPoint dstEndPoint,
                    TrafficSelector trafficSelector, TrafficTreatment treatment) {
        this(srcEndPoint, dstEndPoint, trafficSelector, treatment,
             PathInstallationStatus.RECEIVED);
    }

    public PseudoWire(EndPoint srcEndPoint, EndPoint dstEndPoint,
                      TrafficSelector selector, TrafficTreatment treatment,
                      PathInstallationStatus pwStatus) {
        checkNotNull(srcEndPoint);
        checkNotNull(dstEndPoint);
        this.dstEndPoint = dstEndPoint;
        this.srcEndPoint = srcEndPoint;
        this.pseudoWireId = srcEndPoint.deviceId() + "/" + srcEndPoint.port()
                + "-" + dstEndPoint.deviceId() + "/" + dstEndPoint.port();
        checkNotNull(selector);
        checkNotNull(treatment);
        this.selector = selector;
        this.treatment = treatment;
        this.pwStatus = pwStatus;
        this.clusterIntentMap = new HashMap<String, PseudoWireIntent>();
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
            clusterIntentMap.get(clusterName)
                    .installationStatus(installationStatus);
        }
    }

    public void addPseudoWireIntent(ConnectPoint src, ConnectPoint dst,
                                    String dstClusterName,
                                    Integer ingressLabel,
                                    Integer egressLabel,
                                    PathInstallationStatus installationStatus) {
        PseudoWireIntent pwIntent = new PseudoWireIntent(dstClusterName, src, dst,
                                                         ingressLabel,
                                                         egressLabel,
                                                         installationStatus);
        clusterIntentMap.put(pwIntent.dstClusterName(), pwIntent);
    }

    public Collection<PseudoWireIntent> getIntents() {
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

    public TrafficSelector getTrafficSelector() {
        return selector;
    }

    public TrafficTreatment getTrafficTreatment() {
        return treatment;
    }

    @Override
    public String toString() {
        return "PseudoWire [srcEndPoint=" + srcEndPoint + ", dstEndPoint="
                + dstEndPoint + ", pseudoWireId=" + pseudoWireId
                + ", pwStatus=" + pwStatus + ", clusterIntentMap="
                + clusterIntentMap + "]";
    }

}
