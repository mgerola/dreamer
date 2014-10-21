package org.onlab.onos.optical.cfg;

/**
 * WDM Link Java data object converted from a JSON file.
 */
class WdmLink {
    private String srcNodeName;
    private String snkNodeName;
    private String srcNodeId;
    private String snkNodeId;
    private int srcPort;
    private int snkPort;
    private double distance;
    private double cost;
    private int wavelengthNumber;
    private long adminWeight;

    public WdmLink(String name1, String name2) {
        this.srcNodeName = name1;
        this.snkNodeName = name2;
    }

    public WdmLink() {
        // TODO Auto-generated constructor stub
    }

    public void setSrcNodeName(String name) {
        this.srcNodeName = name;
    }

    public String getSrcNodeName() {
        return this.srcNodeName;
    }

    public void setSnkNodeName(String name) {
        this.snkNodeName = name;
    }

    public String getSnkNodeName() {
        return this.snkNodeName;
    }

    public void setSrcNodeId(String nodeId) {
        this.srcNodeId = nodeId;
    }

    public String getSrcNodeId() {
        return this.srcNodeId;
    }

    public void setSnkNodeId(String nodeId) {
        this.snkNodeId = nodeId;
    }

    public String getSnkNodeId() {
        return this.snkNodeId;
    }

    public void setSrcPort(int port) {
        this.srcPort = port;
    }

    public int getSrcPort() {
        return this.srcPort;
    }

    public void setSnkPort(int port) {
        this.snkPort = port;
    }

    public int getSnkPort() {
        return this.snkPort;
    }

    public void setDistance(double x) {
        this.distance = x;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setCost(double x) {
        this.cost = x;
    }

    public double getCost() {
        return this.cost;
    }

    public void setWavelengthNumber(int x) {
        this.wavelengthNumber = x;
    }

    public int getWavelengthNumber() {
        return this.wavelengthNumber;
    }

    public void setAdminWeight(long x) {
        this.adminWeight = x;
    }

    public long getAdminWeight() {
        return this.adminWeight;
    }

    @Override
    public String toString() {
        return new StringBuilder(" srcNodeName: ").append(this.srcNodeName)
                .append(" snkNodeName: ").append(this.snkNodeName)
                .append(" srcNodeId: ").append(this.srcNodeId)
                .append(" snkNodeId: ").append(this.snkNodeId)
                .append(" srcPort: ").append(this.srcPort)
                .append(" snkPort: ").append(this.snkPort)
                .append(" distance: ").append(this.distance)
                .append(" cost: ").append(this.cost)
                .append(" wavelengthNumber: ").append(this.wavelengthNumber)
                .append(" adminWeight: ").append(this.adminWeight).toString();
    }
}
