package org.onosproject.icona.store.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

<<<<<<< HEAD
=======
import net.jcip.annotations.Immutable;

>>>>>>> upstream/icona
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.MplsLabel;
<<<<<<< HEAD
import org.onosproject.icona.store.BackupMasterPseudoWire;
=======
>>>>>>> upstream/icona
import org.onosproject.icona.store.Cluster;
import org.onosproject.icona.store.EndPoint;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.InterLink;
import org.onosproject.icona.store.MasterPseudoWire;
import org.onosproject.icona.store.PseudoWire;
<<<<<<< HEAD
=======
import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
import org.onosproject.icona.store.PseudoWireIntent;
>>>>>>> upstream/icona
import org.onosproject.icona.utils.BitSetIndex;
import org.onosproject.icona.utils.BitSetIndex.IndexType;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
<<<<<<< HEAD
=======
import org.onosproject.net.intent.IntentId;
>>>>>>> upstream/icona
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

@Component(immediate = true)
@Service
public class IconaStoreManager implements IconaStoreService {

    private static final Logger log = LoggerFactory
            .getLogger(IconaStoreManager.class);

    private Map<String, Cluster> clusterNameToCluster;
    private Map<DeviceId, HashMap<PortNumber, InterLink>> swPortInterLink;
    private Map<DeviceId, HashMap<PortNumber, EndPoint>> swPortEndPoint;

    private Map<String, PseudoWire> pseudoWireMap;
    private Map<String, BackupMasterPseudoWire> backupMasterPseudoWireMap;
    private Map<String, MasterPseudoWire> masterPseudoWireMap;
    private Map<ConnectPoint, BitSetIndex> mplsLabelMap;
    private Map<ConnectPoint, Set<PseudoWireIntent>> localIntentMap;

