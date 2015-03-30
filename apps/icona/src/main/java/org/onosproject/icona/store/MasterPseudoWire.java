package org.onosproject.icona.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.onlab.packet.MacAddress;
import org.onosproject.icona.InterClusterPath;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;

import static com.google.common.base.Preconditions.checkNotNull;

public class MasterPseudoWire extends PseudoWire {
    private PathInstallationStatus pwStatus;
    private Map<String, PseudoWireIntent> clusterIntentMap;
    InterClusterPath path;

    private TrafficSelector selector;
    private TrafficTreatment treatment;

    // private InterClusterPath interClusterPath;
    // TODO: ingressLabel and egressLabel

    public MasterPseudoWire(EndPoint srcEndPoint, EndPoint dstEndPoint, String clusterMaster,
                    TrafficSelector trafficSelector, TrafficTreatment treatment) {

        this(srcEndPoint, dstEndPoint, clusterMaster, trafficSelector, treatment, new InterClusterPath(), PathInstallationStatus.RECEIVED);

    }

    public MasterPseudoWire(EndPoint srcEndPoint, EndPoint dstEndPoint, String clusterMaster,
                      TrafficSelector selector, TrafficTreatment treatment, InterClusterPath path,
                      PathInstallationStatus pwStatus) {
        super(srcEndPoint, dstEndPoint, clusterMaster, pwStatus);
        checkNotNull(selector);
        checkNotNull(treatment);
        this.selector = selector;
        this.treatment = treatment;
        this.pwStatus = pwStatus;
        this.path = path;
        this.clusterIntentMap = new HashMap<String, PseudoWireIntent>();
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
                                    MacAddress srcMac,
                                    MacAddress dstMac,
                                    PathInstallationStatus installationStatus,
                                    boolean isIngress,
                                    boolean isEgress) {
        PseudoWireIntent pwIntent = new PseudoWireIntent(dstClusterName, src, dst,
                                                         srcMac,
                                                         dstMac,
                                                         installationStatus,
                                                         isIngress,
                                                         isEgress);
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

    public InterClusterPath getInterClusterPath() {
        return path;
    }

    public void setInterClusterPath(InterClusterPath path){
        this.path = path;
    }

    @Override
    public String toString() {
        return "MasterPseudoWire [" + super.toString() + "pwStatus=" + pwStatus + ", clusterIntentMap="
                + clusterIntentMap + "]";
    }


}
