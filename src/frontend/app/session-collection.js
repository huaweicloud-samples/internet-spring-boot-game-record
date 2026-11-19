var module = angular.module('scorekeep');
module.service('SessionCollection', function($http, SessionService, UserService, api) {
  var collection = {};

  collection.createSession = function(sessionname, userid) {
    var session = new SessionService();
    return session.$save()
    .then(function(saved) {
      if ( saved && saved !== session ) { angular.extend(session, saved); }
      session.name = sessionname;
      session.owner = userid;
      return session.$update({ id: session.id })
      .then(function() {
        return session;
      });
    });
  };

  collection.joinSession = function(sessionid, userid) {
    var session = new SessionService();
    return session.$get({ id: sessionid })
    .then(function(loaded) {
      if ( loaded && loaded !== session ) { angular.extend(session, loaded); }
      if ( session.players == null ) {
        session.players = [ userid ];
      } else if ( session.players.indexOf(userid) < 0 ) {
        session.players.push(userid);
      }
      return session.$update({ id: session.id || sessionid })
      .then(function() {
        return session;
      });
    });
  };

  collection.deleteSession = function(sessionid) {
    return $http.delete(api + 'session/' + sessionid);
  };

  return collection;
});
