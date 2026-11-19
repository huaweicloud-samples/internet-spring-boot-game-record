var module = angular.module('scorekeep');
module.service('GameCollection', function($http, GameService, api) {
  var collection = {};

  collection.createGame = function(sessionid, gamename, gamerules) {
    var game = new GameService();
    return game.$save({ sessionid: sessionid })
    .then(function(saved) {
      if ( saved && saved !== game ) { angular.extend(game, saved); }
      game.session = sessionid;
      game.name = gamename;
      game.rules = gamerules;
      return game.$update({ sessionid: sessionid, id: game.id })
      .then(function() {
        return game;
      });
    });
  };

  collection.setField = function(sessionid, gameid, fieldname, value) {
    return $http.put(api + 'game/' + sessionid + '/' + gameid + '/' + fieldname + '/' + value);
  };

  collection.setPlayers = function(sessionid, gameid, players){
    return $http.post(api + 'game/' + sessionid + '/' + gameid + '/users', players);
  };

  collection.deleteGame = function(sessionid, gameid) {
    return $http.delete(api + 'game/' + sessionid + '/' + gameid);
  };

  collection.move = function(sessionid, gameid, userid, move) {
    return $http.post(api + 'move/' + sessionid + '/' + gameid + '/' + userid, move);
  };

  return collection;
});
