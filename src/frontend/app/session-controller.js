var module = angular.module('scorekeep');
module.controller('SessionController', Session);
function Session($scope, $http, $location, $interval, $routeParams, SessionService, RulesService, GameCollection, SessionCollection, UserService, GameService, api) {
  $scope.games = GameService.query({ sessionid: $routeParams.sessionid });
  $scope.session = new SessionService();
  $scope.user = UserService.get({ id: $routeParams.userid });
  $scope.allrules = RulesService.query();

  $scope.loadSession = function() {
    GetSession = $scope.games.$promise.then(function(result) {
      return $scope.session.$get({ id: $routeParams.sessionid });
    });
    GetSession.then(function() {
      // 对齐会话中登记的对局与已加载的对局列表
      gameids = [];
      if ( $scope.games == null ) {
        $scope.games = [];
      }
      for (var i = 0; i < $scope.games.length; i++) {
        // 已从场次中移除的对局，从列表中剔除
        if ( !$scope.session.games || !$scope.session.games.includes($scope.games[i].id) ) {
          $scope.games.splice(i, 1);
          i--;
        } else {
          gameids.push($scope.games[i].id);
        }
      }
      if ( $scope.session.games == null ) {
        $scope.session.games = [];
      }
      for (var i = 0; i < $scope.session.games.length; i++) {
        if ( !gameids.includes($scope.session.games[i]) ) {
          game = new GameService();
          game.$get({ id: $scope.session.games[i], sessionid: $routeParams.sessionid });
          $scope.games.push(game);
        }
      }
    });
  };
  $scope.loadSession();
  $scope.interval = $interval(function(){
    $scope.loadSession();
  }, 5000);

  $scope.createGame = function (gamename, gamerules) {
    var sessionid = $routeParams.sessionid;
    CreateGame = GameCollection.createGame(sessionid, gamename, gamerules);
    CreateGame.then(function(game) {
      $scope.games.push(game);
    });
  };

  $scope.setGameRules = function(gameid, rulesid){
    return GameCollection.setField($scope.session.id, gameid, "rules", rulesid);
  };

  $scope.startGame = function(index){
    players = $scope.games[index].players = $scope.session.players;
    gameid = $scope.games[index].id;
    rulesid = $scope.games[index].rules;
    sessionid = $scope.session.id;
    SetPlayers = GameCollection.setPlayers(sessionid, gameid, players);
    SetRules = SetPlayers.then(function() {
      return $scope.setGameRules(gameid, rulesid);
    });
    SetStartTime = SetRules.then(function() {
      time = $scope.games[index].startTime = Date.now();
      return GameCollection.setField($scope.session.id, $scope.games[index].id, "starttime", time);
    });
    SetStartTime.then(function() {
      $location.path('/game/' + $scope.session.id + '/' + $scope.games[index].id + '/' + $scope.user.id);
    });
  };

  $scope.endGame = function(index){
    time = $scope.games[index].endTime = Date.now();
    GameCollection.setField($scope.session.id, $scope.games[index].id, "endtime", time);
  };

  $scope.deleteGame = function (index) {
    var sessionid = $routeParams.sessionid;
    var gameid = $scope.games[index].id;
    DeleteGame = GameCollection.deleteGame(sessionid, gameid);
    DeleteGame.then(function () {
      $scope.games.splice(index, 1);
    });
  };

  $scope.deleteSession = function () {
    var sessionId = $scope.session.id;
    DeleteSession = SessionCollection.deleteSession(sessionId);
    DeleteSession.then(function() {
      $location.path('/');
    });
  };

  $scope.$on('$destroy', function(){
    if($scope.interval)
        $interval.cancel($scope.interval);
  });
}
