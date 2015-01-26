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

//    public IconaIntraEvent(IntraInterLinkElement linkElement,
//                           String onosInstanceId) {
//        super();
//        this.intraType = IntraType.INTERLINK;
//        this.intraElement = linkElement;
//        this.onosInstanceId = onosInstanceId;
//    }

    // public IconaIntraEvent(IntraPortElement portElement,
    // String onosInstanceId) {
    // super();
    // this.intraType = IntraType.PORT;
    // this.intraElement = portElement;
    // this.onosInstanceId = onosInstanceId;
    // }

//    public IconaIntraEvent(IntraLinkElement linkElement, String onosInstanceId) {
//        super();
//        this.intraType = IntraType.LINK;
//        this.intraElement = linkElement;
//        this.onosInstanceId = onosInstanceId;
//    }
//
//    public IconaIntraEvent(IntraSwitchElement switchElement,
//                           String onosInstanceId) {
//        super();
//        this.intraType = IntraType.SWITCH;
//        this.intraElement = switchElement;
//        this.onosInstanceId = onosInstanceId;
//    }
//
//    public IntraInterLinkElement getIntraInterLinkElement() {
//        if (this.intraType != IntraType.INTERLINK) {
//            return null;
//        }
//        IntraInterLinkElement interLinkElement = (IntraInterLinkElement) this.intraElement;
//        return interLinkElement;
//    }

    public IntraPseudoWireElement intraPseudoWireElement() {
        if (this.intraType != IntraType.PSEUDOWIRE) {
            return null;
        }
        IntraPseudoWireElement localPseudoWireElement = (IntraPseudoWireElement) this.intraElement;
        return localPseudoWireElement;
    }

    // public IntraPortElement getInterPortElement() {
    // if (this.intraType != IntraType.PORT) {
    // return null;
    // }
    // IntraPortElement portElement = (IntraPortElement) this.intraElement;
    // return portElement;
    // }

//    public IntraLinkElement getIntraLinkElement() {
//        if (this.intraType != IntraType.LINK) {
//            return null;
//        }
//        IntraLinkElement linkElement = (IntraLinkElement) this.intraElement;
//        return linkElement;
//    }
//
//    public IntraSwitchElement getIntraSwitchElement() {
//        if (this.intraType != IntraType.SWITCH) {
//            return null;
//        }
//        IntraSwitchElement switchElement = (IntraSwitchElement) this.intraElement;
//        return switchElement;
//    }

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
