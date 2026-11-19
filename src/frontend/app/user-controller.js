var module = angular.module('scorekeep');
module.controller('UserController', User);
function User($scope, $http, UserService, api) {
  $scope.players = UserService.query();

  $scope.username = "我的名称";

  $scope.createPlayer = function () {
    player = new UserService();
    player.$save(function() {
      player.name = $scope.username;
      player.$update({ id: player.id }, function() {
        $scope.players.push(angular.copy(player));
      });
    });
  };

  $scope.deletePlayer = function (index) {
    var playerId = $scope.players[index].id;
    $http.delete(api + 'user/' + playerId);
    $scope.players.splice(index, 1);
  };
}
