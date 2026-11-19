var module = angular.module('scorekeep');
module.controller('GameController', Game);
function Game($q, $scope, $http, $interval, $routeParams, UserService, GameService, StateService, api) {
  var EMPTY = '.';
  var BOARD_SIZE = 9;

  $scope.game = new GameService();
  $scope.state = new StateService();
  $scope.cells = [];
  $scope.gamestate = [];
  $scope.moving = 0;
  $scope.user = UserService.get({ id: $routeParams.userid });
  $scope.winner = '';

  /* 局面文本为 9 个字符的棋盘；终局时前面多一个结果字符。
     展示用的 gamestate 以 1..9 为下标，与棋盘格位一一对应，下标 0 留给当前应落子方。 */
  function applyState(stateText) {
    var text = stateText || '';
    var terminal = text.length === BOARD_SIZE + 1;
    var board = terminal ? text.substring(1) : text;
    $scope.cells = board.split('');
    $scope.gamestate = [terminal ? text.charAt(0) : turnOf($scope.cells)]
      .concat($scope.cells.map(function (cell) {
        return cell === EMPTY ? '' : cell;
      }));

    var outcome = terminal ? text.charAt(0) : '';
    if (outcome === 'A') {
      $scope.winner = 'X 获胜';
    } else if (outcome === 'B') {
      $scope.winner = 'O 获胜';
    } else if (outcome === 'D') {
      $scope.winner = '平局';
    } else {
      $scope.winner = '';
    }
  }

  function turnOf(cells) {
    var filled = cells.filter(function (cell) {
      return cell !== EMPTY;
    }).length;
    return filled % 2 === 0 ? 'X' : 'O';
  }

  function loadLatestState() {
    return $scope.game.$get({ sessionid: $routeParams.sessionid, id: $routeParams.gameid })
      .then(function () {
        var states = $scope.game.states;
        if (states == null || states.length === 0) {
          return null;
        }
        return $scope.state.$get({
          sessionid: $routeParams.sessionid,
          gameid: $routeParams.gameid,
          id: states[states.length - 1]
        });
      })
      .then(function (result) {
        if (result) {
          applyState($scope.state.state);
        }
      });
  }

  $scope.playgame = function () {
    return $q(function (resolve) {
      loadLatestState().then(resolve, resolve);
    });
  };

  $scope.playgame();
  $scope.interval = $interval(function () {
    $scope.playgame();
  }, 5000);

  $scope.move = function (cellid) {
    if ($scope.moving === 1 || $scope.winner !== '') {
      return;
    }
    var index = cellid - 1;
    if (index < 0 || index >= BOARD_SIZE || $scope.cells[index] !== EMPTY) {
      return;
    }
    $scope.moving = 1;
    var move = turnOf($scope.cells) + cellid;

    $http.post(api + 'move/' + $routeParams.sessionid + '/' + $routeParams.gameid + '/' + $routeParams.userid, move)
      .then(function () {
        return loadLatestState();
      })
      .then(function () {
        $scope.moving = 0;
      }, function () {
        $scope.moving = 0;
        $scope.playgame();
      });
  };

  $scope.$on('$destroy', function () {
    if ($scope.interval)
        $interval.cancel($scope.interval);
  });
}
