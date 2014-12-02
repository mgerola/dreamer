package org.onlab.onos.icona.store;

import java.util.Collection;

import org.onlab.onos.net.DeviceId;
import org.onlab.onos.net.PortNumber;

public interface IconaStoreService {

    // public PseudoWire getPseudoWire(long srcSw, int srcPort, long dstSw, int
    // dstPort);
    //
    //
    //
    // public PseudoWire getPseudoWire(String pseudoWireId);
    //
    EndPoint getEndPoint(DeviceId id, PortNumber port);

    // public void addPseudoWire(PseudoWire pseudoWire);

    Collection<EndPoint> getEndPoints(DeviceId id);

    Collection<EndPoint> getEndPoints();

    void addEndpoint(String clusterName, String sw, long port);

    void remEndpoint(String clusterName, String dpid, long portNumber);

    InterLink getInterLink(DeviceId id, PortNumber port);

    Collection<InterLink> getInterLinks(DeviceId id);

    void addInterLink(String srcClusterId, String dstClusterId, String srcId,
                      long srcPort, String dstId, long dstPort);

    void remInterLink(String srcClusterName, String dstClusterName,
                      String srcId, long srcPort, String dstId, long dstPort);

    Collection<InterLink> getInterLinks();

    Cluster getCluster(String clusterId);

    Cluster addCluster(Cluster cluster);

    Collection<Cluster> remOldCluster(int interval);

    Collection<Cluster> getClusters();

}
