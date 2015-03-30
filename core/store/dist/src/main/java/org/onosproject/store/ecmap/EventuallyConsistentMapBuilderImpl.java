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
package org.onosproject.store.ecmap;

import org.onlab.util.KryoNamespace;
import org.onosproject.cluster.ClusterService;
import org.onosproject.cluster.NodeId;
import org.onosproject.store.cluster.messaging.ClusterCommunicationService;
import org.onosproject.store.service.ClockService;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.EventuallyConsistentMapBuilder;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Eventually consistent map builder.
 */
public class EventuallyConsistentMapBuilderImpl<K, V>
        implements EventuallyConsistentMapBuilder<K, V> {
    private final ClusterService clusterService;
    private final ClusterCommunicationService clusterCommunicator;

    private String name;
    private KryoNamespace.Builder serializerBuilder;
    private ExecutorService eventExecutor;
    private ExecutorService communicationExecutor;
    private ScheduledExecutorService backgroundExecutor;
    private ClockService<K, V> clockService;
    private BiFunction<K, V, Collection<NodeId>> peerUpdateFunction;
    private boolean tombstonesDisabled = false;
    private long antiEntropyPeriod = 5;
    private TimeUnit antiEntropyTimeUnit = TimeUnit.SECONDS;
    private boolean convergeFaster = false;

    /**
     * Creates a new eventually consistent map builder.
     *
     * @param clusterService cluster service
     * @param clusterCommunicator cluster communication service
     */
    public EventuallyConsistentMapBuilderImpl(ClusterService clusterService,
                                              ClusterCommunicationService clusterCommunicator) {
        this.clusterService = checkNotNull(clusterService);
        this.clusterCommunicator = checkNotNull(clusterCommunicator);
    }

    @Override
    public EventuallyConsistentMapBuilder withName(String name) {
        this.name = checkNotNull(name);
        return this;
    }

    @Override
    public EventuallyConsistentMapBuilder withSerializer(
            KryoNamespace.Builder serializerBuilder) {
        this.serializerBuilder = checkNotNull(serializerBuilder);
        return this;
    }

    @Override
    public EventuallyConsistentMapBuilder withClockService(
            ClockService<K, V> clockService) {
        this.clockService = checkNotNull(clockService);
        return this;
    }

    @Override
    public EventuallyConsistentMapBuilder withEventExecutor(ExecutorService executor) {
        this.eventExecutor = checkNotNull(executor);
        return this;
    }

    @Override
    public EventuallyConsistentMapBuilder<K, V> withCommunicationExecutor(
            ExecutorService executor) {
        communicationExecutor = checkNotNull(executor);
        return this;
    }

    @Override
    public EventuallyConsistentMapBuilder withBackgroundExecutor(ScheduledExecutorService executor) {
        this.backgroundExecutor = checkNotNull(executor);
        return this;
    }

    @Override
    public EventuallyConsistentMapBuilder withPeerUpdateFunction(
            BiFunction<K, V, Collection<NodeId>> peerUpdateFunction) {
        this.peerUpdateFunction = checkNotNull(peerUpdateFunction);
        return this;
    }

    @Override
    public EventuallyConsistentMapBuilder<K, V> withTombstonesDisabled() {
        tombstonesDisabled = true;
        return this;
    }

    @Override
    public EventuallyConsistentMapBuilder<K, V> withAntiEntropyPeriod(long period, TimeUnit unit) {
        checkArgument(period > 0, "anti-entropy period must be greater than 0");
        antiEntropyPeriod = period;
        antiEntropyTimeUnit = checkNotNull(unit);
        return this;
    }

    @Override
    public EventuallyConsistentMapBuilder<K, V> withFasterConvergence() {
        convergeFaster = true;
        return this;
    }

    @Override
    public EventuallyConsistentMap<K, V> build() {
        checkNotNull(name, "name is a mandatory parameter");
        checkNotNull(serializerBuilder, "serializerBuilder is a mandatory parameter");
        checkNotNull(clockService, "clockService is a mandatory parameter");

        return new EventuallyConsistentMapImpl<>(name,
                                                 clusterService,
                                                 clusterCommunicator,
                                                 serializerBuilder,
                                                 clockService,
                                                 peerUpdateFunction,
                                                 eventExecutor,
                                                 communicationExecutor,
                                                 backgroundExecutor,
                                                 tombstonesDisabled,
                                                 antiEntropyPeriod,
                                                 antiEntropyTimeUnit,
                                                 convergeFaster);
    }
}
