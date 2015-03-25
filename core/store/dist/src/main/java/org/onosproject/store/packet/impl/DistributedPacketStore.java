/*
 * Copyright 2014 Open Networking Laboratory
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
package org.onosproject.store.packet.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.KryoNamespace;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.NodeId;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketEvent;
import org.onosproject.net.packet.PacketEvent.Type;
import org.onosproject.net.packet.PacketStore;
import org.onosproject.net.packet.PacketStoreDelegate;
import org.onosproject.store.AbstractStore;
import org.onosproject.store.cluster.messaging.ClusterCommunicationService;
import org.onosproject.store.cluster.messaging.ClusterMessage;
import org.onosproject.store.cluster.messaging.ClusterMessageHandler;
import org.onosproject.store.cluster.messaging.MessageSubject;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.serializers.KryoSerializer;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.groupedThreads;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Distributed packet store implementation allowing packets to be sent to
 * remote instances.
 */
@Component(immediate = true)
@Service
public class DistributedPacketStore
        extends AbstractStore<PacketEvent, PacketStoreDelegate>
        implements PacketStore {

    private final Logger log = getLogger(getClass());

    // TODO: make this configurable.
    private static final int MESSAGE_HANDLER_THREAD_POOL_SIZE = 4;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterService clusterService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ClusterCommunicationService communicationService;

    private static final MessageSubject PACKET_OUT_SUBJECT =
            new MessageSubject("packet-out");

    private static final KryoSerializer SERIALIZER = new KryoSerializer() {
        @Override
        protected void setupKryoPool() {
            serializerPool = KryoNamespace.newBuilder()
                    .register(KryoNamespaces.API)
                    .nextId(KryoNamespaces.BEGIN_USER_CUSTOM_ID)
                    .build();
        }
    };

    private ExecutorService messageHandlingExecutor;

    @Activate
    public void activate() {
        messageHandlingExecutor =  Executors.newFixedThreadPool(
                MESSAGE_HANDLER_THREAD_POOL_SIZE,
                groupedThreads("onos/store/packet", "message-handlers"));

        communicationService.addSubscriber(PACKET_OUT_SUBJECT,
                                           new InternalClusterMessageHandler(),
                                           messageHandlingExecutor);

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        communicationService.removeSubscriber(PACKET_OUT_SUBJECT);
        messageHandlingExecutor.shutdown();
        log.info("Stopped");
    }

    @Override
    public void emit(OutboundPacket packet) {
        NodeId myId = clusterService.getLocalNode().id();
        NodeId master = mastershipService.getMasterFor(packet.sendThrough());

        if (master == null) {
            return;
        }

        if (myId.equals(master)) {
            notifyDelegate(new PacketEvent(Type.EMIT, packet));
            return;
        }

        // TODO check unicast return value
        communicationService.unicast(new ClusterMessage(myId, PACKET_OUT_SUBJECT,
                                                        SERIALIZER.encode(packet)),
                                     master);
        // error log: log.warn("Failed to send packet-out to {}", master);
    }

    /**
     * Handles incoming cluster messages.
     */
    private class InternalClusterMessageHandler implements ClusterMessageHandler {
        @Override
        public void handle(ClusterMessage message) {
            if (!message.subject().equals(PACKET_OUT_SUBJECT)) {
                log.warn("Received message with wrong subject: {}", message);
            }

            OutboundPacket packet = SERIALIZER.decode(message.payload());
            notifyDelegate(new PacketEvent(Type.EMIT, packet));
        }
    }

}
