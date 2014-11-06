package org.onlab.onos.icona;

import java.nio.ByteBuffer;

public class IntraPseudoWireElement extends IconaIntraElement<IntraPseudoWireElement> {

    // private String srcClusterName;
    // private String dstClusterName;

    private long srcDpid;
    private int srcPort;

    private long dstDpid;
    private int dstPort;

    private String srcMacAddress;
    private String dstMacAddress;

    private IntentUpdateType intentUpdateType;

    public enum IntentUpdateType {
        INSTALL,
        DELETE,
    }

    public IntraPseudoWireElement() {
    }

    public IntraPseudoWireElement(long srcDpid, int srcPort, long dstDpid, int dstPort,
            IntentUpdateType intentUpdateType) {
        this.srcDpid = srcDpid;
        this.srcPort = srcPort;
        this.dstDpid = dstDpid;
        this.dstPort = dstPort;
        this.intentUpdateType = intentUpdateType;
    }

    public IntraPseudoWireElement(long srcDpid, int srcPort, long dstDpid, int dstPort,
            IntentUpdateType intentUpdateType, String srcMacAddress, String dstMacAddress) {
        this.srcDpid = srcDpid;
        this.srcPort = srcPort;
        this.dstDpid = dstDpid;
        this.dstPort = dstPort;
        this.intentUpdateType = intentUpdateType;

        this.srcMacAddress = srcMacAddress;
        this.dstMacAddress = dstMacAddress;
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

    public IntentUpdateType getIntentUpdateType() {
        return intentUpdateType;
    }

    public String getSrcMacAddress() {
        return srcMacAddress;
    }

    public String getDstMacAddress() {
        return dstMacAddress;
    }

    @Override
    ByteBuffer getIDasByteBuffer() {

        return getIntentElementId(this.srcDpid, this.srcPort, this.dstDpid, this.dstPort,
                this.intentUpdateType);
    }

    public static ByteBuffer getIntentElementId(long srcDpid, int srcPort,
            long dstDpid, int dstPort, IntentUpdateType intentType) {
        char type = 0;
        switch (intentType) {
        case DELETE:
            type = 'D';
            break;
        case INSTALL:
            type = 'I';
            break;
        default:
            break;
        }
        return (ByteBuffer) ByteBuffer
                .allocate(2 * Character.SIZE + 2 * Long.SIZE + 2 * Integer.SIZE)
                .putChar('W')
                .putChar(type)
                .putLong(srcDpid)
                .putInt(srcPort)
                .putLong(dstDpid)
                .putInt(dstPort)
                .flip();
    }

    @Override
    public String toString() {
        return "IntentElement [srcDPID=" + srcDpid + ", srcPort=" + srcPort
                + ", dstDPID=" + dstDpid + ", dstPort=" + dstPort + ", intentUpdateType="
                + intentUpdateType + "]";
    }

}
