var controllers = angular.module('controllers', ['ngRoute', 'ngCookies']);

controllers.controller('customerController', ['$location', '$route', '$scope', '$cookieStore', 'customerService',
    function ($location, $route, $scope, $cookieStore, customerService) {
        if (!$cookieStore.get('access_token')) {
            $location.path('/login');
            return;
        }
        customerService.getCustomers().then(function (result) {
            $scope.customers = result.data;
        });
        $scope.deleteCustomer = function (id) {
            customerService.deleteCustomer(id).then(function () {
                $route.reload();
            });
        };
        $scope.addCustomer = function (fn, ln) {
            customerService.addCustomer(fn, ln).then(function () {
                $location.path('/');
            });
        };
        $scope.openAddCustomerForm = function () {
            $location.path('add');
        };
        $scope.closeAddCustomerForm = function () {
            $location.path('/');
        };
    }]);

controllers.controller('loginController', ['$scope', '$location', '$cookieStore', 'loginService',
    function ($scope, $location, $cookieStore, loginService) {
        $scope.login = function (username, password) {
            loginService.login(username, password).then(function (response) {
                $cookieStore.put('access_token', response.data.token);
                $location.path('/');
            }).catch(function (error) {
                console.log(error);
                alert('Not logged in! :(')
            });
        }
    }]);

controllers.factory('customerService', function ($http) {
    return {
        getCustomers: function () {
            return $http.get('api/customer')
        },
        addCustomer: function (fn, ln) {
            return $http.post('api/customer', {
                firstName: fn,
                lastName: ln
            })
        },
        deleteCustomer: function (id) {
            return $http.delete('api/customer/' + id)
        }
    }
}).config(function($httpProvider) {
    $httpProvider.interceptors.push(function($q, $location) {
        return {
            'responseError': function(error) {
                if (error) {
                    $location.path('/login')
                }
                return $q.reject(error);
            }
        };
    });
});

controllers.factory('loginService', function ($http) {
    return {
        login: function (username, password) {
            return $http.post('api/tokens', {
                username: username,
                password: password
            });
        }
    }
});