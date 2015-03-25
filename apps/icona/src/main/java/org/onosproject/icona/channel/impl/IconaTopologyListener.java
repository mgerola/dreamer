package org.onosproject.icona.channel.impl;

import java.util.Optional;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

import org.onosproject.icona.IconaConfigService;
import org.onosproject.icona.channel.inter.IconaTopologyEvent;
import org.onosproject.icona.channel.inter.InterEndPointElement;
import org.onosproject.icona.channel.inter.InterLinkElement;
import org.onosproject.icona.channel.inter.InterPseudoWireElement;
import org.onosproject.icona.store.EndPoint;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.PseudoWire;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public class IconaTopologyListener
        implements EntryListener<byte[], IconaTopologyEvent> {

    private IconaStoreService iconaStoreService;
    private IconaConfigService iconaConfigService;

    public IconaTopologyListener(IconaStoreService stroreService,
                                 IconaConfigService configLoader) {
        this.iconaStoreService = stroreService;
        this.iconaConfigService = configLoader;
    }

    @Override
    public void entryAdded(EntryEvent<byte[], IconaTopologyEvent> arg0) {

        if (arg0.getValue().getEntryPointElement() != null) {
            InterEndPointElement endPointEvent = arg0.getValue()
                    .getEntryPointElement();

            iconaStoreService.addEndpoint(arg0.getValue().getClusterName(),
                                          endPointEvent.getDpid(),
                                          endPointEvent.getPortNumber());

        } else if (arg0.getValue().getInterLinkElement() != null) {
            InterLinkElement interLinkEvent = arg0.getValue()
                    .getInterLinkElement();

            iconaStoreService.addInterLink(arg0.getValue().getClusterName(),
                                           interLinkEvent
                                                   .getRemoteClusterName(),
                                           interLinkEvent.getLocalId(),
                                           interLinkEvent.getLocalPort(),
                                           interLinkEvent.getRemoteId(),
                                           interLinkEvent.getRemotePort());

        } else if (arg0.getValue().getInterPseudoWireElement() != null) {
            InterPseudoWireElement pwElement = arg0.getValue()
                    .getInterPseudoWireElement();

            Optional<EndPoint> srcEndPoint = iconaStoreService
                    .getEndPoint(DeviceId.deviceId(pwElement.srcId()),
                                 PortNumber.portNumber(pwElement.srcPort()));
            Optional<EndPoint> dstEndPoint = iconaStoreService
                    .getEndPoint(DeviceId.deviceId(pwElement.dstId()),
                                 PortNumber.portNumber(pwElement.dstPort()));

            if (srcEndPoint.isPresent() && dstEndPoint.isPresent()) {

                if (!arg0.getValue().getClusterName()
                        .equals(iconaConfigService.getClusterName())) {
                    // PseudoWire to be added
                    if (iconaStoreService.getPseudoWire(pwElement
                            .getPseudoWireId()) == null) {
                        iconaStoreService
                                .addPseudoWire(new PseudoWire(
                                                              srcEndPoint.get(),
                                                              dstEndPoint.get(),
                                                              arg0.getValue()
                                                                      .getClusterName(),
                                                              pwElement
                                                                      .pseudoWireInstallationStatus()));

                    }
                }
            }

        }
    }

    @Override
    public void entryRemoved(EntryEvent<byte[], IconaTopologyEvent> arg0) {

        if (arg0.getOldValue().getEntryPointElement() != null) {
            InterEndPointElement endPointEvent = arg0.getOldValue()
                    .getEntryPointElement();

            iconaStoreService.remEndpoint(arg0.getOldValue().getClusterName(),
                                          endPointEvent.getDpid(),
                                          endPointEvent.getPortNumber());

        } else if (arg0.getOldValue().getInterLinkElement() != null) {
            InterLinkElement interLinkEvent = arg0.getOldValue()
                    .getInterLinkElement();

            iconaStoreService.remInterLink(arg0.getOldValue().getClusterName(),
                                           interLinkEvent
                                                   .getRemoteClusterName(),
                                           interLinkEvent.getLocalId(),
                                           interLinkEvent.getLocalPort(),
                                           interLinkEvent.getRemoteId(),
                                           interLinkEvent.getRemotePort());

        } else if (arg0.getOldValue().getClusterElement() != null) {
            iconaStoreService.remCluster(arg0.getOldValue().getClusterElement()
                    .getClusterName());

        } else if (arg0.getOldValue().getInterPseudoWireElement() != null) {
            InterPseudoWireElement pwElement = arg0.getValue()
                    .getInterPseudoWireElement();

            if (iconaConfigService.getClusterName() != arg0.getOldValue()
                    .getClusterName()) {

                iconaStoreService.remPseudoWire(pwElement.getPseudoWireId());
            }
        }

    }

    @Override
    public void entryUpdated(EntryEvent<byte[], IconaTopologyEvent> arg0) {
        if (arg0.getValue().getInterPseudoWireElement() != null) {
            InterPseudoWireElement pwElement = arg0.getValue()
                    .getInterPseudoWireElement();

            if (!arg0.getValue().getClusterName()
                    .equals(iconaConfigService.getClusterName())) {

                iconaStoreService.updatePseudoWireStatus(pwElement
                        .getPseudoWireId(), pwElement
                        .pseudoWireInstallationStatus());
            }
        }
    }

    @Override
    public void mapCleared(MapEvent arg0) {
    }

    @Override
    public void mapEvicted(MapEvent arg0) {
    }

    @Override
    public void entryEvicted(EntryEvent<byte[], IconaTopologyEvent> arg0) {

    }

}
