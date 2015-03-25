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
 ONOS GUI -- Main Application Module
 */

(function () {
    'use strict';

    // define core module dependencies here...
    var coreDependencies = [
        'ngRoute',
        'onosMast',
        'onosNav',
        'onosUtil',
        'onosSvg',
        'onosRemote',
        'onosLayer',
        'onosWidget'
    ];

    // view IDs.. note the first view listed is loaded at startup
    var viewIds = [
        // {INJECTED-VIEW-IDS-START}
        'topo',
        'device',
        'sample',
        // {INJECTED-VIEW-IDS-END}

        // dummy entry
        ''
    ];

    var viewDependencies = [];

    viewIds.forEach(function (id) {
        if (id) {
            viewDependencies.push('ov' + capitalize(id));
        }
    });

    var moduleDependencies = coreDependencies.concat(viewDependencies);

    function capitalize(word) {
        return word ? word[0].toUpperCase() + word.slice(1) : word;
    }

    angular.module('onosApp', moduleDependencies)

        .controller('OnosCtrl', [
            '$log', '$route', '$routeParams', '$location',
            'KeyService', 'ThemeService', 'GlyphService', 'PanelService',
            'FlashService', 'QuickHelpService', 'WebSocketService',

            function ($log, $route, $routeParams, $location,
                      ks, ts, gs, ps, flash, qhs, wss) {
                var self = this;

                self.$route = $route;
                self.$routeParams = $routeParams;
                self.$location = $location;
                self.version = '1.1.0';

                // initialize services...
                ts.init();
                ks.installOn(d3.select('body'));
                ks.bindQhs(qhs);
                gs.init();
                ps.init();
                flash.initFlash();
                qhs.initQuickHelp();

                // TODO: register handler for user settings, etc.

                wss.createWebSocket({
                    wsport: $location.search().wsport
                });

                $log.log('OnosCtrl has been created');

                $log.debug('route: ', self.$route);
                $log.debug('routeParams: ', self.$routeParams);
                $log.debug('location: ', self.$location);
            }])

        .config(['$routeProvider', function ($routeProvider) {
            // If view ID not provided, route to the first view in the list.
            $routeProvider
                .otherwise({
                    redirectTo: '/' + viewIds[0]
                });

            function viewCtrlName(vid) {
                return 'Ov' + capitalize(vid) + 'Ctrl';
            }

            function viewTemplateUrl(vid) {
                return 'app/view/' + vid + '/' + vid + '.html';
            }

            // Add routes for each defined view.
            viewIds.forEach(function (vid) {
                if (vid) {
                    $routeProvider.when('/' + vid, {
                        controller: viewCtrlName(vid),
                        controllerAs: 'ctrl',
                        templateUrl: viewTemplateUrl(vid)
                    });
                }
            });
        }]);
}());
