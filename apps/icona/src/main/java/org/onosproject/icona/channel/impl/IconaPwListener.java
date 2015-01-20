package org.onosproject.icona.channel.impl;

import org.onosproject.cluster.LeadershipService;
import org.onosproject.icona.channel.IconaIntraEvent;
import org.onosproject.icona.store.IconaStoreService;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

public class IconaPwListener implements EntryListener<byte[], IconaIntraEvent> {

    private LeadershipService leadershipService;

    public IconaPwListener(LeadershipService leadershipService) {
        this.leadershipService = leadershipService;
    }
    
    @Override
    public void entryAdded(EntryEvent<byte[], IconaIntraEvent> event) {
        // TODO Auto-generated method stub
        
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
