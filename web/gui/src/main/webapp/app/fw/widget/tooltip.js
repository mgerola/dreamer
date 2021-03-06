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
 ONOS GUI -- Widget -- Tooltip Service
 */

(function () {
    'use strict';

    // injected references
    var $log, $timeout, fs;

    // constants
    var hoverHeight = 35,
        hoverDelay = 500,
        exitDelay = 100;

    // internal state
    var tooltip, currElemId;

    function init() {
        tooltip = d3.select('#tooltip');
        tooltip.html('');
    }

    // === API functions ------------------------------------------------

    function showTooltip(el, msg) {
        if (!el || !msg) {
            return;
        }
        var elem = d3.select(el),
            mouseX = d3.event.pageX,
            mouseY = d3.event.pageY;
        currElemId = elem.attr('id');

        tooltip.transition()
            .delay(hoverDelay)
            .each('start', function () {
                d3.select(this).style('display', 'none');
            })
            .each('end', function () {
                d3.select(this).style({
                    display: 'inline-block',
                    left: mouseX + 'px',
                    top: (mouseY - hoverHeight) + 'px'
                })
                    .text(msg);
            });
    }

    function cancelTooltip(el) {
        if (!el) {
            return;
        }
        var elem = d3.select(el);

        if (elem.attr('id') === currElemId) {
            tooltip.transition()
                .delay(exitDelay)
                .style({
                    display: 'none'
                })
                .text('');
        }
    }

    angular.module('onosWidget')
        .factory('TooltipService', ['$log', '$timeout', 'FnService',

        function (_$log_, _$timeout_, _fs_) {
            $log = _$log_;
            $timeout = _$timeout_;
            fs = _fs_;

            init();

            return {
                showTooltip: showTooltip,
                cancelTooltip: cancelTooltip
            };
        }]);
}());
