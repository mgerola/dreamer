package org.onosproject.icona.cli;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.icona.store.Cluster;
import org.onosproject.icona.store.IconaStoreService;

public class ClusterIdCompleter implements Completer {

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
