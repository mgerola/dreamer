package org.onosproject.icona;

import java.nio.ByteBuffer;

@Deprecated
public class IntraInterLinkElement
        extends IconaIntraElement<IntraInterLinkElement> {

    private String srcClusterName;
    private String dstClusterName;
    private long srcDpid;
    private int srcPort;
    private long dstDpid;
    private int dstPort;

    // private InterLinkUpdateType interLinkUpdateType;
    //
    // enum InterLinkUpdateType {
    // INSTALL,
    // DELETE,
    // }

    public IntraInterLinkElement(String srcClusterName, String dstClusterName,
                                 long srcDpid, int srcPort, long dstDpid,
                                 int dstPort) {
        // InterLinkUpdateType interLinkUpdateType) {
        this.srcClusterName = srcClusterName;
        this.dstClusterName = dstClusterName;
        this.srcDpid = srcDpid;
        this.srcPort = srcPort;
        this.dstDpid = dstDpid;
        this.dstPort = dstPort;
        // this.interLinkUpdateType = interLinkUpdateType;
    }

    // public InterLinkUpdateType getInterLinkUpdateType() {
    // return interLinkUpdateType;
    // }
    public String getSrcClusterName() {
        return srcClusterName;
    }

    public String getDstClusterName() {
        return dstClusterName;
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

    @Override
    ByteBuffer getIDasByteBuffer() {
        return getInterLinkId(this.srcDpid, this.srcPort, this.dstDpid,
                              this.dstPort);
    }

    public static ByteBuffer getInterLinkId(long srcDpid, int srcPort,
                                            long dstDpid, int dstPort) {
        return (ByteBuffer) ByteBuffer
                .allocate(Character.SIZE + 2 * Long.SIZE + 2 * Integer.SIZE)
                .putChar('I').putLong(srcDpid).putInt(srcPort).putLong(dstDpid)
                .putInt(dstPort).flip();
    }

    @Override
    public String toString() {
        return "LocalInterLinkElement [srcClusterName=" + srcClusterName
                + ", dstClusterName=" + dstClusterName + ", srcDpid=" + srcDpid
                + ", srcPort=" + srcPort + ", dstDpid=" + dstDpid
                + ", dstPort=" + dstPort + "]";
    }

}
