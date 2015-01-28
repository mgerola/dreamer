package org.onosproject.icona.channel.impl;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

import org.onosproject.icona.channel.inter.IconaTopologyEvent;
import org.onosproject.icona.channel.inter.InterEndPointElement;
import org.onosproject.icona.channel.inter.InterLinkElement;
import org.onosproject.icona.store.IconaStoreService;

public class IconaTopologyListener
        implements EntryListener<byte[], IconaTopologyEvent> {

    private IconaStoreService storeService;

    public IconaTopologyListener(IconaStoreService stroreService) {
        this.storeService = stroreService;
    }

    @Override
    public void entryAdded(EntryEvent<byte[], IconaTopologyEvent> arg0) {

        if (arg0.getValue().getEntryPointElement() != null) {
            InterEndPointElement endPointEvent = arg0.getValue()
                    .getEntryPointElement();

            storeService.addEndpoint(arg0.getValue().getClusterName(),
                                     endPointEvent.getDpid(),
                                     endPointEvent.getPortNumber());

        } else {
            if (arg0.getValue().getInterLinkElement() != null) {
                InterLinkElement interLinkEvent = arg0.getValue()
                        .getInterLinkElement();

                storeService
                        .addInterLink(arg0.getValue().getClusterName(),
                                      interLinkEvent.getRemoteClusterName(),
                                      interLinkEvent.getLocalId(),
                                      interLinkEvent.getLocalPort(),
                                      interLinkEvent.getRemoteId(),
                                      interLinkEvent.getRemotePort());

            }
        }

    }

    @Override
    public void entryRemoved(EntryEvent<byte[], IconaTopologyEvent> arg0) {

        if (arg0.getOldValue().getEntryPointElement() != null) {
            InterEndPointElement endPointEvent = arg0.getOldValue()
                    .getEntryPointElement();

            storeService.remEndpoint(arg0.getOldValue().getClusterName(),
                                     endPointEvent.getDpid(),
                                     endPointEvent.getPortNumber());

        } else if (arg0.getOldValue().getInterLinkElement() != null) {
            InterLinkElement interLinkEvent = arg0.getOldValue()
                    .getInterLinkElement();

            storeService.remInterLink(arg0.getOldValue().getClusterName(),
                                      interLinkEvent.getRemoteClusterName(),
                                      interLinkEvent.getLocalId(),
                                      interLinkEvent.getLocalPort(),
                                      interLinkEvent.getRemoteId(),
                                      interLinkEvent.getRemotePort());
        
        } else if (arg0.getOldValue().getClusterElement() != null){
            storeService.remCluster(arg0.getOldValue().getClusterElement().getClusterName());
        }

    }

    @Override
    public void entryUpdated(EntryEvent<byte[], IconaTopologyEvent> arg0) {
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
