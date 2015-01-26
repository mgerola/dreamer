package org.onosproject.icona;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

@Deprecated
public class IconaIntentListener
        implements EntryListener<byte[], IconaIntentEvent> {

    @Override
    public void entryAdded(EntryEvent<byte[], IconaIntentEvent> event) {
        // if (registryService.isClusterLeader()) {
        // IconaIntentEvent intentEvent = event.getValue();
        // // Involved clusters: should install, reserve or delete a local
        // // PW.
        // if (intentEvent.getDstCluster().equals(
        // LinkDiscoveryManager.getClusterName())) {
        // switch (intentEvent.getIntentRequestType()) {
        // case DELETE:
        // break;
        // case INSTALL:
        // if (registryService.isClusterLeader()) {
        // log.info("Replica: intentEvent {} srcDpid {} dstDpid {}",
        // intentEvent.getIntentRequestType(),
        // intentEvent.getSrcDpid(), intentEvent.getDstDpid());
        // ApplicationIntent appIntent = new ApplicationIntent();
        // appIntent.setIntentType("SHORTEST_PATH");
        // appIntent.setSrcSwitchDpid(HexString.toHexString(intentEvent
        // .getSrcDpid()));
        // appIntent.setSrcSwitchPort(intentEvent.getSrcPort());
        // appIntent.setDstSwitchDpid(HexString.toHexString(intentEvent
        // .getDstDpid()));
        // appIntent.setDstSwitchPort(intentEvent.getDstPort());
        // // TODO: Manage MAC!
        // appIntent.setMatchSrcMac(intentEvent.getSrcMacAddress());
        // appIntent.setMatchDstMac(intentEvent.getDstMacAddress());
        //
        // appIntent.setIntentId(HexString.toHexString(intentEvent
        // .getSrcDpid()) + "/" + intentEvent.getSrcPort()
        // + "-" + HexString.toHexString(intentEvent
        // .getDstDpid()) + "/"
        // + intentEvent.getDstPort());
        // appIntent.setStaticPath(false);
        //
        // pathRuntime.addApplicationIntents("ICONA",
        // Collections.singletonList(appIntent));
        // }
        // break;
        // case RESERVE:
        // //TODO: all instances of the cluster should save the state?
        // if(registryService.isClusterLeader()){
        // // TODO: check ShortestPath
        // log.info("Replica: intentEvent {} srcDpid {} dstDpid {}",
        // intentEvent.getIntentRequestType(),
        // intentEvent.getSrcDpid(), intentEvent.getDstDpid());
        //
        // intentEvent.setIntentReplayType(IntentReplayType.ACK);
        // intentChannel.put(intentEvent.getID(), intentEvent);
        // }
        // break;
        // default:
        // break;
        //
        // }
        // }
        // }

    }

    @Override
    public void entryRemoved(EntryEvent<byte[], IconaIntentEvent> event) {
    }

    @Override
    public void entryUpdated(EntryEvent<byte[], IconaIntentEvent> event) {
        // IconaIntentEvent intentEvent = event.getValue();
        // // ONLY the PW Cluster should read and save the PW
        // if (intentEvent.getClusterLeader().equals(
        // LinkDiscoveryManager.getClusterName())) {
        //
        // switch (intentEvent.getIntentRequestType()) {
        // case DELETE:
        // break;
        // case INSTALL:
        // log.info("PW leader: intentEvent {} srcDpid {} dstDpid {}",
        // intentEvent.getIntentRequestType(),
        // intentEvent.getSrcDpid(), intentEvent.getDstDpid());
        // if (intentEvent.getIntentReplayType() == IntentReplayType.ACK) {
        //
        // iconaDatabase.getPseudoWire(intentEvent.getPseudoWireId())
        // .setIntentStatus(intentEvent.getDstCluster(),
        // PathInstallationStatus.INSTALLED);
        //
        // log.info("intentEvent {} {} srcDpid {} dstDpid {}",
        // intentEvent.getIntentRequestType(),
        // intentEvent.getIntentReplayType(),
        // intentEvent.getSrcDpid(), intentEvent.getDstDpid());
        //
        // if (registryService.isClusterLeader()) {
        // checkIntentInstalled(intentEvent.getPseudoWireId());
        // }
        //
        // } else if (intentEvent.getIntentReplayType() ==
        // IntentReplayType.NACK) {
        // // TODO
        // }
        //
        // break;
        // case RESERVE:
        // log.info("PW leader: intentEvent {} srcDpid {} dstDpid {}",
        // intentEvent.getIntentRequestType(),
        // intentEvent.getSrcDpid(), intentEvent.getDstDpid());
        // if (intentEvent.getIntentReplayType() == IntentReplayType.ACK) {
        // iconaDatabase.getPseudoWire(intentEvent.getPseudoWireId())
        // .setIntentStatus(intentEvent.getDstCluster(),
        // PathInstallationStatus.RESERVED);
        //
        // if (registryService.isClusterLeader()) {
        // checkIntentReserved(intentEvent.getPseudoWireId());
        // }
        //
        // } else if (intentEvent.getIntentReplayType() ==
        // IntentReplayType.NACK) {
        // // TODO
        // }
        // break;
        // default:
        // break;
        //
        // }
        // }
    }

    @Override
    public void entryEvicted(EntryEvent<byte[], IconaIntentEvent> event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mapEvicted(MapEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mapCleared(MapEvent event) {
        // TODO Auto-generated method stub

    }

}
