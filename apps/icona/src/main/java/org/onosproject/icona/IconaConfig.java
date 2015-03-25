package org.onosproject.icona;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IconaConfig {

    private String clusterName;

    /**
     * Gets a list of addresses in the system.
     *
     * @return the list of addresses
     */
    public String clusterName() {
        return clusterName;
    }

    /**
     * Sets a list of addresses in the system.
     *
     * @param addresses the list of addresses
     */
    @JsonProperty("clusterName")
    public void setAddresses(String clusterName) {
        this.clusterName = clusterName;
    }

}
