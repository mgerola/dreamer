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
package org.onosproject.net.intent.impl;

import org.onosproject.net.flow.FlowRuleOperations;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentData;

import java.util.List;

/**
 * A collection of methods to process an intent.
 *
 * This interface is public, but intended to be used only by IntentManager and
 * IntentProcessPhase subclasses stored under phase package.
 */
public interface IntentProcessor {

    /**
     * Compiles an intent recursively.
     *
     * @param intent intent
     * @param previousInstallables previous intent installables
     * @return result of compilation
     */
    List<Intent> compile(Intent intent, List<Intent> previousInstallables);

    /**
     * Generate a {@link FlowRuleOperations} instance from the specified intent data.
     *
     * @param current intent data stored in the store
     * @param pending intent data being processed
     * @return flow rule operations
     */
    FlowRuleOperations coordinate(IntentData current, IntentData pending);

    /**
     * Generate a {@link FlowRuleOperations} instance from the specified intent data.
     *
     * @param current intent data stored in the store
     * @param pending intent data being processed
     * @return flow rule operations
     */
    FlowRuleOperations uninstallCoordinate(IntentData current, IntentData pending);

    /**
     * Applies a batch operation of FlowRules.
     *
     * @param flowRules batch operation to apply
     */
    // TODO: consider a better name
    // This methods gives strangeness a bit because
    // it doesn't receive/return intent related information
    void applyFlowRules(FlowRuleOperations flowRules);
}
