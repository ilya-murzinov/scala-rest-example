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
 app.directive('ngEnter', function() {
     return function(scope, element, attrs) {
         element.bind("keydown keypress", function(event) {
             if(event.which === 13) {
                 scope.$apply(function(){
                     scope.$eval(attrs.ngEnter, {'event': event});
                 });

                 event.preventDefault();
             }
         });
     };
 });