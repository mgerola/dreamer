package org.onlab.onos.cli.net;

import org.apache.karaf.shell.commands.Command;
import org.onlab.onos.cli.AbstractShellCommand;
import org.onlab.onos.icona.store.IconaStoreService;

import com.fasterxml.jackson.databind.ObjectMapper;

@Command(scope = "icona", name = "topology", description = "Lists summary of the current icona topology")
public class IconaTopologyCommand extends AbstractShellCommand {

    protected IconaStoreService storeService;

    private static final String FMT = "Clusters=%d, InterLinks=%d, EndPoints=%d";

    @Override
    protected void execute() {
        storeService = get(IconaStoreService.class);
        if (outputJson()) {
            print("%s",
                  new ObjectMapper()
                          .createObjectNode()
                          .put("clusterCount",
                               storeService.getClusters().size())
                          .put("ILCount", storeService.getInterLinks().size())
                          .put("EPCount", storeService.getEndPoints().size()));
        } else {
            print(FMT, storeService.getClusters().size(), storeService
                    .getInterLinks().size(), storeService.getEndPoints().size());
        }
    }

}
