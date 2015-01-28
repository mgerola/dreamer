package org.onosproject.icona.channel.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Date;

import org.onosproject.icona.channel.inter.IconaManagementEvent;
import org.onosproject.icona.store.Cluster;
import org.onosproject.icona.store.IconaStoreService;
import org.slf4j.Logger;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

public class IconaMgmtListener
        implements EntryListener<String, IconaManagementEvent> {

    private final Logger log = getLogger(getClass());

    private IconaStoreService storeService;

    public IconaMgmtListener(IconaStoreService stroreService) {
        this.storeService = stroreService;
    }

    @Override
    public void entryAdded(EntryEvent<String, IconaManagementEvent> update) {
        // log.info("Received Hello " + update.getValue().getClusterName());

        if (storeService.getCluster(update.getValue().getClusterName()) != null) {
            storeService.getCluster(update.getValue().getClusterName())
                    .setLastSeen(new Date());
            return;
        }
        storeService.addCluster(new Cluster(update.getValue().getClusterName(),
                                            new Date()));

        log.info("Added cluster {} ", update.getValue().getClusterName());

    }

    @Override
    public void entryEvicted(EntryEvent<String, IconaManagementEvent> arg0) {
    }

    @Override
    public void entryRemoved(EntryEvent<String, IconaManagementEvent> update) {

    }

    @Override
    public void entryUpdated(EntryEvent<String, IconaManagementEvent> event) {
    }

    @Override
    public void mapCleared(MapEvent arg0) {
    }

    @Override
    public void mapEvicted(MapEvent arg0) {
    }

}
