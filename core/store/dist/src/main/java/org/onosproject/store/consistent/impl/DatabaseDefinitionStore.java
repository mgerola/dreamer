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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.onosproject.cluster.DefaultControllerNode;
import org.onosproject.cluster.NodeId;
import org.onlab.packet.IpAddress;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

/**
 * Allows for reading and writing partitioned database definition as a JSON file.
 */
public class DatabaseDefinitionStore {

    private final Logger log = getLogger(getClass());

    private final File definitionfile;

    /**
     * Creates a reader/writer of the database definition file.
     *
     * @param filePath location of the definition file
     */
    public DatabaseDefinitionStore(String filePath) {
        definitionfile = new File(filePath);
    }

    /**
     * Creates a reader/writer of the database definition file.
     *
     * @param filePath location of the definition file
     */
    public DatabaseDefinitionStore(File filePath) {
        definitionfile = checkNotNull(filePath);
    }

    /**
     * Returns the Map from database partition name to set of initial active member nodes.
     *
     * @return Map from partition name to set of active member nodes
     * @throws IOException when I/O exception of some sort has occurred.
     */
    public Map<String, Set<DefaultControllerNode>> read() throws IOException {

        final Map<String, Set<DefaultControllerNode>> partitions = Maps.newHashMap();

        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode tabletNodes = (ObjectNode) mapper.readTree(definitionfile);
        final Iterator<Entry<String, JsonNode>> fields = tabletNodes.fields();
        while (fields.hasNext()) {
            final Entry<String, JsonNode> next = fields.next();
            final Set<DefaultControllerNode> nodes = new HashSet<>();
            final Iterator<JsonNode> elements = next.getValue().elements();
            while (elements.hasNext()) {
                ObjectNode nodeDef = (ObjectNode) elements.next();
                nodes.add(new DefaultControllerNode(new NodeId(nodeDef.get("id").asText()),
                                                    IpAddress.valueOf(nodeDef.get("ip").asText()),
                                                    nodeDef.get("tcpPort").asInt(DatabaseManager.COPYCAT_TCP_PORT)));
            }

            partitions.put(next.getKey(), nodes);
        }
        return partitions;
    }

    /**
     * Updates the Map from database partition name to set of member nodes.
     *
     * @param partitionName name of the database partition to update
     * @param nodes set of initial member nodes
     * @throws IOException when I/O exception of some sort has occurred.
     */
    public void write(String partitionName, Set<DefaultControllerNode> nodes) throws IOException {
        checkNotNull(partitionName);
        checkArgument(partitionName.isEmpty(), "Partition name cannot be empty");

        // load current
        Map<String, Set<DefaultControllerNode>> config;
        try {
            config = read();
        } catch (IOException e) {
            log.info("Reading partition config failed, assuming empty definition.");
            config = new HashMap<>();
        }
        // update with specified
        config.put(partitionName, nodes);

        // write back to file
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode partitionNodes = mapper.createObjectNode();
        for (Entry<String, Set<DefaultControllerNode>> tablet : config.entrySet()) {
            ArrayNode nodeDefs = mapper.createArrayNode();
            partitionNodes.set(tablet.getKey(), nodeDefs);

            for (DefaultControllerNode node : tablet.getValue()) {
                ObjectNode nodeDef = mapper.createObjectNode();
                nodeDef.put("id", node.id().toString())
                       .put("ip", node.ip().toString())
                       .put("tcpPort", node.tcpPort());
                nodeDefs.add(nodeDef);
            }
        }
        mapper.writeTree(new JsonFactory().createGenerator(definitionfile, JsonEncoding.UTF8),
                         partitionNodes);
    }
}
