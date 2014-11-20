package org.onlab.onos.icona.store.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onlab.onos.icona.store.Cluster;
import org.onlab.onos.icona.store.EndPoint;
import org.onlab.onos.icona.store.InterLink;
import org.onlab.onos.icona.store.PseudoWire;
import org.onlab.onos.net.DeviceId;
import org.onlab.onos.net.PortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IconaStore implements IconaStoreService {

    private static final Logger log = LoggerFactory.getLogger(IconaStore.class);

    private Map<String, Cluster> clusterNameToCluster;
    private Map<DeviceId, HashMap<PortNumber, InterLink>> swPortInterLink;
    private Map<Long, HashMap<PortNumber, EndPoint>> swPortEndPoint;
    private Map<String, PseudoWire> pseudoWireMap;

    public IconaStore() {
        clusterNameToCluster = new HashMap<String, Cluster>();
        swPortInterLink = new HashMap<DeviceId, HashMap<PortNumber, InterLink>>();
        swPortEndPoint = new HashMap<Long, HashMap<PortNumber, EndPoint>>();
        pseudoWireMap = new HashMap<String, PseudoWire>();
    }

    // PseudoWire
    // @Override
    // public PseudoWire getPseudoWire(long srcSw, int srcPort, long dstSw, int
    // dstPort) {
    // return pseudoWireMap
    // .get(srcSw + "/" + srcPort + "-" + dstSw + "/" + dstPort);
    // }
    //
    // @Override
    // public PseudoWire getPseudoWire(String pseudoWireId) {
    // return pseudoWireMap.get(pseudoWireId);
    // }
    //
    // @Override
    // public void addPseudoWire(PseudoWire pseudoWire) {
    // pseudoWireMap.put(pseudoWire.getPseudoWireId(), pseudoWire);
    // }
    //
    // // EndPoints
    // @Override
    // public EndPoint getEndPoint(long sw, int port) {
    // if (swPortEndPoint.get(sw) != null) {
    //
    // return (swPortEndPoint.get(sw)).get(port);
    // }
    // return null;
    // }
    //
    // @Override
    // public Set<EndPoint> getEndPoints() {
    // Set<EndPoint> temp = new HashSet<EndPoint>();
    // for (HashMap<Integer, EndPoint> entry : swPortEndPoint.values()) {
    // temp.addAll(entry.values());
    // }
    // return temp;
    // }
    //
    // @Override
    // public Set<EndPoint> getEndPoints(long swId) {
    // Set<EndPoint> temp = new HashSet<EndPoint>();
    //
    // if (swPortEndPoint.get(swId) != null) {
    // for (EndPoint endPoint : swPortEndPoint.get(swId).values()) {
    // temp.add(endPoint);
    // }
    // }
    // return temp;
    // }
    //
    // @Override
    // public void addEndpoint(String clusterName, long sw, int port) {
    // if (swPortEndPoint.get(sw) == null)
    // swPortEndPoint.put(sw, new HashMap<Integer, EndPoint>());
    //
    // HashMap<Integer, EndPoint> temp = swPortEndPoint.get(sw);
    // temp.put(port, new EndPoint(clusterName, sw, port));
    //
    // log.info("swPortEndPoint add {}", swPortEndPoint);
    // }
    //
    // @Override
    // public void remEndPoint(long sw, int port) {
    // if (swPortEndPoint.get(sw) != null)
    // swPortEndPoint.get(sw).remove(port);
    //
    // log.info("swPortEndPoint rem {}", swPortEndPoint);
    // }
    //
    // @Override
    // public void remEndPoints(long sw) {
    // if (swPortEndPoint.get(sw) != null) {
    // swPortEndPoint.remove(sw);
    // }
    // }

    // InterLinks
    // @Override
    // public InterLink getInterLink(InterLink interLink) {
    // if (swPortInterLink.get(interLink.getSrcDpid()) != null) {
    // InterLink interLinkLocal =
    // swPortInterLink.get(interLink.getSrcDpid()).get(
    // interLink.getSrcPort());
    // if (interLinkLocal != null
    // && interLinkLocal.getDstDpid() == interLink.getDstDpid()
    // && interLinkLocal.getDstPort() == interLink.getDstPort()) {
    // return swPortInterLink.get(interLink.getSrcDpid()).get(
    // interLink.getSrcPort());
    // }
    // }
    // return null;
    // }

    @Override
    public InterLink getInterLink(DeviceId id, long port) {
        if (swPortInterLink.get(id) != null) {

            return swPortInterLink.get(id).get(port);
        }
        return null;
    }

     @Override
     public Set<InterLink> getInterLinks() {
     Set<InterLink> temp = new HashSet<InterLink>();
     for (HashMap<PortNumber, InterLink> portInterlink :
     swPortInterLink.values()) {
     temp.addAll(portInterlink.values());
     }
     return temp;
     }
    //
    // @Override
    // public Collection<InterLink> getInterLinks(DeviceId id) {
    // if (swPortInterLink.get(id) != null) {
    //
    // return swPortInterLink.get(id).values();
    // }
    // return Collections.emptyList();
    // }

    @Override
    public void addInterLink(String srcClusterName, String dstClusterName,
                             String srcId, long srcPort, String dstId,
                             long dstPort) {
        InterLink interLink = new InterLink(srcClusterName, dstClusterName,
                                            srcId, srcPort, dstId, dstPort);
        if (swPortInterLink.get(interLink.getSrcDpid()) == null) {
            swPortInterLink.put(interLink.getSrcDpid(),
                                new HashMap<PortNumber, InterLink>());
        }
        HashMap<PortNumber, InterLink> temp = swPortInterLink.get(interLink
                .getSrcDpid());
        temp.put(interLink.getSrcPort(), interLink);

        clusterNameToCluster.get(interLink.getSrcClusterName())
                .addInterLink(interLink);
    }

    // @Override
    // public void remInterLink(InterLink interLink) {
    // if (swPortInterLink.get(interLink.getSrcDpid()) != null) {
    // InterLink interLinkLocal =
    // swPortInterLink.get(interLink.getSrcDpid()).get(
    // interLink.getSrcPort());
    // if (interLinkLocal != null
    // && interLinkLocal.getDstDpid() == interLink.getDstDpid()
    // && interLinkLocal.getDstPort() == interLink.getDstPort()) {
    // log.info("swPortInterLink prima {}", swPortInterLink);
    // swPortInterLink.get(interLink.getSrcDpid())
    // .remove(interLink.getSrcPort());
    // log.info("swPortInterLink dopo {}", swPortInterLink);
    // return;
    // }
    // }
    // log.warn("Interlink {} cannot be removed because does not exist",
    // interLink.toString());
    //
    // clusterNameToCluster.get(interLink.getSrcClusterName()).remInterLink(interLink);
    //
    // }
    //
    // Clusters
    @Override
    public Cluster getCluster(String clusterName) {
        return clusterNameToCluster.get(clusterName);
    }

    @Override
    public Cluster addCluster(Cluster cluster) {
        return clusterNameToCluster.put(cluster.getClusterName(), cluster);
    }

    // @Override
    // public void remCluster(Cluster cluster) {
    // if (clusterNameToCluster.get(cluster.getClusterName()) == null) {
    // log.warn("The cluster {} is not present in the database",
    // cluster.getClusterName());
    // return;
    // }
    // clusterNameToCluster.remove(cluster.getClusterName());
    // }
    //
     @Override
     public Set<Cluster> getClusters() {
     Set<Cluster> temp = new HashSet<Cluster>();
     temp.addAll(clusterNameToCluster.values());
     return temp;
     }

    @Override
    public List<Cluster> remOldCluster(int interval) {
        List<Cluster> removedClusters = new ArrayList<Cluster>();
        if (clusterNameToCluster != null) {
            for (Cluster cluster : clusterNameToCluster.values()) {
                if ((cluster.getLastSeen().getTime() + interval) <= new Date()
                        .getTime()) {
                    clusterNameToCluster.remove(cluster.getClusterName());
                    removedClusters.add(cluster);
                }
            }
        }
        return removedClusters;
    }
}
