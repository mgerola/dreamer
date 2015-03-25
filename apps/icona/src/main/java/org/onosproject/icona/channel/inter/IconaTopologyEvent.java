package org.onosproject.icona.channel.inter;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class IconaTopologyEvent implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 5673375805024568470L;
    private ElementType elementType;
    private IconaTopologyElement<?> topologyElement;
    private String clusterName;

    public enum ElementType {
        ENDPOINT, INTERLINK, CLUSTER, PSEUDOWIRE,
    }

    public IconaTopologyEvent() {

    }

    public IconaTopologyEvent(InterEndPointElement entryPointElement,
                              String clusterName) {
        super();
        this.elementType = ElementType.ENDPOINT;
        this.topologyElement = entryPointElement;
        this.clusterName = clusterName;
    }

    public IconaTopologyEvent(InterLinkElement interLinkElement,
                              String clusterName) {
        super();
        this.elementType = ElementType.INTERLINK;
        this.topologyElement = interLinkElement;
        this.clusterName = clusterName;
    }

    public IconaTopologyEvent(String clusterName) {
        super();
        this.elementType = ElementType.CLUSTER;
        this.topologyElement = new InterClusterElement(clusterName);
        this.clusterName = clusterName;
    }
    
    public IconaTopologyEvent(InterPseudoWireElement interPseudoWireElement,
                              String clusterName) {
        super();
        this.elementType = ElementType.PSEUDOWIRE;
        this.topologyElement = interPseudoWireElement;
        this.clusterName = clusterName;
    }

    public InterEndPointElement getEntryPointElement() {
        if (this.elementType != ElementType.ENDPOINT) {
            return null;
        }
        InterEndPointElement entryPointElement = (InterEndPointElement) this.topologyElement;
        return entryPointElement;
    }

    public InterLinkElement getInterLinkElement() {
        if (this.elementType != ElementType.INTERLINK) {
            return null;
        }
        InterLinkElement interLinkElement = (InterLinkElement) this.topologyElement;
        return interLinkElement;
    }

    public InterClusterElement getClusterElement() {
        if (this.elementType != ElementType.CLUSTER) {
            return null;
        }
        InterClusterElement interLinkElement = (InterClusterElement) this.topologyElement;
        return interLinkElement;
    }
    
    public InterPseudoWireElement getInterPseudoWireElement() {
        if (this.elementType != ElementType.PSEUDOWIRE) {
            return null;
        }
        InterPseudoWireElement interPseudoWireElement = (InterPseudoWireElement) this.topologyElement;
        return interPseudoWireElement;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public IconaTopologyElement<?> getTopologyElement() {
        return topologyElement;
    }

    public String getClusterName() {
        return clusterName;
    }

    public byte[] getID() {
        return getIDasByteBuffer().array();
    }

    public ByteBuffer getIDasByteBuffer() {
        ByteBuffer element = null;
       
        element = topologyElement.getIDasByteBuffer();

        byte[] cluster = ("@" + clusterName).getBytes(StandardCharsets.UTF_8);

        ByteBuffer buf = ByteBuffer.allocate(element.capacity()
                + cluster.length);
        buf.put(element);
        buf.put(cluster);
        buf.flip();
        return buf;
    }

    @Override
    public String toString() {
        return "IconaTopologyElement [" + "localCluster=" + clusterName
                + "elementType=" + elementType + "+ topologyElement="
                + topologyElement.toString() + "]";
    }

}
