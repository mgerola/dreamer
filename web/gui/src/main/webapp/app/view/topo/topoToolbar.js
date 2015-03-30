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
 ONOS GUI -- Topology Toolbar Module.
 Functions for creating and interacting with the toolbar.
 */

(function () {
    'use strict';

    // injected references
    var $log, tbs, api;

    // internal state
    var toolbar, keyData;

    // constants
    var name = 'topo-tbar';

    // key to button mapping data
    var k2b = {
        O: { id: 'summary-tog', gid: 'summary', isel: true},
        I: { id: 'instance-tog', gid: 'uiAttached', isel: true },
        D: { id: 'details-tog', gid: 'details', isel: true },

        H: { id: 'hosts-tog', gid: 'endstation', isel: false },
        M: { id: 'offline-tog', gid: 'switch', isel: true },
        P: { id: 'ports-tog', gid: 'ports', isel: true },
        B: { id: 'bkgrnd-tog', gid: 'map', isel: true },

        //X: { id: 'nodelock-tog', gid: 'lock', isel: false },
        Z: { id: 'oblique-tog', gid: 'oblique', isel: false },
        L: { id: 'cycleLabels-btn', gid: 'cycleLabels' },
        R: { id: 'resetZoom-btn', gid: 'resetZoom' },

        V: { id: 'relatedIntents-btn', gid: 'relatedIntents' },
        leftArrow: { id: 'prevIntent-btn', gid: 'prevIntent' },
        rightArrow: { id: 'nextIntent-btn', gid: 'nextIntent' },
        W: { id: 'intentTraffic-btn', gid: 'intentTraffic' },
        A: { id: 'allTraffic-btn', gid: 'allTraffic' },
        F: { id: 'flows-btn', gid: 'flows' },

        E: { id: 'eqMaster-btn', gid: 'eqMaster' }
    };

    function init(_api_) {
        api = _api_;
    }

    function initKeyData() {
        keyData = d3.map(k2b);
        keyData.forEach(function(key, value) {
            var data = api.getActionEntry(key);
            value.cb = data[0];                     // on-click callback
            value.tt = data[1] + ' (' + key + ')';  // tooltip
        });
    }

    function addButton(key) {
        var v = keyData.get(key);
        v.btn = toolbar.addButton(v.id, v.gid, v.cb, v.tt);
    }
    function addToggle(key) {
        var v = keyData.get(key);
        v.tog = toolbar.addToggle(v.id, v.gid, v.isel, v.cb, v.tt);
    }

    function addFirstRow() {
        addToggle('I');
        addToggle('O');
        addToggle('D');
        toolbar.addSeparator();

        addToggle('H');
        addToggle('M');
        addToggle('P');
        addToggle('B');
    }
    function addSecondRow() {
        //addToggle('X');
        addToggle('Z');
        addButton('L');
        addButton('R');
    }
    function addThirdRow() {
        addButton('V');
        addButton('leftArrow');
        addButton('rightArrow');
        addButton('W');
        addButton('A');
        addButton('F');
        toolbar.addSeparator();
        addButton('E');
    }

    function createToolbar() {
        initKeyData();
        toolbar = tbs.createToolbar(name);
        addFirstRow();
        toolbar.addRow();
        addSecondRow();
        toolbar.addRow();
        addThirdRow();
        toolbar.show();
    }

    function destroyToolbar() {
        tbs.destroyToolbar(name);
    }

    // allows us to ensure the button states track key strokes
    function keyListener(key) {
        var v = keyData.get(key);

        if (v) {
            // we have a valid button mapping
            if (v.tog) {
                // it's a toggle button
                v.tog.toggleNoCb();
            }
        }
    }

    function toggleToolbar() {
        toolbar.toggle();
    }

    angular.module('ovTopo')
        .factory('TopoToolbarService', ['$log', 'ToolbarService',

        function (_$log_, _tbs_) {
            $log = _$log_;
            tbs = _tbs_;

            return {
                init: init,
                createToolbar: createToolbar,
                destroyToolbar: destroyToolbar,
                keyListener: keyListener,
                toggleToolbar: toggleToolbar
            };
        }]);
}());