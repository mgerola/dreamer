package org.onosproject.cli.icona;

import static com.google.common.collect.Lists.newArrayList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.icona.store.Cluster;
import org.onosproject.icona.store.EndPoint;
import org.onosproject.icona.store.IconaStoreService;
import org.onosproject.icona.store.InterLink;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Command(scope = "icona", name = "clusters", description = "Lists all clusters")
public class ClustersCommand extends AbstractShellCommand {

    private static final String FMT = "id=%s, lastSeen=%s";
    private static final DateFormat DF = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static final String INTERLINK = "srcCluster=%s, dstCluster=%s, "
            + "srcSwId=%s, srcPort=%s, dstSwId=%s, dstPort=%s";
    private static final String ENDPOINT = "Cluster=%s, SwId=%s, Port=%s";

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
            result.put("srcClusterId", interlink.srcClusterName())
                    .put("dstCusterId", interlink.dstClusterName())
                    .put("srcId", interlink.src().deviceId().toString())
                    .put("srcPort", interlink.src().port().toLong())
                    .put("srcId", interlink.dst().deviceId().toString())
                    .put("srcPort", interlink.dst().port().toLong());

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
            print(INTERLINK, interlink.srcClusterName(),
                  interlink.dstClusterName(), interlink.src().deviceId(),
                  interlink.src().port(), interlink.dst().deviceId(),
                  interlink.dst().port());
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
            result.put("cluster", endpoint.clusterName())
                    .put("srcSwId", endpoint.deviceId().toString())
                    .put("srcPort", endpoint.port().toLong());
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
            print(ENDPOINT, endpoint.clusterName(), endpoint.deviceId(),
                  endpoint.port());
        }
    }
}
