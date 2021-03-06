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
package org.onosproject.net.driver;

import com.google.common.collect.ImmutableSet;
import org.onosproject.net.MutableAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Default implementation of driver data descriptor.
 */
public class DefaultDriverData implements DriverData {

    private final Driver type;
    private final Map<String, String> properties;

    /**
     * Creates new driver data.
     *
     * @param type parent driver type
     */
    public DefaultDriverData(Driver type) {
        this.type = type;
        this.properties = new HashMap<>();
    }

    @Override
    public Driver type() {
        return type;
    }

    @Override
    public <T extends Behaviour> T behaviour(Class<T> behaviourClass) {
        return type.createBehaviour(this, behaviourClass, false);
    }

    @Override
    public MutableAnnotations set(String key, String value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public MutableAnnotations clear(String... keys) {
        if (keys.length == 0) {
            properties.clear();
        } else {
            for (String key : keys) {
                properties.remove(key);
            }
        }
        return this;
    }

    @Override
    public Set<String> keys() {
        return ImmutableSet.copyOf(properties.keySet());
    }

    @Override
    public String value(String key) {
        return properties.get(key);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("type", type)
                .add("properties", properties)
                .toString();
    }

}
