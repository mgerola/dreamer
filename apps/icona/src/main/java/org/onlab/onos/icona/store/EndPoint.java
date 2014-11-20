package org.onlab.onos.icona.store;




public class EndPoint {

    private String clusterName;
    long dpid;
    int port;

    public EndPoint(String clusterName, long dpid, int port) {
        this.clusterName = clusterName;
        this.dpid = dpid;
        this.port = port;
    }

    public long getDpid() {
        return dpid;
    }

    public int getPort() {
        return port;
    }

    public String getClusterName() {
        return clusterName;
    }

    @Override
    public String toString() {
        return "EndPoint [clusterName=" + clusterName + ", dpid=" + dpid + ", port="
                + port + "]";
    }
}
