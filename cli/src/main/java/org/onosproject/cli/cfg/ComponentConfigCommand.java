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
package org.onosproject.cli.cfg;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.cfg.ConfigProperty;
import org.onosproject.cli.AbstractShellCommand;

import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Manages application inventory.
 */
@Command(scope = "onos", name = "cfg",
        description = "Manages component configuration")
public class ComponentConfigCommand extends AbstractShellCommand {

    static final String GET = "get";
    static final String SET = "set";

    private static final String FMT = "    name=%s, type=%s, value=%s, defaultValue=%s, description=%s";

    @Argument(index = 0, name = "command",
            description = "Command name (activate|deactivate|uninstall)",
            required = false, multiValued = false)
    String command = null;

    @Argument(index = 1, name = "component", description = "Component name",
            required = false, multiValued = false)
    String component = null;

    @Argument(index = 2, name = "name", description = "Property name",
            required = false, multiValued = false)
    String name = null;

    @Argument(index = 3, name = "value", description = "Property value",
            required = false, multiValued = false)
    String value = null;

    ComponentConfigService service;

    @Override
    protected void execute() {
        service = get(ComponentConfigService.class);
        if (isNullOrEmpty(command)) {
            listComponents();
        } else if (command.equals(GET) && isNullOrEmpty(component)) {
            listAllComponentsProperties();
        } else if (command.equals(GET) && isNullOrEmpty(name)) {
            listComponentProperties(component);
        } else if (command.equals(GET)) {
            listComponentProperty(component, name);
        } else if (command.equals(SET) && isNullOrEmpty(value)) {
            service.unsetProperty(component, name);
        } else if (command.equals(SET)) {
            service.setProperty(component, name, value);
        } else {
            error("Illegal usage");
        }
    }

    private void listAllComponentsProperties() {
        service.getComponentNames().forEach(this::listComponentProperties);
    }

    private void listComponents() {
        service.getComponentNames().forEach(n -> print("%s", n));
    }

    private void listComponentProperties(String component) {
        Set<ConfigProperty> props = service.getProperties(component);
        print("%s", component);
        props.forEach(p -> print(FMT, p.name(), p.type().toString().toLowerCase(),
                                 p.value(), p.defaultValue(), p.description()));
    }

    private void listComponentProperty(String component, String name) {
        // FIXME: implement after getProperty is defined and implemented
    }

}
