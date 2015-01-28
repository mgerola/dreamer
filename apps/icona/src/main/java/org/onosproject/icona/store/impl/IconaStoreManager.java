package org.onosproject.icona.store.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.icona.store.Cluster;
import org.onosproject.icona.store.EndPoint;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.InterLink;
import org.onosproject.icona.store.PseudoWire;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
@Service
public class IconaStoreManager implements IconaStoreService {

    private static final Logger log = LoggerFactory
            .getLogger(IconaStoreManager.class);

    private Map<String, Cluster> clusterNameToCluster;
    private Map<DeviceId, HashMap<PortNumber, InterLink>> swPortInterLink;
    private Map<DeviceId, HashMap<PortNumber, EndPoint>> swPortEndPoint;
    private Map<String, PseudoWire> pseudoWireMap;

    // TODO: save EPs and ILs to the Cluster
    @Activate
    public void activate() {
        log.info("Started");
        clusterNameToCluster = new HashMap<String, Cluster>();
        swPortInterLink = new HashMap<DeviceId, HashMap<PortNumber, InterLink>>();
        swPortEndPoint = new HashMap<DeviceId, HashMap<PortNumber, EndPoint>>();
        pseudoWireMap = new HashMap<String, PseudoWire>();
    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
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

    // EndPoints
    @Override
    public Optional<EndPoint> getEndPoint(DeviceId sw, PortNumber port) {
        if (swPortEndPoint.get(sw) != null) {

            return Optional.ofNullable((swPortEndPoint.get(sw)).get(port));
        }
        return Optional.empty();
    }

    @Override
    public Collection<EndPoint> getEndPoints() {
        Collection<EndPoint> temp = new HashSet<EndPoint>();
        if (!swPortEndPoint.values().isEmpty()) {

            for (HashMap<PortNumber, EndPoint> portEndPoint : swPortEndPoint
                    .values()) {
                temp.addAll(portEndPoint.values());
            }

        }
        return temp;
    }

    @Override
    public Collection<EndPoint> getEndPoints(DeviceId swId) {

        if (swPortEndPoint.get(swId) != null) {
            return swPortEndPoint.get(swId).values();
        }
        return Collections.emptyList();
    }

    @Override
    public void addEndpoint(String clusterName, String dpid, long port) {
        EndPoint endPoint = new EndPoint(clusterName, DeviceId.deviceId(dpid),
                                         PortNumber.portNumber(port));
        if (swPortEndPoint.get(endPoint.deviceId()) == null) {
            swPortEndPoint.put(endPoint.deviceId(),
                               new HashMap<PortNumber, EndPoint>());
        }

        HashMap<PortNumber, EndPoint> temp = swPortEndPoint.get(endPoint
                .deviceId());
        temp.put(endPoint.port(), endPoint);

    }

    @Override
    public void remEndpoint(String clusterName, String dpid, long port) {
        EndPoint endPoint = new EndPoint(clusterName, DeviceId.deviceId(dpid),
                                         PortNumber.portNumber(port));
        if (swPortEndPoint.get(endPoint.deviceId()) != null) {
            swPortEndPoint.get(endPoint.deviceId()).remove(endPoint.port());
        }
    }

    // InterLinks

    @Override
    public Optional<InterLink> getInterLink(DeviceId id, PortNumber port) {
        if (swPortInterLink.get(id) != null) {

            return Optional.ofNullable(swPortInterLink.get(id).get(port));
        }
        return Optional.empty();
    }

    @Override
    public Collection<InterLink> getInterLinks() {

        if (!swPortInterLink.values().isEmpty()) {
            Set<InterLink> temp = new HashSet<InterLink>();

            for (HashMap<PortNumber, InterLink> portInterlink : swPortInterLink
                    .values()) {
                temp.addAll(portInterlink.values());
            }
            return temp;
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<InterLink> getInterLinks(DeviceId id) {
        if (swPortInterLink.get(id) != null) {

            return swPortInterLink.get(id).values();
        }
        return Collections.emptyList();
    }

    @Override
    public void addInterLink(String srcClusterName, String dstClusterName,
                             String srcId, long srcPort, String dstId,
                             long dstPort) {
        InterLink interLink = new InterLink(srcClusterName, dstClusterName,
                                            srcId, srcPort, dstId, dstPort);
        if (swPortInterLink.get(interLink.src().deviceId()) == null) {
            swPortInterLink.put(interLink.src().deviceId(),
                                new HashMap<PortNumber, InterLink>());
        }
        HashMap<PortNumber, InterLink> temp = swPortInterLink.get(interLink
                .src().deviceId());
        temp.put(interLink.src().port(), interLink);

        clusterNameToCluster.get(interLink.srcClusterName())
                .addInterLink(interLink);
        clusterNameToCluster.get(interLink.dstClusterName())
                .addInterLink(interLink);
        log.info("New interLink added {}", interLink);
    }

    @Override
    public void remInterLink(String srcClusterName, String dstClusterName,
                             String srcId, long srcPort, String dstId,
                             long dstPort) {

        InterLink interLink = new InterLink(srcClusterName, dstClusterName,
                                            srcId, srcPort, dstId, dstPort);
        if (swPortInterLink.get(interLink.src().deviceId()) != null) {
            InterLink localInterlink = swPortInterLink
                    .get(interLink.src().deviceId()).get(interLink.src().deviceId());
            if (localInterlink.dst().deviceId().equals(interLink.dst().deviceId())
                    && localInterlink.dst().port()
                            .equals(interLink.dst().port())) {
                swPortInterLink.get(interLink.src().deviceId())
                        .remove(interLink.src().port());
            }
        }
        clusterNameToCluster.get(interLink.srcClusterName())
                .remInterLink(interLink);
        log.info("New interLink removed {}", interLink);

    }

    // Clusters
    @Override
    public Cluster getCluster(String clusterName) {
        return clusterNameToCluster.get(clusterName);
    }

    @Override
    public Cluster addCluster(Cluster cluster) {
        log.info("New cluster added {}", cluster);
        return clusterNameToCluster.put(cluster.getClusterName(), cluster);
    }

    @Override
    public Collection<Cluster> getClusters() {
        Collection<Cluster> temp = new HashSet<Cluster>();
        temp.addAll(clusterNameToCluster.values());
        return temp;
    }

    @Override
    public Collection<Cluster> getOldCluster(int interval) {

        Collection<Cluster> removedClusters = new HashSet<Cluster>();
        if (!clusterNameToCluster.isEmpty()) {
            for (Cluster cluster : clusterNameToCluster.values()) {
                if ((cluster.getLastSeen().getTime() + interval) <= new Date()
                        .getTime()) {
                    removedClusters.add(cluster);
                    // TODO manage PWs

                }
            }
            return removedClusters;
        }
        return Collections.emptyList();
    }

    @Override
    public void remCluster(String clusterName) {
        log.info("New cluster removed {}",
                 clusterNameToCluster.get(clusterName));
        clusterNameToCluster.remove(clusterName);

    }

    @Override
    public boolean addPseudoWire(PseudoWire pw) {
        // TODO: find a better way to save pseudowire
        if (pseudoWireMap.containsKey(pw.getPseudoWireId())) {
            log.warn("Pseudowire alreday exists {}", pw);
            return false;
        }
        pseudoWireMap.put(pw.getPseudoWireId(), pw);
        return true;

    }

    @Override
    public PseudoWire getPseudoWire(String pseudoWireId) {
         return pseudoWireMap.get(pseudoWireId);
    }

//    @Override
//    public boolean addPseudoWireIntent(PseudoWire pw, PseudoWireIntent pwIntent) {
//        if (pseudoWireMap.get(pw.getPseudoWireId()) != null){
//            pw.addPseudoWireIntent(pwIntent);
//        }
//        return false;
//    }

}
