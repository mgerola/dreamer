package org.onosproject.icona.channel.inter;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.onosproject.net.ConnectPoint;

public class InterEndPointElement extends IconaTopologyElement<InterEndPointElement>
        implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8487845250398385171L;

    private String dpid;
    private long port;

    // TODO: metrics ILs

    public InterEndPointElement(ConnectPoint cp) {
        this.dpid = cp.deviceId().toString();
        this.port = cp.port().toLong();

    }

    public String getDpid() {
        return dpid;
    }

    public long getPortNumber() {
        return port;
    }

    @Override
    public String toString() {
        return "EndPointEvent [dpid=" + dpid + ", port=" + port + "]";
    }

    @Override
    public ByteBuffer getIDasByteBuffer() {
        return getEndPointId(this.dpid, this.port);
    }

    public static ByteBuffer getEndPointId(String dpid, long port) {
        return (ByteBuffer) ByteBuffer.allocate(2 * Long.SIZE + Character.SIZE)
                .putChar('E').putLong(Long.parseLong(dpid.split(":")[1], 16))
                .putLong(port).flip();
    }

}
