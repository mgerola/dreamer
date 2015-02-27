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
package org.onosproject.store.hz;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.io.FileNotFoundException;
import java.util.UUID;

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.test.TestHazelcastInstanceFactory;

/**
 * Dummy StoreManager to use specified Hazelcast instance.
 */
public class TestStoreManager extends StoreManager {

    private TestHazelcastInstanceFactory factory;

    /**
     * Gets the Hazelcast Config for testing.
     *
     * @return Hazelcast Configuration for testing
     */
    public static Config getTestConfig() {
        Config config;
        try {
            config = new FileSystemXmlConfig(HAZELCAST_XML_FILE);
        } catch (FileNotFoundException e) {
            // falling back to default
            config = new Config();
        }
        // avoid accidentally joining other cluster
        config.getGroupConfig().setName(UUID.randomUUID().toString());
        // quickly form single node cluster
        config.getNetworkConfig().getJoin()
            .getTcpIpConfig()
            .setEnabled(true).setConnectionTimeoutSeconds(0);
        config.getNetworkConfig().getJoin()
            .getMulticastConfig()
            .setEnabled(false);
        return config;
    }

    /**
     * Creates an instance of dummy Hazelcast instance for testing.
     *
     * @return HazelcastInstance
     */
    public HazelcastInstance initSingleInstance() {
        return initInstances(1)[0];
    }

    /**
     * Creates some instances of dummy Hazelcast instances for testing.
     *
     * @param count number of instances to create
     * @return array of HazelcastInstances
     */
    public HazelcastInstance[] initInstances(int count) {
        checkArgument(count > 0, "Cluster size must be > 0");
        factory = new TestHazelcastInstanceFactory(count);
        return factory.newInstances(getTestConfig());
    }

    /**
     * Sets the Hazelast instance to return on #getHazelcastInstance().
     *
     * @param instance Hazelast instance to return on #getHazelcastInstance()
     */
    public void setHazelcastInstance(HazelcastInstance instance) {
        this.instance = instance;
    }

    @Override
    public void activate() {
        // Hazelcast setup removed from original code.
        checkState(this.instance != null, "HazelcastInstance needs to be set");
    }

    @Override
    public void deactivate() {
        // Hazelcast instance shutdown removed from original code.
        factory.shutdownAll();
    }
}
