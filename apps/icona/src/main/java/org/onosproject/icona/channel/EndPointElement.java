package org.onosproject.icona.channel;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public class EndPointElement extends IconaTopologyElement<EndPointElement>
        implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8487845250398385171L;

    private String dpid;
    private long port;

    // TODO: metrics ILs

    public EndPointElement(ConnectPoint cp) {
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
