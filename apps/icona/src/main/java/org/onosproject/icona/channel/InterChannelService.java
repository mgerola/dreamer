package org.onosproject.icona.channel;

import java.util.Date;

import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public interface InterChannelService {

    public void addInterLinkEvent(String srcClusterName, String dstClusterName,
                                  DeviceId srcId, PortNumber srcPort,
                                  DeviceId dstId, PortNumber dstPort);

    public void remInterLinkEvent(String srcClusterName, String dstClusterName,
                                  DeviceId srcId, PortNumber srcPort,
                                  DeviceId dstId, PortNumber dstPort);

    public void addEndPointEvent(String clusterName, DeviceId id,
                                 PortNumber port);

    public void remEndPointEvent(String clusterName, DeviceId id,
                                 PortNumber port);

    public void helloManagement(Date date, String clusterName);

    // public void addIntentEvent(IconaIntentEvent intentEvent);
    //
    // public void remIntentEvent(IconaIntentEvent intentEvent);
    //
    // public void addManagementEvent(IconaManagementEvent managementEvent);
    //
    // public void remManagementEvent(IconaManagementEvent managementEvent);
}
