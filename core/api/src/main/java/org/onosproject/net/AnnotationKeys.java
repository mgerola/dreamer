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
package org.onosproject.net;

/**
 * Collection of keys for annotation.
 * Definitions of annotation keys needs to be here to avoid scattering.
 */
public final class AnnotationKeys {

    // Prohibit instantiation
    private AnnotationKeys() {}

    /**
     * Annotation key for instance name.
     */
    public static final String NAME = "name";

    /**
     * Annotation key for instance type (e.g. host type).
     */
    public static final String TYPE = "type";

    /**
     * Annotation key for latitude (e.g. latitude of device).
     */
    public static final String LATITUDE = "latitude";

    /**
     * Annotation key for longitute (e.g. longitude of device).
     */
    public static final String LONGITUDE = "longitude";

    /**
     * Annotation key for southbound protocol.
     */
    public static final String PROTOCOL = "protocol";

    /**
     * Annotation key for durable links.
     */
    public static final String DURABLE = "durable";

    /**
     * Annotation key for latency.
     */
    public static final String LATENCY = "latency";

    /**
     * Annotation key for bandwidth.
     * The value for this key is interpreted as Mbps.
     */
    public static final String BANDWIDTH = "bandwidth";

    /**
     * Annotation key for the number of optical waves.
     */
    public static final String OPTICAL_WAVES = "optical.waves";

    /**
     * Returns the value annotated object for the specified annotation key.
     * The annotated value is expected to be String that can be parsed as double.
     * If parsing fails, the returned value will be 1.0.
     *
     * @param annotated annotated object whose annotated value is obtained
     * @param key key of annotation
     * @return double value of annotated object for the specified key
     */
    public static double getAnnotatedValue(Annotated annotated, String key) {
        double value;
        try {
            value = Double.parseDouble(annotated.annotations().value(key));
        } catch (NumberFormatException e) {
            value = 1.0;
        }
        return value;
    }
}
