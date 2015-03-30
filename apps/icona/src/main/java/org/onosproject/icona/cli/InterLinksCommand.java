package org.onosproject.icona.cli;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.InterLink;

@Command(scope = "icona", name = "interlinks", description = "Lists all interlinks")
public class InterLinksCommand extends ClustersCommand {

    @Override
    protected void execute() {
        IconaStoreService service = AbstractShellCommand.get(IconaStoreService.class);

        if (outputJson()) {
            print("%s", jsonIL(service, getSortedInterLinks(service)));
        } else {
            for (InterLink interlink : getSortedInterLinks(service)) {
                printInterLink(interlink);
            }
        }
    }

}
