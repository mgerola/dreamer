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
package org.onosproject.store.mastership.impl;

import static org.onlab.util.Tools.groupedThreads;
import static org.onosproject.mastership.MastershipEvent.Type.BACKUPS_CHANGED;
import static org.onosproject.mastership.MastershipEvent.Type.MASTER_CHANGED;
import static org.slf4j.LoggerFactory.getLogger;
import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.KryoNamespace;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.Leadership;
import org.onosproject.cluster.LeadershipEvent;
import org.onosproject.cluster.LeadershipEventListener;
import org.onosproject.cluster.LeadershipService;
import org.onosproject.cluster.NodeId;
import org.onosproject.cluster.RoleInfo;
import org.onosproject.mastership.MastershipEvent;
import org.onosproject.mastership.MastershipStore;
import org.onosproject.mastership.MastershipStoreDelegate;
import org.onosproject.mastership.MastershipTerm;
import org.onosproject.net.DeviceId;
import org.onosproject.net.MastershipRole;
import org.onosproject.store.AbstractStore;
import org.onosproject.store.cluster.messaging.ClusterCommunicationService;
import org.onosproject.store.cluster.messaging.ClusterMessage;
import org.onosproject.store.cluster.messaging.ClusterMessageHandler;
import org.onosproject.store.cluster.messaging.MessageSubject;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.serializers.KryoSerializer;
import org.onosproject.store.serializers.StoreSerializer;
import org.slf4j.Logger;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Implementation of the MastershipStore on top of Leadership Service.
 */
