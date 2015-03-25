package org.onosproject.icona.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.icona.BFSTree;
import org.onosproject.icona.IconaConfigService;
import org.onosproject.icona.InterClusterPath;
import org.onosproject.icona.channel.inter.InterChannelService;
import org.onosproject.icona.store.BackupInterLink;
import org.onosproject.icona.store.Cluster;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.InterLink;
import org.slf4j.Logger;

@Component(immediate = true)
public class IconaBackUpManager {
	private final Logger log = getLogger(getClass());
    private final Short STARTUP_DELAY = 25;
    private final Short NUM_THREADS = 1;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
   protected IconaStoreService iconaStoreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
   protected InterChannelService interChannelService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
   protected IconaConfigService iconaConfigService;

   @Activate
   public void activate() {
       log.info("Starting Inter Link Backup Service");
       final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(NUM_THREADS);
       executor.schedule(() -> computeBackups(), STARTUP_DELAY, TimeUnit.SECONDS);
   }

@Deactivate
   public void deactivate() {
       log.info("Stopping Inter Link Backup Service");
   }

	private void computeBackups() {
		log.info("Starting Inter Link Backup computation");
		Collection<InterLink> outgoingILs = iconaStoreService.getCluster(
				iconaConfigService.getClusterName()).getOutgoingInterLinks();
		for (InterLink il : outgoingILs) {
			log.info(
					"Computing InterLink Backup Path  from local switch DPID {} and port {} "
							+ "and remote switch DPID {} and port {}: ", il
							.src().deviceId(), il.src().port(), il.dst()
							.deviceId(), il.dst().port());
			BFSTree geoTree = new BFSTree(
					iconaStoreService.getCluster(il.srcClusterName()),
					iconaStoreService, il);

			Cluster dstCluster = iconaStoreService.getCluster(il.dstClusterName());
			InterClusterPath interClusterPath = geoTree.getPath(dstCluster);
			for (InterLink pippo : interClusterPath.getInterlinks()) {
				log.info(">>>>>> " + pippo.src().deviceId().toString() +"  "+ pippo.dst().deviceId().toString());
			}
			checkNotNull(interClusterPath.getInterlinks());

			il.setBIL(new BackupInterLink(il, interClusterPath));
			checkArgument(iconaStoreService.addBackupMasterPseudoWire(il.getBIL().getPW()));
			//install BIL in the dataplane

		}
	}
}
