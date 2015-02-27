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
package org.onosproject.net.group.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.ApplicationId;
import org.onosproject.event.AbstractListenerRegistry;
import org.onosproject.event.EventDeliveryService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.group.Group;
import org.onosproject.net.group.GroupBuckets;
import org.onosproject.net.group.GroupDescription;
import org.onosproject.net.group.GroupEvent;
import org.onosproject.net.group.GroupKey;
import org.onosproject.net.group.GroupListener;
import org.onosproject.net.group.GroupOperation;
import org.onosproject.net.group.GroupOperations;
import org.onosproject.net.group.GroupProvider;
import org.onosproject.net.group.GroupProviderRegistry;
import org.onosproject.net.group.GroupProviderService;
import org.onosproject.net.group.GroupService;
import org.onosproject.net.group.GroupStore;
import org.onosproject.net.group.GroupStore.UpdateType;
import org.onosproject.net.group.GroupStoreDelegate;
import org.onosproject.net.provider.AbstractProviderRegistry;
import org.onosproject.net.provider.AbstractProviderService;
import org.slf4j.Logger;

import com.google.common.collect.Sets;

/**
 * Provides implementation of the group service APIs.
 */
@Component(immediate = true)
@Service
public class GroupManager
        extends AbstractProviderRegistry<GroupProvider, GroupProviderService>
        implements GroupService, GroupProviderRegistry {

    private final Logger log = getLogger(getClass());

    private final AbstractListenerRegistry<GroupEvent, GroupListener>
                listenerRegistry = new AbstractListenerRegistry<>();
    private final GroupStoreDelegate delegate = new InternalGroupStoreDelegate();
    private final DeviceListener deviceListener = new InternalDeviceListener();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected GroupStore store;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected EventDeliveryService eventDispatcher;

    @Activate
    public void activate() {
        store.setDelegate(delegate);
        eventDispatcher.addSink(GroupEvent.class, listenerRegistry);
        deviceService.addListener(deviceListener);
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        store.unsetDelegate(delegate);
        eventDispatcher.removeSink(GroupEvent.class);
        log.info("Stopped");
    }

    /**
     * Create a group in the specified device with the provided parameters.
     *
     * @param groupDesc group creation parameters
     *
     */
    @Override
    public void addGroup(GroupDescription groupDesc) {
        store.storeGroupDescription(groupDesc);
    }

    /**
     * Return a group object associated to an application cookie.
     *
     * NOTE1: The presence of group object in the system does not
     * guarantee that the "group" is actually created in device.
     * GROUP_ADDED notification would confirm the creation of
     * this group in data plane.
     *
     * @param deviceId device identifier
     * @param appCookie application cookie to be used for lookup
     * @return group associated with the application cookie or
     *               NULL if Group is not found for the provided cookie
     */
    @Override
    public Group getGroup(DeviceId deviceId, GroupKey appCookie) {
        return store.getGroup(deviceId, appCookie);
    }

    /**
     * Append buckets to existing group. The caller can optionally
     * associate a new cookie during this updation. GROUP_UPDATED or
     * GROUP_UPDATE_FAILED notifications would be provided along with
     * cookie depending on the result of the operation on the device.
     *
     * @param deviceId device identifier
     * @param oldCookie cookie to be used to retrieve the existing group
     * @param buckets immutable list of group bucket to be added
     * @param newCookie immutable cookie to be used post update operation
     * @param appId Application Id
     */
    @Override
    public void addBucketsToGroup(DeviceId deviceId,
                           GroupKey oldCookie,
                           GroupBuckets buckets,
                           GroupKey newCookie,
                           ApplicationId appId) {
        store.updateGroupDescription(deviceId,
                                     oldCookie,
                                     UpdateType.ADD,
                                     buckets,
                                     newCookie);
    }

    /**
     * Remove buckets from existing group. The caller can optionally
     * associate a new cookie during this updation. GROUP_UPDATED or
     * GROUP_UPDATE_FAILED notifications would be provided along with
     * cookie depending on the result of the operation on the device.
     *
     * @param deviceId device identifier
     * @param oldCookie cookie to be used to retrieve the existing group
     * @param buckets immutable list of group bucket to be removed
     * @param newCookie immutable cookie to be used post update operation
     * @param appId Application Id
     */
    @Override
    public void removeBucketsFromGroup(DeviceId deviceId,
                                GroupKey oldCookie,
                                GroupBuckets buckets,
                                GroupKey newCookie,
                                ApplicationId appId) {
        store.updateGroupDescription(deviceId,
                                     oldCookie,
                                     UpdateType.REMOVE,
                                     buckets,
                                     newCookie);
    }

    /**
     * Delete a group associated to an application cookie.
     * GROUP_DELETED or GROUP_DELETE_FAILED notifications would be
     * provided along with cookie depending on the result of the
     * operation on the device.
     *
     * @param deviceId device identifier
     * @param appCookie application cookie to be used for lookup
     * @param appId Application Id
     */
    @Override
    public void removeGroup(DeviceId deviceId,
                            GroupKey appCookie,
                            ApplicationId appId) {
        store.deleteGroupDescription(deviceId, appCookie);
    }

    /**
     * Retrieve all groups created by an application in the specified device
     * as seen by current controller instance.
     *
     * @param deviceId device identifier
     * @param appId application id
     * @return collection of immutable group objects created by the application
     */
    @Override
    public Iterable<Group> getGroups(DeviceId deviceId,
                                     ApplicationId appId) {
        return store.getGroups(deviceId);
    }

    /**
     * Adds the specified group listener.
     *
     * @param listener group listener
     */
    @Override
    public void addListener(GroupListener listener) {
        listenerRegistry.addListener(listener);
    }

    /**
     * Removes the specified group listener.
     *
     * @param listener group listener
     */
    @Override
    public void removeListener(GroupListener listener) {
        listenerRegistry.removeListener(listener);
    }

    @Override
    protected GroupProviderService createProviderService(GroupProvider provider) {
        return new InternalGroupProviderService(provider);
    }

    private class InternalGroupStoreDelegate implements GroupStoreDelegate {
        @Override
        public void notify(GroupEvent event) {
            final Group group = event.subject();
            GroupProvider groupProvider =
                    getProvider(group.deviceId());
            GroupOperations groupOps = null;
            switch (event.type()) {
            case GROUP_ADD_REQUESTED:
                log.debug("GROUP_ADD_REQUESTED for Group {} on device {}",
                          group.id(), group.deviceId());
                GroupOperation groupAddOp = GroupOperation.
                        createAddGroupOperation(group.id(),
                                                group.type(),
                                                group.buckets());
                groupOps = new GroupOperations(
                                          Arrays.asList(groupAddOp));
                groupProvider.performGroupOperation(group.deviceId(), groupOps);
                break;

            case GROUP_UPDATE_REQUESTED:
                log.debug("GROUP_UPDATE_REQUESTED for Group {} on device {}",
                          group.id(), group.deviceId());
                GroupOperation groupModifyOp = GroupOperation.
                        createModifyGroupOperation(group.id(),
                                                group.type(),
                                                group.buckets());
                groupOps = new GroupOperations(
                                   Arrays.asList(groupModifyOp));
                groupProvider.performGroupOperation(group.deviceId(), groupOps);
                break;

            case GROUP_REMOVE_REQUESTED:
                log.debug("GROUP_REMOVE_REQUESTED for Group {} on device {}",
                          group.id(), group.deviceId());
                GroupOperation groupDeleteOp = GroupOperation.
                        createDeleteGroupOperation(group.id(),
                                                group.type());
                groupOps = new GroupOperations(
                                   Arrays.asList(groupDeleteOp));
                groupProvider.performGroupOperation(group.deviceId(), groupOps);
                break;

            case GROUP_ADDED:
            case GROUP_UPDATED:
            case GROUP_REMOVED:
            case GROUP_ADD_FAILED:
            case GROUP_UPDATE_FAILED:
            case GROUP_REMOVE_FAILED:
                eventDispatcher.post(event);
                break;

            default:
                break;
            }
        }
    }

    private class InternalGroupProviderService
            extends AbstractProviderService<GroupProvider>
            implements GroupProviderService {

        protected InternalGroupProviderService(GroupProvider provider) {
            super(provider);
        }

        @Override
        public void groupOperationFailed(DeviceId deviceId,
                                         GroupOperation operation) {
            store.groupOperationFailed(deviceId, operation);
        }

        private void groupMissing(Group group) {
            checkValidity();
            GroupProvider gp = getProvider(group.deviceId());
            switch (group.state()) {
                case PENDING_DELETE:
                    log.debug("Group {} delete confirmation from device {}",
                              group, group.deviceId());
                    store.removeGroupEntry(group);
                    break;
                case ADDED:
                case PENDING_ADD:
                    log.debug("Group {} is in store but not on device {}",
                              group, group.deviceId());
                    GroupOperation groupAddOp = GroupOperation.
                                    createAddGroupOperation(group.id(),
                                                            group.type(),
                                                            group.buckets());
                    GroupOperations groupOps = new GroupOperations(
                                              Arrays.asList(groupAddOp));
                    gp.performGroupOperation(group.deviceId(), groupOps);
                    break;
                default:
                    log.debug("Group {} has not been installed.", group);
                    break;
            }
        }


        private void extraneousGroup(Group group) {
            log.debug("Group {} is on device {} but not in store.",
                      group, group.deviceId());
            checkValidity();
            store.addOrUpdateExtraneousGroupEntry(group);
        }

        private void groupAdded(Group group) {
            checkValidity();

            log.trace("Group {} Added or Updated in device {}",
                      group, group.deviceId());
            store.addOrUpdateGroupEntry(group);
        }

        @Override
        public void pushGroupMetrics(DeviceId deviceId,
                                     Collection<Group> groupEntries) {
            log.trace("Received group metrics from device {}",
                    deviceId);
            boolean deviceInitialAuditStatus =
                    store.deviceInitialAuditStatus(deviceId);
            Set<Group> southboundGroupEntries =
                    Sets.newHashSet(groupEntries);
            Set<Group> storedGroupEntries =
                    Sets.newHashSet(store.getGroups(deviceId));
            Set<Group> extraneousStoredEntries =
                    Sets.newHashSet(store.getExtraneousGroups(deviceId));

            log.trace("Displaying all southboundGroupEntries for device {}", deviceId);
            for (Iterator<Group> it = southboundGroupEntries.iterator(); it.hasNext();) {
                Group group = it.next();
                log.trace("Group {} in device {}", group, deviceId);
            }

            log.trace("Displaying all stored group entries for device {}", deviceId);
            for (Iterator<Group> it = storedGroupEntries.iterator(); it.hasNext();) {
                Group group = it.next();
                log.trace("Stored Group {} for device {}", group, deviceId);
            }

            for (Iterator<Group> it = southboundGroupEntries.iterator(); it.hasNext();) {
                Group group = it.next();
                if (storedGroupEntries.remove(group)) {
                    // we both have the group, let's update some info then.
                    log.trace("Group AUDIT: group {} exists "
                            + "in both planes for device {}",
                            group.id(), deviceId);
                    groupAdded(group);
                    it.remove();
                }
            }
            for (Group group : southboundGroupEntries) {
                // there are groups in the switch that aren't in the store
                log.trace("Group AUDIT: extraneous group {} exists "
                        + "in data plane for device {}",
                        group.id(), deviceId);
                extraneousStoredEntries.remove(group);
                extraneousGroup(group);
            }
            for (Group group : storedGroupEntries) {
                // there are groups in the store that aren't in the switch
                log.trace("Group AUDIT: group {} missing "
                        + "in data plane for device {}",
                        group.id(), deviceId);
                groupMissing(group);
            }
            for (Group group : extraneousStoredEntries) {
                // there are groups in the extraneous store that
                // aren't in the switch
                log.trace("Group AUDIT: clearing extransoeus group {} "
                        + "from store for device {}",
                        group.id(), deviceId);
                store.removeExtraneousGroupEntry(group);
            }

            if (!deviceInitialAuditStatus) {
                log.debug("Group AUDIT: Setting device {} initial "
                        + "AUDIT completed", deviceId);
                store.deviceInitialAuditCompleted(deviceId, true);
            }
        }
    }

    private class InternalDeviceListener implements DeviceListener {

        @Override
        public void event(DeviceEvent event) {
            switch (event.type()) {
            case DEVICE_REMOVED:
                log.debug("Clearing device {} initial "
                        + "AUDIT completed status as device is going down",
                        event.subject().id());
                store.deviceInitialAuditCompleted(event.subject().id(), false);
                break;

            default:
                break;
            }
        }
    }
}
