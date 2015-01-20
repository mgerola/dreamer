package org.onosproject.icona.channel;

import java.nio.ByteBuffer;

import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public class InterLinkElement extends IconaTopologyElement<InterLinkElement> {

    /**
     *
     */
    private static final long serialVersionUID = 3347196674593714925L;
    private String remoteClusterName;
    private String localId;
    private long localPort;
    private String remoteId;
    private long remotePort;

    public InterLinkElement(String remoteClusterName, ConnectPoint src,
                            ConnectPoint dst) {
        this.remoteClusterName = remoteClusterName;
        this.localId = src.deviceId().toString();
        this.localPort = src.port().toLong();
        this.remoteId = dst.deviceId().toString();
        this.remotePort = dst.port().toLong();
    }

    public String getLocalId() {
        return localId;
    }

    public long getLocalPort() {
        return localPort;
    }

    public String getRemoteClusterName() {
        return remoteClusterName;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public long getRemotePort() {
        return remotePort;
    }

    @Override
    public ByteBuffer getIDasByteBuffer() {
        return getInterLinkId(this.localId, this.localPort, this.remoteId,
                              this.remotePort);
    }

    public static ByteBuffer getInterLinkId(String localId, long localPort,
                                            String remoteId, long remotePort) {

        return (ByteBuffer) ByteBuffer.allocate(Character.SIZE + 4 * Long.SIZE)
                .putChar('I')
                .putLong(Long.parseLong(localId.split(":")[1], 16))
                .putLong(localPort)
                .putLong(Long.parseLong(remoteId.split(":")[1], 16))
                .putLong(remotePort).flip();
    }

    @Override
    public String toString() {
        return "InterLinkEvent [remoteClusterName=" + remoteClusterName
                + ", localDpid=" + localId + ", localPort=" + localPort
                + ", remoteDpid=" + remoteId + ", remotePort=" + remotePort
                + "]";
    }

}
