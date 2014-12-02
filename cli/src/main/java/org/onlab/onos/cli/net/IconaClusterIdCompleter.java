package org.onlab.onos.cli.net;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.onlab.onos.cli.AbstractShellCommand;
import org.onlab.onos.icona.store.Cluster;
import org.onlab.onos.icona.store.IconaStoreService;

public class IconaClusterIdCompleter implements Completer {

    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {
        // Delegate string completer
        StringsCompleter delegate = new StringsCompleter();

        IconaStoreService service = AbstractShellCommand
                .get(IconaStoreService.class);
        Iterator<Cluster> it = service.getClusters().iterator();
        SortedSet<String> strings = delegate.getStrings();
        while (it.hasNext()) {
            strings.add(it.next().getClusterName().toString());
        }

        // Now let the completer do the work for figuring out what to offer.
        return delegate.complete(buffer, cursor, candidates);

    }

}
