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
 ONOS GUI -- Topology Force Module.
 Visualization of the topology in an SVG layer, using a D3 Force Layout.
 */

(function () {
    'use strict';

    // injected refs
    var $log, fs, sus, is, ts, flash, wss,
        tis, tms, td3, tss, tts, tos, fltr, tls,
        icfg, uplink, svg;

    // configuration
    var linkConfig = {
        light: {
            baseColor: '#666',
            inColor: '#66f',
            outColor: '#f00'
        },
        dark: {
            baseColor: '#aaa',
            inColor: '#66f',
            outColor: '#f66'
        },
        inWidth: 12,
        outWidth: 10
    };

    // internal state
    var settings,   // merged default settings and options
        force,      // force layout object
        drag,       // drag behavior handler
        network = {
            nodes: [],
            links: [],
            lookup: {},
            revLinkToKey: {}
        },
        lu,                     // shorthand for lookup
        rlk,                    // shorthand for revLinktoKey
        showHosts = false,      // whether hosts are displayed
        showOffline = true,     // whether offline devices are displayed
        nodeLock = false,       // whether nodes can be dragged or not (locked)
        dim;                    // the dimensions of the force layout [w,h]

    // SVG elements;
    var linkG, linkLabelG, portLabelG, nodeG;

    // D3 selections;
    var link, linkLabel, node;

    // default settings for force layout
    var defaultSettings = {
        gravity: 0.4,
        friction: 0.7,
        charge: {
            // note: key is node.class
            device: -8000,
            host: -5000,
            _def_: -12000
        },
        linkDistance: {
            // note: key is link.type
            direct: 100,
            optical: 120,
            hostLink: 3,
            _def_: 50
        },
        linkStrength: {
            // note: key is link.type
            // range: {0.0 ... 1.0}
            //direct: 1.0,
            //optical: 1.0,
            //hostLink: 1.0,
            _def_: 1.0
        }
    };


    // ==========================
    // === EVENT HANDLERS

    function addDevice(data) {
        var id = data.id,
            d;

        uplink.showNoDevs(false);

        // although this is an add device event, if we already have the
        //  device, treat it as an update instead..
        if (lu[id]) {
            updateDevice(data);
            return;
        }

        d = tms.createDeviceNode(data);
        network.nodes.push(d);
        lu[id] = d;
        updateNodes();
        fStart();
    }

    function updateDevice(data) {
        var id = data.id,
            d = lu[id],
            wasOnline;

        if (d) {
            wasOnline = d.online;
            angular.extend(d, data);
            if (tms.positionNode(d, true)) {
                sendUpdateMeta(d);
            }
            updateNodes();
            if (wasOnline !== d.online) {
                tms.findAttachedLinks(d.id).forEach(restyleLinkElement);
                updateOfflineVisibility(d);
            }
        }
    }

    function removeDevice(data) {
        var id = data.id,
            d = lu[id];
        if (d) {
            removeDeviceElement(d);
        }
    }

    function addHost(data) {
        var id = data.id,
            d, lnk;

        // although this is an add host event, if we already have the
        //  host, treat it as an update instead..
        if (lu[id]) {
            updateHost(data);
            return;
        }

        d = tms.createHostNode(data);
        network.nodes.push(d);
        lu[id] = d;
        updateNodes();

        lnk = tms.createHostLink(data);
        if (lnk) {
            d.linkData = lnk;    // cache ref on its host
            network.links.push(lnk);
            lu[d.ingress] = lnk;
            lu[d.egress] = lnk;
            updateLinks();
        }

        fStart();
    }

    function updateHost(data) {
        var id = data.id,
            d = lu[id];
        if (d) {
            angular.extend(d, data);
            if (tms.positionNode(d, true)) {
                sendUpdateMeta(d);
            }
            updateNodes();
        }
    }

    function removeHost(data) {
        var id = data.id,
            d = lu[id];
        if (d) {
            removeHostElement(d, true);
        }
    }

    function addLink(data) {
        var result = tms.findLink(data, 'add'),
            bad = result.badLogic,
            d = result.ldata;

        if (bad) {
            //logicError(bad + ': ' + link.id);
            return;
        }

        if (d) {
            // we already have a backing store link for src/dst nodes
            addLinkUpdate(d, data);
            return;
        }

        // no backing store link yet
        d = tms.createLink(data);
        if (d) {
            network.links.push(d);
            lu[d.key] = d;
            updateLinks();
            fStart();
        }
    }

    function updateLink(data) {
        var result = tms.findLink(data, 'update'),
            bad = result.badLogic;
        if (bad) {
            //logicError(bad + ': ' + link.id);
            return;
        }
        result.updateWith(link);
    }

    function removeLink(data) {
        var result = tms.findLink(data, 'remove');

        if (!result.badLogic) {
            result.removeRawLink();
        }
    }

    // ========================

    function addLinkUpdate(ldata, link) {
        // add link event, but we already have the reverse link installed
        ldata.fromTarget = link;
        rlk[link.id] = ldata.key;
        restyleLinkElement(ldata);
    }


    var widthRatio = 1.4,
        linkScale = d3.scale.linear()
            .domain([1, 12])
            .range([widthRatio, 12 * widthRatio])
            .clamp(true),
        allLinkTypes = 'direct indirect optical tunnel';

    function restyleLinkElement(ldata, immediate) {
        // this fn's job is to look at raw links and decide what svg classes
        // need to be applied to the line element in the DOM
        var th = ts.theme(),
            el = ldata.el,
            type = ldata.type(),
            lw = ldata.linkWidth(),
            online = ldata.online(),
            delay = immediate ? 0 : 1000;

        el.classed('link', true);
        el.classed('inactive', !online);
        el.classed(allLinkTypes, false);
        if (type) {
            el.classed(type, true);
        }
        el.transition()
            .duration(delay)
            .attr('stroke-width', linkScale(lw))
            .attr('stroke', linkConfig[th].baseColor);
    }

    function removeLinkElement(d) {
        var idx = fs.find(d.key, network.links, 'key'),
            removed;
        if (idx >=0) {
            // remove from links array
            removed = network.links.splice(idx, 1);
            // remove from lookup cache
            delete lu[removed[0].key];
            updateLinks();
            fResume();
        }
    }

    function removeHostElement(d, upd) {
        // first, remove associated hostLink...
        removeLinkElement(d.linkData);

        // remove hostLink bindings
        delete lu[d.ingress];
        delete lu[d.egress];

        // remove from lookup cache
        delete lu[d.id];
        // remove from nodes array
        var idx = fs.find(d.id, network.nodes);
        network.nodes.splice(idx, 1);

        // remove from SVG
        // NOTE: upd is false if we were called from removeDeviceElement()
        if (upd) {
            updateNodes();
            fResume();
        }
    }

    function removeDeviceElement(d) {
        var id = d.id;
        // first, remove associated hosts and links..
        tms.findAttachedHosts(id).forEach(removeHostElement);
        tms.findAttachedLinks(id).forEach(removeLinkElement);

        // remove from lookup cache
        delete lu[id];
        // remove from nodes array
        var idx = fs.find(id, network.nodes);
        network.nodes.splice(idx, 1);

        if (!network.nodes.length) {
            uplink.showNoDevs(true);
        }

        // remove from SVG
        updateNodes();
        fResume();
    }

    function updateHostVisibility() {
        sus.visible(nodeG.selectAll('.host'), showHosts);
        sus.visible(linkG.selectAll('.hostLink'), showHosts);
        sus.visible(linkLabelG.selectAll('.hostLinkLabel'), showHosts);
    }

    function updateOfflineVisibility(dev) {
        function updDev(d, show) {
            var b;
            sus.visible(d.el, show);

            tms.findAttachedLinks(d.id).forEach(function (link) {
                b = show && ((link.type() !== 'hostLink') || showHosts);
                sus.visible(link.el, b);
            });
            tms.findAttachedHosts(d.id).forEach(function (host) {
                b = show && showHosts;
                sus.visible(host.el, b);
            });
        }

        if (dev) {
            // updating a specific device that just toggled off/on-line
            updDev(dev, dev.online || showOffline);
        } else {
            // updating all offline devices
            tms.findDevices(true).forEach(function (d) {
                updDev(d, showOffline);
            });
        }
    }


    function sendUpdateMeta(d, clearPos) {
        var metaUi = {},
            ll;

        // if we are not clearing the position data (unpinning),
        // attach the x, y, longitude, latitude...
        if (!clearPos) {
            ll = tms.lngLatFromCoord([d.x, d.y]);
            metaUi = {x: d.x, y: d.y, lng: ll[0], lat: ll[1]};
        }
        d.metaUi = metaUi;
        wss.sendEvent('updateMeta', {
            id: d.id,
            'class': d.class,
            memento: metaUi
        });
    }


    function mkSvgClass(d) {
        return d.fixed ? d.svgClass + ' fixed' : d.svgClass;
    }

    function vis(b) {
        return b ? 'visible' : 'hidden';
    }

    function toggleHosts() {
        showHosts = !showHosts;
        updateHostVisibility();
        flash.flash('Hosts ' + vis(showHosts));
    }

    function toggleOffline() {
        showOffline = !showOffline;
        updateOfflineVisibility();
        flash.flash('Offline devices ' + vis(showOffline));
    }

    function cycleDeviceLabels() {
        td3.incDevLabIndex();
        tms.findDevices().forEach(function (d) {
            td3.updateDeviceLabel(d);
        });
    }

    function unpin() {
        var hov = tss.hovered();
        if (hov) {
            sendUpdateMeta(hov, true);
            hov.fixed = false;
            hov.el.classed('fixed', false);
            fResume();
        }
    }

    function showMastership(masterId) {
        if (!masterId) {
            restoreLayerState();
        } else {
            showMastershipFor(masterId);
        }
    }

    function restoreLayerState() {
        // NOTE: this level of indirection required, for when we have
        //          the layer filter functionality re-implemented
        suppressLayers(false);
    }

    function showMastershipFor(id) {
        suppressLayers(true);
        node.each(function (n) {
            if (n.master === id) {
                n.el.classed('suppressed', false);
            }
        });
    }

    function suppressLayers(b) {
        node.classed('suppressed', b);
        link.classed('suppressed', b);
//        d3.selectAll('svg .port').classed('inactive', b);
//        d3.selectAll('svg .portText').classed('inactive', b);
    }

    // ==========================================

    function updateNodes() {
        // select all the nodes in the layout:
        node = nodeG.selectAll('.node')
            .data(network.nodes, function (d) { return d.id; });

        // operate on existing nodes:
        node.filter('.device').each(td3.deviceExisting);
        node.filter('.host').each(td3.hostExisting);

        // operate on entering nodes:
        var entering = node.enter()
            .append('g')
            .attr({
                id: function (d) { return sus.safeId(d.id); },
                class: mkSvgClass,
                transform: function (d) { return sus.translate(d.x, d.y); },
                opacity: 0
            })
            .call(drag)
            .on('mouseover', tss.nodeMouseOver)
            .on('mouseout', tss.nodeMouseOut)
            .transition()
            .attr('opacity', 1);

        // augment entering nodes:
        entering.filter('.device').each(td3.deviceEnter);
        entering.filter('.host').each(td3.hostEnter);

        // operate on both existing and new nodes:
        td3.updateDeviceColors();

        // operate on exiting nodes:
        // Note that the node is removed after 2 seconds.
        // Sub element animations should be shorter than 2 seconds.
        var exiting = node.exit()
            .transition()
            .duration(2000)
            .style('opacity', 0)
            .remove();

        // exiting node specifics:
        exiting.filter('.host').each(td3.hostExit);
        exiting.filter('.device').each(td3.deviceExit);

        // finally, resume the force layout
        fResume();
    }

    // ==========================

    function updateLinks() {
        var th = ts.theme();

        link = linkG.selectAll('.link')
            .data(network.links, function (d) { return d.key; });

        // operate on existing links:
        link.each(function (d) {
            // this is supposed to be an existing link, but we have observed
            //  occasions (where links are deleted and added rapidly?) where
            //  the DOM element has not been defined. So protect against that...
            if (d.el) {
                restyleLinkElement(d, true);
            }
        });

        // operate on entering links:
        var entering = link.enter()
            .append('line')
            .attr({
                x1: function (d) { return d.source.x; },
                y1: function (d) { return d.source.y; },
                x2: function (d) { return d.target.x; },
                y2: function (d) { return d.target.y; },
                stroke: linkConfig[th].inColor,
                'stroke-width': linkConfig.inWidth
            });

        // augment links
        entering.each(td3.linkEntering);

        // operate on both existing and new links:
        //link.each(...)

        // apply or remove labels
        td3.applyLinkLabels();

        // operate on exiting links:
        link.exit()
            .attr('stroke-dasharray', '3 3')
            .attr('stroke', linkConfig[th].outColor)
            .style('opacity', 0.5)
            .transition()
            .duration(1500)
            .attr({
                'stroke-dasharray': '3 12',
                'stroke-width': linkConfig.outWidth
            })
            .style('opacity', 0.0)
            .remove();
    }


    // ==========================
    // force layout tick function

    function fResume() {
        if (!tos.isOblique()) {
            force.resume();
        }
    }

    function fStart() {
        if (!tos.isOblique()) {
            force.start();
        }
    }

    var tickStuff = {
        nodeAttr: {
            transform: function (d) { return sus.translate(d.x, d.y); }
        },
        linkAttr: {
            x1: function (d) { return d.source.x; },
            y1: function (d) { return d.source.y; },
            x2: function (d) { return d.target.x; },
            y2: function (d) { return d.target.y; }
        },
        linkLabelAttr: {
            transform: function (d) {
                var lnk = tms.findLinkById(d.key);
                if (lnk) {
                    return td3.transformLabel({
                        x1: lnk.source.x,
                        y1: lnk.source.y,
                        x2: lnk.target.x,
                        y2: lnk.target.y
                    });
                }
            }
        }
    };

    function tick() {
        // guard against null (which can happen when our view pages out)...
        if (node) node.attr(tickStuff.nodeAttr);
        if (link) link.attr(tickStuff.linkAttr);
        if (linkLabel) linkLabel.attr(tickStuff.linkLabelAttr);
    }


    // ==========================
    // === MOUSE GESTURE HANDLERS

    function zoomingOrPanning(ev) {
        return ev.metaKey || ev.altKey;
    }

    function atDragEnd(d) {
        // once we've finished moving, pin the node in position
        d.fixed = true;
        d3.select(this).classed('fixed', true);
        sendUpdateMeta(d);
    }

    // predicate that indicates when dragging is active
    function dragEnabled() {
        var ev = d3.event.sourceEvent;
        // nodeLock means we aren't allowing nodes to be dragged...
        return !nodeLock && !zoomingOrPanning(ev);
    }

    // predicate that indicates when clicking is active
    function clickEnabled() {
        return true;
    }

    // ==========================
    // function entry points for traffic module

    var allTrafficClasses = 'primary secondary animated optical';

    function clearLinkTrafficStyle() {
        link.style('stroke-width', null)
            .classed(allTrafficClasses, false);
    }

    function removeLinkLabels() {
        network.links.forEach(function (d) {
            d.label = '';
        });
    }

    function updateLinkLabelModel() {
        // create the backing data for showing labels..
        var data = [];
        link.each(function (d) {
            if (d.label) {
                data.push({
                    id: 'lab-' + d.key,
                    key: d.key,
                    label: d.label,
                    ldata: d
                });
            }
        });

        linkLabel = linkLabelG.selectAll('.linkLabel')
            .data(data, function (d) { return d.id; });
    }

    // ==========================
    // Module definition

    function mkModelApi(uplink) {
        return {
            projection: uplink.projection,
            network: network,
            restyleLinkElement: restyleLinkElement,
            removeLinkElement: removeLinkElement
        };
    }

    function mkD3Api(uplink) {
        return {
            node: function () { return node; },
            link: function () { return link; },
            linkLabel: function () { return linkLabel; },
            instVisible: function () { return tis.isVisible(); },
            posNode: tms.positionNode,
            showHosts: function () { return showHosts; },
            restyleLinkElement: restyleLinkElement,
            updateLinkLabelModel: updateLinkLabelModel
        }
    }

    function mkSelectApi(uplink) {
        return {
            node: function () { return node; },
            zoomingOrPanning: zoomingOrPanning,
            updateDeviceColors: td3.updateDeviceColors
        };
    }

    function mkTrafficApi(uplink) {
        return {
            clearLinkTrafficStyle: clearLinkTrafficStyle,
            removeLinkLabels: removeLinkLabels,
            updateLinks: updateLinks,
            findLinkById: tms.findLinkById,
            hovered: tss.hovered,
            validateSelectionContext: tss.validateSelectionContext,
            selectOrder: tss.selectOrder
        }
    }

    function mkObliqueApi(uplink, fltr) {
        return {
            force: function() { return force; },
            zoomLayer: uplink.zoomLayer,
            nodeGBBox: function() { return nodeG.node().getBBox(); },
            node: function () { return node; },
            link: function () { return link; },
            linkLabel: function () { return linkLabel; },
            nodes: function () { return network.nodes; },
            tickStuff: tickStuff,
            nodeLock: function (b) {
                var old = nodeLock;
                nodeLock = b;
                return old;
            },
            opacifyMap: uplink.opacifyMap,
            inLayer: fltr.inLayer
        };
    }

    function mkFilterApi(uplink) {
        return {
            node: function () { return node; },
            link: function () { return link; }
        };
    }

    function mkLinkApi(svg, uplink) {
        return {
            svg: svg,
            zoomer: uplink.zoomer(),
            network: network,
            portLabelG: function () { return portLabelG; },
            showHosts: function () { return showHosts; }
        };
    }

    angular.module('ovTopo')
    .factory('TopoForceService',
        ['$log', 'FnService', 'SvgUtilService', 'IconService', 'ThemeService',
            'FlashService', 'WebSocketService',
            'TopoInstService', 'TopoModelService',
            'TopoD3Service', 'TopoSelectService', 'TopoTrafficService',
            'TopoObliqueService', 'TopoFilterService', 'TopoLinkService',

        function (_$log_, _fs_, _sus_, _is_, _ts_, _flash_, _wss_,
                  _tis_, _tms_, _td3_, _tss_, _tts_, _tos_, _fltr_, _tls_) {
            $log = _$log_;
            fs = _fs_;
            sus = _sus_;
            is = _is_;
            ts = _ts_;
            flash = _flash_;
            wss = _wss_;
            tis = _tis_;
            tms = _tms_;
            td3 = _td3_;
            tss = _tss_;
            tts = _tts_;
            tos = _tos_;
            fltr = _fltr_;
            tls = _tls_;

            icfg = is.iconConfig();

            var themeListener = ts.addListener(function () {
                updateLinks();
                updateNodes();
            });

            // forceG is the SVG group to display the force layout in
            // uplink is the api from the main topo source file
            // dim is the initial dimensions of the SVG as [w,h]
            // opts are, well, optional :)
            function initForce(_svg_, forceG, _uplink_, _dim_, opts) {
                uplink = _uplink_;
                dim = _dim_;
                svg = _svg_;

                lu = network.lookup;
                rlk = network.revLinkToKey;

                $log.debug('initForce().. dim = ' + dim);

                tms.initModel(mkModelApi(uplink), dim);
                td3.initD3(mkD3Api(uplink));
                tss.initSelect(mkSelectApi(uplink));
                tts.initTraffic(mkTrafficApi(uplink));
                tos.initOblique(mkObliqueApi(uplink, fltr));
                fltr.initFilter(mkFilterApi(uplink), d3.select('#mast-right'));
                tls.initLink(mkLinkApi(svg, uplink), td3);

                settings = angular.extend({}, defaultSettings, opts);

                linkG = forceG.append('g').attr('id', 'topo-links');
                linkLabelG = forceG.append('g').attr('id', 'topo-linkLabels');
                nodeG = forceG.append('g').attr('id', 'topo-nodes');
                portLabelG = forceG.append('g').attr('id', 'topo-portLabels');

                link = linkG.selectAll('.link');
                linkLabel = linkLabelG.selectAll('.linkLabel');
                node = nodeG.selectAll('.node');

                force = d3.layout.force()
                    .size(dim)
                    .nodes(network.nodes)
                    .links(network.links)
                    .gravity(settings.gravity)
                    .friction(settings.friction)
                    .charge(settings.charge._def_)
                    .linkDistance(settings.linkDistance._def_)
                    .linkStrength(settings.linkStrength._def_)
                    .on('tick', tick);

                drag = sus.createDragBehavior(force,
                    tss.selectObject, atDragEnd, dragEnabled, clickEnabled);
            }

            function newDim(_dim_) {
                dim = _dim_;
                force.size(dim);
                tms.newDim(dim);
            }

            function destroyForce() {
                force.stop();

                tls.destroyLink();
                fltr.destroyFilter();
                tos.destroyOblique();
                tts.destroyTraffic();
                tss.destroySelect();
                td3.destroyD3();
                tms.destroyModel();
                ts.removeListener(themeListener);
                themeListener = null;

                // clean up the DOM
                svg.selectAll('g').remove();
                svg.selectAll('defs').remove();

                // clean up internal state
                network.nodes = [];
                network.links = [];
                network.lookup = {};
                network.revLinkToKey = {};

                linkG = linkLabelG = nodeG = portLabelG = null;
                link = linkLabel = node = null;
                force = drag = null;
            }

            return {
                initForce: initForce,
                newDim: newDim,
                destroyForce: destroyForce,

                updateDeviceColors: td3.updateDeviceColors,
                toggleHosts: toggleHosts,
                togglePorts: tls.togglePorts,
                toggleOffline: toggleOffline,
                cycleDeviceLabels: cycleDeviceLabels,
                unpin: unpin,
                showMastership: showMastership,

                addDevice: addDevice,
                updateDevice: updateDevice,
                removeDevice: removeDevice,
                addHost: addHost,
                updateHost: updateHost,
                removeHost: removeHost,
                addLink: addLink,
                updateLink: updateLink,
                removeLink: removeLink
            };
        }]);
}());
