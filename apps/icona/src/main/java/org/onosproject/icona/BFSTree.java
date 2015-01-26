package org.onosproject.icona;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.onosproject.icona.store.Cluster;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.InterLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BFSTree {
    // TODO: remove
    private static final Logger log = LoggerFactory
            .getLogger(BFSTree.class);

    LinkedList<Cluster> clusterQueue = new LinkedList<>();
    HashSet<String> clusterSearched = new HashSet<>();
    HashMap<String, InterLink> upstreamInterLinks = new HashMap<>();
    HashMap<String, InterClusterPath> interClusterPaths = new HashMap<>();
    Cluster rootCluster;
    IconaStoreService iconaStoreService;
    // PathIntentMap intents = null;
    // double bandwidth = 0.0; // 0.0 means no limit for bandwidth (normal BFS
    // tree)


    public BFSTree(Cluster rootCluster, IconaStoreService iconaStoreService) {
        this.rootCluster = rootCluster;
        this.iconaStoreService = iconaStoreService;
        calcTree();
    }


    protected final void calcTree() {
        clusterQueue.add(rootCluster);
        clusterSearched.add(rootCluster.getClusterName());
        while (!clusterQueue.isEmpty()) {
            Cluster cluster = clusterQueue.poll();
            for (InterLink interLink : cluster.getInterLinks()) {
                String reachedCluster = interLink.dstClusterName();
                if (clusterSearched.contains(reachedCluster)) {
                    continue;
                }
                // if (intents != null &&
                // intents.getAvailableBandwidth(link) < bandwidth) {
                // continue;
                // }
                clusterQueue.add(iconaStoreService.getCluster(reachedCluster));
                clusterSearched.add(reachedCluster);
                upstreamInterLinks.put(reachedCluster, interLink);
            }
        }

    }

    public InterClusterPath getPath(Cluster leafCluster) {
        InterClusterPath interClusterPath = interClusterPaths.get(leafCluster
                .getClusterName());
        if (interClusterPath == null && clusterSearched.contains(leafCluster
                .getClusterName())) {
            interClusterPath = new InterClusterPath();
            String cluster = leafCluster.getClusterName();
            while (!cluster.equals(rootCluster.getClusterName())) {
                InterLink upstreamLink = upstreamInterLinks.get(cluster);
                interClusterPath.addInterlinks(upstreamLink);
                cluster = upstreamLink.srcClusterName();
            }
            interClusterPaths.put(leafCluster.getClusterName(), interClusterPath);
        }
        return interClusterPath;
    }
}

