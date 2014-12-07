var controllers = angular.module('controllers', ['ngRoute']);

controllers.controller('customerController', ['$location', '$route', '$scope', 'customerService',
    function ($location, $route, $scope, customerService) {
        if (!$scope.token) {
            $location.path('/login');
            return;
        }
        customerService.getCustomers().then(function (result) {
            $scope.customers = result.data;
        });
        $scope.deleteCustomer = function (id) {
            customerService.deleteCustomer(id, $scope.token);
        };
        $scope.addCustomer = function (fn, ln) {
            customerService.addCustomer(fn, ln, $scope.token);
        };
        $scope.openAddCustomerForm = function () {
            $location.path('add');
        };
        $scope.closeAddCustomerForm = function () {
            $location.path('/');
        };
    }]);

controllers.controller('loginController', ['$scope', '$location', 'loginService',
    function ($scope, $location, loginService) {
        $scope.login = function (username, password) {
            loginService.login(username, password).then(function (response) {
                $scope.token = response.data.token;
                $location.path('/');
            }).catch(function (error) {
                console.log(error);
            });
        }
    }]);

controllers.factory('customerService', function ($http) {
    return {
        getCustomers: function (token) {
            return $http.get('api/customer', {
                headers: {
                    'Access-Token': token
                }
            })
        },
        addCustomer: function (fn, ln, token) {
            return $http.post('api/customer', {
                headers: {
                    'Access-Token': token
                },
                firstName: fn,
                lastName: ln
            }).then(function () {
                $location.path('/');
            }).catch(function (error) {
                $location.path('/login');
                console.log(error);
            });
        },
        deleteCustomer: function (id, token) {
            return $http.delete('api/customer/' + id, {
                headers: {
                    'Access-Token': token
                }
            }).then(function () {
                console.log('deleted');
                $route.reload();
            }).catch(function (error) {
                $location.path('/login');
            });
        }
    }
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