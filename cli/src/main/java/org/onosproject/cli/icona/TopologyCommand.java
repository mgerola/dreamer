package org.onosproject.cli.icona;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.icona.store.IconaStoreService;

import com.fasterxml.jackson.databind.ObjectMapper;

@Command(scope = "icona", name = "topology", description = "Lists summary of the current icona topology")
public class TopologyCommand extends AbstractShellCommand {

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
