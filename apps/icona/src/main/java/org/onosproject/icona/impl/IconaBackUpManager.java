package org.onosproject.icona.impl;

import static org.slf4j.LoggerFactory.getLogger;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.icona.channel.inter.InterChannelService;
import org.onosproject.icona.store.IconaStoreService;
import org.slf4j.Logger;

@Component(immediate = true)
public class IconaBackUpManager {
	private final Logger log = getLogger(getClass());
    
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
   protected IconaStoreService iconaStoreService;
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
   protected InterChannelService interChannelService;
   
   @Activate
   public void activate() {
       log.info("Starting Inter Link Backup Service");
   }
   
   @Deactivate
   public void deactivate() {
       log.info("Stopping Inter Link Backup Service");
   }

   
}
