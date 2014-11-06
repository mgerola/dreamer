package org.onlab.onos.icona;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class EndPointElement extends IconaTopologyElement<EndPointElement> implements
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8487845250398385171L;

    private long dpid;
    private int port;

    // TODO: metrics ILs

    public EndPointElement(long dpid, int portNumber) {
        this.dpid = dpid;
        this.port = portNumber;

    }

    public long getDpid() {
        return dpid;
    }

    public int getPortNumber() {
        return port;
    }




    @Override
    public String toString() {
        return "EndPointEvent [dpid=" + dpid + ", port=" + port + "]";
    }

    @Override
    public ByteBuffer getIDasByteBuffer() {
        return getEndPointLink(this.dpid, this.port);
    }

    public static ByteBuffer getEndPointLink(long dpid, int port) {
        return (ByteBuffer) ByteBuffer
                .allocate(Long.SIZE + Character.SIZE + Integer.SIZE)
                .putChar('E')
                .putLong(dpid)
                .putInt(port).flip();
    }

}
