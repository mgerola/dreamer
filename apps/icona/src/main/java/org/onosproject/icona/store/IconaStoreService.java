package org.onosproject.icona.store;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.onlab.packet.MplsLabel;
import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.intent.IntentId;

public interface IconaStoreService {

    Optional<EndPoint> getEndPoint(DeviceId id, PortNumber port);

    Collection<EndPoint> getEndPoints(DeviceId id);

    Collection<EndPoint> getEndPoints();

    void addEndpoint(String clusterName, String sw, long port);

    void remEndpoint(String clusterName, String dpid, long portNumber);

    Optional<InterLink> getInterLink(DeviceId id, PortNumber port);

    Collection<InterLink> getInterLinks(DeviceId id);

    void addInterLink(String srcClusterId, String dstClusterId, String srcId,
                      long srcPort, String dstId, long dstPort);

    void remInterLink(String srcClusterName, String dstClusterName,
                      String srcId, long srcPort, String dstId, long dstPort);

    Collection<InterLink> getInterLinks();

    Cluster getCluster(String clusterId);

    Cluster addCluster(Cluster cluster);

    Collection<Cluster> getOldCluster(int interval);

    Collection<Cluster> getClusters();

    void remCluster(String clusterName);

    void addPseudoWire(PseudoWire pw);
    
    void addMasterPseudoWire(MasterPseudoWire pw);
    
    MasterPseudoWire getMasterPseudoWire(String pseudoWireId);
    
    void updateMasterPseudoWireStatus(String pseudoWireId, PathInstallationStatus pwStatus);

    PseudoWire getPseudoWire(String pseudoWireId);
    
    void remPseudoWire(String pseudoWireId);

    String getPseudoWireId(ConnectPoint srcCP, ConnectPoint dstCP);
    
    Collection<PseudoWire> getPseudoWires();
    
    void updatePseudoWireStatus(String pseudoWireId, PathInstallationStatus pwStatus);
    
    MplsLabel reserveAvailableMplsLabel(ConnectPoint connectPoint);

    void releaseMplsLabel(ConnectPoint connectPoint, MplsLabel mplsLabel);
    
//    void updateLocalIntent(IntentId oldIntentId, IntentId newIntentId, ConnectPoint src, ConnectPoint dst);

    void addLocalIntent(String pseudoWireId, PseudoWireIntent localIntent);

    Collection<PseudoWireIntent> getLocalIntents(ConnectPoint src);

}
