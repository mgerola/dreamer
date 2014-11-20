package org.onlab.onos.icona.store;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cluster {

    private static final Logger log = LoggerFactory
            .getLogger(Cluster.class);
    private String clusterName;
    private Date lastSeenTimestamp;
    private Set<InterLink> interlinks;


    public Cluster(String clusterName, Date lastSeenTimestamp) {
        super();
        this.clusterName = clusterName;
        this.lastSeenTimestamp = lastSeenTimestamp;
        this.interlinks = new HashSet<InterLink>();
    }
    public String getClusterName() {
        return clusterName;
    }
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Date getLastSeen() {
        return lastSeenTimestamp;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeenTimestamp = lastSeen;
    }

    public void addInterLink(InterLink interLink) {
        interlinks.add(interLink);
    }

    public void remInterLink(InterLink interLink) {
        if(!interlinks.remove(interLink)){
            log.warn("Interlink {} is not present in this cluster {}", interLink,
                    getClusterName());
        }
    }

    public Set<InterLink> getInterLinks() {
        return interlinks;
    }

}
