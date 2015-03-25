package org.onosproject.icona.channel.inter;

import java.util.Date;

import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentReplayType;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentRequestType;
import org.onosproject.icona.store.EndPoint;
import org.onosproject.icona.store.MasterPseudoWire;
import org.onosproject.icona.store.PseudoWire;
import org.onosproject.icona.store.PseudoWireIntent;
import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
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
    
    void addPseudoWireEvent(ConnectPoint src, ConnectPoint dst,
                            String clusterName,
                            PathInstallationStatus pwStatus,
                            String pseudoWireId); 
    
    void addPseudoWireEvent(PseudoWire pw);  
    
    void remPseudoWireEvent(PseudoWire pw); 
    

     IconaPseudoWireIntentEvent addPseudoWireIntentEvent(String clustrLeader, String pseudoWireId,
                            PseudoWireIntent pseudoWireIntent,
                            IntentRequestType intentRequestType,
                            IntentReplayType intentReplayType);

    void addPseudoWireIntentEvent(IconaPseudoWireIntentEvent intentEvent);

    void remIntentEvent(IconaPseudoWireIntentEvent intentEvent);
}
