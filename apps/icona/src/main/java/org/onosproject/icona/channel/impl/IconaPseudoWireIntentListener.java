package org.onosproject.icona.channel.impl;

import java.util.Iterator;
import java.util.Optional;

import org.onlab.packet.MplsLabel;
import org.onosproject.icona.IconaConfigService;
import org.onosproject.icona.IconaPseudoWireService;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent;
import org.onosproject.icona.channel.inter.InterChannelService;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentReplayType;
import org.onosproject.icona.channel.inter.IconaPseudoWireIntentEvent.IntentRequestType;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.InterLink;
import org.onosproject.icona.store.MasterPseudoWire;
import org.onosproject.icona.store.PseudoWireIntent;
import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.intent.IntentId;
import org.onosproject.net.topology.PathService;
import org.slf4j.Logger;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

public class IconaPseudoWireIntentListener
        implements EntryListener<byte[], IconaPseudoWireIntentEvent> {



    protected IconaConfigService iconaConfigService;

    protected IconaStoreService iconaStoreService;

    protected InterChannelService interChannelService;

    protected IconaPseudoWireService iconaPseudoWireService;

    protected PathService pathService;

    public IconaPseudoWireIntentListener(
                                         IconaConfigService iconaConfigService,
                                         IconaStoreService iconaStoreService,
                                         InterChannelService interChannelService,
                                         IconaPseudoWireService iconaPseudoWireService,
                                         PathService pathService) {
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

                if (iconaConfigService.isLeader()) {
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
                    if (!intentEvent.ingressLabel().equals(0)) {
                        ingressLabel = Optional.ofNullable(MplsLabel
                                .mplsLabel(intentEvent.ingressLabel()));
                    }
                    if (!intentEvent.egressLabel().equals(0)) {
                        egressLabel = Optional.ofNullable(MplsLabel
                                .mplsLabel(intentEvent.egressLabel()));
                    }
                    log.info("Cluster {}, egress {} label {} ingress {} label {}",
                             iconaConfigService.getClusterName(), egress,
                             egressLabel, ingress, ingressLabel);

                    IntentId intentId = iconaPseudoWireService
                            .installPseudoWireIntent(ingress, ingressLabel,
                                                     egress, egressLabel);
                    // TODO: check the correct installation...
                    intentEvent.intentReplayType(IntentReplayType.ACK);
                    interChannelService.addPseudoWireIntentEvent(intentEvent);

                    log.info("Intent ID: {}", intentId);

                    if (intentEvent.clusterLeader()
                            .equals(iconaConfigService.getClusterName())) {
                        PseudoWireIntent pw = iconaStoreService
                                .getMasterPseudoWire(intentEvent.pseudoWireId())
                                .getLocalIntent();
                        pw.intentId(intentId);
                        pw.ingressLabel(ingressLabel);
                        pw.egressLabel(egressLabel);
                        pw.installationStatus(PathInstallationStatus.INSTALLED);

                    } else {
                        log.info("PW: {}", iconaStoreService
                                .getPseudoWire(intentEvent.pseudoWireId()));

                        PseudoWireIntent pw = iconaStoreService
                                .getPseudoWire(intentEvent.pseudoWireId())
                                .getLocalIntent();
                        pw.intentId(intentId);
                        pw.ingressLabel(ingressLabel);
                        pw.egressLabel(egressLabel);
                        pw.installationStatus(PathInstallationStatus.INSTALLED);
                    }
                }
                break;

            case RESERVE:
                // TODO: all instances of the cluster should save the intent

                if(iconaConfigService.isLeader()) {
                    log.info("Replica: intentEvent {} srcDpid {} dstDpid {}",
                             intentEvent.intentRequestType(),
                             intentEvent.srcId(), intentEvent.dstId());

                    if (intentEvent.srcId().equals(intentEvent.dstId())
                            || !pathService
                                    .getPaths(DeviceId.deviceId(intentEvent
                                                      .srcId()),
                                              DeviceId.deviceId(intentEvent
                                                      .dstId())).isEmpty()) {
                        int egressLabel = 0;
                        if (!intentEvent.isEgress()) {
                            // reserve an available egress label.
                            MplsLabel egressLabelReserved  = iconaStoreService
                                    .reserveAvailableMplsLabel(new ConnectPoint(
                                                                                DeviceId.deviceId(intentEvent
                                                                                        .dstId()),
                                                                                PortNumber
                                                                                        .portNumber(intentEvent
                                                                                        .dstPort())));
                            egressLabel = egressLabelReserved.toInt();
                            intentEvent
                                    .egressLabel(egressLabel);
                        }

                        // Save localIntent
                        PseudoWireIntent pwIntent = new PseudoWireIntent(
                                                                         intentEvent
                                                                                 .dstCluster(),
                                                                         intentEvent
                                                                                 .srcId(),
                                                                         intentEvent
                                                                                 .srcPort(),
                                                                         intentEvent
                                                                                 .dstId(),
                                                                         intentEvent
                                                                                 .dstPort(),
                                                                         null,
                                                                         egressLabel,
                                                                         PathInstallationStatus.RESERVED,
                                                                         intentEvent
                                                                                 .isIngress(),
                                                                         intentEvent
                                                                                 .isEgress());

                        iconaStoreService.addLocalIntent(intentEvent
                                .pseudoWireId(), pwIntent);

                        intentEvent.intentReplayType(IntentReplayType.ACK);
                    } else {

                        intentEvent.intentReplayType(IntentReplayType.NACK);
                    }

                    log.info("Replica: intentRequest {} intentReplay {} srcDpid {} dstDpid {}",
                             intentEvent.intentRequestType(),
                             intentEvent.intentReplayType(),
                             intentEvent.srcId(), intentEvent.dstId());

                    interChannelService.addPseudoWireIntentEvent(intentEvent);
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

                    iconaStoreService
                            .getMasterPseudoWire(intentEvent.pseudoWireId())
                            .setIntentStatus(intentEvent.dstCluster(),
                                             PathInstallationStatus.INSTALLED);

                    log.info("intentEvent {} {} srcDpid {} dstDpid {}",
                             intentEvent.intentRequestType(),
                             intentEvent.intentReplayType(),
                             intentEvent.srcId(), intentEvent.dstId());

                    if (iconaConfigService.isLeader()) {
                        interChannelService
                                .addPseudoWireEvent(iconaStoreService
                                        .getMasterPseudoWire(intentEvent
                                                .pseudoWireId()));
                        interChannelService.remIntentEvent(intentEvent);
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

                    MasterPseudoWire pseudoWire = iconaStoreService
                            .getMasterPseudoWire(intentEvent.pseudoWireId());
                    pseudoWire.setIntentStatus(intentEvent.dstCluster(),
                                               PathInstallationStatus.RESERVED);
                    pseudoWire.getIntent(intentEvent.dstCluster())
                            .egressLabel(MplsLabel.mplsLabel(intentEvent
                                                 .egressLabel()));

                    if (iconaConfigService.isLeader()) {

                        interChannelService.remIntentEvent(intentEvent);
                        checkIntentReserved(intentEvent);

                        interChannelService
                                .addPseudoWireEvent(iconaStoreService
                                        .getMasterPseudoWire(intentEvent
                                                .pseudoWireId()));
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
        MasterPseudoWire pw = iconaStoreService
                .getMasterPseudoWire(pseudoWireEvent.pseudoWireId());
        log.info("INSTALL: Dentro!!!!!");
        if (pw.getPwStatus() != PathInstallationStatus.INSTALLED) {
            for (PseudoWireIntent intent : pw.getIntents()) {
                log.info("INSTALL: loop dentro!!!!!");
                if (intent.installationStatus() != PathInstallationStatus.INSTALLED) {
                    return;
                }
            }
            log.info("INSTALLED");
            pw.setPwStatus(PathInstallationStatus.INSTALLED);

            for (PseudoWireIntent intent : pw.getIntents()) {

                // TODO: check the removal of both request
                interChannelService
                        .addPseudoWireIntentEvent(iconaConfigService
                                                          .getClusterName(),
                                                  pseudoWireEvent
                                                          .pseudoWireId(),
                                                  intent,
                                                  IntentRequestType.INSTALL,
                                                  IntentReplayType.EMPTY);
            }
            iconaStoreService.getPseudoWire(pseudoWireEvent.pseudoWireId())
                    .setPwStatus(PathInstallationStatus.INSTALLED);
            // TODO: send PW to the interChannel
        }
    }

    // if all Intent are reserved, publish install!
    private void checkIntentReserved(IconaPseudoWireIntentEvent pseudoWireEvent) {
        log.info("Reserve: Dentro!!!!!");
        MasterPseudoWire pw = iconaStoreService
                .getMasterPseudoWire(pseudoWireEvent.pseudoWireId());
        if (pw.getPwStatus() != PathInstallationStatus.RESERVED
                || pw.getPwStatus() != PathInstallationStatus.INSTALLED) {
            for (PseudoWireIntent intent : pw.getIntents()) {
                log.info("Reserve: loop dentro!!!!!");
                if (intent.installationStatus() != PathInstallationStatus.RESERVED) {
                    log.info("NOT RESERVED!!!!!!!!!");
                    return;
                }
            }

            Iterator<InterLink> iter = pw.getInterClusterPath().getInterlinks()
                    .iterator();
            while (iter.hasNext()) {
                // For each IL we use the same label for ingress and egress. We
                // have
                // the egress label in one cluster, we need to store and send
                // the
                // ingress one.

                if (iter.hasNext()) {
                    InterLink interLink = iter.next();
                    pw.getIntent(interLink.srcClusterName())
                            .egressLabel()
                            .ifPresent(egressLabel -> pw
                                               .getIntent(interLink
                                                                  .dstClusterName())
                                               .ingressLabel(egressLabel));

                }

            }
            log.info("intents {}", pw.getIntents());
            pw.setPwStatus(PathInstallationStatus.RESERVED);

            // TODO: send PW to the interChannel
            for (PseudoWireIntent intent : pw.getIntents()) {

                interChannelService
                        .addPseudoWireIntentEvent(iconaConfigService
                                                          .getClusterName(),
                                                  pseudoWireEvent
                                                          .pseudoWireId(),
                                                  intent,
                                                  IntentRequestType.INSTALL,
                                                  IntentReplayType.EMPTY);

            }
            pw.setPwStatus(PathInstallationStatus.INSTALLED);

            // TODO: send PW to the interChannel
        }
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
