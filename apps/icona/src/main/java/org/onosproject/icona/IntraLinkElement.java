package org.onosproject.icona;

import java.nio.ByteBuffer;

@Deprecated
public class IntraLinkElement extends IconaIntraElement<IntraLinkElement> {

    private long srcDpid;
    private int srcPort;

    private long dstDpid;
    private int dstPort;

    private LinkUpdateType linkUpdateType;

    enum LinkUpdateType {
        INSTALL, DELETE,
    }

    public IntraLinkElement(long srcDpid, int srcPort, long dstDpid,
                            int dstPort, LinkUpdateType linkUpdateType) {
        this.srcDpid = srcDpid;
        this.srcPort = srcPort;
        this.dstDpid = dstDpid;
        this.dstPort = dstPort;
        this.linkUpdateType = linkUpdateType;
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

    public LinkUpdateType getLinkUpdateType() {
        return linkUpdateType;
    }

    @Override
    ByteBuffer getIDasByteBuffer() {
        return getLinkElementId(this.srcDpid, this.srcPort, this.dstDpid,
                                this.dstPort, this.linkUpdateType);
    }

    public static ByteBuffer getLinkElementId(long srcDpid, int srcPort,
                                              long dstDpid, int dstPort,
                                              LinkUpdateType linkUpdateType) {
        char type = 0;
        switch (linkUpdateType) {
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
                .putChar('L').putChar(type).putLong(srcDpid).putInt(srcPort)
                .putLong(dstDpid).putInt(dstPort).flip();
    }

    @Override
    public String toString() {
        return "IntraLinkElement [srcDpid=" + srcDpid + ", srcPort=" + srcPort
                + ", dstDpid=" + dstDpid + ", dstPort=" + dstPort
                + ", linkUpdateType=" + linkUpdateType + "]";
    }

}
