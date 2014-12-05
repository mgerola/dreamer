package org.onosproject.icona;

import java.nio.ByteBuffer;

public class IntraSwitchElement extends IconaIntraElement<IntraSwitchElement> {

    private long dpid;
    private SwUpdateType swUpdateType;

    enum SwUpdateType {
        DISCONNETED,
    }

    public long getDpid() {
        return dpid;
    }

    public SwUpdateType getSwUpdateType() {
        return swUpdateType;
    }

    public IntraSwitchElement(long dpid, SwUpdateType swUpdateType) {
        super();
        this.dpid = dpid;
        this.swUpdateType = swUpdateType;
    }

    @Override
    ByteBuffer getIDasByteBuffer() {
        return getSwElementId(this.dpid, this.swUpdateType);
    }

    public static ByteBuffer getSwElementId(long dpid,
                                            SwUpdateType linkUpdateType) {
        char type = 0;
        switch (linkUpdateType) {
        case DISCONNETED:
            type = 'D';
            break;
        default:
            break;
        }
        return (ByteBuffer) ByteBuffer.allocate(Character.SIZE + Long.SIZE)
                .putChar('S').putChar(type).putLong(dpid).flip();
    }

    @Override
    public String toString() {
        return "IntraSwitchElement [dpid=" + dpid + ", swUpdateType="
                + swUpdateType + "]";
    }

}
