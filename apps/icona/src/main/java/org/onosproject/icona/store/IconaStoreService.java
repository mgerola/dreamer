package org.onosproject.icona.store;

import java.util.Collection;
import java.util.Optional;

import org.onlab.packet.MplsLabel;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public interface IconaStoreService {

    // public PseudoWire getPseudoWire(long srcSw, int srcPort, long dstSw, int
    // dstPort);
    //
    //
    //

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

    boolean addMasterPseudoWire(MasterPseudoWire pw);

    MasterPseudoWire getMasterPseudoWire(String pseudoWireId);

    MplsLabel reserveAvailableMplsLabel(ConnectPoint connectPoint);

    void releaseMplsLabel(ConnectPoint connectPoint, MplsLabel mplsLabel);


//    boolean addPseudoWireIntent(PseudoWire pw, PseudoWireIntent pwIntent);

}
