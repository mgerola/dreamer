package org.onlab.onos.cli.net;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.onos.icona.store.Cluster;
import org.onlab.onos.icona.store.EndPoint;
import org.onlab.onos.icona.store.IconaStoreService;
import org.onlab.onos.icona.store.InterLink;

@Command(scope = "icona", name = "cluster", description = "Detailed information about a particular cluster")
public class IconaClusterIdCommand extends IconaClustersCommand {

    @Argument(index = 0, name = "clusterId", description = "Cluster ID", required = true, multiValued = false)
    String clusterId = null;

    @Override
    protected void execute() {
        IconaStoreService service = get(IconaStoreService.class);
        clusterId.trim();
        if (clusterId != null) {
            if (service.getCluster(clusterId) != null) {
                Cluster cluster = service.getCluster(clusterId);
                if (outputJson()) {
                    //TODO: make it!
                    json(service, service.getClusters());
                } else {
                    printCluster(cluster);
                    print("InterLinks");
                    for (InterLink interlink : cluster.getInterLinks()) {
                        printInterLink(interlink);
                    }
                    print("EndPoints");
                    for (EndPoint endPoint : cluster.getEndPoints()) {
                        printEndPoint(endPoint);
                    }
                }
            } else {
                print("Cluster %s does not exist", clusterId);
            }
        }
    }

}
