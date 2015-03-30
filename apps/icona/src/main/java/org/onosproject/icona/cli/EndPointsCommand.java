package org.onosproject.icona.cli;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.EndPoint;

@Command(scope = "icona", name = "endpoints", description = "Lists all endpoints")
public class EndPointsCommand extends ClustersCommand {

    @Override
    protected void execute() {
        IconaStoreService service = AbstractShellCommand.get(IconaStoreService.class);

        if (outputJson()) {
            print("%s", jsonEP(service, getSortedEndPoint(service)));
        } else {
            for (EndPoint endPoint : getSortedEndPoint(service)) {
                printEndPoint(endPoint);
            }
        }
    }
}
