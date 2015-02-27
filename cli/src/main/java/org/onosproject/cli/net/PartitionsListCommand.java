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
package org.onosproject.cli.net;

import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.store.service.PartitionInfo;
import org.onosproject.store.service.StorageAdminService;

import java.util.List;

/**
 * Command to list the database partitions in the system.
 */
@Command(scope = "onos", name = "partitions",
        description = "Lists information about partitions in the system")
public class PartitionsListCommand extends AbstractShellCommand {

    private static final String FMT = "%-20s %8s %25s %s";

    @Override
    protected void execute() {
        StorageAdminService storageAdminService = get(StorageAdminService.class);
        List<PartitionInfo> partitionInfo = storageAdminService.getPartitionInfo();

        print(FMT, "Name", "Term", "Members", "");

        for (PartitionInfo info : partitionInfo) {
            boolean first = true;
            for (String member : info.members()) {
                if (first) {
                    print(FMT, info.name(), info.term(), member,
                          member.equals(info.leader()) ? "*" : "");
                    first = false;
                } else {
                    print(FMT, "", "", member,
                          member.equals(info.leader()) ? "*" : "");
                }
            }
        }
    }
}
