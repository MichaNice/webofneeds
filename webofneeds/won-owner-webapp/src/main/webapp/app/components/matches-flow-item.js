;

import angular from 'angular';
import squareImageModule from './square-image';
import extendedGalleryModule from './extended-gallery';
import feedbackGridModule from './feedback-grid';
import { attach } from '../utils';
import { actionCreators }  from '../actions/actions';
import {
    labels,
    relativeTime,
} from '../won-label-utils';
import {
    selectAllByConnections,
    selectLastUpdateTime,
} from '../selectors';

const serviceDependencies = ['$q', '$ngRedux', '$scope', '$interval'];
function genComponentConf() {
    let template = `
        <div ng-show="self.images" class="mfi__gallery">
            <won-extended-gallery
                max-thumbnails="self.maxThumbnails"
                items="self.images"
                class="horizontal"
                ng-show="self.images.length > 0">
            </won-extended-gallery>
            <won-square-image 
                title="self.connectionData.getIn(['remoteNeed','won:hasContent','dc:title'])"
                uri="self.connectionData.getIn(['remoteNeed','@id'])"
                ng-show="self.images.length == 0">
            </won-square-image>
        </div>
        <div class="mfi__description clickable">
            <div class="mfi__description__topline">
                <div class="mfi__description__topline__title clickable">
                    {{self.connectionData.getIn(['remoteNeed','won:hasContent','dc:title'])}}
                </div>
                <div class="mfi__description__topline__date">
                    {{ self.remoteCreatedOn }}
                </div>
            </div>
            <div class="mfi__description__subtitle">
                <span
                    class="mfi__description__subtitle__group"
                    ng-show="self.connection.get('group')">
                        <img
                            src="generated/icon-sprite.svg#ico36_group"
                            class="mfi__description__subtitle__group__icon">
                        <span>
                            {{ self.connectionData.get('group') }}
                        </span>
                        <span class="mfi__description__subtitle__group__dash">
                            &ndash;
                        </span>
                </span>
                <span class="mfi__description__subtitle__type">
                    {{
                        self.labels.type[
                            self.connectionData.getIn(['remoteNeed','won:hasBasicNeedType', '@id'])
                        ]
                    }}
                </span>
            </div>
            <!-- include once you have content in your needs that needs to be displayed here -->
            <div class="mfi__description__content" ng-show="false">
                <div class="mfi__description__content__location">
                    <img
                        class="mfi__description__content__indicator"
                        src="generated/icon-sprite.svg#ico16_indicator_location"/>
                    <span>
                        Vienna area
                    </span>
                </div>
                <div class="mfi__description__content__datetime">
                    <img
                        class="mfi__description__content__indicator"
                        src="generated/icon-sprite.svg#ico16_indicator_time"/>
                    <span>o</span>
                    <span>Available until 5th May</span>
                </div>
            </div>
        </div>
        <div
            class="mfi__match clickable"
            ng-if="!self.feedbackVisible"
            ng-click="self.showFeedback()">
                <div class="mfi__match__description">
                    <div class="mfi__match__description__title">
                        {{ self.connectionData.getIn(['ownNeed','won:hasContent','dc:title']) }}
                    </div>
                    <div class="mfi__match__description__type">
                        {{
                            self.labels.type[
                                self.connectionData.getIn(['ownNeed','won:hasBasicNeedType','@id'])
                            ]
                        }}
                    </div>
                </div>
                <won-square-image
                    src="self.connectionData.getIn(['ownNeed','titleImgSrc'])"
                    title="self.connectionData.getIn(['ownNeed','won:hasContent','dc:title'])"
                    uri="self.connectionData.getIn(['ownNeed','@id'])">
                </won-square-image>
        </div>
        <won-feedback-grid
            connection-uri="self.connectionUri"
            ng-if="self.feedbackVisible"/>
    `;

    class Controller {
        constructor() {
            attach(this, serviceDependencies, arguments);
            this.labels = labels;
            this.feedbackVisible = false;
            this.maxThumbnails = 4;
            this.images=[];

            const selectFromState = (state) => {

                const connectionData = selectAllByConnections(state).get(this.connectionUri);
                return {
                    remoteCreatedOn: relativeTime(
                        selectLastUpdateTime(state),
                        connectionData.getIn(['remoteNeed', 'dct:created'])
                    ),
                    connectionData: connectionData,
                };
            };

            const disconnect = this.$ngRedux.connect(selectFromState, actionCreators)(this);
            this.$scope.$on('$destroy', disconnect);
        }

        showFeedback() {
            this.feedbackVisible = true;
        }

        hideFeedback() {
            this.feedbackVisible = false;
        }

        toggleFeedback(){
            this.feedbackVisible = !this.feedbackVisible;
        }
    }
    Controller.$inject = serviceDependencies;

    return {
        restrict: 'E',
        controller: Controller,
        controllerAs: 'self',
        bindToController: true, //scope-bindings -> ctrl
        scope: {
            connectionUri: "="
        },
        template: template
    }
}

export default angular.module('won.owner.components.matchesFlowItem', [
    squareImageModule,
    extendedGalleryModule,
    feedbackGridModule
])
    .directive('wonMatchesFlowItem', genComponentConf)
    .name;

