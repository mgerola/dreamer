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
package org.onosproject.cli.net;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentEvent;
import org.onosproject.net.intent.IntentListener;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.IntentState;
import org.onosproject.net.intent.Key;

import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.onosproject.net.intent.IntentState.FAILED;
import static org.onosproject.net.intent.IntentState.WITHDRAWN;

/**
 * Removes an intent.
 */
@Command(scope = "onos", name = "remove-intent",
         description = "Removes the specified intent")
public class IntentRemoveCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "app",
              description = "Application ID",
              required = true, multiValued = false)
    String applicationIdString = null;

    @Argument(index = 1, name = "id",
              description = "Intent ID",
              required = true, multiValued = false)
    String id = null;

    @Option(name = "-p", aliases = "--purge",
            description = "Purge the intent from the store after removal",
            required = false, multiValued = false)
    private boolean purgeAfterRemove = false;

    @Option(name = "-s", aliases = "--sync",
            description = "Waits for the removal before returning",
            required = false, multiValued = false)
    private boolean sync = false;

    @Override
    protected void execute() {
        IntentService intentService = get(IntentService.class);
        CoreService coreService = get(CoreService.class);

        ApplicationId appId = appId();
        if (applicationIdString != null) {
            appId = coreService.getAppId(applicationIdString);
            if (appId == null) {
                print("Cannot find application Id %s", applicationIdString);
                return;
            }
        }

        if (id.startsWith("0x")) {
            id = id.replaceFirst("0x", "");
        }

        Key key = Key.of(new BigInteger(id, 16).longValue(), appId);
        Intent intent = intentService.getIntent(key);

        if (intent != null) {
            IntentListener listener = null;
            final CountDownLatch withdrawLatch, purgeLatch;
            if (purgeAfterRemove || sync) {
                // set up latch and listener to track uninstall progress
                withdrawLatch = new CountDownLatch(1);
                purgeLatch = purgeAfterRemove ? new CountDownLatch(1) : null;
                listener = (IntentEvent event) -> {
                    if (Objects.equals(event.subject().key(), key)) {
                        if (event.type() == IntentEvent.Type.WITHDRAWN ||
                                event.type() == IntentEvent.Type.FAILED) {
                            withdrawLatch.countDown();
                        } else if (purgeAfterRemove &&
                                event.type() == IntentEvent.Type.PURGED) {
                            purgeLatch.countDown();
                        }
                    }
                };
                intentService.addListener(listener);
            } else {
                purgeLatch = null;
                withdrawLatch = null;
            }

            // request the withdraw
            intentService.withdraw(intent);

            if (purgeAfterRemove || sync) {
                try { // wait for withdraw event
                    withdrawLatch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    print("Timed out waiting for intent {} withdraw", key);
                }
                // double check the state
                IntentState state = intentService.getIntentState(key);
                if (purgeAfterRemove && (state == WITHDRAWN || state == FAILED)) {
                    intentService.purge(intent);
                }
                if (sync) { // wait for purge event
                    /* TODO
                       Technically, the event comes before map.remove() is called.
                       If we depend on sync and purge working together, we will
                       need to address this.
                    */
                    try {
                        purgeLatch.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        print("Timed out waiting for intent {} purge", key);
                    }
                }
            }

            if (listener != null) {
                // clean up the listener
                intentService.removeListener(listener);
            }
        }
    }
}
