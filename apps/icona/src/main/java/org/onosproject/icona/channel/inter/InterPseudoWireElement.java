package org.onosproject.icona.channel.inter;

import java.nio.ByteBuffer;

import org.onosproject.net.ConnectPoint;

public class InterPseudoWireElement extends IconaTopologyElement<InterPseudoWireElement> {

    private String srcId;
    private long srcPort;

    private String dstId;
    private long dstPort;

//    private IntentStatus intentStatus;

    public enum IntentStatus {
        INSTALLED,
        RESERVED,
        REMOVED,
    }

    public InterPseudoWireElement() {
    }

    public InterPseudoWireElement(ConnectPoint src, ConnectPoint dst)
                                  //IntentStatus intentStatus) 
    {
        this.srcId = src.deviceId().toString();
        this.srcPort = src.port().toLong();
        this.dstId = dst.deviceId().toString();
        this.dstPort = dst.port().toLong();
//        this.intentStatus = intentStatus;

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

//    public IntentStatus intentStatus() {
//        return intentStatus;
//    }

    @Override
    public ByteBuffer getIDasByteBuffer() {

        return getIntentElementId(this.srcId, this.srcPort, this.dstId, this.dstPort);
                //, this.intentStatus);
    }

    public static ByteBuffer getIntentElementId(String srcId, long srcPort,
            String dstId, long dstPort)
            //, IntentStatus intentStatus) 
            {
        char type = 0;
//        switch (intentStatus) {
//        case REMOVED:
//            type = 'D';
//            break;
//        case INSTALLED:
//            type = 'I';
//            break;
//        case RESERVED:
//            type = 'R';
//            break;
//        }
        return (ByteBuffer) ByteBuffer
                .allocate(1 * Character.SIZE + 4 * Long.SIZE)
                .putChar('W')
//                .putChar(type)
                .putLong(Long.parseLong(srcId.split(":")[1], 16))
                .putLong(srcPort)
                .putLong(Long.parseLong(dstId.split(":")[1], 16))
                .putLong(dstPort)
                .flip();
    }

    @Override
    public String toString() {
        return "InterPseudoWireElement [srcId=" + srcId + ", srcPort="
                + srcPort + ", dstId=" + dstId + ", dstPort=" + dstPort;
                
                //+ ", intentStatus=" + intentStatus + "]";
    }


}
