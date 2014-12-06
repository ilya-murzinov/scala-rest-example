var app = angular.module('app', []);

app.controller('customerController', function($scope, myService) {
    myService.getCustomers().then(function(response) {
        $scope.customers = response;
    });
});

app.factory('customerService', function($http) {
    return {
        getCustomers: function() {
            return $http.get('api/customer')
                .then(function(result) {
                    return result.data;
                });
        }
    }
});