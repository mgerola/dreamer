package org.onosproject.icona.channel.intra;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.onosproject.cluster.NodeId;

public class IconaIntraEvent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -463140479589768339L;

    private IntraType intraType;
    private IconaIntraElement<?> intraElement;
    private String nodeId;

    public enum IntraType {
        PSEUDOWIRE
    }

    public IconaIntraEvent() {

    }

    public IconaIntraEvent(IntraPseudoWireElement pwElement,
                           NodeId nodeId) {
        this.intraType = IntraType.PSEUDOWIRE;
        this.intraElement = pwElement;
        this.nodeId = nodeId.toString();
    }

    public IntraPseudoWireElement intraPseudoWireElement() {
        if (this.intraType != IntraType.PSEUDOWIRE) {
            return null;
        }
        IntraPseudoWireElement localPseudoWireElement = (IntraPseudoWireElement) this.intraElement;
        return localPseudoWireElement;
    }

    public String getNodeId() {
        return nodeId;
    }

    public byte[] getID() {
        return getIDasByteBuffer().array();
    }

    public ByteBuffer getIDasByteBuffer() {
        ByteBuffer element = null;

        element = intraElement.getIDasByteBuffer();

        byte[] instanceId = ("@" + nodeId)
                .getBytes(StandardCharsets.UTF_8);

        ByteBuffer buf = ByteBuffer.allocate(element.capacity()
                + instanceId.length);
        buf.put(element);
        buf.put(instanceId);
        buf.flip();
        return buf;
    }
}
