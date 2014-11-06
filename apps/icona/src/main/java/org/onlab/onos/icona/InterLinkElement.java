package org.onlab.onos.icona;

import java.nio.ByteBuffer;

public class InterLinkElement extends IconaTopologyElement<InterLinkElement> {

    private String remoteClusterName;
    private long localDpid;
    private int localPort;
    private long remoteDpid;
    private int remotePort;

    public InterLinkElement(String remoteClusterName, long srcDpid, int srcPort,
            long dstDpid, int dstPort) {
        this.remoteClusterName = remoteClusterName;
        this.localDpid = srcDpid;
        this.localPort = srcPort;
        this.remoteDpid = dstDpid;
        this.remotePort = dstPort;
    }

    public long getLocalDpid() {
        return localDpid;
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getRemoteClusterName() {
        return remoteClusterName;
    }

    public long getRemoteDpid() {
        return remoteDpid;
    }

    public int getRemotePort() {
        return remotePort;
    }


    @Override
    public ByteBuffer getIDasByteBuffer() {
        return getInterLinkId(this.localDpid, this.localPort, this.remoteDpid,
                this.remotePort);
    }

    public static ByteBuffer getInterLinkId(long localDpid, int localPort, long remoteDpid,
            int remotePort) {
        return (ByteBuffer) ByteBuffer
                .allocate(Character.SIZE + 2 * Long.SIZE + 2 * Integer.SIZE)
                .putChar('I')
                .putLong(localDpid)
                .putInt(localPort)
                .putLong(localDpid)
                .putInt(localPort)
                .flip();
    }

    @Override
    public String toString() {
        return "InterLinkEvent [remoteClusterName=" + remoteClusterName + ", localDpid="
                + localDpid + ", localPort=" + localPort + ", remoteDpid=" + remoteDpid
                + ", remotePort=" + remotePort + "]";
    }

}