package org.onosproject.net.intent;

import java.util.List;
import java.util.Optional;

import org.onlab.packet.MplsLabel;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.Path;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Abstraction of explicit MPLS label-switched path.
 */

public class MplsPathIntent extends PathIntent {

    private final Optional<MplsLabel> ingressLabel;
    private final Optional<MplsLabel> egressLabel;

    /**
     * Creates a new point-to-point intent with the supplied ingress/egress
     * ports and using the specified explicit path.
     *
     * @param appId application identifier
     * @param selector traffic selector
     * @param treatment treatment
     * @param path traversed links
     * @param ingressLabel MPLS egress label
     * @param egressLabel MPLS ingress label
     * @param constraints optional list of constraints
     * @param priority    priority to use for flows generated by this intent
     * @throws NullPointerException {@code path} is null
     */
    private MplsPathIntent(ApplicationId appId, TrafficSelector selector,
            TrafficTreatment treatment, Path path, Optional<MplsLabel> ingressLabel,
            Optional<MplsLabel> egressLabel, List<Constraint> constraints,
            int priority) {
        super(appId, selector, treatment, path, constraints,
              priority);

        this.ingressLabel = checkNotNull(ingressLabel);
        this.egressLabel = checkNotNull(egressLabel);
    }

    /**
     * Returns a new host to host intent builder.
     *
     * @return host to host intent builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder of a host to host intent.
     */
    public static final class Builder extends PathIntent.Builder {
        private Optional<MplsLabel> ingressLabel = Optional.empty();
        private Optional<MplsLabel> egressLabel = Optional.empty();

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

        @Override
        public Builder path(Path path) {
            return (Builder) super.path(path);
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
         * Sets the ingress label of the intent that will be built.
         *
         * @param egressLabel ingress label
         * @return this builder
         */
        public Builder egressLabel(Optional<MplsLabel> egressLabel) {
            this.egressLabel = egressLabel;
            return this;
        }


        /**
         * Builds a host to host intent from the accumulated parameters.
         *
         * @return point to point intent
         */
        public MplsPathIntent build() {

            return new MplsPathIntent(
                    appId,
                    selector,
                    treatment,
                    path,
                    ingressLabel,
                    egressLabel,
                    constraints,
                    priority
            );
        }
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
     * Returns the MPLS label which the egress traffic should tagged.
     *
     * @return egress MPLS label
     */
    public Optional<MplsLabel> egressLabel() {
        return egressLabel;
    }

}
