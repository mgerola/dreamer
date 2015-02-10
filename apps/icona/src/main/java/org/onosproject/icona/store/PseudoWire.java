package org.onosproject.icona.store;

import static com.google.common.base.Preconditions.checkNotNull;

import org.onosproject.icona.InterClusterPath;

public class PseudoWire {

    private EndPoint srcEndPoint;
    private EndPoint dstEndPoint;
    private String pseudoWireId;
    
    private InterClusterPath path;
    
    public PseudoWire(EndPoint srcEndPoint, EndPoint dstEndPoint, InterClusterPath path) {
    checkNotNull(srcEndPoint);
    checkNotNull(dstEndPoint);
    
    this.dstEndPoint = dstEndPoint;
    this.srcEndPoint = srcEndPoint;
    this.pseudoWireId = srcEndPoint.deviceId() + "/" + srcEndPoint.port()
            + "-" + dstEndPoint.deviceId() + "/" + dstEndPoint.port();
    this.path = path;
    }
    
    public EndPoint getSrcEndPoint() {
        return srcEndPoint;
    }

    public EndPoint getDstEndPoint() {
        return dstEndPoint;
    }

    public String getPseudoWireId() {
        return pseudoWireId;
    }
    
    public InterClusterPath getInterClusterPath() {
        return path;
    }
    
    public void setInterClusterPath(InterClusterPath path){
        this.path = path;
    }

    @Override
    public String toString() {
        return "PseudoWire [srcEndPoint=" + srcEndPoint + ", dstEndPoint="
                + dstEndPoint + ", pseudoWireId=" + pseudoWireId + ", path="
                + path + "]";
    }
    
    
}
