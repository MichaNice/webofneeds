/**
 * Created by ksinger on 20.08.2015.
 */
;
import angular from 'angular';
import { attach } from '../utils';
import { actionCreators }  from '../actions/actions';

function genLoginConf() {
    let template = `<a class="wl__button" ng-click="self.open = !self.open">
                        <span class="wl__button__caption">Sign in</span>
                        <img src="generated/icon-sprite.svg#ico16_arrow_up_hi" class="wl__button__carret">
                    </a>
                    <div ng-form="loginForm">
                        <input
                            id="loginEmail"
                            placeholder="Email address"
                            ng-model="self.email"
                            type="email"
                            required
                            autofocus
                            ng-keyup="self.loginReset() || ($event.keyCode == 13 && self.login(self.email, self.password))"/>
                        <span class="wl__errormsg">
                            {{self.loginError}}
                        </span>
                        <input
                            placeholder="Password"
                            ng-model="self.password"
                            type="password"
                            required
                            ng-keyup="self.loginReset() || ($event.keyCode == 13 && self.login(self.email, self.password))"/>
                    </div>
                    <!-- TODO: Implement remember me and forgot password --><!--div class="wl__table">
                        <div class="wlt__left">
                            <input type="checkbox" ng-model="self.rememberMe" id="rememberMe"/><label for="rememberMe">Remember me</label>
                        </div>
                        <div class="wlt__right">
                            <a href="#">Forgot Password?</a>
                        </div>
                    </div>-->
                    <button
                        class="won-button--filled lighterblue"
                        ng-disabled="loginForm.$invalid"
                        ng-click="::self.login(self.email, self.password)">
                            Sign in
                    </button>
                    <span class="wl__register">
                        No Account yet?
                        <a ui-sref="landingpage({focusSignup: true})">
                            Sign up
                        </a>
                    </span>`;

    const serviceDependencies = ['$q', '$ngRedux', '$scope', /*'$routeParams' /*injections as strings here*/];

    class Controller {
        constructor(/* arguments <- serviceDependencies */){
            attach(this, serviceDependencies, arguments);

            this.email = "";
            this.password = "";

            const login = (state) => ({
                loggedIn: state.getIn(['user','loggedIn']),
                loginError: state.getIn(['user','loginError'])
            });

            const disconnect = this.$ngRedux.connect(login, actionCreators)(this);
            this.$scope.$on('$destroy',disconnect);
        }
    }
    Controller.$inject = serviceDependencies;

    return {
        restrict: 'E',
        controller: Controller,
        controllerAs: 'self',
        bindToController: true, //scope-bindings -> ctrl
        scope: {open: '='},
        template: template
    }
}

export default angular.module('won.owner.components.login', [])
    .directive('wonLogin', genLoginConf)
    .name;

