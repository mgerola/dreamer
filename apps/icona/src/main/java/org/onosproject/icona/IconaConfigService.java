package org.onosproject.icona;

import org.onosproject.core.ApplicationId;

public interface IconaConfigService {

    String getClusterName();

    public boolean isLeader();

    ApplicationId getApplicationId();

}
