var controllers = angular.module('controllers', ['ngRoute', 'ngCookies']);

controllers.controller('commonController', ['$location', '$scope', 'loginService', '$cookieStore',
    function ($location, $scope, loginService, $cookieStore) {
        $scope.logout = function() {
            loginService.logout().then(function() {
                $cookieStore.remove('logged_in');
                $location.path('/login');
            });
        };
        $scope.loggedIn = function() {
            return $cookieStore.get('logged_in');
        }
    }]);

controllers.controller('customerController', ['$location', '$route', '$scope', '$cookieStore', 'customerService',
    function ($location, $route, $scope, $cookieStore, customerService) {
        if (!$cookieStore.get('access_token')) {
            $cookieStore.remove('logged_in');
            $location.path('/login');
            return;
        } else {
            $cookieStore.put('logged_in', 'yes');
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
                $cookieStore.put('logged_in', 'yes');
                $location.path('/');
            }).catch(function () {
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
    $httpProvider.interceptors.push(function($q, $location, $cookieStore) {
        return {
            'responseError': function(error) {
                if (error.status == 401) {
                    $cookieStore.remove('logged_in');
                    $location.path('/login');
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
        },
        logout: function() {
            return $http.get('api/logout');
        }
    }
});