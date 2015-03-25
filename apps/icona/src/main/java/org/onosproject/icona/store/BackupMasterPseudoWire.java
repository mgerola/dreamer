package org.onosproject.icona.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.onosproject.icona.InterClusterPath;
import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
import org.onosproject.net.ConnectPoint;

public class BackupMasterPseudoWire extends BackupPseudoWire {
    private PathInstallationStatus pwStatus;
    private Map<String, PseudoWireIntent> clusterIntentMap;

    // private InterClusterPath interClusterPath;
    // TODO: ingressLabel and egressLabel

    public BackupMasterPseudoWire(ConnectPoint srcEndPoint, ConnectPoint dstEndPoint) {

        this(srcEndPoint, dstEndPoint, null,
             PathInstallationStatus.RECEIVED);

    }

    public BackupMasterPseudoWire(ConnectPoint srcEndPoint, ConnectPoint dstEndPoint,
                      InterClusterPath path,
                      PathInstallationStatus pwStatus) {
        super(srcEndPoint, dstEndPoint, path);
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
                                                         isEgress);
        clusterIntentMap.put(pwIntent.dstClusterName(), pwIntent);
    }

    public Collection<PseudoWireIntent> getIntents() {
        return clusterIntentMap.values();
    }

    public PseudoWireIntent getIntent(String clusterName){
        return clusterIntentMap.get(clusterName);
    }

    @Override
    public String toString() {
        return "MasterPseudoWire [srcEndPoint=" + super.getSrcEndPoint() + ", dstEndPoint="
                + super.getDstEndPoint() + ", pseudoWireId=" + super.getPseudoWireId() + "pwStatus=" + pwStatus + ", clusterIntentMap="
                + clusterIntentMap + ", path=" + super.getInterClusterPath() + "]";
    }



}