@Component(immediate = true, enabled = false)
@Service
public class ConsistentDeviceMastershipStore
    extends AbstractStore<MastershipEvent, MastershipStoreDelegate>
    implements MastershipStore {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LeadershipService leadershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterService clusterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterCommunicationService clusterCommunicator;

    private NodeId localNodeId;
    private final Set<DeviceId> connectedDevices = Sets.newHashSet();

    private static final MessageSubject ROLE_QUERY_SUBJECT =
            new MessageSubject("mastership-store-device-role-query");
    private static final MessageSubject ROLE_RELINQUISH_SUBJECT =
            new MessageSubject("mastership-store-device-role-relinquish");

    private static final Pattern DEVICE_MASTERSHIP_TOPIC_PATTERN =
            Pattern.compile("/devices/(.*)/mastership");

    private static final long PEER_REQUEST_TIMEOUT_MS = 5000;
    private ExecutorService messageHandlingExecutor;
    private final LeadershipEventListener leadershipEventListener =
            new InternalDeviceMastershipEventListener();

    private static final String NODE_ID_NULL = "Node ID cannot be null";
    private static final String DEVICE_ID_NULL = "Device ID cannot be null";;

    public static final StoreSerializer SERIALIZER = new KryoSerializer() {
        @Override
        protected void setupKryoPool() {
            serializerPool = KryoNamespace.newBuilder()
                    .register(KryoNamespaces.API)
                    .register(MastershipRole.class)
                    .register(MastershipEvent.class)
                    .build();
        }
    };

    @Activate
    public void activate() {
        messageHandlingExecutor =
                Executors.newSingleThreadExecutor(groupedThreads("onos/store/device/mastership", "message-handler"));
        clusterCommunicator.addSubscriber(ROLE_QUERY_SUBJECT,
                new RoleQueryHandler(),
                messageHandlingExecutor);
        clusterCommunicator.addSubscriber(ROLE_RELINQUISH_SUBJECT,
               new RoleRelinquishHandler(),
               messageHandlingExecutor);
        localNodeId = clusterService.getLocalNode().id();
        leadershipService.addListener(leadershipEventListener);

        log.info("Started.");
    }

    @Deactivate
    public void deactivate() {
        clusterCommunicator.removeSubscriber(ROLE_QUERY_SUBJECT);
        clusterCommunicator.removeSubscriber(ROLE_RELINQUISH_SUBJECT);
        messageHandlingExecutor.shutdown();
        leadershipService.removeListener(leadershipEventListener);

        log.info("Stoppped.");
    }

    @Override
    public MastershipRole requestRole(DeviceId deviceId) {
        checkArgument(deviceId != null, DEVICE_ID_NULL);

        String leadershipTopic = createDeviceMastershipTopic(deviceId);
        if (connectedDevices.add(deviceId)) {
            leadershipService.runForLeadership(leadershipTopic);
            return MastershipRole.STANDBY;
        } else {
            Leadership leadership = leadershipService.getLeadership(leadershipTopic);
            if (leadership != null && leadership.leader().equals(localNodeId)) {
                return MastershipRole.MASTER;
            } else {
                return MastershipRole.STANDBY;
            }
        }
    }

    @Override
    public MastershipRole getRole(NodeId nodeId, DeviceId deviceId) {
        checkArgument(nodeId != null, NODE_ID_NULL);
        checkArgument(deviceId != null, DEVICE_ID_NULL);

        String leadershipTopic = createDeviceMastershipTopic(deviceId);
        Leadership leadership = leadershipService.getLeadership(leadershipTopic);
        if (leadership != null && nodeId.equals(leadership.leader())) {
            return MastershipRole.MASTER;
        }

        if (localNodeId.equals(nodeId)) {
            if (connectedDevices.contains(deviceId)) {
                return MastershipRole.STANDBY;
            } else {
                return MastershipRole.NONE;
            }
        } else {
            try {
                MastershipRole role = complete(clusterCommunicator.sendAndReceive(
                                                        new ClusterMessage(
                                                                    localNodeId,
                                                                    ROLE_QUERY_SUBJECT,
                                                                    SERIALIZER.encode(deviceId)),
                                                        nodeId));
                return role == null ? MastershipRole.NONE : role;
            } catch (IOException e) {
                log.warn("Failed to query {} for {}'s role. Defaulting to NONE", nodeId, deviceId, e);
                return MastershipRole.NONE;
            }
        }
    }

    @Override
    public NodeId getMaster(DeviceId deviceId) {
        checkArgument(deviceId != null, DEVICE_ID_NULL);

        String leadershipTopic = createDeviceMastershipTopic(deviceId);
        Leadership leadership = leadershipService.getLeadership(leadershipTopic);
        return leadership != null ? leadership.leader() : null;
    }

    @Override
    public RoleInfo getNodes(DeviceId deviceId) {
        checkArgument(deviceId != null, DEVICE_ID_NULL);

        Map<NodeId, MastershipRole> roles = Maps.newHashMap();
        clusterService
            .getNodes()
            .stream()
            .parallel()
            .forEach((node) -> roles.put(node.id(), getRole(node.id(), deviceId)));

        NodeId master = null;
        final List<NodeId> standbys = Lists.newLinkedList();

        for (Map.Entry<NodeId, MastershipRole> entry : roles.entrySet()) {
            if (entry.getValue() == MastershipRole.MASTER) {
                master = entry.getKey();
            } else if (entry.getValue() == MastershipRole.STANDBY) {
                standbys.add(entry.getKey());
            }
        }

        return new RoleInfo(master, standbys);
    }

    @Override
    public Set<DeviceId> getDevices(NodeId nodeId) {
        checkArgument(nodeId != null, NODE_ID_NULL);

        return leadershipService
                .ownedTopics(nodeId)
                .stream()
                .filter(this::isDeviceMastershipTopic)
                .map(this::extractDeviceIdFromTopic)
                .collect(Collectors.toSet());
    }

    @Override
    public MastershipEvent setMaster(NodeId nodeId, DeviceId deviceId) {
        checkArgument(nodeId != null, NODE_ID_NULL);
        checkArgument(deviceId != null, DEVICE_ID_NULL);

        throw new UnsupportedOperationException("This operation is not supported in " + this.getClass().getName());
    }

    @Override
    public MastershipTerm getTermFor(DeviceId deviceId) {
        checkArgument(deviceId != null, DEVICE_ID_NULL);

        String leadershipTopic = createDeviceMastershipTopic(deviceId);
        Leadership leadership = leadershipService.getLeadership(leadershipTopic);
        return leadership != null ? MastershipTerm.of(leadership.leader(), leadership.epoch()) : null;
    }

    @Override
    public MastershipEvent setStandby(NodeId nodeId, DeviceId deviceId) {
        checkArgument(nodeId != null, NODE_ID_NULL);
        checkArgument(deviceId != null, DEVICE_ID_NULL);

        throw new UnsupportedOperationException("This operation is not supported in " + this.getClass().getName());
    }

    @Override
    public MastershipEvent relinquishRole(NodeId nodeId, DeviceId deviceId) {
        checkArgument(nodeId != null, NODE_ID_NULL);
        checkArgument(deviceId != null, DEVICE_ID_NULL);

        if (!nodeId.equals(localNodeId)) {
            log.debug("Forwarding request to relinquish "
                    + "role for device {} to {}", deviceId, nodeId);
            try {
                return complete(clusterCommunicator.sendAndReceive(
                                                        new ClusterMessage(
                                                                    localNodeId,
                                                                    ROLE_RELINQUISH_SUBJECT,
                                                                    SERIALIZER.encode(deviceId)),
                                                        nodeId));
            } catch (IOException e) {
                log.warn("Failed to send a request to relinquish role for {} to {}", deviceId, nodeId, e);
                return null;
            }
        }

        // Check if this node is can be managed by this node.
        if (!connectedDevices.contains(deviceId)) {
            return null;
        }

        String leadershipTopic = createDeviceMastershipTopic(deviceId);
        Leadership currentLeadership = leadershipService.getLeadership(leadershipTopic);

        MastershipEvent.Type eventType = null;
        if (currentLeadership != null && currentLeadership.leader().equals(localNodeId)) {
            eventType = MastershipEvent.Type.MASTER_CHANGED;
        } else {
            eventType = MastershipEvent.Type.BACKUPS_CHANGED;
        }

        connectedDevices.remove(deviceId);
        leadershipService.withdraw(leadershipTopic);

        return new MastershipEvent(eventType, deviceId, getNodes(deviceId));
    }

    private class RoleQueryHandler implements ClusterMessageHandler {
        @Override
        public void handle(ClusterMessage message) {
            DeviceId deviceId = SERIALIZER.decode(message.payload());
            try {
                message.respond(SERIALIZER.encode(getRole(localNodeId, deviceId)));
            } catch (IOException e) {
                log.error("Failed to responsd to role query", e);
            }
        }
    }


    @Override
    public void relinquishAllRole(NodeId nodeId) {
        // Noop. LeadershipService already takes care of detecting and purging deadlocks.
    }

    private class RoleRelinquishHandler implements ClusterMessageHandler {
        @Override
        public void handle(ClusterMessage message) {
            DeviceId deviceId = SERIALIZER.decode(message.payload());
            try {
                message.respond(SERIALIZER.encode(relinquishRole(localNodeId, deviceId)));
            } catch (IOException e) {
                log.error("Failed to relinquish role.", e);
            }
        }
    }

    private class InternalDeviceMastershipEventListener implements LeadershipEventListener {
        @Override
        public void event(LeadershipEvent event) {
            Leadership leadership = event.subject();
            if (!isDeviceMastershipTopic(leadership.topic())) {
                return;
            }
            NodeId nodeId = leadership.leader();
            DeviceId deviceId = extractDeviceIdFromTopic(leadership.topic());
            if (Objects.equal(nodeId, localNodeId) && connectedDevices.contains(deviceId)) {
                switch (event.type()) {
                case LEADER_ELECTED:
                    notifyDelegate(new MastershipEvent(MASTER_CHANGED, deviceId, getNodes(deviceId)));
                    break;
                case LEADER_REELECTED:
                    // There is no concept of leader re-election in the new distributed leadership manager.
                    throw new IllegalStateException("Unexpected event type");
                case LEADER_BOOTED:
                    notifyDelegate(new MastershipEvent(BACKUPS_CHANGED, deviceId, getNodes(deviceId)));
                    break;
                default:
                    return;
                }
            }
        }
    }

    private String createDeviceMastershipTopic(DeviceId deviceId) {
        return "/devices/" + deviceId.toString() + "/mastership";
    }

    private DeviceId extractDeviceIdFromTopic(String topic) {
        Matcher m = DEVICE_MASTERSHIP_TOPIC_PATTERN.matcher(topic);
        if (m.matches()) {
            return DeviceId.deviceId(m.group(1));
        } else {
            throw new IllegalArgumentException("Invalid device mastership topic: " + topic);
        }
    }

    private boolean isDeviceMastershipTopic(String topic) {
        Matcher m = DEVICE_MASTERSHIP_TOPIC_PATTERN.matcher(topic);
        return m.matches();
    }

    private <T> T complete(Future<byte[]> future) {
        try {
            return SERIALIZER.decode(future.get(PEER_REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for operation to complete.", e);
            return null;
        } catch (TimeoutException | ExecutionException e) {
            log.error("Failed remote operation", e);
            return null;
        }
    }
}