package org.onosproject.net.intent;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.onlab.packet.MplsLabel;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.MplsPathIntent.Builder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

public class IngressMplsBackupIntent extends ConnectivityIntent {
    
    private final ConnectPoint ingressPoint;
    private final Optional<MplsLabel> ingressLabel;
    private final ConnectPoint egressPoint;
    private final MplsLabel previousEgressLabel;
    private final MplsLabel backupLabel;
   
    
    private IngressMplsBackupIntent(ApplicationId appId, Key key, 
                                   TrafficSelector selector,
                      TrafficTreatment treatment,
                      ConnectPoint ingressPoint,
                      Optional<MplsLabel> ingressLabel,
                      ConnectPoint egressPoint,
                      MplsLabel previousEgressLabel,
                      MplsLabel backupLabel,
                      List<Constraint> constraints, 
                      int priority) {
        super(appId, key, Collections.emptyList(), selector, treatment, constraints, priority); 
        
        this.ingressPoint = checkNotNull(ingressPoint);
        this.ingressLabel = checkNotNull(ingressLabel);
        this.egressPoint = checkNotNull(egressPoint);
        this.previousEgressLabel = checkNotNull(previousEgressLabel);
        this.backupLabel = checkNotNull(backupLabel);
    }

    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder of an MPLS intent.
     */
    public static final class Builder extends ConnectivityIntent.Builder {
        ConnectPoint ingressPoint = null;
        ConnectPoint egressPoint = null;
        Optional<MplsLabel> ingressLabel = Optional.empty();
        MplsLabel previousEgressLabel = null;
        MplsLabel backupLabel = null;

        private Builder() {
            // Hide constructor
        }

        @Override
        public Builder appId(ApplicationId appId) {
            return (Builder) super.appId(appId);
        }

        @Override
        public Builder key(Key key) {
            return (Builder) super.key(key);
        }

        @Override
        public Builder selector(TrafficSelector selector) {
            return (Builder) super.selector(selector);
        }

        @Override
        public Builder treatment(TrafficTreatment treatment) {
            return (Builder) super.treatment(treatment);
        }

        @Override
        public Builder constraints(List<Constraint> constraints) {
            return (Builder) super.constraints(constraints);
        }

        @Override
        public Builder priority(int priority) {
            return (Builder) super.priority(priority);
        }

        /**
         * Sets the ingress point of the point to point intent that will be built.
         *
         * @param ingressPoint ingress connect point
         * @return this builder
         */
        public Builder ingressPoint(ConnectPoint ingressPoint) {
            this.ingressPoint = ingressPoint;
            return this;
        }

        /**
         * Sets the egress point of the point to point intent that will be built.
         *
         * @param egressPoint egress connect point
         * @return this builder
         */
        public Builder egressPoint(ConnectPoint egressPoint) {
            this.egressPoint = egressPoint;
            return this;
        }

        /**
         * Sets the ingress label of the intent that will be built.
         *
         * @param ingressLabel ingress label
         * @return this builder
         */
        public Builder ingressLabel(Optional<MplsLabel> ingressLabel) {
            this.ingressLabel = ingressLabel;
            return this;
        }

        /**
         * Sets the previous used egress label of the intent that will be built.
         *
         * @param previousEgressLabel previous egress label
         * @return this builder
         */
        public Builder egressLabel(MplsLabel previousEgressLabel) {
            this.previousEgressLabel = previousEgressLabel;
            return this;
        }
        
        /**
         * Sets the backup label of the intent that will be built.
         *
         * @param backupLabel backup label
         * @return this builder
         */
        public Builder backupLabel(MplsLabel backupLabel) {
            this.backupLabel = backupLabel;
            return this;
        }

        /**
         * Builds a point to point intent from the accumulated parameters.
         *
         * @return point to point intent
         */
        public IngressMplsBackupIntent build() {

            return new IngressMplsBackupIntent(
                    appId,
                    key,
                    selector,
                    treatment,
                    ingressPoint,
                    ingressLabel,
                    egressPoint,
                    previousEgressLabel,
                    backupLabel,
                    constraints,
                    priority
            );
        }
    }



    /**
     * Constructor for serializer.
     */
    protected IngressMplsBackupIntent() {
        super();
        this.ingressPoint = null;
        this.ingressLabel = null;
        this.egressPoint = null;
        this.previousEgressLabel = null;
        this.backupLabel = null;
    }

    /**
     * Returns the port on which the ingress traffic should be connected to
     * the egress.
     *
     * @return ingress switch port
     */
    public ConnectPoint ingressPoint() {
        return ingressPoint;
    }

    /**
     * Returns the port on which the traffic should egress.
     *
     * @return egress switch port
     */
    public ConnectPoint egressPoint() {
        return egressPoint;
    }


    /**
     * Returns the MPLS label which the ingress traffic should tagged.
     *
     * @return ingress MPLS label
     */
    public Optional<MplsLabel> ingressLabel() {
        return ingressLabel;
    }

    /**
     * Returns the MPLS label which the egress traffic was previously tagged.
     *
     * @return egress MPLS label
     */
    public MplsLabel previousEgressLabel() {
        return previousEgressLabel;
    }

    /**
     * Returns the MPLS label which should tag the backup traffic.
     *
     * @return egress MPLS label
     */
    public MplsLabel backupLabel() {
        return backupLabel;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("id", id())
                .add("appId", appId())
                .add("key", key())
                .add("priority", priority())
                .add("selector", selector())
                .add("treatment", treatment())
                .add("ingressPoint", ingressPoint)
                .add("ingressLabel", ingressLabel)
                .add("egressPoint", egressPoint)
                .add("previousEgressLabel", previousEgressLabel)
                .add("backupLabel", backupLabel)
                .add("constraints", constraints())
                .toString();
    }


}
