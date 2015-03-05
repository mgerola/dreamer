package org.onosproject.icona.channel.inter;

import java.util.Date;

import org.onosproject.icona.InterClusterPath;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentReplayType;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentRequestType;
import org.onosproject.icona.store.EndPoint;
import org.onosproject.icona.store.PseudoWireIntent;
import org.onosproject.net.ConnectPoint;

public interface InterChannelService {

    void addInterLinkEvent(String srcClusterName, String dstClusterName,
                           ConnectPoint src, ConnectPoint dst);

    void remInterLinkEvent(String srcClusterName, String dstClusterName,
                           ConnectPoint src, ConnectPoint dst);

    void addEndPointEvent(String clusterName, ConnectPoint cp);

    void remEndPointEvent(EndPoint endPoint);

    void helloManagement(Date date, String clusterName);

    
    void remCluster(String ClusterName);

    void addCluster(String ClusterName);
    
    void addPseudoWireEvent(ConnectPoint src, ConnectPoint dst, InterClusterPath path, String clusterName);
        
    void remPseudoWireEvent(); 
    

     IconaPseudoWireIntentEvent addMasterPseudoWireEvent(String clustrLeader, String pseudoWireId,
                            PseudoWireIntent pseudoWireIntent,
                            IntentRequestType intentRequestType,
                            IntentReplayType intentReplayType);

    void addMasterPseudoWireEvent(IconaPseudoWireIntentEvent intentEvent);

    void remIntentEvent(IconaPseudoWireIntentEvent intentEvent);
   

    // public void addIntentEvent(IconaIntentEvent intentEvent);
    //
    // public void remIntentEvent(IconaIntentEvent intentEvent);
    //
    // public void addManagementEvent(IconaManagementEvent managementEvent);
    //
    // public void remManagementEvent(IconaManagementEvent managementEvent);
}
