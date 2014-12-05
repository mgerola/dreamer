package org.onosproject.icona;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class IconaIntentEvent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1340522102236993668L;

    private String clusterLeader;
    private String pseudoWireId;
    private String dstCluster;

    private long srcDpid;
    private int srcPort;

    private long dstDpid;
    private int dstPort;

    private IntentRequestType intentRequestType;
    private IntentReplayType intentReplayType;

    private String srcMacAddress;
    private String dstMacAddress;

    enum IntentRequestType {
        RESERVE, INSTALL, DELETE,
    }

    enum IntentReplayType {
        ACK, NACK, EMPTY,
    }

    public IconaIntentEvent() {
    }

    // public IntentEvent(String clusterLeader, String pwId,
    // IconaIntent intent, IntentRequestType intentRequestType,
    // IntentReplayType intentReplayType) {
    // this.dstCluster = intent.getDstClusterName();
    // this.srcDpid = intent.getSrcDpid();
    // this.srcPort = intent.getSrcPort();
    // this.dstDpid = intent.getDstDpid();
    // this.dstPort = intent.getDstPort();
    //
    // this.clusterLeader = clusterLeader;
    // this.pseudoWireId = pwId;
    // this.intentRequestType = intentRequestType;
    // this.intentReplayType = intentReplayType;
    // this.srcMacAddress = intent.getSrcMacAddress();
    // this.dstMacAddress = intent.getDstMacAddress();
    // }

    public String getDstCluster() {
        return dstCluster;
    }

    public void setDstCluster(String dstCluster) {
        this.dstCluster = dstCluster;
    }

    public long getSrcDpid() {
        return srcDpid;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public long getDstDpid() {
        return dstDpid;
    }

    public int getDstPort() {
        return dstPort;
    }

    public void setSrcDpid(long srcDpid) {
        this.srcDpid = srcDpid;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public void setDstDpid(long dstDpid) {
        this.dstDpid = dstDpid;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public String getSrcMacAddress() {
        return srcMacAddress;
    }

    public String getDstMacAddress() {
        return dstMacAddress;
    }

    public String getClusterLeader() {
        return clusterLeader;
    }

    public String getPseudoWireId() {
        return pseudoWireId;
    }

    public IntentRequestType getIntentRequestType() {
        return intentRequestType;
    }

    public IntentReplayType getIntentReplayType() {
        return intentReplayType;
    }

    public void setIntentReplayType(IntentReplayType intentReplayType) {
        this.intentReplayType = intentReplayType;
    }

    public void setIntentRequestType(IntentRequestType intentRequestType) {
        this.intentRequestType = intentRequestType;
    }

    public byte[] getID() {

        return getIntentElementId(this.srcDpid, this.srcPort, this.dstDpid,
                                  this.dstPort, this.intentRequestType,
                                  this.intentReplayType, this.pseudoWireId)
                .array();
    }

    public static ByteBuffer getIntentElementId(long srcDpid,
                                                int srcPort,
                                                long dstDpid,
                                                int dstPort,
                                                IntentRequestType intentRequestType,
                                                IntentReplayType intentReplayType,
                                                String pseudoWireId) {
        char requestType = 0;
        switch (intentRequestType) {
        case INSTALL:
            requestType = 'I';
            break;
        case RESERVE:
            requestType = 'R';
            break;
        case DELETE:
            requestType = 'D';
            break;
        default:
            break;
        }

        ByteBuffer id = ByteBuffer
                .allocate(3 * Character.SIZE + 2 * Long.SIZE + 2 * Integer.SIZE
                                  + Character.SIZE * pseudoWireId.length())
                .putChar('I').putChar(requestType)
                // .putChar(replayType)
                .putLong(srcDpid).putInt(srcPort).putLong(dstDpid)
                .putInt(dstPort);
        for (char ch : pseudoWireId.toCharArray()) {
            id.putChar(ch);
        }
        return (ByteBuffer) id.flip();
    }

    @Override
    public String toString() {
        return "IntentEvent [clusterLeader=" + clusterLeader
                + ", pseudoWireId=" + pseudoWireId + ", dstCluster="
                + dstCluster + ", srcDpid=" + srcDpid + ", srcPort=" + srcPort
                + ", dstDpid=" + dstDpid + ", dstPort=" + dstPort
                + ", intentRequestType=" + intentRequestType
                + ", intentReplayType=" + intentReplayType + "]";
    }

}
