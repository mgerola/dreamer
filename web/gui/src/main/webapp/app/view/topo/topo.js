/*
 * Copyright 2014,2015 Open Networking Laboratory
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
 ONOS GUI -- Topology View Module
 */

(function () {
    'use strict';

    var moduleDependencies = [
        'onosUtil',
        'onosSvg',
        'onosRemote'
    ];

    // references to injected services etc.
    var $log, fs, ks, zs, gs, ms, sus, flash, wss,
        tes, tfs, tps, tis, tss, tts, tos, ttbs;

    // DOM elements
    var ovtopo, svg, defs, zoomLayer, mapG, forceG, noDevsLayer;

    // Internal state
    var zoomer, actionMap;

    // --- Short Cut Keys ------------------------------------------------

    function setUpKeys() {
        // key bindings need to be made after the services have been injected
        // thus, deferred to here...
        actionMap = {
            I: [toggleInstances, 'Toggle ONOS instances pane'],
            O: [tps.toggleSummary, 'Toggle ONOS summary pane'],
            D: [tss.toggleDetails, 'Disable / enable details pane'],

            H: [tfs.toggleHosts, 'Toggle host visibility'],
            M: [tfs.toggleOffline, 'Toggle offline visibility'],
            B: [toggleMap, 'Toggle background map'],
            P: [tfs.togglePorts, 'Toggle Port Highlighting'],

            //X: [toggleNodeLock, 'Lock / unlock node positions'],
            Z: [tos.toggleOblique, 'Toggle oblique view (Experimental)'],
            L: [tfs.cycleDeviceLabels, 'Cycle device labels'],
            U: [tfs.unpin, 'Unpin node (hover mouse over)'],
            R: [resetZoom, 'Reset pan / zoom'],
            dot: [ttbs.toggleToolbar, 'Toggle Toolbar'],

            V: [tts.showRelatedIntentsAction, 'Show all related intents'],
            rightArrow: [tts.showNextIntentAction, 'Show next related intent'],
            leftArrow: [tts.showPrevIntentAction, 'Show previous related intent'],
            W: [tts.showSelectedIntentTrafficAction, 'Monitor traffic of selected intent'],
            A: [tts.showAllTrafficAction, 'Monitor all traffic'],
            F: [tts.showDeviceLinkFlowsAction, 'Show device link flows'],

            E: [equalizeMasters, 'Equalize mastership roles'],

            esc: handleEscape,

            _keyListener: ttbs.keyListener,

            _helpFormat: [
                ['I', 'O', 'D', '-', 'H', 'M', 'P', 'B' ],
                ['X', 'Z', 'L', 'U', 'R', '-', 'dot'],
                ['V', 'rightArrow', 'leftArrow', 'W', 'A', 'F', '-', 'E' ]
            ]
        };

        ks.keyBindings(actionMap);

        ks.gestureNotes([
            ['click', 'Select the item and show details'],
            ['shift-click', 'Toggle selection state'],
            ['drag', 'Reposition (and pin) device / host'],
            ['cmd-scroll', 'Zoom in / out'],
            ['cmd-drag', 'Pan']
        ]);
    }

    // --- Keystroke functions -------------------------------------------

    // NOTE: this really belongs in the TopoPanelService -- but how to
    //       cleanly link in the updateDeviceColors() call? To be fixed later.
    function toggleInstances() {
        tis.toggle();
        tfs.updateDeviceColors();
    }

    function toggleMap() {
        sus.visible(mapG, !sus.visible(mapG));
    }

    function resetZoom() {
        zoomer.reset();
    }

    function equalizeMasters() {
        wss.sendEvent('equalizeMasters');
        flash.flash('Equalizing master roles');
    }

    function handleEscape() {
        if (tis.showMaster()) {
            // if an instance is selected, cancel the affinity mapping
            tis.cancelAffinity()

        } else if (tss.haveDetails()) {
            // else if we have node selections, deselect them all
            tss.deselectAll();

        } else if (tis.isVisible()) {
            // else if the Instance Panel is visible, hide it
            tis.hide();
            tfs.updateDeviceColors();

        } else if (tps.summaryVisible()) {
            // else if the Summary Panel is visible, hide it
            tps.hideSummaryPanel();

        } else {
            // TODO: set hover mode to hoverModeNone
            // talk to Thomas about this: shouldn't it be done
            // when we deselect the node (if tss.haveDetails()...)
        }
    }

    // --- Toolbar Functions ---------------------------------------------

    function getActionEntry(key) {
        var entry = actionMap[key];
        return fs.isA(entry) || [entry, ''];
    }

    function setUpToolbar() {
        ttbs.init({
            getActionEntry: getActionEntry
        });
        ttbs.createToolbar();
    }

    // --- Glyphs, Icons, and the like -----------------------------------

    function setUpDefs() {
        defs = svg.append('defs');
        gs.loadDefs(defs);
        sus.loadGlowDefs(defs);
    }


    // --- Pan and Zoom --------------------------------------------------

    // zoom enabled predicate. ev is a D3 source event.
    function zoomEnabled(ev) {
        return (ev.metaKey || ev.altKey);
    }

    function zoomCallback() {
        var sc = zoomer.scale();

        // keep the map lines constant width while zooming
        mapG.style('stroke-width', (2.0 / sc) + 'px');
    }

    function setUpZoom() {
        zoomLayer = svg.append('g').attr('id', 'topo-zoomlayer');
        zoomer = zs.createZoomer({
            svg: svg,
            zoomLayer: zoomLayer,
            zoomEnabled: zoomEnabled,
            zoomCallback: zoomCallback
        });
    }


    // callback invoked when the SVG view has been resized..
    function svgResized(s) {
        tfs.newDim([s.width, s.height]);
    }

    // --- Background Map ------------------------------------------------

    function setUpNoDevs() {
        var g, box;
        noDevsLayer = svg.append('g').attr({
            id: 'topo-noDevsLayer',
            transform: sus.translate(500,500)
        });
        // Note, SVG viewbox is '0 0 1000 1000', defined in topo.html.
        // We are translating this layer to have its origin at the center

        g = noDevsLayer.append('g');
        gs.addGlyph(g, 'bird', 100).attr('class', 'noDevsBird');
        g.append('text').text('No devices are connected')
            .attr({ x: 120, y: 80});

        box = g.node().getBBox();
        box.x -= box.width/2;
        box.y -= box.height/2;
        g.attr('transform', sus.translate(box.x, box.y));

        showNoDevs(true);
    }

    function showNoDevs(b) {
        sus.visible(noDevsLayer, b);
    }

    function setUpMap() {
        mapG = zoomLayer.append('g').attr('id', 'topo-map');
        // returns a promise for the projection...
        return ms.loadMapInto(mapG, '*continental_us');
    }

    function opacifyMap(b) {
        mapG.transition()
            .duration(1000)
            .attr('opacity', b ? 1 : 0);
    }

    // --- Controller Definition -----------------------------------------

    angular.module('ovTopo', moduleDependencies)
        .controller('OvTopoCtrl', ['$scope', '$log', '$location', '$timeout',
            'FnService', 'MastService', 'KeyService', 'ZoomService',
            'GlyphService', 'MapService', 'SvgUtilService', 'FlashService',
            'WebSocketService',
            'TopoEventService', 'TopoForceService', 'TopoPanelService',
            'TopoInstService', 'TopoSelectService', 'TopoTrafficService',
            'TopoObliqueService', 'TopoToolbarService',

        function ($scope, _$log_, $loc, $timeout, _fs_, mast,
                  _ks_, _zs_, _gs_, _ms_, _sus_, _flash_, _wss_,
                  _tes_, _tfs_, _tps_, _tis_, _tss_, _tts_, _tos_, _ttbs_) {
            var self = this,
                projection,
                dim,
                uplink = {
                    // provides function calls back into this space
                    showNoDevs: showNoDevs,
                    projection: function () { return projection; },
                    zoomLayer: function () { return zoomLayer; },
                    zoomer: function () { return zoomer; },
                    opacifyMap: opacifyMap
                };

            $log = _$log_;
            fs = _fs_;
            ks = _ks_;
            zs = _zs_;
            gs = _gs_;
            ms = _ms_;
            sus = _sus_;
            flash = _flash_;
            wss = _wss_;
            tes = _tes_;
            tfs = _tfs_;
            // TODO: consider funnelling actions through TopoForceService...
            //  rather than injecting references to these 'sub-modules',
            //  just so we can invoke functions on them.
            tps = _tps_;
            tis = _tis_;
            tss = _tss_;
            tts = _tts_;
            tos = _tos_;
            ttbs = _ttbs_;

            self.notifyResize = function () {
                svgResized(fs.windowSize(mast.mastHeight()));
            };

            // Cleanup on destroyed scope..
            $scope.$on('$destroy', function () {
                $log.log('OvTopoCtrl is saying Buh-Bye!');
                tes.stop();
                tps.destroyPanels();
                tis.destroyInst();
                tfs.destroyForce();
                ttbs.destroyToolbar();
            });

            // svg layer and initialization of components
            ovtopo = d3.select('#ov-topo');
            svg = ovtopo.select('svg');
            // set the svg size to match that of the window, less the masthead
            svg.attr(fs.windowSize(mast.mastHeight()));
            dim = [svg.attr('width'), svg.attr('height')];

            setUpKeys();
            setUpToolbar();
            setUpDefs();
            setUpZoom();
            setUpNoDevs();
            setUpMap().then(
                function (proj) {
                    projection = proj;
                    $log.debug('** We installed the projection: ', proj);
                }
            );

            forceG = zoomLayer.append('g').attr('id', 'topo-force');
            tfs.initForce(svg, forceG, uplink, dim);
            tis.initInst({ showMastership: tfs.showMastership });
            tps.initPanels();
            tes.start();

            $log.log('OvTopoCtrl has been created');
        }]);
}());
