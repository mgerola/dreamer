/*
 * Copyright 2015 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.bgprouter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onlab.util.KryoNamespace;
import org.onosproject.config.NetworkConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRule.Type;
import org.onosproject.net.flow.FlowRuleOperations;
import org.onosproject.net.flow.FlowRuleOperationsContext;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.group.DefaultGroupBucket;
import org.onosproject.net.group.DefaultGroupDescription;
import org.onosproject.net.group.DefaultGroupKey;
import org.onosproject.net.group.Group;
import org.onosproject.net.group.GroupBucket;
import org.onosproject.net.group.GroupBuckets;
import org.onosproject.net.group.GroupDescription;
import org.onosproject.net.group.GroupEvent;
import org.onosproject.net.group.GroupListener;
import org.onosproject.net.group.GroupService;
import org.onosproject.net.host.InterfaceIpAddress;
import org.onosproject.net.packet.PacketService;
import org.onosproject.routing.FibEntry;
import org.onosproject.routing.FibListener;
import org.onosproject.routing.FibUpdate;
import org.onosproject.routing.RoutingService;
import org.onosproject.routing.config.BgpSpeaker;
import org.onosproject.routing.config.Interface;
import org.onosproject.routing.config.RoutingConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

/**
 * BgpRouter component.
 */
@Component(immediate = true)
public class BgpRouter {

    private static final Logger log = LoggerFactory.getLogger(BgpRouter.class);

    private static final String BGP_ROUTER_APP = "org.onosproject.bgprouter";

    private static final int PRIORITY_OFFSET = 100;
    private static final int PRIORITY_MULTIPLIER = 5;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected GroupService groupService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected RoutingService routingService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected RoutingConfigurationService configService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    //
    // NOTE: Unused reference - needed to guarantee that the
    // NetworkConfigReader component is activated and the network configuration
    // is read.
    //
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigService networkConfigService;

    private ApplicationId appId;

    // Reference count for how many times a next hop is used by a route
    private final Multiset<IpAddress> nextHopsCount = ConcurrentHashMultiset.create();

    // Mapping from prefix to its current next hop
    private final Map<IpPrefix, IpAddress> prefixToNextHop = Maps.newHashMap();

    // Mapping from next hop IP to next hop object containing group info
    private final Map<IpAddress, NextHop> nextHops = Maps.newHashMap();

    // Stores FIB updates that are waiting for groups to be set up
    private final Multimap<NextHopGroupKey, FibEntry> pendingUpdates = HashMultimap.create();

    // Device id of data-plane switch - should be learned from config
    private DeviceId deviceId;

    // Device id of control-plane switch (OVS) connected to BGP Speaker - should be
    // learned from config
    private DeviceId ctrlDeviceId;

    private final GroupListener groupListener = new InternalGroupListener();

    private TunnellingConnectivityManager connectivityManager;

    private IcmpHandler icmpHandler;

    private InternalTableHandler provisionStaticTables = new InternalTableHandler();

    private KryoNamespace.Builder appKryo = new KryoNamespace.Builder()
                    .register(IpAddress.Version.class)
                    .register(IpAddress.class)
                    .register(NextHopGroupKey.class);

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(BGP_ROUTER_APP);
        getDeviceConfiguration(configService.getBgpSpeakers());

        groupService.addListener(groupListener);

        provisionStaticTables.provision(true, configService.getInterfaces());

        connectivityManager = new TunnellingConnectivityManager(appId,
                                                                configService,
                                                                packetService,
                                                                flowService);

        icmpHandler = new IcmpHandler(configService, packetService);

        routingService.start(new InternalFibListener());

        connectivityManager.start();

        icmpHandler.start();

