package org.onosproject.icona.channel;

import java.io.Serializable;
import java.nio.ByteBuffer;


public class ClusterElement extends IconaTopologyElement<EndPointElement>
implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6386641729134504457L;
    
    private String clusterName;
    
    public ClusterElement (String clusterName){
        this.clusterName = clusterName;
    }
    
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
    
    @Override
    public ByteBuffer getIDasByteBuffer() {
        return getClusterId(clusterName);
    }

    public static ByteBuffer getClusterId(String clusterName) {
        return (ByteBuffer) ByteBuffer.allocate(Integer.SIZE + Character.SIZE)
                .putChar('C').putInt(clusterName.hashCode())
                .flip();
    }

}