    // TODO: save EPs and ILs to the Cluster
    @Activate
    public void activate() {
        log.info("Started");
        clusterNameToCluster = new HashMap<String, Cluster>();
        swPortInterLink = new HashMap<DeviceId, HashMap<PortNumber, InterLink>>();
        swPortEndPoint = new HashMap<DeviceId, HashMap<PortNumber, EndPoint>>();
        pseudoWireMap = new HashMap<String, PseudoWire>();
        masterPseudoWireMap = new HashMap<String, MasterPseudoWire>();
        mplsLabelMap = new HashMap<ConnectPoint, BitSetIndex>();
        localIntentMap = new HashMap<ConnectPoint, Set<PseudoWireIntent>>();
    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
    }

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
        return ImmutableList.copyOf(temp);
    }

    @Override
    public Collection<EndPoint> getEndPoints(DeviceId swId) {

        if (swPortEndPoint.get(swId) != null) {
            return ImmutableList.copyOf(swPortEndPoint.get(swId).values());
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
        
        if(clusterNameToCluster.get(endPoint.clusterName()) == null){
            addCluster(new Cluster(clusterName, new Date()));
        }
        clusterNameToCluster.get(endPoint.clusterName()).addEndPoint(endPoint);
    }

    @Override
    public void remEndpoint(String clusterName, String dpid, long port) {
        EndPoint endPoint = new EndPoint(clusterName, DeviceId.deviceId(dpid),
                                         PortNumber.portNumber(port));
        if (swPortEndPoint.get(endPoint.deviceId()) != null) {
            swPortEndPoint.get(endPoint.deviceId()).remove(endPoint.port());
        }
        clusterNameToCluster.get(endPoint.clusterName()).remEndPoint(endPoint);
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
            return ImmutableList.copyOf(temp);
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<InterLink> getInterLinks(DeviceId id) {
        if (swPortInterLink.get(id) != null) {

            return ImmutableList.copyOf(swPortInterLink.get(id).values());
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
        
        if(clusterNameToCluster.get(interLink.srcClusterName()) == null){
            addCluster(new Cluster(interLink.srcClusterName(), new Date()));
        }
        clusterNameToCluster.get(interLink.srcClusterName())
                .addInterLink(interLink);
        
        if(clusterNameToCluster.get(interLink.dstClusterName()) == null){
            addCluster(new Cluster(interLink.dstClusterName(), new Date()));
        }

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
            InterLink localInterlink = swPortInterLink.get(interLink.src()
                                                                   .deviceId())
                    .get(interLink.src().port());
            if (localInterlink.equals(interLink)) {

                swPortInterLink.get(interLink.src().deviceId())
                        .remove(interLink.src().port());
            }
        }

        if (clusterNameToCluster.get(interLink.srcClusterName())
                .equals(interLink)) {
            clusterNameToCluster.get(interLink.srcClusterName())
                    .remInterLink(interLink);
        }

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

    // public PseudoWire addPseudoWire() {
    //
    // }

    @Override
    public Collection<Cluster> getClusters() {
        Collection<Cluster> temp = new HashSet<Cluster>();
        temp.addAll(clusterNameToCluster.values());
        return ImmutableList.copyOf(temp);
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
            return ImmutableList.copyOf(removedClusters);
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
    public boolean addBackupMasterPseudoWire(BackupMasterPseudoWire pw) {
        // TODO: find a better way to save pseudowire
        if (backupMasterPseudoWireMap.containsKey(pw.getPseudoWireId())) {
            log.warn("Pseudowire alreday exists {}", pw);
            return false;
        }
        backupMasterPseudoWireMap.put(pw.getPseudoWireId(), pw);
        return true;

    @Override
    public void addPseudoWire(PseudoWire pw) {
        // TODO: find a better way to save pseudowire
        if (pseudoWireMap.containsKey(pw.getPseudoWireId())) {
            log.warn("Pseudowire alreday exists {}", pw);
        }

        pseudoWireMap.put(pw.getPseudoWireId(), pw);
    }

    @Override
    public void addMasterPseudoWire(MasterPseudoWire pw) {
        // TODO: find a better way to save pseudowire: differnet id or else...
        if (masterPseudoWireMap.containsKey(pw.getPseudoWireId())) {
            log.warn("Pseudowire alreday exists {}", pw);
        }
        masterPseudoWireMap.put(pw.getPseudoWireId(), pw);
    }

    @Override
    public PseudoWire getPseudoWire(String pseudoWireId) {
        return (PseudoWire) pseudoWireMap.get(pseudoWireId);
    }

    @Override
    public MasterPseudoWire getMasterPseudoWire(String pseudoWireId) {
        return masterPseudoWireMap.get(pseudoWireId);
    }

    @Override
    public void remPseudoWire(String pseudoWireId) {
        masterPseudoWireMap.remove(pseudoWireId);
        pseudoWireMap.remove(pseudoWireId);

    }

    @Override
    public Collection<PseudoWire> getPseudoWires() {
        if (masterPseudoWireMap.isEmpty() && pseudoWireMap.isEmpty()) {
            return Collections.emptyList();
        }
        // TODO: find a better way
        Collection<PseudoWire> merge = new HashSet<PseudoWire>();
        merge.addAll(pseudoWireMap.values());
        merge.addAll(masterPseudoWireMap.values());
        return ImmutableList.copyOf(merge);
    }

    @Override
    public MplsLabel reserveAvailableMplsLabel(ConnectPoint connectPoint) {
        if (mplsLabelMap.get(connectPoint) == null) {
            mplsLabelMap.put(connectPoint,
                             new BitSetIndex(IndexType.MPLS_LABEL));
        }
        return MplsLabel
                .mplsLabel(mplsLabelMap.get(connectPoint).getNewIndex());
    }

    @Override
    public void releaseMplsLabel(ConnectPoint connectPoint, MplsLabel mplsLabel) {
        if (mplsLabelMap.get(connectPoint) == null) {

        }
        mplsLabelMap.get(connectPoint).releaseIndex(mplsLabel.toInt());
    }

    @Override
    public void updateMasterPseudoWireStatus(String pseudoWireId,
                                             PathInstallationStatus pwStatus) {
        if (masterPseudoWireMap.get(pseudoWireId) != null) {
            masterPseudoWireMap.get(pseudoWireId).setPwStatus(pwStatus);
        } else {
            log.error("Impossible to update MasterPseudoWire with ID {}: does not exist!",
                      pseudoWireId);
        }

    }

    @Override
    public void updatePseudoWireStatus(String pseudoWireId,
                                       PathInstallationStatus pwStatus) {
        if (pseudoWireMap.get(pseudoWireId) != null) {
            pseudoWireMap.get(pseudoWireId).setPwStatus(pwStatus);
        } else {
            log.error("Impossible to update MasterPseudoWire with ID {}: does not exist!",
                      pseudoWireId);
        }

    }

    @Override
    public void addLocalIntent(String pseudoWireId, PseudoWireIntent localIntent) {
        if (getMasterPseudoWire(pseudoWireId) != null) {
            getMasterPseudoWire(pseudoWireId).setLocalIntent(localIntent);
        } else if (getPseudoWire(pseudoWireId) != null) {
            getPseudoWire(pseudoWireId).setLocalIntent(localIntent);
        }

        if (!localIntent.isIngress()) {
            if (localIntentMap.get(localIntent.src()) == null) {
                localIntentMap.put(localIntent.src(),
                                   new HashSet<PseudoWireIntent>());
            }
            localIntentMap.get(localIntent.src()).add(localIntent);
        }
        if (!localIntent.isEgress()) {
            if (localIntentMap.get(localIntent.dst()) == null) {
                localIntentMap.put(localIntent.dst(),
                                   new HashSet<PseudoWireIntent>());
            }
            localIntentMap.get(localIntent.dst()).add(localIntent);
        }
        log.info("localIntentMap {}", localIntentMap);
    }

    @Override
    public Collection<PseudoWireIntent> getLocalIntents(ConnectPoint cp) {
        log.info("Local intents {} and get {}", localIntentMap, localIntentMap.get(cp));
        //TODO: not secure!
        if(localIntentMap.get(cp) == null) {
            return Collections.emptyList();
        }
        return ImmutableList.copyOf(localIntentMap.get(cp));
    }

    @Override
    public String getPseudoWireId(ConnectPoint srcCP, ConnectPoint dstCP) {
        String pseudoWireId = srcCP.deviceId() + "/" + srcCP.port() + "-"
                + dstCP.deviceId() + "/" + dstCP.port();
        return pseudoWireId;
    }
}
