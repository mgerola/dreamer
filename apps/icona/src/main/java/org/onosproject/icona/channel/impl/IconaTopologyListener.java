package org.onosproject.icona.channel.impl;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

import org.onosproject.icona.channel.EndPointElement;
import org.onosproject.icona.channel.IconaTopologyEvent;
import org.onosproject.icona.channel.InterLinkElement;
import org.onosproject.icona.store.IconaStoreService;

public class IconaTopologyListener
        implements EntryListener<byte[], IconaTopologyEvent> {

    private final Logger log = getLogger(getClass());
    private static IconaStoreService storeService;

    public IconaTopologyListener(IconaStoreService stroreService) {
        IconaTopologyListener.storeService = stroreService;
    }

    @Override
    public void entryAdded(EntryEvent<byte[], IconaTopologyEvent> arg0) {

        if (arg0.getValue().getEntryPointElement() != null) {
            log.info("EntryPointElement Added");
            EndPointElement endPointEvent = arg0.getValue()
                    .getEntryPointElement();

            storeService.addEndpoint(arg0.getValue().getClusterName(),
                                     endPointEvent.getDpid(),
                                     endPointEvent.getPortNumber());

        } else {
            if (arg0.getValue().getInterLinkElement() != null) {
                InterLinkElement interLinkEvent = arg0.getValue()
                        .getInterLinkElement();
                log.info("InterLink Added");

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
    public void entryEvicted(EntryEvent<byte[], IconaTopologyEvent> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void entryRemoved(EntryEvent<byte[], IconaTopologyEvent> arg0) {
        log.info("Entry removed!");

        if (arg0.getOldValue().getEntryPointElement() != null) {
            log.info("EntryPointElement removed");
            EndPointElement endPointEvent = arg0.getOldValue()
                    .getEntryPointElement();

            storeService.remEndpoint(arg0.getOldValue().getClusterName(),
                                     endPointEvent.getDpid(),
                                     endPointEvent.getPortNumber());

        } else if (arg0.getOldValue().getInterLinkElement() != null) {
            InterLinkElement interLinkEvent = arg0.getOldValue()
                    .getInterLinkElement();
            log.info("IL removed");

            storeService.remInterLink(arg0.getOldValue().getClusterName(),
                                      interLinkEvent.getRemoteClusterName(),
                                      interLinkEvent.getLocalId(),
                                      interLinkEvent.getLocalPort(),
                                      interLinkEvent.getRemoteId(),
                                      interLinkEvent.getRemotePort());

        }

    }

    @Override
    public void entryUpdated(EntryEvent<byte[], IconaTopologyEvent> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mapCleared(MapEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mapEvicted(MapEvent arg0) {
        // TODO Auto-generated method stub

    }

}
