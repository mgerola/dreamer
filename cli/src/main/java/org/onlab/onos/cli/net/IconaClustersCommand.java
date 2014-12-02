package org.onlab.onos.cli.net;

import static com.google.common.collect.Lists.newArrayList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.karaf.shell.commands.Command;
import org.onlab.onos.cli.AbstractShellCommand;
import org.onlab.onos.icona.store.Cluster;
import org.onlab.onos.icona.store.EndPoint;
import org.onlab.onos.icona.store.IconaStoreService;
import org.onlab.onos.icona.store.InterLink;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Command(scope = "icona", name = "clusters", description = "Lists all clusters")
public class IconaClustersCommand extends AbstractShellCommand {

    private static final String FMT = "id=%s, lastSeen=%s";
    private static final DateFormat DF = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static final String INTERLINK = "srcCluster=%s, dstCluster=%s, "
            + "srcSwId=%s, srcPort=%s, dstSwId=%s, dstPort=%s";

    @Override
    protected void execute() {
        IconaStoreService service = get(IconaStoreService.class);

        if (outputJson()) {
            print("%s", json(service, getSortedClusters(service)));
        } else {
            for (Cluster cluster : getSortedClusters(service)) {
                printCluster(cluster);
            }
        }
    }

    /**
     * Returns JSON node representing the specified devices.
     *
     * @param service device service
     * @param devices collection of devices
     * @return JSON node
     */
    public static JsonNode json(IconaStoreService service,
                                Iterable<Cluster> clusters) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode result = mapper.createArrayNode();
        for (Cluster cluster : clusters) {
            result.add(json(service, mapper, cluster));
        }
        return result;
    }

    /**
     * Returns JSON node representing the specified device.
     *
     * @param service device service
     * @param mapper object mapper
     * @param device infrastructure device
     * @return JSON node
     */
    protected static ObjectNode json(IconaStoreService service,
                                     ObjectMapper mapper, Cluster cluster) {
        ObjectNode result = mapper.createObjectNode();
        if (cluster != null) {
            result.put("id", cluster.getClusterName())
                    .put("lastSeen", DF.format(cluster.getLastSeen()));
            // .put("ILs")
            // .put("EPs")
            // .set("annotations", annotations(mapper, device.annotations()));
        }
        return result;
    }

    /**
     * Returns the list of devices sorted using the device ID URIs.
     *
     * @param service device service
     * @return sorted device list
     */
    public static List<Cluster> getSortedClusters(IconaStoreService service) {
        List<Cluster> clusters = newArrayList(service.getClusters());
        // Collections.sort(clusters, Comparators.ELEMENT_COMPARATOR);
        return clusters;
    }

    /**
     * Prints information about the specified device.
     *
     * @param service device service
     * @param device infrastructure device
     */
    public void printCluster(Cluster cluster) {
        if (cluster != null) {
            print(FMT, cluster.getClusterName(),
                  DF.format(cluster.getLastSeen()));
        }
    }

    /**
     * Returns JSON node representing the specified devices.
     *
     * @param service device service
     * @param devices collection of devices
     * @return JSON node
     */
    protected static JsonNode jsonIL(IconaStoreService service,
                                     Iterable<InterLink> interlinks) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode result = mapper.createArrayNode();
        for (InterLink interlink : interlinks) {
            result.add(jsonIL(service, mapper, interlink));
        }
        return result;
    }

    /**
     * Returns JSON node representing the specified device.
     *
     * @param service device service
     * @param mapper object mapper
     * @param device infrastructure device
     * @return JSON node
     */
    public static ObjectNode jsonIL(IconaStoreService service,
                                    ObjectMapper mapper, InterLink interlink) {
        ObjectNode result = mapper.createObjectNode();
        if (interlink != null) {
            result.put("srcClusterId", interlink.getSrcClusterName())
                    .put("dstCusterId", interlink.getDstClusterName())
                    .put("srcId", interlink.getSrcId().toString())
                    .put("srcPort", interlink.getSrcPort().toLong())
                    .put("srcId", interlink.getDstId().toString())
                    .put("srcPort", interlink.getDstPort().toLong());

        }
        return result;
    }

    /**
     * Returns the list of devices sorted using the device ID URIs.
     *
     * @param service device service
     * @return sorted device list
     */
    public static List<InterLink> getSortedInterLinks(IconaStoreService service) {
        List<InterLink> interlinks = newArrayList(service.getInterLinks());
        // Collections.sort(interlinks, Comparators.ELEMENT_COMPARATOR);
        return interlinks;
    }

    /**
     * Prints information about the specified device.
     *
     * @param service device service
     * @param device infrastructure device
     */
    public void printInterLink(InterLink interlink) {
        if (interlink != null) {
            print(INTERLINK, interlink.getSrcClusterName(),
                  interlink.getDstClusterName(), interlink.getSrcId(),
                  interlink.getSrcPort(), interlink.getDstId(),
                  interlink.getDstPort());
        }
    }

    /**
     * Returns JSON node representing the specified devices.
     *
     * @param service device service
     * @param devices collection of devices
     * @return JSON node
     */
    public static JsonNode jsonEP(IconaStoreService service,
                                  Iterable<EndPoint> endpoints) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode result = mapper.createArrayNode();
        for (EndPoint endpoint : endpoints) {
            result.add(jsonEP(service, mapper, endpoint));
        }
        return result;
    }

    /**
     * Returns JSON node representing the specified device.
     *
     * @param service device service
     * @param mapper object mapper
     * @param device infrastructure device
     * @return JSON node
     */
    public static ObjectNode jsonEP(IconaStoreService service,
                                    ObjectMapper mapper, EndPoint endpoint) {
        ObjectNode result = mapper.createObjectNode();
        if (endpoint != null) {
            result.put("cluster", endpoint.getClusterName())
                    .put("srcSwId", endpoint.getId().toString())
                    .put("srcPort", endpoint.getPort().toLong());
        }
        return result;
    }

    /**
     * Returns the list of devices sorted using the device ID URIs.
     *
     * @param service device service
     * @return sorted device list
     */
    public static List<EndPoint> getSortedEndPoint(IconaStoreService service) {
        List<EndPoint> endpoints = newArrayList(service.getEndPoints());
        // Collections.sort(endpoints, Comparators.ELEMENT_COMPARATOR);
        return endpoints;
    }

    /**
     * Prints information about the specified device.
     *
     * @param service device service
     * @param device infrastructure device
     */
    protected void printEndPoint(EndPoint endpoint) {
        if (endpoint != null) {
            print(FMT, endpoint.getClusterName(), endpoint.getId(),
                  endpoint.getPort());
        }
    }
}
