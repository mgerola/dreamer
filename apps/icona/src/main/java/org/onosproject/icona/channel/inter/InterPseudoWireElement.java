package org.onosproject.icona.channel.inter;

import java.nio.ByteBuffer;

import org.onosproject.icona.store.PseudoWire.PathInstallationStatus;
import org.onosproject.net.ConnectPoint;

public class InterPseudoWireElement
        extends IconaTopologyElement<InterPseudoWireElement> {

    private String srcId;
    private long srcPort;

    private String dstId;
    private long dstPort;

    private PathInstallationStatus pwStatus;
    private String pseudoWireId;

    public InterPseudoWireElement() {
    }

    public InterPseudoWireElement(ConnectPoint src, ConnectPoint dst,
                                  PathInstallationStatus pwStatus, String pseudoWireId) {
        this.srcId = src.deviceId().toString();
        this.srcPort = src.port().toLong();
        this.dstId = dst.deviceId().toString();
        this.dstPort = dst.port().toLong();
        this.pwStatus = pwStatus;
        this.pseudoWireId = pseudoWireId;

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

     public PathInstallationStatus pseudoWireInstallationStatus() {
     return pwStatus;
     }

    public String getPseudoWireId() {
        return pseudoWireId;
    }

    @Override
    public ByteBuffer getIDasByteBuffer() {

        return getIntentElementId(this.srcId, this.srcPort, this.dstId,
                                  this.dstPort);
                                  //this.pwStatus);
    }

    public static ByteBuffer getIntentElementId(String srcId, long srcPort,
                                                String dstId, long dstPort){
                                                //PathInstallationStatus intentStatus) {
//        char type = 0;
//        switch (intentStatus) {
//        case RECEIVED:
//            type = 'D';
//            break;
//        case INSTALLED:
//            type = 'I';
//            break;
//        case RESERVED:
//            type = 'R';
//            break;
//        case COMMITTED:
//            type = 'C';
//            break;
//        default:
//            break;
//        }
        return (ByteBuffer) ByteBuffer
                .allocate(1 * Character.SIZE + 4 * Long.SIZE).putChar('W')
//                .putChar(type)
                .putLong(Long.parseLong(srcId.split(":")[1], 16))
                .putLong(srcPort)
                .putLong(Long.parseLong(dstId.split(":")[1], 16))
                .putLong(dstPort).flip();
    }

    @Override
    public String toString() {
        return "InterPseudoWireElement [srcId=" + srcId + ", srcPort="
                + srcPort + ", dstId=" + dstId + ", dstPort=" + dstPort
                + ", pwStatus=" + pwStatus + "]";
    }



}
