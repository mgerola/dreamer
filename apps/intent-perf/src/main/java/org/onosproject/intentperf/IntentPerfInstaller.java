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
package org.onosproject.intentperf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.onlab.util.Counter;
import org.onosproject.cluster.ClusterService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Device;
import org.onosproject.net.MastershipRole;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentEvent;
import org.onosproject.net.intent.IntentListener;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.onosproject.net.intent.PointToPointIntent;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.apache.felix.scr.annotations.ReferenceCardinality.MANDATORY_UNARY;
import static org.onlab.util.Tools.delay;
import static org.onlab.util.Tools.groupedThreads;
import static org.onosproject.net.intent.IntentEvent.Type.INSTALLED;
import static org.onosproject.net.intent.IntentEvent.Type.WITHDRAWN;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Application to set up demos.
 */
@Component(immediate = true)
public class IntentPerfInstaller {

    //FIXME make this configurable
    private static final int NUM_WORKERS = 1;
    private static final int NUM_KEYS = 10_000;

    public static final int START_DELAY = 5_000; // ms
    private static final int REPORT_PERIOD = 5_000; //ms

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = MANDATORY_UNARY)
    protected IntentService intentService;

    @Reference(cardinality = MANDATORY_UNARY)
    protected ClusterService clusterService;

    @Reference(cardinality = MANDATORY_UNARY)
    protected DeviceService deviceService;

    private ExecutorService workers;
    private ApplicationId appId;
    private Listener listener;
    private boolean stopped;

    private Timer reportTimer;

    private int lastKey = 0;

    @Activate
    public void activate() {
        String nodeId = clusterService.getLocalNode().ip().toString();
        appId = coreService.registerApplication("org.onosproject.intentperf." + nodeId);

        reportTimer = new Timer("onos-intent-perf-reporter");
        workers = Executors.newFixedThreadPool(NUM_WORKERS, groupedThreads("onos/intent-perf", "worker-%d"));
        log.info("Started with Application ID {}", appId.id());

        // Schedule delayed start
        reportTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                start();
            }
        }, START_DELAY);
    }

    @Deactivate
    public void deactivate() {
        stop();
        log.info("Stopped");
    }

    public void start() {
        // perhaps we want to prime before listening...
        // we will need to discard the first few results for priming and warmup
        listener = new Listener();
        intentService.addListener(listener);

        // Schedule reporter task on report period boundary
        reportTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                listener.report();
            }
        }, REPORT_PERIOD - currentTimeMillis() % REPORT_PERIOD, REPORT_PERIOD);

        // Submit workers
        stopped = false;
        Set<Device> devices = new HashSet<>();
        for (int i = 0; i < NUM_WORKERS; i++) {
            workers.submit(new Submitter(createIntents(NUM_KEYS, 2, lastKey, devices)));
        }
    }

    public void stop() {
        if (listener != null) {
            reportTimer.cancel();
            intentService.removeListener(listener);
            listener = null;
            reportTimer = null;
        }
        stopped = true;
        try {
            workers.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Failed to stop worker.");
        }
    }

    private Iterable<Intent> subset(Set<Intent> intents) {
        List<Intent> subset = Lists.newArrayList(intents);
        Collections.shuffle(subset);
        return subset.subList(0, subset.size() / 2);
    }

    /**
     * Creates a specified number of intents for testing purposes.
     *
     * @param numberOfKeys number of intents
     * @param pathLength   path depth
     * @param firstKey     first key to attempt
     * @param devices      set of previously utilized devices  @return set of intents
     */
    private Set<Intent> createIntents(int numberOfKeys, int pathLength,
                                      int firstKey, Set<Device> devices) {
        Iterator<Device> deviceItr = deviceService.getAvailableDevices().iterator();
        Set<Intent> result = new HashSet<>();

        Device ingressDevice = null;
        while (deviceItr.hasNext()) {
            Device device = deviceItr.next();
            if (deviceService.getRole(device.id()) == MastershipRole.MASTER &&
                    !devices.contains(device)) {
                ingressDevice = device;
                devices.add(device);
                break;
            }
        }
        checkState(ingressDevice != null, "There are no local devices");

        for (int count = 0, k = firstKey; count < numberOfKeys; k++) {
            Key key = Key.of(k, appId);
            if (!intentService.isLocal(key)) {
                // Bail if the key is not local
                continue;
            }

            //FIXME
            TrafficSelector selector = DefaultTrafficSelector.builder().build();
            TrafficTreatment treatment = DefaultTrafficTreatment.builder().build();
            ConnectPoint ingress = new ConnectPoint(ingressDevice.id(), PortNumber.portNumber(1));
            ConnectPoint egress = new ConnectPoint(ingressDevice.id(), PortNumber.portNumber(2));

            Intent intent = new PointToPointIntent(appId, key,
                                                   selector, treatment,
                                                   ingress, egress,
                                                   Collections.emptyList());
            result.add(intent);

            // Bump up the counter and remember this as the last key used.
            count++;
            lastKey = k;
            if (lastKey % 1000 == 0) {
                log.info("Building intents... {} ({})", count, lastKey);
            }
        }
        log.info("Created {} intents", numberOfKeys);
        return result;
    }

    // Submits intent operations.
    final class Submitter implements Runnable {

        private Set<Intent> intents = Sets.newHashSet();
        private Set<Intent> submitted = Sets.newHashSet();
        private Set<Intent> withdrawn = Sets.newHashSet();

        private Submitter(Set<Intent> intents) {
            this.intents = intents;
        }

        @Override
        public void run() {
            delay(2000); // take a breath to start
            prime();
            while (!stopped) {
                cycle();
                delay(800); // take a breath
            }
        }

        // Submits the specified intent.
        private void submit(Intent intent) {
            intentService.submit(intent);
            submitted.add(intent);
            withdrawn.remove(intent); //TODO could check result here...
        }

        // Withdraws the specified intent.
        private void withdraw(Intent intent) {
            intentService.withdraw(intent);
            withdrawn.add(intent);
            submitted.remove(intent); //TODO could check result here...
        }

        // Primes the cycle.
        private void prime() {
            int i = 0;
            withdrawn.addAll(intents);
            for (Intent intent : intents) {
                submit(intent);
                // only submit half of the intents to start
                if (i++ >= intents.size() / 2) {
                    break;
                }
            }
        }

        // Runs a single operation cycle.
        private void cycle() {
            long start = currentTimeMillis();
            subset(submitted).forEach(this::withdraw);
            subset(withdrawn).forEach(this::submit);
            long delta = currentTimeMillis() - start;
            if (delta > 5000 || delta < 0) {
                log.warn("Cycle took {} ms", delta);
            }
        }
    }


    // Event listener to monitor throughput.
    final class Listener implements IntentListener {

        private final Map<IntentEvent.Type, Counter> counters;
        private final Counter runningTotal = new Counter();

        public Listener() {
            counters = initCounters();
        }

        private Map<IntentEvent.Type, Counter> initCounters() {
            Map<IntentEvent.Type, Counter> map = Maps.newHashMap();
            for (IntentEvent.Type type : IntentEvent.Type.values()) {
                map.put(type, new Counter());
            }
            return map;
        }

        @Override
        public void event(IntentEvent event) {
            if (event.subject().appId().equals(appId)) {
                counters.get(event.type()).add(1);
            }
        }

        public void report() {
            StringBuilder stringBuilder = new StringBuilder();
            Counter installed = counters.get(INSTALLED);
            Counter withdrawn = counters.get(WITHDRAWN);
            double current = installed.throughput() + withdrawn.throughput();
            runningTotal.add(installed.total() + withdrawn.total());
            for (IntentEvent.Type type : IntentEvent.Type.values()) {
                stringBuilder.append(printCounter(type)).append("; ");
            }
            log.info("Throughput: OVERALL={}; CURRENT={}; {}",
                     format("%.2f", runningTotal.throughput()),
                     format("%.2f", current), stringBuilder);
        }

        private String printCounter(IntentEvent.Type event) {
            Counter counter = counters.get(event);
            String result = format("%s=%.2f", event, counter.throughput());
            counter.reset();
            return result;
        }
    }

}
