package org.onosproject.icona.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.onosproject.icona.InterClusterPath;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;

import static com.google.common.base.Preconditions.checkNotNull;

public class MasterPseudoWire extends PseudoWire {
    private PathInstallationStatus pwStatus;
    private Map<String, PseudoWireIntent> clusterIntentMap;

    private TrafficSelector selector;
    private TrafficTreatment treatment;

    // private InterClusterPath interClusterPath;
    // TODO: ingressLabel and egressLabel

    public enum PathInstallationStatus {
        RECEIVED, INITIALIZED, RESERVED, COMMITTED, INSTALLED,

    }

    public MasterPseudoWire(EndPoint srcEndPoint, EndPoint dstEndPoint,
                    TrafficSelector trafficSelector, TrafficTreatment treatment) {
        
        this(srcEndPoint, dstEndPoint, trafficSelector, treatment, null,
             PathInstallationStatus.RECEIVED);

    }

    public MasterPseudoWire(EndPoint srcEndPoint, EndPoint dstEndPoint,
                      TrafficSelector selector, TrafficTreatment treatment, InterClusterPath path,
                      PathInstallationStatus pwStatus) {
        super(srcEndPoint, dstEndPoint, path);
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
                                    PathInstallationStatus installationStatus,
                                    boolean isIngress,
                                    boolean isEgress,
                                    boolean isBackup) {
        PseudoWireIntent pwIntent = new PseudoWireIntent(dstClusterName, src, dst,
                                                         ingressLabel,
                                                         egressLabel,
                                                         installationStatus,
                                                         isIngress,
                                                         isEgress,
                                                         isBackup);
        clusterIntentMap.put(pwIntent.dstClusterName(), pwIntent);
    }

    public Collection<PseudoWireIntent> getIntents() {
        return clusterIntentMap.values();
    }
    
    public PseudoWireIntent getIntent(String clusterName){
        return clusterIntentMap.get(clusterName);
    }

    public TrafficSelector getTrafficSelector() {
        return selector;
    }

    public TrafficTreatment getTrafficTreatment() {
        return treatment;
    }

    @Override
    public String toString() {
        return "MasterPseudoWire [srcEndPoint=" + super.getSrcEndPoint() + ", dstEndPoint="
                + super.getDstEndPoint() + ", pseudoWireId=" + super.getPseudoWireId() + "pwStatus=" + pwStatus + ", clusterIntentMap="
                + clusterIntentMap + ", path=" + super.getInterClusterPath() + ", selector="
                + selector + ", treatment=" + treatment + "]";
    }

    

}
