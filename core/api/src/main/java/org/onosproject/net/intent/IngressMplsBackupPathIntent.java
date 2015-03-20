package org.onosproject.net.intent;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.onlab.packet.MplsLabel;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.Path;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;

public class IngressMplsBackupPathIntent extends MplsPathIntent {

    private MplsLabel backupLabel;

    public IngressMplsBackupPathIntent(ApplicationId appId,
                                            TrafficSelector selector,
                                            TrafficTreatment treatment,
                                            Path path,
                                            Optional<MplsLabel> ingressLabel,
                                            Optional<MplsLabel> egressLabel,
                                            MplsLabel backupLabel) {
        this(appId, selector, treatment, path, ingressLabel, egressLabel,
             backupLabel, Collections.emptyList(), DEFAULT_INTENT_PRIORITY);
    }

    public IngressMplsBackupPathIntent(ApplicationId appId,
                                            TrafficSelector selector,
                                            TrafficTreatment treatment,
                                            Path path,
                                            Optional<MplsLabel> ingressLabel,
                                            Optional<MplsLabel> egressLabel,
                                            MplsLabel backupLabel,
                                            List<Constraint> constraints,
                                            int priority) {
        super(appId, selector, treatment, path, ingressLabel, egressLabel,
              constraints, priority);
        checkNotNull(backupLabel);
        this.backupLabel = backupLabel;
    }

    public MplsLabel backupLabel() {
        return backupLabel;
    }

}
