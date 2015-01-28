package org.onosproject.icona.channel.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.LeadershipService;
import org.onosproject.icona.IconaConfigService;
import org.onosproject.icona.IconaPseudoWireService;
import org.onosproject.icona.IconaService;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent;
import org.onosproject.icona.channel.inter.InterChannelService;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentReplayType;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentRequestType;
import org.onosproject.icona.impl.IconaManager;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.PseudoWireIntent;
import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.PointToPointIntent;
import org.slf4j.Logger;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

public class IconaPseudoWireIntentListener
        implements EntryListener<byte[], IconaPseudoWireIntentEvent> {

    protected LeadershipService leadershipService;

    protected ClusterService clusterService;

    protected IconaConfigService iconaConfigService;

    protected IconaStoreService iconaStoreService;

    protected InterChannelService interChannelService; 
    
    protected IconaPseudoWireService iconaPseudoWireService;

    public IconaPseudoWireIntentListener(LeadershipService leadershipService,
                                         ClusterService clusterService,
                                         IconaConfigService iconaConfigService,
                                         IconaStoreService iconaStoreService,
                                         InterChannelService interChannelService,
                                         IconaPseudoWireService iconaPseudoWireService) {
        this.leadershipService = leadershipService;
        this.clusterService = clusterService;
        this.iconaConfigService = iconaConfigService;
        this.iconaStoreService = iconaStoreService;
        this.interChannelService = interChannelService;
        this.iconaPseudoWireService = iconaPseudoWireService;
        

    }

    private final Logger log = getLogger(getClass());

    @Override
    public void entryAdded(EntryEvent<byte[], IconaPseudoWireIntentEvent> event) {
        log.info("IconaPseudowireIntent Event recieved");
        if (event.getValue().dstCluster()
                .equals(iconaConfigService.getClusterName())) {

            IconaPseudoWireIntentEvent intentEvent = checkNotNull(event
                    .getValue());

            // Involved clusters: should install, reserve or delete a local
            // PW.

            log.info("IconaPseudowireIntent Event Is for me");
            switch (intentEvent.intentRequestType()) {
            case DELETE:
                break;
            case INSTALL:
                log.info("IconaPseudowireIntent INSTALL");
                if (leadershipService.getLeader(iconaConfigService
                        .getIconaLeaderPath()) != null
                        && clusterService
                                .getLocalNode()
                                .id()
                                .equals(leadershipService
                                                .getLeader(iconaConfigService
                                                        .getIconaLeaderPath()))) {
                    log.info("Replica: intentEvent {} srcDpid {} dstDpid {}",
                             intentEvent.intentRequestType(),
                             intentEvent.srcId(), intentEvent.dstId());

                    ConnectPoint ingress = new ConnectPoint(
                                                            DeviceId.deviceId(intentEvent
                                                                    .srcId()),
                                                            PortNumber
                                                                    .portNumber(intentEvent
                                                                            .srcPort()));
                    ConnectPoint egress = new ConnectPoint(
                                                           DeviceId.deviceId(intentEvent
                                                                   .dstId()),
                                                           PortNumber
                                                                   .portNumber(intentEvent
                                                                           .dstPort()));
                 // TODO: FIXME!
                    iconaPseudoWireService.installPseudoWireIntent(ingress,
                                                                   egress);

                }
                break;
            case RESERVE:
                log.info("IconaPseudowireIntent reserve 1");
                // TODO: all instances of the cluster should save the state?
                if (leadershipService.getLeader(iconaConfigService
                        .getIconaLeaderPath()) != null
                        && clusterService
                                .getLocalNode()
                                .id()
                                .equals(leadershipService
                                                .getLeader(iconaConfigService
                                                        .getIconaLeaderPath()))) {
                    
                    // TODO: check ShortestPath
                    log.info("Replica: intentEvent {} srcDpid {} dstDpid {}",
                             intentEvent.intentRequestType(),
                             intentEvent.srcId(), intentEvent.dstId());

                    intentEvent.intentReplayType(IntentReplayType.ACK);
                    interChannelService.addPseudoWireEvent(intentEvent);

                }
                break;
            default:
                break;

            }
        }
    }

    @Override
    public void entryRemoved(EntryEvent<byte[], IconaPseudoWireIntentEvent> event) {
    }

    @Override
    public void entryUpdated(EntryEvent<byte[], IconaPseudoWireIntentEvent> event) {
        IconaPseudoWireIntentEvent intentEvent = event.getValue();
        
        log.info("IconaPseudowireIntent Event update");
        // ONLY the PW Cluster should read and save the PW
        if (event.getValue().clusterLeader()
                .equals(iconaConfigService.getClusterName())) {

            switch (intentEvent.intentRequestType()) {
            case DELETE:
                break;
            case INSTALL:
                log.info("PW leader: intentEvent {} srcDpid {} dstDpid {}",
                         intentEvent.intentRequestType(), intentEvent.srcId(),
                         intentEvent.dstId());
                if (intentEvent.intentReplayType() == IntentReplayType.ACK) {

                    iconaStoreService.getPseudoWire(intentEvent.pseudoWireId())
                            .setIntentStatus(intentEvent.dstCluster(),
                                             PathInstallationStatus.INSTALLED);

                    log.info("intentEvent {} {} srcDpid {} dstDpid {}",
                             intentEvent.intentRequestType(),
                             intentEvent.intentReplayType(),
                             intentEvent.srcId(), intentEvent.dstId());

                    if (leadershipService.getLeader(iconaConfigService
                            .getIconaLeaderPath()) != null
                            && clusterService
                                    .getLocalNode()
                                    .id()
                                    .equals(leadershipService
                                                    .getLeader(iconaConfigService
                                                            .getIconaLeaderPath()))) {
                        
                                checkIntentInstalled(intentEvent);
                    }

                } else if (intentEvent.intentReplayType() == IntentReplayType.NACK) {
                    // TODO
                }

                break;
            case RESERVE:
                log.info("PW leader: intentEvent {} srcDpid {} dstDpid {}",
                         intentEvent.intentRequestType(), intentEvent.srcId(),
                         intentEvent.dstId());
                if (intentEvent.intentReplayType() == IntentReplayType.ACK) {
                    iconaStoreService.getPseudoWire(intentEvent.pseudoWireId())
                            .setIntentStatus(intentEvent.dstCluster(),
                                             PathInstallationStatus.RESERVED);

                    if (leadershipService.getLeader(iconaConfigService
                            .getIconaLeaderPath()) != null
                            && clusterService
                                    .getLocalNode()
                                    .id()
                                    .equals(leadershipService
                                                    .getLeader(iconaConfigService
                                                            .getIconaLeaderPath()))) {
                        checkIntentReserved(intentEvent);
                    }

                } else if (intentEvent.intentReplayType() == IntentReplayType.NACK) {
                    // TODO
                }
                break;
            default:
                break;

            }
        }
    }
    
    // if all Intent are reserved, publish INSTALL!
    private void checkIntentInstalled(IconaPseudoWireIntentEvent pseudoWireEvent) {
        for (PseudoWireIntent intent : iconaStoreService
                .getPseudoWire(pseudoWireEvent.pseudoWireId()).getIntents()) {
            if (intent.installationStatus() != PathInstallationStatus.INSTALLED) {
                return;
            }
        }
        log.info("INSTALLED");
        iconaStoreService.getPseudoWire(pseudoWireEvent.dstId())
                .setPwStatus(PathInstallationStatus.INSTALLED);
        for (PseudoWireIntent intent : iconaStoreService
                .getPseudoWire(pseudoWireEvent.pseudoWireId()).getIntents()) {

            // TODO: check the removal of both request
            interChannelService.addPseudoWireEvent(intent.dstClusterName(),
                                                   pseudoWireEvent.pseudoWireId(),
                                                   intent,
                                                   IntentRequestType.INSTALL,
                                                   IntentReplayType.EMPTY);
            // Remove old request
            // TODO: Remove old ack, is correct?
            interChannelService.remIntentEvent(pseudoWireEvent);

        }
        iconaStoreService.getPseudoWire(pseudoWireEvent.dstId())
                .setPwStatus(PathInstallationStatus.INSTALLED);
        // TODO: send PW to the interChannel
    }

    // if all Intent are reserved, publish install!
    private void checkIntentReserved(IconaPseudoWireIntentEvent pseudoWireEvent) {
        for (PseudoWireIntent intent : iconaStoreService
                .getPseudoWire(pseudoWireEvent.pseudoWireId()).getIntents()) {
            if (intent.installationStatus() != PathInstallationStatus.RESERVED) {
                return;
            }
        }

        log.info("From reserved to committed");
        iconaStoreService.getPseudoWire(pseudoWireEvent.pseudoWireId())
                .setPwStatus(PathInstallationStatus.RESERVED);
        // TODO: send PW to the interChannel
        for (PseudoWireIntent intent : iconaStoreService
                .getPseudoWire(pseudoWireEvent.pseudoWireId()).getIntents()) {
            interChannelService.addPseudoWireEvent(intent.dstClusterName(),
                                                   pseudoWireEvent.pseudoWireId(),
                                                   intent,
                                                   IntentRequestType.INSTALL,
                                                   IntentReplayType.EMPTY);

            // TODO: check the removal of both request
            // Remove old request
            // intentEvent.setIntentRequestType(IntentRequestType.RESERVE);
            // intentChannel.remove(intentEvent.getID());
            // TODO: Remove old ack, is correct?
            interChannelService.remIntentEvent(pseudoWireEvent);

        }
        iconaStoreService.getPseudoWire(pseudoWireEvent.pseudoWireId())
                .setPwStatus(PathInstallationStatus.COMMITTED);
        // TODO: send PW to the interChannel
    }

    @Override
    public void entryEvicted(EntryEvent<byte[], IconaPseudoWireIntentEvent> event) {
    }

    @Override
    public void mapEvicted(MapEvent event) {
    }

    @Override
    public void mapCleared(MapEvent event) {
    }
}
