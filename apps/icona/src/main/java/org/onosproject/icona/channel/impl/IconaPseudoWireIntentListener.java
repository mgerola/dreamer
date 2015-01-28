package org.onosproject.icona.channel.impl;

import java.util.Iterator;
import java.util.Optional;

import org.onlab.packet.MplsLabel;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.LeadershipService;
import org.onosproject.icona.IconaConfigService;
import org.onosproject.icona.IconaPseudoWireService;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent;
import org.onosproject.icona.channel.inter.InterChannelService;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentReplayType;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentRequestType;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.InterLink;
import org.onosproject.icona.store.PseudoWire;
import org.onosproject.icona.store.PseudoWireIntent;
import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.topology.PathService;
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

    protected PathService pathService;

    public IconaPseudoWireIntentListener(LeadershipService leadershipService,
                                         ClusterService clusterService,
                                         IconaConfigService iconaConfigService,
                                         IconaStoreService iconaStoreService,
                                         InterChannelService interChannelService,
                                         IconaPseudoWireService iconaPseudoWireService,
                                         PathService pathService) {
        this.leadershipService = leadershipService;
        this.clusterService = clusterService;
        this.iconaConfigService = iconaConfigService;
        this.iconaStoreService = iconaStoreService;
        this.interChannelService = interChannelService;
        this.iconaPseudoWireService = iconaPseudoWireService;
        this.pathService = pathService;

    }

    private final Logger log = getLogger(getClass());

    @Override
    public void entryAdded(EntryEvent<byte[], IconaPseudoWireIntentEvent> event) {
        if (event.getValue().dstCluster()
                .equals(iconaConfigService.getClusterName())) {

            IconaPseudoWireIntentEvent intentEvent = checkNotNull(event
                    .getValue());

            // Involved clusters: should install, reserve or delete a local
            // PW.
            switch (intentEvent.intentRequestType()) {
            case DELETE:
                break;
            case INSTALL:
                // Update of the status
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
                    Optional<MplsLabel> egressLabel = Optional.empty();
                    Optional<MplsLabel> ingressLabel = Optional.empty();
                    if (intentEvent.ingressLabel() != 0) {
                        ingressLabel = Optional.ofNullable(MplsLabel
                                .mplsLabel(intentEvent.ingressLabel()));
                    }
                    if (intentEvent.egressLabel() != 0) {
                        egressLabel = Optional.ofNullable(MplsLabel
                                .mplsLabel(intentEvent.egressLabel()));
                    }
                    log.info("Cluster {}, egress {} label {} ingress {} label {}",
                             iconaConfigService.getClusterName(), egress,
                             intentEvent.egressLabel(), ingress,
                             intentEvent.ingressLabel());
                    iconaPseudoWireService
                            .installPseudoWireIntent(ingress, ingressLabel,
                                                     egress, egressLabel);
                    // TODO: check the correct installation...
//                    intentEvent.intentReplayType(IntentReplayType.ACK);
//                    interChannelService.addPseudoWireEvent(intentEvent);

                }
                break;
            case RESERVE:
                // TODO: all instances of the cluster should save the intent

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

                    if (intentEvent.srcId().equals(intentEvent.dstId())
                            || !pathService
                                    .getPaths(DeviceId.deviceId(intentEvent
                                                      .srcId()),
                                              DeviceId.deviceId(intentEvent
                                                      .dstId())).isEmpty()) {
                        if (!intentEvent.isEgress()) {
                            // reserve an available egress label.
                            MplsLabel egressLabelReserved = iconaStoreService
                                    .reserveAvailableMplsLabel(new ConnectPoint(
                                                                                DeviceId.deviceId(intentEvent
                                                                                        .dstId()),
                                                                                PortNumber
                                                                                        .portNumber(intentEvent
                                                                                                .dstPort())));
                            intentEvent
                                    .egressLabel(egressLabelReserved.toInt());
                        }

                        intentEvent.intentReplayType(IntentReplayType.ACK);
                    } else {
                        intentEvent.intentReplayType(IntentReplayType.NACK);
                    }

                    log.info("Replica: intentRequest {} intentReplay {} srcDpid {} dstDpid {}",
                             intentEvent.intentRequestType(),
                             intentEvent.intentReplayType(),
                             intentEvent.srcId(), intentEvent.dstId());

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
                    // TODO: NACK case
                    log.info("NACK");
                }

                break;
            case RESERVE:
                log.info("PW leader: intentEvent {} srcDpid {} dstDpid {}",
                         intentEvent.intentRequestType(), intentEvent.srcId(),
                         intentEvent.dstId());
                if (intentEvent.intentReplayType() == IntentReplayType.ACK
                        && intentEvent.egressLabel() != null) {

                    PseudoWire pseudoWire = iconaStoreService
                            .getPseudoWire(intentEvent.pseudoWireId());
                    pseudoWire.setIntentStatus(intentEvent.dstCluster(),
                                               PathInstallationStatus.RESERVED);
                    pseudoWire.getIntent(intentEvent.dstCluster())
                            .egressLabel(MplsLabel.mplsLabel(intentEvent
                                                 .egressLabel()));

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
                    log.info("NACK");
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
        iconaStoreService.getPseudoWire(pseudoWireEvent.pseudoWireId())
                .setPwStatus(PathInstallationStatus.INSTALLED);

        for (PseudoWireIntent intent : iconaStoreService
                .getPseudoWire(pseudoWireEvent.pseudoWireId()).getIntents()) {

            // TODO: check the removal of both request
            interChannelService.addPseudoWireEvent(iconaConfigService
                                                           .getClusterName(),
                                                   pseudoWireEvent
                                                           .pseudoWireId(),
                                                   intent,
                                                   IntentRequestType.INSTALL,
                                                   IntentReplayType.EMPTY);
            // Remove old request
            pseudoWireEvent.intentReplayType(IntentReplayType.ACK);
            interChannelService.remIntentEvent(pseudoWireEvent);

        }
        iconaStoreService.getPseudoWire(pseudoWireEvent.pseudoWireId())
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

        iconaStoreService.getPseudoWire(pseudoWireEvent.pseudoWireId())
                .setPwStatus(PathInstallationStatus.RESERVED);

        Iterator<InterLink> iter = iconaStoreService
                .getPseudoWire(pseudoWireEvent.pseudoWireId())
                .getInterClusterPath().getInterlinks().iterator();
        while (iter.hasNext()) {
            // For each IL we use the same label for ingress and egress. We have
            // the egress label in one cluster, we need to store and send the
            // ingress one.

            PseudoWire pw = iconaStoreService.getPseudoWire(pseudoWireEvent
                    .pseudoWireId());

            if (iter.hasNext()) {
                InterLink interLink = iter.next();
                pw.getIntent(interLink.srcClusterName())
                        .egressLabel()
                        .ifPresent(egressLabel -> pw
                                           .getIntent(interLink
                                                              .dstClusterName())
                                           .ingressLabel(egressLabel));

            }
            log.info("intents {}", pw.getIntents());

        }
        // TODO: send PW to the interChannel
        for (PseudoWireIntent intent : iconaStoreService
                .getPseudoWire(pseudoWireEvent.pseudoWireId()).getIntents()) {

            interChannelService.addPseudoWireEvent(iconaConfigService
                                                           .getClusterName(),
                                                   pseudoWireEvent
                                                           .pseudoWireId(),
                                                   intent,
                                                   IntentRequestType.INSTALL,
                                                   IntentReplayType.EMPTY);

            // TODO: check the removal of both request
            // Remove old request
            pseudoWireEvent.intentReplayType(IntentReplayType.ACK);
            interChannelService.remIntentEvent(pseudoWireEvent);

        }
        iconaStoreService.getPseudoWire(pseudoWireEvent.pseudoWireId())
                .setPwStatus(PathInstallationStatus.INSTALLED);
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
