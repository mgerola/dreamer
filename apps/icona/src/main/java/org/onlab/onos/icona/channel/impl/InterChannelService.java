package org.onlab.onos.icona.channel.impl;

import java.util.Date;

import org.onlab.onos.icona.channel.IconaTopologyEvent;
import org.onlab.onos.net.DeviceId;

public interface InterChannelService {

    public void addInterLinkEvent(String srcClusterName, String dstClusterName,
                                  DeviceId srcId, long srcPort, DeviceId dstId,
                                  long dstPort);

    public void remInterLinkEvent(IconaTopologyEvent topologyEvent);


    public void helloManagement(Date date, String clusterName);

    // public void addIntentEvent(IconaIntentEvent intentEvent);
    //
    // public void remIntentEvent(IconaIntentEvent intentEvent);
    //
    // public void addManagementEvent(IconaManagementEvent managementEvent);
    //
    // public void remManagementEvent(IconaManagementEvent managementEvent);
}
