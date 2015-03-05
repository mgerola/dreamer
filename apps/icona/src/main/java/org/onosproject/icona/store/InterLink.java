package org.onosproject.icona.store;

import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public class InterLink {

    private String srcClusterName;
    private String dstClusterName;

    private ConnectPoint src;
    private ConnectPoint dst;

    public InterLink(String srcClusterName, String dstClusterName,
                     String srcDpid, Long srcPort, String dstDpid, Long dstPort) {
        this.src = new ConnectPoint(DeviceId.deviceId(srcDpid), PortNumber.portNumber(srcPort));
        this.srcClusterName = srcClusterName;

        this.dst = new ConnectPoint(DeviceId.deviceId(dstDpid), PortNumber.portNumber(dstPort));
        this.dstClusterName = dstClusterName;
    }

    public ConnectPoint src(){
        return this.src;
    }

    public ConnectPoint dst(){
        return this.dst;
    }

    public String srcClusterName() {
        return srcClusterName;
    }

    public String dstClusterName() {
        return dstClusterName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dst == null) ? 0 : dst.hashCode());
        result = prime * result
                + ((dstClusterName == null) ? 0 : dstClusterName.hashCode());
        result = prime * result + ((src == null) ? 0 : src.hashCode());
        result = prime * result
                + ((srcClusterName == null) ? 0 : srcClusterName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InterLink other = (InterLink) obj;
        if (dst == null) {
            if (other.dst != null)
                return false;
        } else if (!dst.equals(other.dst))
            return false;
        if (dstClusterName == null) {
            if (other.dstClusterName != null)
                return false;
        } else if (!dstClusterName.equals(other.dstClusterName))
            return false;
        if (src == null) {
            if (other.src != null)
                return false;
        } else if (!src.equals(other.src))
            return false;
        if (srcClusterName == null) {
            if (other.srcClusterName != null)
                return false;
        } else if (!srcClusterName.equals(other.srcClusterName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "InterLink [srcClusterName=" + srcClusterName
                + ", dstClusterName=" + dstClusterName + ", src=" + src
                + ", dst=" + dst + "]";
    }
    
    


}
