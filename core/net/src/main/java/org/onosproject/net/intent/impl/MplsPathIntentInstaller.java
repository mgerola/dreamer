package org.onosproject.net.intent.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collections;
import java.util.Iterator;
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
import org.onosproject.core.DefaultGroupId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleBatchEntry;
import org.onosproject.net.flow.FlowRuleBatchOperation;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.FlowRuleBatchEntry.FlowRuleOperation;
import org.onosproject.net.flow.criteria.Criteria.EthTypeCriterion;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.Criterion.Type;
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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Installer for {@link MplsPathIntent packet path connectivity intents}.
 */
@Component(immediate = true)
public class MplsPathIntentInstaller implements IntentInstaller<MplsPathIntent> {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentExtensionService intentManager;

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
        intentManager.registerInstaller(MplsPathIntent.class, this);
    }

    @Deactivate
    public void deactivate() {
        intentManager.unregisterInstaller(MplsPathIntent.class);
    }

    @Override
    public List<FlowRuleBatchOperation> install(MplsPathIntent intent) {
        LinkResourceAllocations allocations = assignMplsLabel(intent);
        return generateRules(intent, allocations, FlowRuleOperation.ADD);

    }

    @Override
    public List<FlowRuleBatchOperation> uninstall(MplsPathIntent intent) {
        LinkResourceAllocations allocations = resourceService
                .getAllocations(intent.id());
        resourceService.releaseResources(allocations);

        List<FlowRuleBatchOperation> rules = generateRules(intent,
                                                           allocations,
                                                           FlowRuleOperation.REMOVE);
        return rules;
    }

    @Override
    public List<FlowRuleBatchOperation> replace(MplsPathIntent oldIntent,
                                                MplsPathIntent newIntent) {

        List<FlowRuleBatchOperation> batches = Lists.newArrayList();
        batches.addAll(uninstall(oldIntent));
        batches.addAll(install(newIntent));
        return batches;
    }

    private LinkResourceAllocations assignMplsLabel(MplsPathIntent intent) {

        // TODO: do it better... Suggestions?
        Set<Link> linkRequest;
        log.info("Path: {} - ingress label: {} - egress label: {}", intent.path().links(), intent.ingressLabel(), intent.egressLabel());
        if(intent.path().links().size() > 2) {
        linkRequest = Sets.newHashSetWithExpectedSize(intent.path()
                .links().size() - 2);
        for (int i = 1; i <= intent.path().links().size() - 2; i++) {
            Link link = intent.path().links().get(i);
            linkRequest.add(link);
            // add the inverse link. I want that the label is reserved both for
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

    private List<FlowRuleBatchOperation> generateRules(MplsPathIntent intent,
                                                       LinkResourceAllocations allocations,
                                                       FlowRuleOperation operation) {

        Iterator<Link> links = intent.path().links().iterator();
        Link srcLink = links.next();
        ConnectPoint prev = srcLink.dst();

        Link link = links.next();
        // List of flow rules to be installed
        List<FlowRuleBatchEntry> rules = Lists.newLinkedList();
        
        //Path in the same switch
        if (intent.path().links().size() == 2) {
            rules.add(singleFlow(prev.port(), link, intent, operation));
            return Lists.newArrayList(new FlowRuleBatchOperation(rules));
            
        } else {
            MplsLabel mpls;
            // Ingress traffic
            // Get the new MPLS label
            mpls = getMplsLabel(allocations, link);
            checkNotNull(mpls);
            MplsLabel prevLabel = mpls;

            rules.add(ingressFlow(prev.port(), link, intent, mpls, operation));

            prev = link.dst();

            while (links.hasNext()) {

                link = links.next();

                if (links.hasNext()) {
                    // Transit traffic
                    // Get the new MPLS label
                    mpls = getMplsLabel(allocations, link);
                    checkNotNull(mpls);
                    rules.add(transitFlow(prev.port(), link, intent, prevLabel,
                                          mpls, operation));
                    prevLabel = mpls;

                } else {
                    // Egress traffic
                    rules.add(egressFlow(prev.port(), link, intent, prevLabel,
                                         operation));
                }

                prev = link.dst();
            }
            return Lists.newArrayList(new FlowRuleBatchOperation(rules));
        }
    }

    private FlowRuleBatchEntry singleFlow(PortNumber inPort, Link link,
                                          MplsPathIntent intent, FlowRuleOperation operation){
        TrafficSelector.Builder ingressSelector = DefaultTrafficSelector
                .builder(intent.selector());
        TrafficTreatment.Builder treat = DefaultTrafficTreatment.builder(intent.treatment());
        
        
        if (intent.ingressLabel().isPresent()) {
            ingressSelector.matchEthType(Ethernet.MPLS_UNICAST)
                    .matchMplsLabel(intent.ingressLabel().get());
            
            // Swap the MPLS label
            if(intent.egressLabel().isPresent()){
                treat.setMpls(intent.egressLabel().get());
            } else{
                // if the ingress ethertype is defined, the egress traffic
                // will be use that value, otherwise the IPv4 ethertype is used.
                treat.popMpls(outputEtherType(intent.selector()));
            }

        } else {
            // Push and set the MPLS label
            if(intent.egressLabel().isPresent()){
                treat.setMpls(intent.egressLabel().get());
            } 
            // if the flow has not MPLS label and we do not need to add a label (egress is null), we simply forward the traffic
        }
        treat.setOutput(link.src().port());
        return flowRuleBatchEntry(intent, link.src().deviceId(),
                                  ingressSelector.build(), treat.build(),
                                  operation);
    }
    private FlowRuleBatchEntry ingressFlow(PortNumber inPort, Link link,
                                           MplsPathIntent intent,
                                           MplsLabel label,
                                           FlowRuleOperation operation) {

        TrafficSelector.Builder ingressSelector = DefaultTrafficSelector
                .builder(intent.selector());
        TrafficTreatment.Builder treat = DefaultTrafficTreatment.builder();
        ingressSelector.matchInport(inPort);

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
        log.info("Installing ingress flow to switch {} with MPLS label source {} and destination {}"
                 , link.src().deviceId(), intent.ingressLabel().get(), label.toString());
        }else{
            log.info("Installing ingress flow to switch {} with MPLS label source {} and destination {}"
                     , link.src().deviceId(), "no MPLS label" , label.toString());
        }
        return flowRuleBatchEntry(intent, link.src().deviceId(),
                                  ingressSelector.build(), treat.build(),
                                  operation);
    }

    private FlowRuleBatchEntry transitFlow(PortNumber inPort, Link link,
                                           MplsPathIntent intent,
                                           MplsLabel prevLabel,
                                           MplsLabel outLabel,
                                           FlowRuleOperation operation) {

        // Ignore the ingress Traffic Selector and use only the MPLS label
        // assigned in the previous link
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchInport(inPort).matchEthType(Ethernet.MPLS_UNICAST)
                .matchMplsLabel(prevLabel.label());
        TrafficTreatment.Builder treat = DefaultTrafficTreatment.builder();

        // Set the new label only if the label on the packet is
        // different
        if (prevLabel.equals(outLabel)) {
            treat.setMpls(outLabel.label());
        }

        treat.setOutput(link.src().port());
        log.info("Installing transient flow to switch {} with MPLS label source {} and destination {}"
                 , link.src().deviceId(), prevLabel.toString(), outLabel.toString());
        return flowRuleBatchEntry(intent, link.src().deviceId(),
                                  selector.build(), treat.build(), operation);
    }

    private FlowRuleBatchEntry egressFlow(PortNumber inPort, Link link,
                                          MplsPathIntent intent,
                                          MplsLabel prevLabel,
                                          FlowRuleOperation operation) {
        // egress point: either set the egress MPLS label or pop the
        // MPLS label based on the intent annotations

        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchInport(inPort).matchEthType(Ethernet.MPLS_UNICAST)
                .matchMplsLabel(prevLabel.label());

        // apply the intent's treatments
        TrafficTreatment.Builder treat = DefaultTrafficTreatment.builder(intent
                .treatment());

        if (intent.egressLabel().isPresent()) {
            treat.setMpls(intent.egressLabel().get());
        } else {
            // if the ingress ethertype is defined, the egress traffic
            // will be use that value, otherwise the IPv4 ethertype is used.
            treat.popMpls(outputEtherType(intent.selector()));
        }
        treat.setOutput(link.src().port());
        if(intent.egressLabel().isPresent()){
        log.info("Installing egress flow to switch {} with MPLS label source {} and destination {}"
                 , link.src().deviceId(), prevLabel.toString(), intent.egressLabel().get());
        }else{
            log.info("Installing egress flow to switch {} with MPLS label source {} and destination {}"
                     , link.src().deviceId(), prevLabel.toString(), "pop MPLS label");
        }
        return flowRuleBatchEntry(intent, link.src().deviceId(),
                                  selector.build(), treat.build(), operation);
    }
    
    private Short outputEtherType(TrafficSelector selector){
        Criterion c = selector.getCriterion(Type.ETH_TYPE);
        if (c != null && c instanceof EthTypeCriterion) {
            EthTypeCriterion ethertype = (EthTypeCriterion) c;
            return ethertype.ethType();
        } else {
        return Ethernet.TYPE_IPV4;
        }
    } 

    protected FlowRuleBatchEntry flowRuleBatchEntry(MplsPathIntent intent,
                                                    DeviceId deviceId,
                                                    TrafficSelector selector,
                                                    TrafficTreatment treat,
                                                    FlowRuleOperation operation) {
        FlowRule rule = new DefaultFlowRule(
                                            deviceId,
                                            selector,
                                            treat,
                                            123, // FIXME 123
                                            appId,
                                            new DefaultGroupId(
                                                               (short) (intent
                                                                       .id()
                                                                       .fingerprint() & 0xffff)),
                                            0, true);
        return new FlowRuleBatchEntry(operation, rule, intent.id()
                .fingerprint());

    }
}
