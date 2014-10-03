package org.onlab.metrics;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;

import com.codahale.metrics.Counter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * This class holds the Metrics registry for ONOS.
 * All metrics (Counter, Histogram, Timer, Meter, Gauge) use a hierarchical
 * string-based naming scheme: COMPONENT.FEATURE.NAME.
 * Example: "Topology.Counters.TopologyUpdates".
 * The COMPONENT and FEATURE names have to be registered in advance before
 * a metric can be created. Example:
 * <pre>
 *   <code>
 *     private final MetricsManager.MetricsComponent COMPONENT =
 *         MetricsManager.registerComponent("Topology");
 *     private final MetricsManager.MetricsFeature FEATURE =
 *         COMPONENT.registerFeature("Counters");
 *     private final Counter counterTopologyUpdates =
 *         MetricsManager.createCounter(COMPONENT, FEATURE, "TopologyUpdates");
 *   </code>
 * </pre>
 * Gauges are slightly different because they are not created directly in
 * this class, but are allocated by the caller and passed in for registration:
 * <pre>
 *   <code>
 *     private final Gauge<Long> gauge =
 *         new {@literal Gauge<Long>}() {
 *             {@literal @}Override
 *             public Long getValue() {
 *                 return gaugeValue;
 *             }
 *         };
 *     MetricsManager.registerMetric(COMPONENT, FEATURE, GAUGE_NAME, gauge);
 *   </code>
 * </pre>
 */
@Component(immediate = true)
public final class MetricsManager implements MetricsService {

    /**
     * Registry to hold the Components defined in the system.
     */
    private ConcurrentMap<String, MetricsComponent> componentsRegistry;

    /**
     * Registry for the Metrics objects created in the system.
     */
    private final MetricRegistry metricsRegistry;

    /**
     * Default Reporter for this metrics manager.
     */
    private final CsvReporter reporter;

    public MetricsManager() {
        this.componentsRegistry = new ConcurrentHashMap<>();
        this.metricsRegistry = new MetricRegistry();

        this.reporter = CsvReporter.forRegistry(metricsRegistry)
                .formatFor(Locale.US)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MICROSECONDS)
                .build(new File("/tmp/"));

        reporter.start(10, TimeUnit.SECONDS);
    }

    @Activate
    public void activate() {
    }

    @Deactivate
    public void deactivate() {
    }

    /**
     * Registers a component.
     *
     * @param name name of the Component to register
     * @return MetricsComponent object that can be used to create Metrics.
     */
  @Override
  public MetricsComponent registerComponent(final String name) {
        MetricsComponent component = componentsRegistry.get(name);
        if (component == null) {
            final MetricsComponent createdComponent = new MetricsComponent(name);
            component = componentsRegistry.putIfAbsent(name, createdComponent);
            if (component == null) {
                component = createdComponent;
            }
        }
        return component;
    }

    /**
     * Generates a name for a Metric from its component and feature.
     *
     * @param component component the metric is defined in
     * @param feature feature the metric is defined in
     * @param metricName local name of the metric
     *
     * @return full name of the metric
     */
  private String generateName(final MetricsComponent component,
                                      final MetricsFeature feature,
                                      final String metricName) {
        return MetricRegistry.name(component.getName(),
                                   feature.getName(),
                                   metricName);
    }

    /**
     * Creates a Counter metric.
     *
     * @param component component the Counter is defined in
     * @param feature feature the Counter is defined in
     * @param metricName local name of the metric
     * @return the created Counter Meteric
     */
  @Override
  public Counter createCounter(final MetricsComponent component,
                                        final MetricsFeature feature,
                                        final String metricName) {
        final String name = generateName(component, feature, metricName);
        return metricsRegistry.counter(name);
    }

    /**
     * Creates a Histogram metric.
     *
     * @param component component the Histogram is defined in
     * @param feature feature the Histogram is defined in
     * @param metricName local name of the metric
     * @return the created Histogram Metric
     */
  @Override
  public Histogram createHistogram(final MetricsComponent component,
                                            final MetricsFeature feature,
                                            final String metricName) {
        final String name = generateName(component, feature, metricName);
        return metricsRegistry.histogram(name);
    }

    /**
     * Creates a Timer metric.
     *
     * @param component component the Timer is defined in
     * @param feature feature the Timeer is defined in
     * @param metricName local name of the metric
     * @return the created Timer Metric
     */
  @Override
  public Timer createTimer(final MetricsComponent component,
                                    final MetricsFeature feature,
                                    final String metricName) {
        final String name = generateName(component, feature, metricName);
        return metricsRegistry.timer(name);
    }

    /**
     * Creates a Meter metric.
     *
     * @param component component the Meter is defined in
     * @param feature feature the Meter is defined in
     * @param metricName local name of the metric
     * @return the created Meter Metric
     */
  @Override
  public Meter createMeter(final MetricsComponent component,
                                    final MetricsFeature feature,
                                    final String metricName) {
        final String name = generateName(component, feature, metricName);
        return metricsRegistry.meter(name);
    }

    /**
     * Registers an already created Metric.  This is used for situation where a
     * caller needs to allocate its own Metric, but still register it with the
     * system.
     *
     * @param <T> Metric type
     * @param component component the Metric is defined in
     * @param feature feature the Metric is defined in
     * @param metricName local name of the metric
     * @param metric Metric to register
     * @return the registered Metric
     */
  @Override
  public <T extends Metric> T registerMetric(
                                        final MetricsComponent component,
                                        final MetricsFeature feature,
                                        final String metricName,
                                        final T metric) {
        final String name = generateName(component, feature, metricName);
        metricsRegistry.register(name, metric);
        return metric;
    }

    /**
     * Fetches the existing Timers.
     *
     * @param filter filter to use to select Timers
     * @return a map of the Timers that match the filter, with the key as the
     *         name String to the Timer.
     */
  @Override
  public Map<String, Timer> getTimers(final MetricFilter filter) {
        return metricsRegistry.getTimers(filter);
    }

    /**
     * Fetches the existing Gauges.
     *
     * @param filter filter to use to select Gauges
     * @return a map of the Gauges that match the filter, with the key as the
     *         name String to the Gauge.
     */
  @Override
  public Map<String, Gauge> getGauges(final MetricFilter filter) {
        return metricsRegistry.getGauges(filter);
    }

    /**
     * Fetches the existing Counters.
     *
     * @param filter filter to use to select Counters
     * @return a map of the Counters that match the filter, with the key as the
     *         name String to the Counter.
     */
  @Override
  public Map<String, Counter> getCounters(final MetricFilter filter) {
        return metricsRegistry.getCounters(filter);
    }

    /**
     * Fetches the existing Meters.
     *
     * @param filter filter to use to select Meters
     * @return a map of the Meters that match the filter, with the key as the
     *         name String to the Meter.
     */
  @Override
  public Map<String, Meter> getMeters(final MetricFilter filter) {
        return metricsRegistry.getMeters(filter);
    }

    /**
     * Fetches the existing Histograms.
     *
     * @param filter filter to use to select Histograms
     * @return a map of the Histograms that match the filter, with the key as the
     *         name String to the Histogram.
     */
  @Override
  public Map<String, Histogram> getHistograms(final MetricFilter filter) {
        return metricsRegistry.getHistograms(filter);
    }

    /**
     * Removes all Metrics that match a given filter.
     *
     * @param filter filter to use to select the Metrics to remove.
     */
  @Override
  public void removeMatching(final MetricFilter filter) {
        metricsRegistry.removeMatching(filter);
    }
}
