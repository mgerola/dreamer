package org.onosproject.icona;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.onosproject.icona.store.InterLink;


public class InterClusterPath implements Serializable {

    private List<InterLink> interlinks;
    
    public InterClusterPath() {
        interlinks = new LinkedList<InterLink>();
    }

    public List<InterLink> getInterlinks() {
        return interlinks;
    }

    public void addInterlinks(InterLink interlink) {
        this.interlinks.add(interlink);
    }

    public void remInterlinks(InterLink interlink) {
        this.interlinks.remove(interlink);
    }

}
