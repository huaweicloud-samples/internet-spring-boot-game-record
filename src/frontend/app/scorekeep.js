var module = angular.module("scorekeep", ["ngRoute", "ngResource"]);

/* 页面与接口由同一进程、同一域名与端口提供，无需跨域 */
module.value('api', '/api/');

/* 接口与页面分处不同来源时，改为接口的完整地址
module.value('api', 'http://<服务地址>/api/');
*/
