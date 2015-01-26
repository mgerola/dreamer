package org.onosproject.icona.store;


import org.onosproject.net.ConnectPoint;
import org.onosproject.net.ElementId;
import org.onosproject.net.PortNumber;

public class EndPoint extends ConnectPoint {

    private String clusterName;

    public EndPoint(String clusterName, ElementId elementId, PortNumber port) {
        super(elementId, port);
        this.clusterName = clusterName;

    }


    public String clusterName() {
        return clusterName;
    }


    @Override
    public String toString() {
        return "EndPoint [clusterName=" + clusterName + ", elementId="
                + elementId() + ", deviceId=" + deviceId() + "]";
    }




}