        log.info("BgpRouter started");
    }

    @Deactivate
    protected void deactivate() {
        routingService.stop();
        connectivityManager.stop();
        icmpHandler.stop();
        provisionStaticTables.provision(false, configService.getInterfaces());

        groupService.removeListener(groupListener);

        log.info("BgpRouter stopped");
    }

    private void getDeviceConfiguration(Map<String, BgpSpeaker> bgps) {
        if (bgps == null || bgps.values().isEmpty()) {
            log.error("BGP speakers configuration is missing");
            return;
        }
        for (BgpSpeaker s : bgps.values()) {
            ctrlDeviceId = s.connectPoint().deviceId();
            if (s.interfaceAddresses() == null || s.interfaceAddresses().isEmpty()) {
                log.error("BGP Router must have interfaces configured");
                return;
            }
            deviceId = s.interfaceAddresses().get(0).connectPoint().deviceId();
            break;
        }

        log.info("Router dpid: {}", deviceId);
        log.info("Control Plane OVS dpid: {}", ctrlDeviceId);
    }

    private void updateFibEntry(Collection<FibUpdate> updates) {
        Map<FibEntry, Group> toInstall = new HashMap<>(updates.size());

        for (FibUpdate update : updates) {
            FibEntry entry = update.entry();

            addNextHop(entry);

            Group group;
            synchronized (pendingUpdates) {
                NextHop nextHop = nextHops.get(entry.nextHopIp());
                group = groupService.getGroup(deviceId,
                                              new DefaultGroupKey(
                                              appKryo.build().serialize(nextHop.group())));

                if (group == null) {
                    log.debug("Adding pending flow {}", update.entry());
                    pendingUpdates.put(nextHop.group(), update.entry());
                    continue;
                }
            }

            toInstall.put(update.entry(), group);
        }

        installFlows(toInstall);
    }

    private void installFlows(Map<FibEntry, Group> entriesToInstall) {
        FlowRuleOperations.Builder builder = FlowRuleOperations.builder();

        for (Map.Entry<FibEntry, Group> entry : entriesToInstall.entrySet()) {
            FibEntry fibEntry = entry.getKey();
            Group group = entry.getValue();

            FlowRule flowRule = generateRibFlowRule(fibEntry.prefix(), group);

            builder.add(flowRule);
        }

        flowService.apply(builder.build());
    }

    private synchronized void deleteFibEntry(Collection<FibUpdate> withdraws) {
        FlowRuleOperations.Builder builder = FlowRuleOperations.builder();

        for (FibUpdate update : withdraws) {
            FibEntry entry = update.entry();

            Group group = deleteNextHop(entry.prefix());
            if (group == null) {
                log.warn("Group not found when deleting {}", entry);
                return;
            }

            FlowRule flowRule = generateRibFlowRule(entry.prefix(), group);

            builder.remove(flowRule);
        }

        flowService.apply(builder.build());
    }

    private FlowRule generateRibFlowRule(IpPrefix prefix, Group group) {
        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4)
                .matchIPDst(prefix)
                .build();

        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .group(group.id())
                .build();


        int priority = prefix.prefixLength() * PRIORITY_MULTIPLIER + PRIORITY_OFFSET;

        return new DefaultFlowRule(deviceId, selector, treatment,
                                   priority, appId, 0, true,
                                   FlowRule.Type.IP);
    }

    private synchronized void addNextHop(FibEntry entry) {
        prefixToNextHop.put(entry.prefix(), entry.nextHopIp());
        if (nextHopsCount.count(entry.nextHopIp()) == 0) {
            // There was no next hop in the multiset

            Interface egressIntf = configService.getMatchingInterface(entry.nextHopIp());
            if (egressIntf == null) {
                log.warn("no egress interface found for {}", entry);
                return;
            }

            NextHopGroupKey groupKey = new NextHopGroupKey(entry.nextHopIp());

            NextHop nextHop = new NextHop(entry.nextHopIp(), entry.nextHopMac(), groupKey);

            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                    .setEthSrc(egressIntf.mac())
                    .setEthDst(nextHop.mac())
                    .pushVlan()
                    .setVlanId(egressIntf.vlan())
                    .setVlanPcp((byte) 0)
                    .setOutput(egressIntf.connectPoint().port())
                    .build();

            GroupBucket bucket = DefaultGroupBucket.createIndirectGroupBucket(treatment);

            GroupDescription groupDescription
                    = new DefaultGroupDescription(deviceId,
                                                  GroupDescription.Type.INDIRECT,
                                                  new GroupBuckets(Collections
                                                                           .singletonList(bucket)),
                                                  new DefaultGroupKey(appKryo.build().serialize(groupKey)),
                                                  appId);

            groupService.addGroup(groupDescription);

            nextHops.put(nextHop.ip(), nextHop);

        }

        nextHopsCount.add(entry.nextHopIp());
    }

    private synchronized Group deleteNextHop(IpPrefix prefix) {
        IpAddress nextHopIp = prefixToNextHop.remove(prefix);
        NextHop nextHop = nextHops.get(nextHopIp);
        if (nextHop == null) {
            log.warn("No next hop found when removing prefix {}", prefix);
            return null;
        }

        Group group = groupService.getGroup(deviceId,
                                            new DefaultGroupKey(appKryo.
                                                                build().
                                                                serialize(nextHop.group())));

        // FIXME disabling group deletes for now until we verify the logic is OK
        /*if (nextHopsCount.remove(nextHopIp, 1) <= 1) {
            // There was one or less next hops, so there are now none

            log.debug("removing group for next hop {}", nextHop);

            nextHops.remove(nextHopIp);

            groupService.removeGroup(deviceId,
                                     new DefaultGroupKey(appKryo.build().serialize(nextHop.group())),
                                     appId);
        }*/

        return group;
    }

    private class InternalFibListener implements FibListener {

        @Override
        public void update(Collection<FibUpdate> updates,
                           Collection<FibUpdate> withdraws) {
            BgpRouter.this.deleteFibEntry(withdraws);
            BgpRouter.this.updateFibEntry(updates);
        }
    }

    private class InternalTableHandler {

        private static final int CONTROLLER_PRIORITY = 255;
        private static final int DROP_PRIORITY = 0;
        private static final int HIGHEST_PRIORITY = 0xffff;
        private Set<InterfaceIpAddress> intfIps = new HashSet<InterfaceIpAddress>();
        private Set<MacAddress> intfMacs = new HashSet<MacAddress>();
        private Map<PortNumber, VlanId> portVlanPair = Maps.newHashMap();

        public void provision(boolean install, Set<Interface> intfs) {
            getInterfaceConfig(intfs);
            processTableZero(install);
            processTableOne(install);
            processTableTwo(install);
            processTableFour(install);
            processTableFive(install);
            processTableSix(install);
            processTableNine(install);
        }

        private void getInterfaceConfig(Set<Interface> intfs) {
            log.info("Processing {} router interfaces", intfs.size());
            for (Interface intf : intfs) {
                intfIps.addAll(intf.ipAddresses());
                intfMacs.add(intf.mac());
                portVlanPair.put(intf.connectPoint().port(), intf.vlan());
            }
        }

        private void processTableZero(boolean install) {
            TrafficSelector.Builder selector;
            TrafficTreatment.Builder treatment;

            // Bcast rule
            selector = DefaultTrafficSelector.builder();
            treatment = DefaultTrafficTreatment.builder();

            selector.matchEthDst(MacAddress.BROADCAST);
            treatment.transition(FlowRule.Type.VLAN_MPLS);

            FlowRule rule = new DefaultFlowRule(deviceId, selector.build(),
                                                treatment.build(),
                                                CONTROLLER_PRIORITY, appId, 0,
                                                true, FlowRule.Type.FIRST);

            FlowRuleOperations.Builder ops = FlowRuleOperations.builder();

            ops = install ? ops.add(rule) : ops.remove(rule);

            // Interface MACs
            for (MacAddress mac : intfMacs) {
                log.debug("adding rule for MAC: {}", mac);
                selector = DefaultTrafficSelector.builder();
                treatment = DefaultTrafficTreatment.builder();

                selector.matchEthDst(mac);
                treatment.transition(FlowRule.Type.VLAN_MPLS);

                rule = new DefaultFlowRule(deviceId, selector.build(),
                                           treatment.build(),
                                           CONTROLLER_PRIORITY, appId, 0,
                                           true, FlowRule.Type.FIRST);

                ops = install ? ops.add(rule) : ops.remove(rule);
            }

            //Drop rule
            selector = DefaultTrafficSelector.builder();
            treatment = DefaultTrafficTreatment.builder();

            treatment.drop();

            rule = new DefaultFlowRule(deviceId, selector.build(),
                                       treatment.build(), DROP_PRIORITY, appId,
                                       0, true, FlowRule.Type.FIRST);

            ops = install ? ops.add(rule) : ops.remove(rule);

            flowService.apply(ops.build(new FlowRuleOperationsContext() {
                @Override
                public void onSuccess(FlowRuleOperations ops) {
                    log.info("Provisioned default table for bgp router");
                }

                @Override
                public void onError(FlowRuleOperations ops) {
                    log.info("Failed to provision default table for bgp router");
                }
            }));

        }

        private void processTableOne(boolean install) {
            TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
            TrafficTreatment.Builder treatment = DefaultTrafficTreatment
                    .builder();
            FlowRuleOperations.Builder ops = FlowRuleOperations.builder();
            FlowRule rule;

            selector.matchVlanId(VlanId.ANY);
            treatment.transition(FlowRule.Type.VLAN);

            rule = new DefaultFlowRule(deviceId, selector.build(),
                                       treatment.build(), CONTROLLER_PRIORITY,
                                       appId, 0, true, FlowRule.Type.VLAN_MPLS);

            ops = install ? ops.add(rule) : ops.remove(rule);

            flowService.apply(ops.build(new FlowRuleOperationsContext() {
                @Override
                public void onSuccess(FlowRuleOperations ops) {
                    log.info("Provisioned vlan/mpls table for bgp router");
                }

                @Override
                public void onError(FlowRuleOperations ops) {
                    log.info(
                            "Failed to provision vlan/mpls table for bgp router");
                }
            }));

        }

        private void processTableTwo(boolean install) {
            TrafficSelector.Builder selector;
            TrafficTreatment.Builder treatment;
            FlowRuleOperations.Builder ops = FlowRuleOperations.builder();
            FlowRule rule;

            //Interface Vlans
            for (Map.Entry<PortNumber, VlanId> portVlan : portVlanPair.entrySet()) {
                log.debug("adding rule for VLAN: {}", portVlan);
                selector = DefaultTrafficSelector.builder();
                treatment = DefaultTrafficTreatment.builder();

                selector.matchVlanId(portVlan.getValue());
                selector.matchInPort(portVlan.getKey());
                treatment.transition(Type.ETHER);
                treatment.deferred().popVlan();

                rule = new DefaultFlowRule(deviceId, selector.build(),
                                           treatment.build(), CONTROLLER_PRIORITY, appId,
                                           0, true, FlowRule.Type.VLAN);

                ops = install ? ops.add(rule) : ops.remove(rule);
            }

            //Drop rule
            selector = DefaultTrafficSelector.builder();
            treatment = DefaultTrafficTreatment.builder();

            treatment.drop();

            rule = new DefaultFlowRule(deviceId, selector.build(),
                                       treatment.build(), DROP_PRIORITY, appId,
                                       0, true, FlowRule.Type.VLAN);

            ops = install ? ops.add(rule) : ops.remove(rule);

            flowService.apply(ops.build(new FlowRuleOperationsContext() {
                @Override
                public void onSuccess(FlowRuleOperations ops) {
                    log.info("Provisioned vlan table for bgp router");
                }

                @Override
                public void onError(FlowRuleOperations ops) {
                    log.info("Failed to provision vlan table for bgp router");
                }
            }));
        }

        private void processTableFour(boolean install) {
            TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
            TrafficTreatment.Builder treatment = DefaultTrafficTreatment
                    .builder();
            FlowRuleOperations.Builder ops = FlowRuleOperations.builder();
            FlowRule rule;

            selector.matchEthType(Ethernet.TYPE_ARP);
            treatment.punt();

            rule = new DefaultFlowRule(deviceId, selector.build(),
                                       treatment.build(), CONTROLLER_PRIORITY,
                                       appId, 0, true, FlowRule.Type.ETHER);

            ops = install ? ops.add(rule) : ops.remove(rule);

            selector = DefaultTrafficSelector.builder();
            treatment = DefaultTrafficTreatment.builder();

            selector.matchEthType(Ethernet.TYPE_IPV4);
            treatment.transition(FlowRule.Type.COS);

            rule = new DefaultFlowRule(deviceId, selector.build(),
                                       treatment.build(), CONTROLLER_PRIORITY,
                                       appId, 0, true, FlowRule.Type.ETHER);

            ops = install ? ops.add(rule) : ops.remove(rule);

            //Drop rule
            selector = DefaultTrafficSelector.builder();
            treatment = DefaultTrafficTreatment.builder();

            treatment.drop();

            rule = new DefaultFlowRule(deviceId, selector.build(),
                                       treatment.build(), DROP_PRIORITY, appId,
                                       0, true, FlowRule.Type.ETHER);

            ops = install ? ops.add(rule) : ops.remove(rule);

            flowService.apply(ops.build(new FlowRuleOperationsContext() {
                @Override
                public void onSuccess(FlowRuleOperations ops) {
                    log.info("Provisioned ether table for bgp router");
                }

                @Override
                public void onError(FlowRuleOperations ops) {
                    log.info("Failed to provision ether table for bgp router");
                }
            }));

        }

        private void processTableFive(boolean install) {
            TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
            TrafficTreatment.Builder treatment = DefaultTrafficTreatment
                    .builder();
            FlowRuleOperations.Builder ops = FlowRuleOperations.builder();
            FlowRule rule;

            treatment.transition(FlowRule.Type.IP);

            rule = new DefaultFlowRule(deviceId, selector.build(),
                                       treatment.build(), DROP_PRIORITY, appId,
                                       0, true, FlowRule.Type.COS);

            ops = install ? ops.add(rule) : ops.remove(rule);

            flowService.apply(ops.build(new FlowRuleOperationsContext() {
                @Override
                public void onSuccess(FlowRuleOperations ops) {
                    log.info("Provisioned cos table for bgp router");
                }

                @Override
                public void onError(FlowRuleOperations ops) {
                    log.info("Failed to provision cos table for bgp router");
                }
            }));

        }

        private void processTableSix(boolean install) {
            TrafficSelector.Builder selector;
            TrafficTreatment.Builder treatment;
            FlowRuleOperations.Builder ops = FlowRuleOperations.builder();
            FlowRule rule;


            //Interface IPs
            for (InterfaceIpAddress ipAddr : intfIps) {
                log.debug("adding rule for IPs: {}", ipAddr.ipAddress());
                selector = DefaultTrafficSelector.builder();
                treatment = DefaultTrafficTreatment.builder();

                selector.matchEthType(Ethernet.TYPE_IPV4);
                selector.matchIPDst(IpPrefix.valueOf(ipAddr.ipAddress(), 32));
                treatment.transition(Type.ACL);

                rule = new DefaultFlowRule(deviceId, selector.build(),
                                           treatment.build(), HIGHEST_PRIORITY, appId,
                                           0, true, FlowRule.Type.IP);

                ops = install ? ops.add(rule) : ops.remove(rule);
            }


            //Drop rule
            selector = DefaultTrafficSelector.builder();
            treatment = DefaultTrafficTreatment.builder();

            treatment.drop();

            rule = new DefaultFlowRule(deviceId, selector.build(),
                                       treatment.build(), DROP_PRIORITY, appId,
                                       0, true, FlowRule.Type.IP);

            ops = install ? ops.add(rule) : ops.remove(rule);

            flowService.apply(ops.build(new FlowRuleOperationsContext() {
                @Override
                public void onSuccess(FlowRuleOperations ops) {
                    log.info("Provisioned FIB table for bgp router");
                }

                @Override
                public void onError(FlowRuleOperations ops) {
                    log.info("Failed to provision FIB table for bgp router");
                }
            }));
        }

        private void processTableNine(boolean install) {
            TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
            TrafficTreatment.Builder treatment = DefaultTrafficTreatment
                    .builder();
            FlowRuleOperations.Builder ops = FlowRuleOperations.builder();
            FlowRule rule;

            treatment.punt();

            rule = new DefaultFlowRule(deviceId, selector.build(),
                                       treatment.build(), CONTROLLER_PRIORITY,
                                       appId, 0, true, FlowRule.Type.DEFAULT);

            ops = install ? ops.add(rule) : ops.remove(rule);

            flowService.apply(ops.build(new FlowRuleOperationsContext() {
                @Override
                public void onSuccess(FlowRuleOperations ops) {
                    log.info("Provisioned Local table for bgp router");
                }

                @Override
                public void onError(FlowRuleOperations ops) {
                    log.info("Failed to provision Local table for bgp router");
                }
            }));
        }
    }

    private class InternalGroupListener implements GroupListener {

        @Override
        public void event(GroupEvent event) {
            Group group = event.subject();

            if (event.type() == GroupEvent.Type.GROUP_ADDED ||
                    event.type() == GroupEvent.Type.GROUP_UPDATED) {
                synchronized (pendingUpdates) {

                    NextHopGroupKey nhGroupKey =
                            appKryo.build().deserialize(group.appCookie().key());
                    Map<FibEntry, Group> entriesToInstall =
                            pendingUpdates.removeAll(nhGroupKey)
                                    .stream()
                                    .collect(Collectors
                                                     .toMap(e -> e, e -> group));

                    installFlows(entriesToInstall);
                }
            }
        }
    }
}
