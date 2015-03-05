package org.onosproject.cli.icona;

import org.onosproject.icona.store.Cluster;
import org.onosproject.icona.store.EndPoint;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.PseudoWire;
import org.apache.karaf.shell.commands.Command;

@Command(scope = "icona", name = "pseudowires", description = "Lists all PswudoWires")
public class PseudoWiresCommand extends ClustersCommand{

    @Override
    protected void execute() {
        IconaStoreService service = get(IconaStoreService.class);

        if (outputJson()) {
            print("%s", jsonPW(service, getSortedPseudoWire(service)));
        } else {
            for (PseudoWire pw : getSortedPseudoWire(service)) {
                printPseudoWire(pw);
            }
        }
    }
}
