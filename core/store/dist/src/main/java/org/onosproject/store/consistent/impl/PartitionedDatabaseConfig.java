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

package org.onosproject.store.consistent.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Partitioned database configuration.
 */
public class PartitionedDatabaseConfig {
    private final Map<String, DatabaseConfig> partitions = new HashMap<>();

    /**
     * Returns the configuration for all partitions.
     * @return partition map to configuartion mapping.
     */
    public Map<String, DatabaseConfig> partitions() {
        return Collections.unmodifiableMap(partitions);
    }

    /**
     * Adds the specified partition name and configuration.
     * @param name partition name.
     * @param config partition config
     * @return this instance
     */
    public PartitionedDatabaseConfig addPartition(String name, DatabaseConfig config) {
        partitions.put(name, config);
        return this;
    }
}
