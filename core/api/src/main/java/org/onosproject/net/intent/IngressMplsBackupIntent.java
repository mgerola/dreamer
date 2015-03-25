package org.onosproject.net.intent;

import java.util.List;
import java.util.Optional;

import org.onlab.packet.MplsLabel;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Link;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.constraint.LinkTypeConstraint;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

public class IngressMplsBackupIntent extends MplsIntent {
    
    private MplsLabel backupLabel;
    
    public IngressMplsBackupIntent(ApplicationId appId, TrafficSelector selector,
                      TrafficTreatment treatment,
                      ConnectPoint ingressPoint,
                      Optional<MplsLabel> ingressLabel,
                      ConnectPoint egressPoint,
                      Optional<MplsLabel> egressLabel,
                      MplsLabel backupLabel) {
        this(appId, selector, treatment, ingressPoint, ingressLabel, egressPoint, egressLabel, backupLabel, ImmutableList.of(new LinkTypeConstraint(false, Link.Type.OPTICAL)), DEFAULT_INTENT_PRIORITY);
    }
    
    public IngressMplsBackupIntent(ApplicationId appId, TrafficSelector selector,
                      TrafficTreatment treatment,
                      ConnectPoint ingressPoint,
                      Optional<MplsLabel> ingressLabel,
                      ConnectPoint egressPoint,
                      Optional<MplsLabel> egressLabel,
                      MplsLabel backupLabel,
                      List<Constraint> constraints, 
                      int priority) {
        super(appId, selector, treatment, ingressPoint, ingressLabel, egressPoint, egressLabel, constraints, priority);
        checkNotNull(backupLabel);
        this.backupLabel = backupLabel;
    }

    public MplsLabel backupLabel() {
        return backupLabel;
    }


}
