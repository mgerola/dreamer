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
package org.onosproject.store.device.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.mastership.MastershipTerm;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceClockProviderService;
import org.onosproject.net.device.DeviceClockService;
import org.onosproject.store.Timestamp;
import org.onosproject.store.impl.MastershipBasedTimestamp;
import org.slf4j.Logger;

/**
 * Clock service to issue Timestamp based on Device Mastership.
 */
@Component(immediate = true)
@Service
public class DeviceClockManager implements DeviceClockService, DeviceClockProviderService {

    private final Logger log = getLogger(getClass());

    // TODO: Implement per device ticker that is reset to 0 at the beginning of a new term.
    private final AtomicLong ticker = new AtomicLong(0);
    private ConcurrentMap<DeviceId, MastershipTerm> deviceMastershipTerms = new ConcurrentHashMap<>();

    @Activate
    public void activate() {
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
    }

    @Override
    public Timestamp getTimestamp(DeviceId deviceId) {
        MastershipTerm term = deviceMastershipTerms.get(deviceId);
        log.trace("term info for {} is: {}", deviceId, term);

        if (term == null) {
            throw new IllegalStateException("Requesting timestamp for " + deviceId + " without mastership");
        }
        return new MastershipBasedTimestamp(term.termNumber(), ticker.incrementAndGet());
    }

    @Override
    public void setMastershipTerm(DeviceId deviceId, MastershipTerm term) {
        log.info("adding term info {} {}", deviceId, term.master());
        deviceMastershipTerms.put(deviceId, term);
    }

    @Override
    public boolean isTimestampAvailable(DeviceId deviceId) {
        return deviceMastershipTerms.containsKey(deviceId);
    }
}
