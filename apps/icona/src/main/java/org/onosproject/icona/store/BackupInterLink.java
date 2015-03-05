package org.onosproject.icona.store;

import org.onosproject.icona.InterClusterPath;
import org.onosproject.icona.store.MasterPseudoWire.PathInstallationStatus;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public class BackupInterLink {

    private InterLink il;
    InterClusterPath path;
    BackupMasterPseudoWire pw;
    
    private ConnectPoint src;
    private ConnectPoint dst;
    
    private String srcClusterName;
    private String dstClusterName;

	public BackupInterLink(InterLink il, InterClusterPath path) {
		this.il = il;
		this.path = path;
		this.srcClusterName = il.srcClusterName();
		this.dstClusterName = il.dstClusterName();
		
		this.src = this.path.getInterlinks().get(0).src();
		this.dst = this.path.getInterlinks().get(this.path.getInterlinks().size()-1).dst();
		this.pw = new BackupMasterPseudoWire(this.src, this.dst, path, PathInstallationStatus.RECEIVED);
	}

	public void setPW(BackupMasterPseudoWire pw) {
		this.pw = pw;
	}

	public BackupMasterPseudoWire getPW() {
		return this.pw;
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
        BackupInterLink other = (BackupInterLink) obj;
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

}
