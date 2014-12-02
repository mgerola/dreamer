package org.onlab.onos.icona;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.onlab.onos.icona.channel.EndPointElement;
import org.onlab.onos.icona.channel.IconaTopologyElement;
import org.onlab.onos.icona.channel.InterLinkElement;

public class IconaTopologyEvent implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 5673375805024568470L;
    private ElementType elementType;
    private IconaTopologyElement<?> topologyElement;
    private String clusterName;

    public enum ElementType {
        ENDPOINT, INTERLINK,
    }

    public IconaTopologyEvent(EndPointElement entryPointElement,
                              String clusterName) {
        super();
        this.elementType = ElementType.ENDPOINT;
        this.topologyElement = entryPointElement;
        this.clusterName = clusterName;
    }

    public IconaTopologyEvent() {

    }

    public IconaTopologyEvent(InterLinkElement interLinkElement,
                              String clusterName) {
        super();
        this.elementType = ElementType.INTERLINK;
        this.topologyElement = interLinkElement;
        this.clusterName = clusterName;
    }

    public EndPointElement getEntryPointElement() {
        if (this.elementType != ElementType.ENDPOINT) {
            return null;
        }
        EndPointElement entryPointElement = (EndPointElement) this.topologyElement;
        return entryPointElement;
    }

    public InterLinkElement getInterLinkElement() {
        if (this.elementType != ElementType.INTERLINK) {
            return null;
        }
        InterLinkElement interLinkElement = (InterLinkElement) this.topologyElement;
        return interLinkElement;
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
