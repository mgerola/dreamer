package org.onosproject.icona.channel.intra;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.onosproject.net.ConnectPoint;

public class IntraPseudoWireElement extends IconaIntraElement<IntraPseudoWireElement> {

    private String srcId;
    private long srcPort;

    private String dstId;
    private long dstPort;

    private IntentUpdateType intentUpdateType;

    private Integer mplsIngressLabel;
    private Integer mplsEgressLabel;

    public enum IntentUpdateType {
        INSTALL,
        DELETE,
    }

    public IntraPseudoWireElement() {
    }

    public IntraPseudoWireElement(ConnectPoint src, ConnectPoint dst,
                                  Optional<Integer> mplsIngressLabel,  Optional<Integer> mplsEgressLabel,
            IntentUpdateType intentUpdateType) {
        this.srcId = src.deviceId().toString();
        this.srcPort = src.port().toLong();
        this.dstId = dst.deviceId().toString();
        this.dstPort = dst.port().toLong();
        this.intentUpdateType = intentUpdateType;
        
        //TODO: 0 is not ok!
        this.mplsIngressLabel = mplsIngressLabel.orElse(0);
        this.mplsEgressLabel = mplsEgressLabel.orElse(0);
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

    public IntentUpdateType intentUpdateType() {
        return intentUpdateType;
    }

    public Integer egressLabel(){
        return mplsEgressLabel;
    }

    public Integer ingressLabel(){
        return mplsIngressLabel;
    }

    @Override
    ByteBuffer getIDasByteBuffer() {

        return getIntentElementId(this.srcId, this.srcPort, this.dstId, this.dstPort,
                this.intentUpdateType);
    }

    public static ByteBuffer getIntentElementId(String srcId, long srcPort,
            String dstId, long dstPort, IntentUpdateType intentType) {
        char type = 0;
        switch (intentType) {
        case DELETE:
            type = 'D';
            break;
        case INSTALL:
            type = 'I';
            break;
        }
        return (ByteBuffer) ByteBuffer
                .allocate(2 * Character.SIZE + 4 * Long.SIZE)
                .putChar('W')
                .putChar(type)
                .putLong(Long.parseLong(srcId.split(":")[1], 16))
                .putLong(srcPort)
                .putLong(Long.parseLong(dstId.split(":")[1], 16))
                .putLong(dstPort)
                .flip();
    }

    @Override
    public String toString() {
        return "IntraPseudoWireElement [srcId=" + srcId + ", srcPort=" + srcPort + ", dstId=" + dstId + ", dstPort="
                + dstPort + ", intentUpdateType=" + intentUpdateType + ", mplsIngressLabel=" + mplsIngressLabel
                + ", mplsEgressLabel=" + mplsEgressLabel + "]";
    }

}

