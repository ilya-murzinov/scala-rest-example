var app = angular.module('app', [
    'ngRoute',
    'controllers'
]);

app.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/', {
                templateUrl: 'fragments/customer-list.html',
                controller: 'customerController'
            }).when('/add', {
                templateUrl: 'fragments/customer-add.html',
                controller: 'customerController'
            }).
            when('/login', {
                templateUrl: 'fragments/login.html',
                controller: 'loginController'
            }).otherwise({
                redirectTo: '/'
            });
    }]);