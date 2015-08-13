/**
 *
 * Created by ksinger on 07.07.2015.
 */
;
export default function wonAppTag() {
    let template = `
        <header>
            <nav class="topnav">
                <div class="topnav__left">
                    <a href="#" class="topnav__button">
                        <img src="generated/icon-sprite.svg#WON_ico_header" class="bigicon">
                        <span class="topnav__page-title">Web of Needs</span>
                    </a>
                </div>
                <div class="topnav__center">
                    <a href="#" class="topnav__button">
                        <img src="generated/icon-sprite.svg#ico36_plus" class="bigicon">
                        <span class="topnav__button__caption">New Need</span>
                    </a>
                </div>
                <div class="topnav__right">
                    <ul class="topnav__list">
                        <li class="">
                            <a href="#" class="topnav__button">
                                <span class="topnav__button__caption">Groups</span>
                                <img src="generated/icon-sprite.svg#ico16_arrow_down" class="topnav__carret">
                                <img src="generated/icon-sprite.svg#ico36_group" class="bigicon">
                            </a>
                        </li>
                        <li>
                            <a href="#" class="topnav__button">
                                <span class="topnav__button__caption">Username</span>
                                <img src="generated/icon-sprite.svg#ico16_arrow_down" class="topnav__carret">
                                <img src="generated/icon-sprite.svg#ico36_person" class="bigicon">
                            </a>
                        </li>
                    </ul>
                </div>
            </nav>
            <nav ng-cloak ng-show="{{false}}" class="mainTabs">
                <div class="mainTabs__left"></div>
                <div class="mainTabs__center">
                    <ul class="tabs">
                        <li class=""><a href="#">Feed</a></li>
                        <li><a href="#">Posts
                            <span class="tabs__unread">5</span>
                        </a></li>
                        <li class="selectedTab"><a href="#">Incoming Requests
                            <span class="tabs__unread">5</span>
                        </a></li>
                        <li><a href="#">Matches
                            <span class="tabs__unread">18</span>
                        </a></li>
                    </ul>
                </div>
                <div class="mainTabs__right">
                    <a href="#">
                        <img src="generated/icon-sprite.svg#ico36_search_nomargin" class="mainTabs__right__searchicon">
                    </a>
                </div>
            </nav>
            <nav ng-cloak ng-show="{{true}}" class="createNeedTitle">
                <div class="cNT_left">x</div>
                <div class="cNT__center">What is your need?</div>
                <div class="cNT__right">
                </div>
            </nav>
        </header>
        <section id="main" class="contentArea">
            <h1 ng-click="ctrl.fooFun()">Hello, from your lovely app-directive! Foo{{ctrl.foo}}! </h1>

            <div class="speechbubbletest">Hello!</div>

        </section>
        <footer></footer>
        `;

    console.log('updated this file at last');

    function link() {
        console.log(`multiline
        template
        string`)
    }

    /*
    function wonAppCtrl() {
        this.foo = 'bar';
        this.fooFun = () => {
            console.log('fooFun called!');
        }
    }
    */


    class WonAppCtrl {
        constructor() {
            this.foo = 'bar';
        }
        fooFun () {
            console.log('fooFun called!');
        }
    }
    WonAppCtrl.$inject = ['$http'/*injections as strings here*/];


    let directive = {
        restrict: 'E',
        link: link,
        //http://blog.thoughtram.io/angularjs/2015/01/02/exploring-angular-1.3-bindToController.html
        controllerAs: 'ctrl',
        controller: WonAppCtrl,
        template: template
    }
    return directive
}
