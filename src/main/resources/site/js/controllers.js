var controllers = angular.module('controllers', ['ngRoute']);

controllers.controller('customerController', function ($location, $route, $scope, customerService) {
    customerService.getCustomers().then(function (response) {
        $scope.customers = response;
    });
    $scope.deleteCustomer = function (id) {
        customerService.deleteCustomer(id);
    };
    $scope.addCustomer = function (fn, ln) {
        customerService.addCustomer(fn, ln);
    };
    $scope.openAddCustomerForm = function () {
        $location.path('add');
    };
    $scope.closeAddCustomerForm = function () {
        $location.path('/');
    };
});

controllers.factory('customerService', function ($http, $route, $location) {
    return {
        getCustomers: function () {
            return $http.get('api/customer')
                .then(function (result) {
                    return result.data;
                });
        },
        addCustomer: function (fn, ln) {
            return $http.post('api/customer', {
                firstName: fn,
                lastName: ln
            }).then(function () {
                $location.path('/');
            });
        },
        deleteCustomer: function (id) {
            return $http.delete('api/customer/' + id)
                .then(function () {
                    console.log('deleted');
                    $route.reload();
                })
        }
    }
});