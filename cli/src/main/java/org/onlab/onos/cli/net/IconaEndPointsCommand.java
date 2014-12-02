package org.onlab.onos.cli.net;

import org.apache.karaf.shell.commands.Command;
import org.onlab.onos.icona.store.IconaStoreService;
import org.onlab.onos.icona.store.EndPoint;

@Command(scope = "icona", name = "endpoints", description = "Lists all endpoints")
public class IconaEndPointsCommand extends IconaClustersCommand {

    @Override
    protected void execute() {
        IconaStoreService service = get(IconaStoreService.class);

        if (outputJson()) {
            print("%s", jsonEP(service, getSortedEndPoint(service)));
        } else {
            for (EndPoint endPoint : getSortedEndPoint(service)) {
                printEndPoint(endPoint);
            }
        }
    }
}
