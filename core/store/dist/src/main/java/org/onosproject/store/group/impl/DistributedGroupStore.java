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
package org.onosproject.store.group.impl;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.KryoNamespace;
import org.onlab.util.NewConcurrentHashMap;
import org.onosproject.cluster.ClusterService;
import org.onosproject.core.DefaultApplicationId;
import org.onosproject.core.DefaultGroupId;
import org.onosproject.core.GroupId;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.MastershipRole;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.instructions.Instructions;
import org.onosproject.net.flow.instructions.L0ModificationInstruction;
import org.onosproject.net.flow.instructions.L2ModificationInstruction;
import org.onosproject.net.flow.instructions.L3ModificationInstruction;
import org.onosproject.net.group.DefaultGroup;
import org.onosproject.net.group.DefaultGroupBucket;
import org.onosproject.net.group.DefaultGroupDescription;
import org.onosproject.net.group.DefaultGroupKey;
import org.onosproject.net.group.Group;
import org.onosproject.net.group.Group.GroupState;
import org.onosproject.net.group.GroupBucket;
import org.onosproject.net.group.GroupBuckets;
import org.onosproject.net.group.GroupDescription;
import org.onosproject.net.group.GroupEvent;
import org.onosproject.net.group.GroupEvent.Type;
import org.onosproject.net.group.GroupKey;
import org.onosproject.net.group.GroupOperation;
import org.onosproject.net.group.GroupStore;
import org.onosproject.net.group.GroupStoreDelegate;
import org.onosproject.net.group.StoredGroupEntry;
import org.onosproject.store.AbstractStore;
import org.onosproject.store.Timestamp;
import org.onosproject.store.cluster.messaging.ClusterCommunicationService;
import org.onosproject.store.cluster.messaging.ClusterMessage;
import org.onosproject.store.cluster.messaging.ClusterMessageHandler;
import org.onosproject.store.impl.MultiValuedTimestamp;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.ClockService;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.EventuallyConsistentMapBuilder;
import org.onosproject.store.service.EventuallyConsistentMapEvent;
import org.onosproject.store.service.EventuallyConsistentMapListener;
import org.onosproject.store.service.StorageService;
import org.slf4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.concurrent.ConcurrentUtils.createIfAbsentUnchecked;
import static org.onlab.util.Tools.groupedThreads;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Manages inventory of group entries using trivial in-memory implementation.
 */
