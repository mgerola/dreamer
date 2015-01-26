package org.onosproject.icona.channel.impl;

import org.onosproject.icona.IconaService;
import org.onosproject.icona.channel.intra.IconaIntraEvent;
import org.onosproject.icona.channel.intra.IntraChannelService;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

public class IconaPseudoWireListener implements EntryListener<byte[], IconaIntraEvent> {

    private IconaService iconaService;
    private IntraChannelService intraChannelService;

    public IconaPseudoWireListener(IconaService iconaService, IntraChannelService intraChannelService) {
        this.iconaService = iconaService;
        this.intraChannelService = intraChannelService;
    }

    @Override
    public void entryAdded(EntryEvent<byte[], IconaIntraEvent> event) {

        if (event.getValue().intraPseudoWireElement() != null){
        iconaService.handlePseudoWire(event.getValue());
        // Remove the intra message
        intraChannelService.remIntraPseudoWire(event.getValue());
        }

    }

    @Override
    public void entryRemoved(EntryEvent<byte[], IconaIntraEvent> event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void entryUpdated(EntryEvent<byte[], IconaIntraEvent> event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void entryEvicted(EntryEvent<byte[], IconaIntraEvent> event) {
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
