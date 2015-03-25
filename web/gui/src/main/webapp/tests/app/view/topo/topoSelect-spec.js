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

/*
 ONOS GUI -- Topo View -- Topo Selection Service - Unit Tests
 */
describe('factory: view/topo/topoSelect.js', function() {
    var $log, fs, tss;

    beforeEach(module('ovTopo', 'onosUtil', 'onosLayer', 'ngRoute', 'onosNav'));

    beforeEach(inject(function (_$log_, FnService, TopoSelectService) {
        $log = _$log_;
        fs = FnService;
        tss = TopoSelectService;
    }));

    it('should define TopoSelectService', function () {
        expect(tss).toBeDefined();
    });

    it('should define api functions', function () {
        expect(fs.areFunctions(tss, [
            'initSelect', 'destroySelect', 'showDetails', 'toggleDetails',
            'nodeMouseOver', 'nodeMouseOut', 'selectObject', 'deselectObject',
            'deselectAll', 'hovered', 'haveDetails', 'selectOrder',
            'validateSelectionContext'
        ])).toBeTruthy();
    });

    // TODO: more tests...
});
