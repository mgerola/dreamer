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
package org.onosproject.net.intent.impl.phase;

import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentData;
import org.onosproject.net.intent.impl.IntentProcessor;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a phase where an intent is being recompiled.
 */
class Recompiling implements IntentProcessPhase {

    private final IntentProcessor processor;
    private final IntentData data;
    private final IntentData stored;

    /**
     * Creates a intent recompiling phase.
     *
     * @param processor intent processor that does work for recompiling
     * @param data      intent data containing an intent to be recompiled
     * @param stored    intent data stored in the store
     */
    Recompiling(IntentProcessor processor, IntentData data, IntentData stored) {
        this.processor = checkNotNull(processor);
        this.data = checkNotNull(data);
        this.stored = checkNotNull(stored);
    }

    @Override
    public Optional<IntentProcessPhase> execute() {
        List<Intent> compiled = processor.compile(data.intent(), stored.installables());
        data.setInstallables(compiled);
        return Optional.of(new Replacing(processor, data, stored));
    }
}