@Component(immediate = true)
@Service
public class DistributedGroupStore
        extends AbstractStore<GroupEvent, GroupStoreDelegate>
        implements GroupStore {

    private final Logger log = getLogger(getClass());

    private final int dummyId = 0xffffffff;
    private final GroupId dummyGroupId = new DefaultGroupId(dummyId);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterCommunicationService clusterCommunicator;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterService clusterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    // Per device group table with (device id + app cookie) as key
    private EventuallyConsistentMap<GroupStoreKeyMapKey,
        StoredGroupEntry> groupStoreEntriesByKey = null;
    // Per device group table with (device id + group id) as key
    private EventuallyConsistentMap<GroupStoreIdMapKey,
        StoredGroupEntry> groupStoreEntriesById = null;
    private EventuallyConsistentMap<GroupStoreKeyMapKey,
        StoredGroupEntry> auditPendingReqQueue = null;
    private final ConcurrentMap<DeviceId, ConcurrentMap<GroupId, Group>>
            extraneousGroupEntriesById = new ConcurrentHashMap<>();
    private ExecutorService messageHandlingExecutor;
    private static final int MESSAGE_HANDLER_THREAD_POOL_SIZE = 1;

    private final HashMap<DeviceId, Boolean> deviceAuditStatus =
            new HashMap<DeviceId, Boolean>();

    private final AtomicInteger groupIdGen = new AtomicInteger();

    private KryoNamespace.Builder kryoBuilder = null;

    @Activate
    public void activate() {
        kryoBuilder = new KryoNamespace.Builder()
            .register(DefaultGroup.class,
                      DefaultGroupBucket.class,
                      DefaultGroupDescription.class,
                      DefaultGroupKey.class,
                      GroupDescription.Type.class,
                      Group.GroupState.class,
                      GroupBuckets.class,
                      DefaultGroupId.class,
                      GroupStoreMessage.class,
                      GroupStoreMessage.Type.class,
                      UpdateType.class,
                      GroupStoreMessageSubjects.class,
                      MultiValuedTimestamp.class,
                      GroupStoreKeyMapKey.class,
                      GroupStoreIdMapKey.class,
                      GroupStoreMapKey.class
                    )
            .register(URI.class)
            .register(DeviceId.class)
            .register(PortNumber.class)
            .register(DefaultApplicationId.class)
            .register(DefaultTrafficTreatment.class,
                      Instructions.DropInstruction.class,
                      Instructions.OutputInstruction.class,
                      Instructions.GroupInstruction.class,
                      Instructions.TableTypeTransition.class,
                      FlowRule.Type.class,
                      L0ModificationInstruction.class,
                      L0ModificationInstruction.L0SubType.class,
                      L0ModificationInstruction.ModLambdaInstruction.class,
                      L2ModificationInstruction.class,
                      L2ModificationInstruction.L2SubType.class,
                      L2ModificationInstruction.ModEtherInstruction.class,
                      L2ModificationInstruction.PushHeaderInstructions.class,
                      L2ModificationInstruction.ModVlanIdInstruction.class,
                      L2ModificationInstruction.ModVlanPcpInstruction.class,
                      L2ModificationInstruction.ModMplsLabelInstruction.class,
                      L2ModificationInstruction.ModMplsTtlInstruction.class,
                      L3ModificationInstruction.class,
                      L3ModificationInstruction.L3SubType.class,
                      L3ModificationInstruction.ModIPInstruction.class,
                      L3ModificationInstruction.ModIPv6FlowLabelInstruction.class,
                      L3ModificationInstruction.ModTtlInstruction.class,
                      org.onlab.packet.MplsLabel.class
                    )
            .register(org.onosproject.cluster.NodeId.class)
            .register(KryoNamespaces.BASIC)
            .register(KryoNamespaces.MISC);

        messageHandlingExecutor = Executors.
                newFixedThreadPool(MESSAGE_HANDLER_THREAD_POOL_SIZE,
                                   groupedThreads("onos/store/group",
                                                  "message-handlers"));
        clusterCommunicator.
            addSubscriber(GroupStoreMessageSubjects.REMOTE_GROUP_OP_REQUEST,
                          new ClusterGroupMsgHandler(),
                          messageHandlingExecutor);

        log.debug("Creating EC map groupstorekeymap");
        EventuallyConsistentMapBuilder<GroupStoreKeyMapKey, StoredGroupEntry>
                keyMapBuilder = storageService.eventuallyConsistentMapBuilder();

        groupStoreEntriesByKey = keyMapBuilder
                .withName("groupstorekeymap")
                .withSerializer(kryoBuilder)
                .withClockService(new GroupStoreLogicalClockManager<>())
                .build();
        log.trace("Current size {}", groupStoreEntriesByKey.size());

        log.debug("Creating EC map groupstoreidmap");
        EventuallyConsistentMapBuilder<GroupStoreIdMapKey, StoredGroupEntry>
                idMapBuilder = storageService.eventuallyConsistentMapBuilder();

        groupStoreEntriesById = idMapBuilder
                        .withName("groupstoreidmap")
                        .withSerializer(kryoBuilder)
                        .withClockService(new GroupStoreLogicalClockManager<>())
                        .build();

        groupStoreEntriesById.addListener(new GroupStoreIdMapListener());
        log.trace("Current size {}", groupStoreEntriesById.size());

        log.debug("Creating EC map pendinggroupkeymap");
        EventuallyConsistentMapBuilder<GroupStoreKeyMapKey, StoredGroupEntry>
                auditMapBuilder = storageService.eventuallyConsistentMapBuilder();

        auditPendingReqQueue = auditMapBuilder
                .withName("pendinggroupkeymap")
                .withSerializer(kryoBuilder)
                .withClockService(new GroupStoreLogicalClockManager<>())
                .build();
        log.trace("Current size {}", auditPendingReqQueue.size());

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
    }

    private static NewConcurrentHashMap<GroupId, Group>
        lazyEmptyExtraneousGroupIdTable() {
        return NewConcurrentHashMap.<GroupId, Group>ifNeeded();
    }

    /**
     * Returns the group store eventual consistent key map.
     *
     * @return Map representing group key table.
     */
    private EventuallyConsistentMap<GroupStoreKeyMapKey, StoredGroupEntry>
        getGroupStoreKeyMap() {
        return groupStoreEntriesByKey;
    }

    /**
     * Returns the group store eventual consistent id map.
     *
     * @return Map representing group id table.
     */
    private EventuallyConsistentMap<GroupStoreIdMapKey, StoredGroupEntry>
        getGroupStoreIdMap() {
        return groupStoreEntriesById;
    }

    /**
     * Returns the pending group request table.
     *
     * @return Map representing group key table.
     */
    private EventuallyConsistentMap<GroupStoreKeyMapKey, StoredGroupEntry>
        getPendingGroupKeyTable() {
        return auditPendingReqQueue;
    }

    /**
     * Returns the extraneous group id table for specified device.
     *
     * @param deviceId identifier of the device
     * @return Map representing group key table of given device.
     */
    private ConcurrentMap<GroupId, Group>
    getExtraneousGroupIdTable(DeviceId deviceId) {
        return createIfAbsentUnchecked(extraneousGroupEntriesById,
                                       deviceId,
                                       lazyEmptyExtraneousGroupIdTable());
    }

    /**
     * Returns the number of groups for the specified device in the store.
     *
     * @return number of groups for the specified device
     */
    @Override
    public int getGroupCount(DeviceId deviceId) {
        return (getGroups(deviceId) != null) ?
                         Iterables.size(getGroups(deviceId)) : 0;
    }

    /**
     * Returns the groups associated with a device.
     *
     * @param deviceId the device ID
     *
     * @return the group entries
     */
    @Override
    public Iterable<Group> getGroups(DeviceId deviceId) {
        // flatten and make iterator unmodifiable
        log.trace("getGroups: for device {} total number of groups {}",
                  deviceId, getGroupStoreKeyMap().values().size());
        return FluentIterable.from(getGroupStoreKeyMap().values())
                .filter(input -> input.deviceId().equals(deviceId))
                .transform(input -> input);
    }

    /**
     * Returns the stored group entry.
     *
     * @param deviceId the device ID
     * @param appCookie the group key
     *
     * @return a group associated with the key
     */
    @Override
    public Group getGroup(DeviceId deviceId, GroupKey appCookie) {
        return getStoredGroupEntry(deviceId, appCookie);
    }

    private StoredGroupEntry getStoredGroupEntry(DeviceId deviceId,
                                                 GroupKey appCookie) {
        return getGroupStoreKeyMap().get(new GroupStoreKeyMapKey(deviceId,
                                                                 appCookie));
    }

    @Override
    public Group getGroup(DeviceId deviceId, GroupId groupId) {
        return getStoredGroupEntry(deviceId, groupId);
    }

    private StoredGroupEntry getStoredGroupEntry(DeviceId deviceId,
                                                 GroupId groupId) {
        return getGroupStoreIdMap().get(new GroupStoreIdMapKey(deviceId,
                                                               groupId));
    }

    private int getFreeGroupIdValue(DeviceId deviceId) {
        int freeId = groupIdGen.incrementAndGet();

        while (true) {
            Group existing = getGroup(deviceId, new DefaultGroupId(freeId));
            if (existing == null) {
                existing = (
                        extraneousGroupEntriesById.get(deviceId) != null) ?
                        extraneousGroupEntriesById.get(deviceId).
                                get(new DefaultGroupId(freeId)) :
                        null;
            }
            if (existing != null) {
                freeId = groupIdGen.incrementAndGet();
            } else {
                break;
            }
        }
        return freeId;
    }

    /**
     * Stores a new group entry using the information from group description.
     *
     * @param groupDesc group description to be used to create group entry
     */
    @Override
    public void storeGroupDescription(GroupDescription groupDesc) {
        log.trace("In storeGroupDescription");
        // Check if a group is existing with the same key
        if (getGroup(groupDesc.deviceId(), groupDesc.appCookie()) != null) {
            log.warn("Group already exists with the same key {}",
                     groupDesc.appCookie());
            return;
        }

        // Check if group to be created by a remote instance
        if (mastershipService.getLocalRole(
                     groupDesc.deviceId()) != MastershipRole.MASTER) {
            log.debug("Device {} local role is not MASTER",
                      groupDesc.deviceId());
            GroupStoreMessage groupOp = GroupStoreMessage.
                    createGroupAddRequestMsg(groupDesc.deviceId(),
                                             groupDesc);
            ClusterMessage message = new ClusterMessage(
                                    clusterService.getLocalNode().id(),
                                    GroupStoreMessageSubjects.
                                    REMOTE_GROUP_OP_REQUEST,
                                    kryoBuilder.build().serialize(groupOp));
            if (!clusterCommunicator.unicast(message,
                                             mastershipService.
                                             getMasterFor(
                                                groupDesc.deviceId()))) {
                log.warn("Failed to send request to master: {} to {}",
                         message,
                         mastershipService.getMasterFor(groupDesc.deviceId()));
                //TODO: Send Group operation failure event
            }
            log.debug("Sent Group operation request for device {} "
                    + "to remote MASTER {}",
                      groupDesc.deviceId(),
                      mastershipService.getMasterFor(groupDesc.deviceId()));
            return;
        }

        log.debug("Store group for device {} is getting handled locally",
                  groupDesc.deviceId());
        storeGroupDescriptionInternal(groupDesc);
    }

    private void storeGroupDescriptionInternal(GroupDescription groupDesc) {
        // Check if a group is existing with the same key
        if (getGroup(groupDesc.deviceId(), groupDesc.appCookie()) != null) {
            return;
        }

        if (deviceAuditStatus.get(groupDesc.deviceId()) == null) {
            // Device group audit has not completed yet
            // Add this group description to pending group key table
            // Create a group entry object with Dummy Group ID
            log.debug("storeGroupDescriptionInternal: Device {} AUDIT "
                    + "pending...Queuing Group ADD request",
                    groupDesc.deviceId());
            StoredGroupEntry group = new DefaultGroup(dummyGroupId, groupDesc);
            group.setState(GroupState.WAITING_AUDIT_COMPLETE);
            EventuallyConsistentMap<GroupStoreKeyMapKey, StoredGroupEntry> pendingKeyTable =
                    getPendingGroupKeyTable();
            pendingKeyTable.put(new GroupStoreKeyMapKey(groupDesc.deviceId(),
                                                        groupDesc.appCookie()),
                                group);
            return;
        }

        // Get a new group identifier
        GroupId id = new DefaultGroupId(getFreeGroupIdValue(groupDesc.deviceId()));
        // Create a group entry object
        StoredGroupEntry group = new DefaultGroup(id, groupDesc);
        // Insert the newly created group entry into key and id maps
        getGroupStoreKeyMap().
            put(new GroupStoreKeyMapKey(groupDesc.deviceId(),
                                        groupDesc.appCookie()), group);
        getGroupStoreIdMap().
            put(new GroupStoreIdMapKey(groupDesc.deviceId(),
                                    id), group);
        notifyDelegate(new GroupEvent(GroupEvent.Type.GROUP_ADD_REQUESTED,
                                      group));
    }

    /**
     * Updates the existing group entry with the information
     * from group description.
     *
     * @param deviceId the device ID
     * @param oldAppCookie the current group key
     * @param type update type
     * @param newBuckets group buckets for updates
     * @param newAppCookie optional new group key
     */
    @Override
    public void updateGroupDescription(DeviceId deviceId,
                                       GroupKey oldAppCookie,
                                       UpdateType type,
                                       GroupBuckets newBuckets,
                                       GroupKey newAppCookie) {
        // Check if group update to be done by a remote instance
        if (mastershipService.
                getLocalRole(deviceId) != MastershipRole.MASTER) {
            GroupStoreMessage groupOp = GroupStoreMessage.
                    createGroupUpdateRequestMsg(deviceId,
                                                oldAppCookie,
                                                type,
                                                newBuckets,
                                                newAppCookie);
            ClusterMessage message =
                    new ClusterMessage(clusterService.getLocalNode().id(),
                                       GroupStoreMessageSubjects.
                                              REMOTE_GROUP_OP_REQUEST,
                                       kryoBuilder.build().serialize(groupOp));
            if (!clusterCommunicator.unicast(message,
                                             mastershipService.
                                             getMasterFor(deviceId))) {
                log.warn("Failed to send request to master: {} to {}",
                         message,
                         mastershipService.getMasterFor(deviceId));
                //TODO: Send Group operation failure event
            }
            return;
        }
        updateGroupDescriptionInternal(deviceId,
                                       oldAppCookie,
                                       type,
                                       newBuckets,
                                       newAppCookie);
    }

    private void updateGroupDescriptionInternal(DeviceId deviceId,
                                       GroupKey oldAppCookie,
                                       UpdateType type,
                                       GroupBuckets newBuckets,
                                       GroupKey newAppCookie) {
        // Check if a group is existing with the provided key
        Group oldGroup = getGroup(deviceId, oldAppCookie);
        if (oldGroup == null) {
            return;
        }

        List<GroupBucket> newBucketList = getUpdatedBucketList(oldGroup,
                                                               type,
                                                               newBuckets);
        if (newBucketList != null) {
            // Create a new group object from the old group
            GroupBuckets updatedBuckets = new GroupBuckets(newBucketList);
            GroupKey newCookie = (newAppCookie != null) ? newAppCookie : oldAppCookie;
            GroupDescription updatedGroupDesc = new DefaultGroupDescription(
                    oldGroup.deviceId(),
                    oldGroup.type(),
                    updatedBuckets,
                    newCookie,
                    oldGroup.appId());
            StoredGroupEntry newGroup = new DefaultGroup(oldGroup.id(),
                                                         updatedGroupDesc);
            newGroup.setState(GroupState.PENDING_UPDATE);
            newGroup.setLife(oldGroup.life());
            newGroup.setPackets(oldGroup.packets());
            newGroup.setBytes(oldGroup.bytes());
            // Remove the old entry from maps and add new entry using new key
            getGroupStoreKeyMap().remove(new GroupStoreKeyMapKey(oldGroup.deviceId(),
                                        oldGroup.appCookie()));
            getGroupStoreIdMap().remove(new GroupStoreIdMapKey(oldGroup.deviceId(),
                                                                 oldGroup.id()));
            getGroupStoreKeyMap().
                put(new GroupStoreKeyMapKey(newGroup.deviceId(),
                                            newGroup.appCookie()), newGroup);
            getGroupStoreIdMap().
            put(new GroupStoreIdMapKey(newGroup.deviceId(),
                                       newGroup.id()), newGroup);

            notifyDelegate(new GroupEvent(Type.GROUP_UPDATE_REQUESTED, newGroup));
        }
    }

    private List<GroupBucket> getUpdatedBucketList(Group oldGroup,
                                                   UpdateType type,
                                                   GroupBuckets buckets) {
        GroupBuckets oldBuckets = oldGroup.buckets();
        List<GroupBucket> newBucketList = new ArrayList<GroupBucket>(
                oldBuckets.buckets());
        boolean groupDescUpdated = false;

        if (type == UpdateType.ADD) {
            // Check if the any of the new buckets are part of
            // the old bucket list
            for (GroupBucket addBucket:buckets.buckets()) {
                if (!newBucketList.contains(addBucket)) {
                    newBucketList.add(addBucket);
                    groupDescUpdated = true;
                }
            }
        } else if (type == UpdateType.REMOVE) {
            // Check if the to be removed buckets are part of the
            // old bucket list
            for (GroupBucket removeBucket:buckets.buckets()) {
                if (newBucketList.contains(removeBucket)) {
                    newBucketList.remove(removeBucket);
                    groupDescUpdated = true;
                }
            }
        }

        if (groupDescUpdated) {
            return newBucketList;
        } else {
            return null;
        }
    }

    /**
     * Triggers deleting the existing group entry.
     *
     * @param deviceId the device ID
     * @param appCookie the group key
     */
    @Override
    public void deleteGroupDescription(DeviceId deviceId,
                                       GroupKey appCookie) {
        // Check if group to be deleted by a remote instance
        if (mastershipService.
                getLocalRole(deviceId) != MastershipRole.MASTER) {
            GroupStoreMessage groupOp = GroupStoreMessage.
                    createGroupDeleteRequestMsg(deviceId,
                                                appCookie);
            ClusterMessage message =
                    new ClusterMessage(clusterService.getLocalNode().id(),
                                       GroupStoreMessageSubjects.
                                              REMOTE_GROUP_OP_REQUEST,
                                       kryoBuilder.build().serialize(groupOp));
            if (!clusterCommunicator.unicast(message,
                                             mastershipService.
                                             getMasterFor(deviceId))) {
                log.warn("Failed to send request to master: {} to {}",
                         message,
                         mastershipService.getMasterFor(deviceId));
                //TODO: Send Group operation failure event
            }
            return;
        }
        deleteGroupDescriptionInternal(deviceId, appCookie);
    }

    private void deleteGroupDescriptionInternal(DeviceId deviceId,
                                                GroupKey appCookie) {
        // Check if a group is existing with the provided key
        StoredGroupEntry existing = getStoredGroupEntry(deviceId, appCookie);
        if (existing == null) {
            return;
        }

        synchronized (existing) {
            existing.setState(GroupState.PENDING_DELETE);
        }
        notifyDelegate(new GroupEvent(Type.GROUP_REMOVE_REQUESTED, existing));
    }

    /**
     * Stores a new group entry, or updates an existing entry.
     *
     * @param group group entry
     */
    @Override
    public void addOrUpdateGroupEntry(Group group) {
        // check if this new entry is an update to an existing entry
        StoredGroupEntry existing = getStoredGroupEntry(group.deviceId(),
                                                        group.id());
        GroupEvent event = null;

        if (existing != null) {
            log.trace("addOrUpdateGroupEntry: updating group "
                    + "entry {} in device {}",
                    group.id(),
                    group.deviceId());
            synchronized (existing) {
                existing.setLife(group.life());
                existing.setPackets(group.packets());
                existing.setBytes(group.bytes());
                if (existing.state() == GroupState.PENDING_ADD) {
                    existing.setState(GroupState.ADDED);
                    existing.setIsGroupStateAddedFirstTime(true);
                    event = new GroupEvent(Type.GROUP_ADDED, existing);
                } else {
                    existing.setState(GroupState.ADDED);
                    existing.setIsGroupStateAddedFirstTime(false);
                    event = new GroupEvent(Type.GROUP_UPDATED, existing);
                }
                //Re-PUT map entries to trigger map update events
                getGroupStoreKeyMap().
                    put(new GroupStoreKeyMapKey(existing.deviceId(),
                                                existing.appCookie()), existing);
                getGroupStoreIdMap().
                    put(new GroupStoreIdMapKey(existing.deviceId(),
                                               existing.id()), existing);
            }
        }

        if (event != null) {
            notifyDelegate(event);
        }
    }

    /**
     * Removes the group entry from store.
     *
     * @param group group entry
     */
    @Override
    public void removeGroupEntry(Group group) {
        StoredGroupEntry existing = getStoredGroupEntry(group.deviceId(),
                                                        group.id());

        if (existing != null) {
            log.trace("removeGroupEntry: removing group "
                    + "entry {} in device {}",
                    group.id(),
                    group.deviceId());
            getGroupStoreKeyMap().remove(new GroupStoreKeyMapKey(existing.deviceId(),
                                                                 existing.appCookie()));
            getGroupStoreIdMap().remove(new GroupStoreIdMapKey(existing.deviceId(),
                                                               existing.id()));
            notifyDelegate(new GroupEvent(Type.GROUP_REMOVED, existing));
        }
    }

    @Override
    public void deviceInitialAuditCompleted(DeviceId deviceId,
                                            boolean completed) {
        synchronized (deviceAuditStatus) {
            if (completed) {
                log.debug("deviceInitialAuditCompleted: AUDIT "
                                  + "completed for device {}", deviceId);
                deviceAuditStatus.put(deviceId, true);
                // Execute all pending group requests
                List<StoredGroupEntry> pendingGroupRequests =
                        getPendingGroupKeyTable().values()
                        .stream()
                        .filter(g-> g.deviceId().equals(deviceId))
                        .collect(Collectors.toList());
                log.trace("deviceInitialAuditCompleted: processing "
                        + "pending group add requests for device {} and "
                        + "number of pending requests {}",
                        deviceId,
                        pendingGroupRequests.size());
                for (Group group:pendingGroupRequests) {
                    GroupDescription tmp = new DefaultGroupDescription(
                            group.deviceId(),
                            group.type(),
                            group.buckets(),
                            group.appCookie(),
                            group.appId());
                    storeGroupDescriptionInternal(tmp);
                    getPendingGroupKeyTable().
                        remove(new GroupStoreKeyMapKey(deviceId, group.appCookie()));
                }
            } else {
                if (deviceAuditStatus.get(deviceId)) {
                    log.debug("deviceInitialAuditCompleted: Clearing AUDIT "
                                      + "status for device {}", deviceId);
                    deviceAuditStatus.put(deviceId, false);
                }
            }
        }
    }

    @Override
    public boolean deviceInitialAuditStatus(DeviceId deviceId) {
        synchronized (deviceAuditStatus) {
            return (deviceAuditStatus.get(deviceId) != null)
                    ? deviceAuditStatus.get(deviceId) : false;
        }
    }

    @Override
    public void groupOperationFailed(DeviceId deviceId, GroupOperation operation) {

        StoredGroupEntry existing = getStoredGroupEntry(deviceId,
                                                        operation.groupId());

        if (existing == null) {
            log.warn("No group entry with ID {} found ", operation.groupId());
            return;
        }

        switch (operation.opType()) {
            case ADD:
                notifyDelegate(new GroupEvent(Type.GROUP_ADD_FAILED, existing));
                break;
            case MODIFY:
                notifyDelegate(new GroupEvent(Type.GROUP_UPDATE_FAILED, existing));
                break;
            case DELETE:
                notifyDelegate(new GroupEvent(Type.GROUP_REMOVE_FAILED, existing));
                break;
            default:
                log.warn("Unknown group operation type {}", operation.opType());
        }

        getGroupStoreKeyMap().remove(new GroupStoreKeyMapKey(existing.deviceId(),
                                                             existing.appCookie()));
        getGroupStoreIdMap().remove(new GroupStoreIdMapKey(existing.deviceId(),
                                                           existing.id()));
    }

    @Override
    public void addOrUpdateExtraneousGroupEntry(Group group) {
        log.trace("addOrUpdateExtraneousGroupEntry: add/update extraneous "
                + "group entry {} in device {}",
                group.id(),
                group.deviceId());
        ConcurrentMap<GroupId, Group> extraneousIdTable =
                getExtraneousGroupIdTable(group.deviceId());
        extraneousIdTable.put(group.id(), group);
        // Check the reference counter
        if (group.referenceCount() == 0) {
            log.trace("addOrUpdateExtraneousGroupEntry: Flow reference "
                    + "counter is zero and triggering remove",
                    group.id(),
                    group.deviceId());
            notifyDelegate(new GroupEvent(Type.GROUP_REMOVE_REQUESTED, group));
        }
    }

    @Override
    public void removeExtraneousGroupEntry(Group group) {
        log.trace("removeExtraneousGroupEntry: remove extraneous "
                + "group entry {} of device {} from store",
                group.id(),
                group.deviceId());
        ConcurrentMap<GroupId, Group> extraneousIdTable =
                getExtraneousGroupIdTable(group.deviceId());
        extraneousIdTable.remove(group.id());
    }

    @Override
    public Iterable<Group> getExtraneousGroups(DeviceId deviceId) {
        // flatten and make iterator unmodifiable
        return FluentIterable.from(
                getExtraneousGroupIdTable(deviceId).values());
    }

    /**
     * ClockService that generates wallclock based timestamps.
     */
    private class GroupStoreLogicalClockManager<T, U>
        implements ClockService<T, U> {

        private final AtomicLong sequenceNumber = new AtomicLong(0);

        @Override
        public Timestamp getTimestamp(T t1, U u1) {
            return new MultiValuedTimestamp<>(System.currentTimeMillis(),
                    sequenceNumber.getAndIncrement());
        }
    }

    /**
     * Map handler to receive any events when the group map is updated.
     */
    private class GroupStoreIdMapListener implements
            EventuallyConsistentMapListener<GroupStoreIdMapKey, StoredGroupEntry> {

        @Override
        public void event(EventuallyConsistentMapEvent<GroupStoreIdMapKey,
                                  StoredGroupEntry> mapEvent) {
            GroupEvent groupEvent = null;
            log.trace("GroupStoreIdMapListener: received groupid map event {}",
                      mapEvent.type());
            if (mapEvent.type() == EventuallyConsistentMapEvent.Type.PUT) {
                log.trace("GroupIdMapListener: Received PUT event");
                if (mapEvent.value().state() == Group.GroupState.ADDED) {
                    if (mapEvent.value().isGroupStateAddedFirstTime()) {
                        groupEvent = new GroupEvent(Type.GROUP_ADDED,
                                                    mapEvent.value());
                        log.trace("GroupIdMapListener: Received first time "
                                + "GROUP_ADDED state update");
                    } else {
                        groupEvent = new GroupEvent(Type.GROUP_UPDATED,
                                                    mapEvent.value());
                        log.trace("GroupIdMapListener: Received following "
                                + "GROUP_ADDED state update");
                    }
                }
            } else if (mapEvent.type() == EventuallyConsistentMapEvent.Type.REMOVE) {
                log.trace("GroupIdMapListener: Received REMOVE event");
                groupEvent = new GroupEvent(Type.GROUP_REMOVED, mapEvent.value());
            }

            if (groupEvent != null) {
                notifyDelegate(groupEvent);
            }
        }
    }
    /**
     * Message handler to receive messages from group subsystems of
     * other cluster members.
     */
    private final class ClusterGroupMsgHandler
                    implements ClusterMessageHandler {
        @Override
        public void handle(ClusterMessage message) {
            log.trace("ClusterGroupMsgHandler: received remote group message");
            if (message.subject() ==
                    GroupStoreMessageSubjects.REMOTE_GROUP_OP_REQUEST) {
                GroupStoreMessage groupOp = kryoBuilder.
                        build().deserialize(message.payload());
                log.trace("received remote group operation request");
                if (!(mastershipService.
                        getLocalRole(groupOp.deviceId()) !=
                        MastershipRole.MASTER)) {
                    log.warn("ClusterGroupMsgHandler: This node is not "
                            + "MASTER for device {}", groupOp.deviceId());
                    return;
                }
                if (groupOp.type() == GroupStoreMessage.Type.ADD) {
                    log.trace("processing remote group "
                            + "add operation request");
                    storeGroupDescriptionInternal(groupOp.groupDesc());
                } else if (groupOp.type() == GroupStoreMessage.Type.UPDATE) {
                    log.trace("processing remote group "
                            + "update operation request");
                    updateGroupDescriptionInternal(groupOp.deviceId(),
                                                   groupOp.appCookie(),
                                                   groupOp.updateType(),
                                                   groupOp.updateBuckets(),
                                                   groupOp.newAppCookie());
                } else if (groupOp.type() == GroupStoreMessage.Type.DELETE) {
                    log.trace("processing remote group "
                            + "delete operation request");
                    deleteGroupDescriptionInternal(groupOp.deviceId(),
                                                   groupOp.appCookie());
                }
            }
        }
    }

    /**
     * Flattened map key to be used to store group entries.
     */
    private class GroupStoreMapKey {
        private final DeviceId deviceId;

        public GroupStoreMapKey(DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof GroupStoreMapKey)) {
                return false;
            }
            GroupStoreMapKey that = (GroupStoreMapKey) o;
            return this.deviceId.equals(that.deviceId);
        }

        @Override
        public int hashCode() {
            int result = 17;

            result = 31 * result + Objects.hash(this.deviceId);

            return result;
        }
    }

    private class GroupStoreKeyMapKey extends GroupStoreMapKey {
        private final GroupKey appCookie;
        public GroupStoreKeyMapKey(DeviceId deviceId,
                                   GroupKey appCookie) {
            super(deviceId);
            this.appCookie = appCookie;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof GroupStoreKeyMapKey)) {
                return false;
            }
            GroupStoreKeyMapKey that = (GroupStoreKeyMapKey) o;
            return (super.equals(that) &&
                    this.appCookie.equals(that.appCookie));
        }

        @Override
        public int hashCode() {
            int result = 17;

            result = 31 * result + super.hashCode() + Objects.hash(this.appCookie);

            return result;
        }
    }

    private class GroupStoreIdMapKey extends GroupStoreMapKey {
        private final GroupId groupId;
        public GroupStoreIdMapKey(DeviceId deviceId,
                                  GroupId groupId) {
            super(deviceId);
            this.groupId = groupId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof GroupStoreIdMapKey)) {
                return false;
            }
            GroupStoreIdMapKey that = (GroupStoreIdMapKey) o;
            return (super.equals(that) &&
                    this.groupId.equals(that.groupId));
        }

        @Override
        public int hashCode() {
            int result = 17;

            result = 31 * result + super.hashCode() + Objects.hash(this.groupId);

            return result;
        }
    }
}
