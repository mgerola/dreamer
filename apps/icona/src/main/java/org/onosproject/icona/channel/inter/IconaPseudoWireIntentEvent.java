package org.onosproject.icona.channel.inter;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.onlab.packet.MplsLabel;
import org.onosproject.icona.store.PseudoWireIntent;

public class IconaPseudoWireIntentEvent implements Serializable {

    /**
         *
         */
    private static final long serialVersionUID = 1340522102236993668L;

    private String clusterLeader;
    private String pseudoWireId;
    private String dstCluster;

    private String srcId;
    private long srcPort;

    private String dstId;
    private long dstPort;

    private int ingressLabel;
    private int egressLabel;

    private IntentRequestType intentRequestType;
    private IntentReplayType intentReplayType;
    
    private Boolean isEgress;
    private Boolean isIngress;
    
    private Boolean isBackup;

    public enum IntentRequestType {
        RESERVE, INSTALL, DELETE,
    }

    public enum IntentReplayType {
        ACK, NACK, EMPTY,
    }

    public IconaPseudoWireIntentEvent() {
    }

    public IconaPseudoWireIntentEvent(String clusterLeader, String pwId,
                                      PseudoWireIntent intent,
                                      IntentRequestType intentRequestType,
                                      IntentReplayType intentReplayType) {

        this.dstCluster = intent.dstClusterName();
        this.srcId = intent.src().deviceId().toString();
        this.srcPort = intent.src().port().toLong();
        this.dstId = intent.dst().deviceId().toString();
        this.dstPort = intent.dst().port().toLong();
        if (intent.ingressLabel().isPresent()) {
            this.ingressLabel = intent.ingressLabel().get().toInt();
        } else {
            this.ingressLabel = 0;
        }
        if (intent.egressLabel().isPresent()) {
            this.egressLabel = intent.egressLabel().get().toInt();
        } else {
            this.egressLabel = 0;
        }

        this.clusterLeader = clusterLeader;
        this.pseudoWireId = pwId;
        this.intentRequestType = intentRequestType;
        this.intentReplayType = intentReplayType;
        this.isEgress = intent.isEgress();
        this.isIngress = intent.isIngress();
        this.isBackup = intent.isBackup();
    }

    public String dstCluster() {
        return dstCluster;
    }

    public void dstCluster(String dstCluster) {
        this.dstCluster = dstCluster;
    }

    public String srcId() {
        return srcId;
    }

    public long srcPort() {
        return srcPort;
    }

    public String dstId() {
        return dstId;
    }

    public long dstPort() {
        return dstPort;
    }

    public void srcId(String srcId) {
        this.srcId = srcId;
    }

    public void srcPort(long srcPort) {
        this.srcPort = srcPort;
    }

    public void dstId(String dstId) {
        this.dstId = dstId;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public String clusterLeader() {
        return clusterLeader;
    }

    public String pseudoWireId() {
        return pseudoWireId;
    }

    public IntentRequestType intentRequestType() {
        return intentRequestType;
    }

    public IntentReplayType intentReplayType() {
        return intentReplayType;
    }

    public void intentReplayType(IntentReplayType intentReplayType) {
        this.intentReplayType = intentReplayType;
    }

    public void intentRequestType(IntentRequestType intentRequestType) {
        this.intentRequestType = intentRequestType;
    }

    public Integer ingressLabel() {
        return ingressLabel;
    }

    public void ingressLabel(int ingressLabel) {
        this.ingressLabel = ingressLabel;
    }

    public Integer egressLabel() {
        return egressLabel;
    }

    public void egressLabel(int egressLabel) {
        this.egressLabel = egressLabel;
    }
    
    public boolean isEgress(){
        return isEgress;
    }
    
    public boolean isIngress(){
        return isIngress;
    }
    
    public boolean isBackup(){
        return isBackup;
    }

    public byte[] getID() {

        return getIntentElementId(this.srcId, this.srcPort, this.dstId,
                                  this.dstPort, this.intentRequestType,
                                  this.intentReplayType, this.pseudoWireId)
                .array();
    }

    public static ByteBuffer getIntentElementId(String srcId,
                                                long srcPort,
                                                String dstId,
                                                long dstPort,
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
                .allocate(3 * Character.SIZE + 4 * Long.SIZE + +Character.SIZE
                                  * pseudoWireId.length())
                .putChar('I')
                .putChar(requestType)
                // .putChar(replayType)
                .putLong(Long.parseLong(srcId.split(":")[1], 16))
                .putLong(srcPort)
                .putLong(Long.parseLong(dstId.split(":")[1], 16))
                .putLong(dstPort);
        for (char ch : pseudoWireId.toCharArray()) {
            id.putChar(ch);
        }
        return (ByteBuffer) id.flip();
    }

    @Override
    public String toString() {
        return "IntentEvent [clusterLeader=" + clusterLeader
                + ", pseudoWireId=" + pseudoWireId + ", dstCluster="
                + dstCluster + ", srcId=" + srcId + ", srcPort=" + srcPort
                + ", dstId=" + dstId + ", dstPort=" + dstPort
                + ", intentRequestType=" + intentRequestType
                + ", intentReplayType=" + intentReplayType + "]";
    }

}
