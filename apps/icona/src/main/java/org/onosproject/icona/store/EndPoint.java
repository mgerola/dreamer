package org.onosproject.icona.store;

import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public class EndPoint {

    private String clusterName;
    private DeviceId id;
    private PortNumber port;

    public EndPoint(String clusterName, String dpid, long port) {
        this.clusterName = clusterName;
        this.id = DeviceId.deviceId(dpid);
        this.port = PortNumber.portNumber(port);
    }

    public DeviceId getId() {
        return id;
    }

    public PortNumber getPort() {
        return port;
    }

    public String getClusterName() {
        return clusterName;
    }

    @Override
    public String toString() {
        return "EndPoint [clusterName=" + clusterName + ", dpid=" + id
                + ", port=" + port + "]";
    }
}
