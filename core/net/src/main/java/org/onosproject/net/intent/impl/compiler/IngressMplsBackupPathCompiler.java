package org.onosproject.net.intent.impl.compiler;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleOperation;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.Criteria.EthTypeCriterion;
import org.onosproject.net.flow.criteria.Criterion.Type;
import org.onosproject.net.intent.FlowRuleIntent;
import org.onosproject.net.intent.IngressMplsBackupPathIntent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentCompiler;
import org.onosproject.net.intent.IntentExtensionService;
import org.onosproject.net.intent.IntentInstaller;
import org.onosproject.net.intent.MplsPathIntent;
import org.onosproject.net.link.LinkStore;
import org.onosproject.net.resource.DefaultLinkResourceRequest;
import org.onosproject.net.resource.LinkResourceAllocations;
import org.onosproject.net.resource.LinkResourceRequest;
import org.onosproject.net.resource.LinkResourceService;
import org.onosproject.net.resource.MplsLabel;
import org.onosproject.net.resource.MplsLabelResourceAllocation;
import org.onosproject.net.resource.ResourceAllocation;
import org.onosproject.net.resource.ResourceType;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Component(immediate = true)
public class IngressMplsBackupPathCompiler
        implements IntentCompiler<IngressMplsBackupPathIntent> {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentExtensionService intentExtensionService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkResourceService resourceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkStore linkStore;

    protected ApplicationId appId;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.net.intent");
        intentExtensionService.registerCompiler(IngressMplsBackupPathIntent.class, this);
    }

    @Deactivate
    public void deactivate() {
        intentExtensionService.unregisterCompiler(IngressMplsBackupPathIntent.class);
    }

    @Override
    public List<Intent> compile(IngressMplsBackupPathIntent intent,
                                List<Intent> installable,
                                Set<LinkResourceAllocations> resources) {
        LinkResourceAllocations allocations = assignMplsLabel(intent);
        List<FlowRule> rules = generateRules(intent, allocations);
        return Arrays.asList(new FlowRuleIntent(appId, rules));
    }


    private LinkResourceAllocations assignMplsLabel(IngressMplsBackupPathIntent intent) {

        // TODO: do it better... Suggestions?
        Set<Link> linkRequest;
        log.info("Path: {} - ingress label: {} - egress label: {}", intent
                .path().links(), intent.ingressLabel(), intent.previousEgressLabel());
        if (intent.path().links().size() > 2) {
            linkRequest = Sets.newHashSetWithExpectedSize(intent.path().links()
                    .size() - 2);
            for (int i = 1; i <= intent.path().links().size() - 2; i++) {
                Link link = intent.path().links().get(i);
                linkRequest.add(link);
                // add the inverse link. I want that the label is reserved both
                // for
                // the direct and inverse link
                linkRequest.add(linkStore.getLink(link.dst(), link.src()));
            }
        } else {
            linkRequest = Collections.emptySet();
        }

        LinkResourceRequest.Builder request = DefaultLinkResourceRequest
                .builder(intent.id(), linkRequest).addMplsRequest();
        LinkResourceAllocations reqMpls = resourceService
                .requestResources(request.build());
        return reqMpls;
    }

    private MplsLabel getMplsLabel(LinkResourceAllocations allocations,
                                   Link link) {

        for (ResourceAllocation allocation : allocations
                .getResourceAllocation(link)) {
            if (allocation.type() == ResourceType.MPLS_LABEL) {
                return ((MplsLabelResourceAllocation) allocation).mplsLabel();

            }
        }
        log.warn("MPLS label was not assigned successfully");
        return null;
    }

    private List<FlowRule> generateRules(IngressMplsBackupPathIntent intent,
                                                              LinkResourceAllocations allocations) {

        Iterator<Link> links = intent.path().links().iterator();
        Link srcLink = links.next();
        ConnectPoint prev = srcLink.dst();

        Link link = links.next();
        // List of flow rules to be installed

        List<FlowRule> rules = Lists.newLinkedList();

        // Path in the same switch
        if (intent.path().links().size() == 2) {
            // TODO: change!
            rules.addAll(singleFlow(prev.port(), link, intent));

        } else {
            MplsLabel mpls;
            // Ingress traffic
            // Get the new MPLS label
            mpls = getMplsLabel(allocations, link);
            checkNotNull(mpls);
            MplsLabel prevLabel = mpls;

            rules.add(ingressFlow(prev.port(), link, intent, mpls));

            prev = link.dst();

            while (links.hasNext()) {

                link = links.next();

                if (links.hasNext()) {
                    // Transit traffic
                    // Get the new MPLS label
                    mpls = getMplsLabel(allocations, link);
                    checkNotNull(mpls);
                    rules.add(transitFlow(prev.port(), link, intent, prevLabel,
                                          mpls));
                    prevLabel = mpls;

                } else {
                    // Egress traffic
                    rules.addAll(egressFlow(prev.port(), link, intent,
                                            prevLabel));
                }

                prev = link.dst();
            }
        }
        return rules;
    }

    private List<FlowRule> singleFlow(PortNumber inPort, Link link,
                                         IngressMplsBackupPathIntent intent) {
        TrafficSelector.Builder ingressSelector = DefaultTrafficSelector
                .builder(intent.selector());
        TrafficTreatment.Builder treat = DefaultTrafficTreatment.builder(intent
                .treatment());
        
        List<FlowRule> rules = new LinkedList<FlowRule>();
        
        if (intent.ingressLabel().isPresent()) {
            ingressSelector.matchEthType(Ethernet.MPLS_UNICAST)
                    .matchMplsLabel(intent.ingressLabel().get());

            // Swap the MPLS label
                treat.setMpls(intent.previousEgressLabel());
                treat.transition(FlowRule.Type.MPLS);
                rules.add(createFlowRule(intent, link.src().deviceId(),
                                        ingressSelector.build(), treat.build()));
               

        } else {
            // Push and set the MPLS label
            treat.pushMpls();
            treat.setMpls(intent.previousEgressLabel());
            rules.add(createFlowRule(intent, link.src().deviceId(),
                                     ingressSelector.build(), treat.build()));
        }
        TrafficSelector.Builder selecEgress = DefaultTrafficSelector.builder();
        selecEgress.matchEthType(Ethernet.MPLS_UNICAST)
                                               .matchMplsLabel(intent.previousEgressLabel());
        // apply the intent's treatments
        TrafficTreatment.Builder treatEgress = DefaultTrafficTreatment
                .builder(intent.treatment());
        treatEgress.pushMpls();
        treatEgress.setMpls(intent.backupLabel());
        treatEgress.setOutput(link.src().port());

        rules.add(new DefaultFlowRule(link.src().deviceId(), selecEgress
                .build(), treatEgress.build(), 123, // FIXME 123
                                          appId, 0, true, FlowRule.Type.MPLS));
        return rules;
    }

    private FlowRule ingressFlow(PortNumber inPort, Link link,
                                          IngressMplsBackupPathIntent intent,
                                          MplsLabel label) {

        TrafficSelector.Builder ingressSelector = DefaultTrafficSelector
                .builder(intent.selector());
        TrafficTreatment.Builder treat = DefaultTrafficTreatment.builder();

        ingressSelector.matchInPort(inPort);

        if (intent.ingressLabel().isPresent()) {
            ingressSelector.matchEthType(Ethernet.MPLS_UNICAST)
                    .matchMplsLabel(intent.ingressLabel().get());

            // Swap the MPLS label
            treat.setMpls(label.label());
        } else {
            // Push and set the MPLS label
            treat.pushMpls().setMpls(label.label());
        }
        // Add the output action
        treat.setOutput(link.src().port());

        if (intent.ingressLabel().isPresent()) {
            log.info("Installing ingress flow to switch {} with MPLS label source {} and destination {}",
                     link.src().deviceId(), intent.ingressLabel().get(),
                     label.toString());
        } else {
            log.info("Installing ingress flow to switch {} with MPLS label source {} and destination {}",
                     link.src().deviceId(), "no MPLS label", label.toString());
        }
        return createFlowRule(intent, link.src().deviceId(),
                                 ingressSelector.build(), treat.build());
    }

    private FlowRule transitFlow(PortNumber inPort, Link link,
                                          IngressMplsBackupPathIntent intent,
                                          MplsLabel prevLabel,
                                          MplsLabel outLabel) {

        // Ignore the ingress Traffic Selector and use only the MPLS label
        // assigned in the previous link
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();

        selector.matchInPort(inPort).matchEthType(Ethernet.MPLS_UNICAST)
                .matchMplsLabel(prevLabel.label());
        TrafficTreatment.Builder treat = DefaultTrafficTreatment.builder();

        // Set the new label only if the label on the packet is
        // different
        if (prevLabel.equals(outLabel)) {
            treat.setMpls(outLabel.label());
        }

        treat.setOutput(link.src().port());

        log.info("Installing transient flow to switch {} with MPLS label source {} and destination {}",
                 link.src().deviceId(), prevLabel.toString(),
                 outLabel.toString());
        return createFlowRule(intent, link.src().deviceId(),
                                 selector.build(), treat.build());
    }

    private List<FlowRule> egressFlow(PortNumber inPort,
                                                     Link link,
                                                     IngressMplsBackupPathIntent intent,
                                                     MplsLabel prevLabel) {

        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();

        selector.matchInPort(inPort).matchEthType(Ethernet.MPLS_UNICAST)
                .matchMplsLabel(prevLabel.label());

        TrafficTreatment.Builder treat = DefaultTrafficTreatment.builder();

            treat.setMpls(intent.previousEgressLabel());

        // Match on the previous label is done on the table 2.
        treat.transition(FlowRule.Type.MPLS);

        List<FlowRule> flowRules = new LinkedList<FlowRule>();

        flowRules.add(createFlowRule(intent, link.src().deviceId(),
                                                 selector.build(),
                                                 treat.build()));

        TrafficSelector.Builder selecEgress = DefaultTrafficSelector.builder();
        selecEgress.matchEthType(Ethernet.MPLS_UNICAST)
                                               .matchMplsLabel(intent.previousEgressLabel());
        // apply the intent's treatments
        TrafficTreatment.Builder treatEgress = DefaultTrafficTreatment
                .builder(intent.treatment());
        treatEgress.pushMpls();
        treatEgress.setMpls(intent.backupLabel());
        treatEgress.setOutput(link.src().port());

        flowRules.add(new DefaultFlowRule(link.src().deviceId(), selecEgress
                .build(), treatEgress.build(), 123, // FIXME 123
                                          appId, 0, true, FlowRule.Type.MPLS));

        return flowRules;
    }

//    private short outputEtherType(TrafficSelector selector) {
//        Criterion c = selector.getCriterion(Type.ETH_TYPE);
//        if (c != null && c instanceof EthTypeCriterion) {
//            EthTypeCriterion ethertype = (EthTypeCriterion) c;
//            return (short) ethertype.ethType();
//        } else {
//            return Ethernet.TYPE_IPV4;
//        }
//    }

    protected FlowRule createFlowRule(IngressMplsBackupPathIntent intent, DeviceId deviceId,
                                      TrafficSelector selector, TrafficTreatment treat) {
        return new DefaultFlowRule(deviceId, selector, treat, intent.priority(), appId, 0, true);
    }

}
