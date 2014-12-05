package org.onosproject.icona.store;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cluster {

    private static final Logger log = LoggerFactory.getLogger(Cluster.class);
    private String clusterName;
    private Date lastSeenTimestamp;
    private Collection<InterLink> interlinks;
    private Collection<EndPoint> endPoints;

    public Cluster(String clusterName, Date lastSeenTimestamp) {
        super();
        this.clusterName = clusterName;
        this.lastSeenTimestamp = lastSeenTimestamp;
        this.interlinks = new HashSet<InterLink>();
        this.endPoints = new HashSet<EndPoint>();
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
        if (!interlinks.remove(interLink)) {
            log.warn("Interlink {} is not present in this cluster {}",
                     interLink, getClusterName());
        }
    }

    public Collection<InterLink> getInterLinks() {
        return interlinks;
    }

    public void remEndPoint(EndPoint endPoint) {
        if (!endPoints.remove(endPoints)) {
            log.warn("EndPoint {} is not present in this cluster {}", endPoint,
                     getClusterName());
        }
    }

    public void addEndPoint(EndPoint endPoint) {
        endPoints.add(endPoint);
    }

    public Collection<EndPoint> getEndPoints() {
        return endPoints;

    }

}
