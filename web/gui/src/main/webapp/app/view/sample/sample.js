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
 ONOS GUI -- Sample View Module
 */

(function () {
    'use strict';
    var $log, tbs, flash,
        togFnDiv, radFnP;

    function btnFn() {
        flash.flash('Hi there friends!');
    }
    function togFn(display) {
        if (display) { togFnDiv.style('display', 'block'); }
        else { togFnDiv.style('display', 'none'); }
    }
    function checkFn() {
        radFnP.text('Checkmark radio button active.')
            .style('color', 'green');
    }
    function xMarkFn() {
        radFnP.text('Xmark radio button active.')
            .style('color', 'red');
    }
    function birdFn() {
        radFnP.text('Bird radio button active.')
            .style('color', '#369');
    }

    angular.module('ovSample', ['onosUtil'])
        .controller('OvSampleCtrl', ['$log', 'ToolbarService', 'FlashService',
            function (_$log_, _tbs_, _flash_) {
                var self = this;
                $log = _$log_;
                tbs = _tbs_;
                flash = _flash_;

                self.message = 'Hey there folks!';

                togFnDiv = d3.select('#ov-sample')
                    .append('div')
                    .text('Look at me!')
                    .style({
                        'display': 'none',
                        'color': 'rgb(204, 89, 81)',
                        'font-size': '20pt'
                    });
                radFnP = d3.select('#ov-sample')
                    .append('p')
                    .style('font-size', '16pt');

                var toolbar = tbs.createToolbar('sample'),
                    rset = [
                        { gid: 'checkMark', cb: checkFn },
                        { gid: 'xMark', cb: xMarkFn },
                        { gid: 'bird', cb: birdFn }
                    ];
                toolbar.addButton('demo-button', 'crown', btnFn);
                toolbar.addToggle('demo-toggle', 'chain', false, togFn);
                toolbar.addSeparator();
                toolbar.addRadioSet('demo-radio', rset);
                toolbar.addSeparator();

             $log.log('OvSampleCtrl has been created');
        }]);
}());
