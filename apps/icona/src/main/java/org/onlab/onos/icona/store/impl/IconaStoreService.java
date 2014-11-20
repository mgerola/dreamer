package org.onlab.onos.icona.store.impl;

import java.util.List;
import java.util.Set;

import org.onlab.onos.icona.store.Cluster;
import org.onlab.onos.icona.store.InterLink;
import org.onlab.onos.net.DeviceId;

public interface IconaStoreService {

    // public PseudoWire getPseudoWire(long srcSw, int srcPort, long dstSw, int
    // dstPort);
    //
    //
    //
    // public PseudoWire getPseudoWire(String pseudoWireId);
    //
    // public EndPoint getEndPoint(long sw, int port);
    //
    // public void addPseudoWire(PseudoWire pseudoWire);
    //
    // public Set<EndPoint> getEndPoints();
    //
    // public Set<EndPoint> getEndPoints(long swId);
    //
    // public void addEndpoint(String clusterName, long sw, int port);
    //
    // public void remEndPoint(long sw, int port);
    //
    // public void remEndPoints(long sw);

    // public InterLink getInterLink(InterLink interLink);

    public InterLink getInterLink(DeviceId id, long port);

    public void addInterLink(String srcClusterName, String dstClusterName,
                             String srcId, long srcPort, String dstId,
                             long dstPort);

  //TODO: ADDED for debug
     public Set<InterLink> getInterLinks();
    //
    // public Collection<InterLink> getInterLinks(DeviceId id);

    // public void remInterLink(InterLink interLink);
    //
    public Cluster getCluster(String clusterName);

    public Cluster addCluster(Cluster cluster);

    // public void remCluster(Cluster cluster);
    public List<Cluster> remOldCluster(int interval);

    //TODO: ADDED for debug
    public Set<Cluster> getClusters();
}
