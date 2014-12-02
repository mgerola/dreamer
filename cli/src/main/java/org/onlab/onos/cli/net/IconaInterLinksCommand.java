package org.onlab.onos.cli.net;

import org.apache.karaf.shell.commands.Command;
import org.onlab.onos.icona.store.IconaStoreService;
import org.onlab.onos.icona.store.InterLink;

@Command(scope = "icona", name = "interlinks", description = "Lists all interlinks")
public class IconaInterLinksCommand extends IconaClustersCommand {

    @Override
    protected void execute() {
        IconaStoreService service = get(IconaStoreService.class);

        if (outputJson()) {
            print("%s", jsonIL(service, getSortedInterLinks(service)));
        } else {
            for (InterLink interlink : getSortedInterLinks(service)) {
                printInterLink(interlink);
            }
        }
    }

}
