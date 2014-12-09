package org.onosproject.icona.channel;

import java.util.Date;

import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public interface InterChannelService {

    void addInterLinkEvent(String srcClusterName, String dstClusterName,
                                  DeviceId srcId, PortNumber srcPort,
                                  DeviceId dstId, PortNumber dstPort);

    void remInterLinkEvent(String srcClusterName, String dstClusterName,
                                  DeviceId srcId, PortNumber srcPort,
                                  DeviceId dstId, PortNumber dstPort);

    void addEndPointEvent(String clusterName, DeviceId id,
                                 PortNumber port);

    void remEndPointEvent(String clusterName, DeviceId id,
                                 PortNumber port);

    void helloManagement(Date date, String clusterName);
    
    void remCluster(String ClusterName);

    void addCluster(String ClusterName);

    // public void addIntentEvent(IconaIntentEvent intentEvent);
    //
    // public void remIntentEvent(IconaIntentEvent intentEvent);
    //
    // public void addManagementEvent(IconaManagementEvent managementEvent);
    //
    // public void remManagementEvent(IconaManagementEvent managementEvent);
}
