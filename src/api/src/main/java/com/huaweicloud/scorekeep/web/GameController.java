package com.huaweicloud.scorekeep.web;

import com.huaweicloud.scorekeep.domain.Game;
import com.huaweicloud.scorekeep.service.GameService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/** 对局接口。 */
@RestController
@RequestMapping("/api/game/{sessionId}")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /** 列出该场次下的全部对局。 */
    @GetMapping
    public List<Game> list(@PathVariable String sessionId) {
        return gameService.list(sessionId);
    }

    /** 在该场次下新建对局。 */
    @PostMapping
    public Game create(@PathVariable String sessionId) {
        return gameService.create(sessionId);
    }

    /** 查询单个对局。 */
    @GetMapping("/{gameId}")
    public Game get(@PathVariable String sessionId, @PathVariable String gameId) {
        return gameService.get(sessionId, gameId);
    }

    /** 覆盖式更新对局。 */
    @PutMapping("/{gameId}")
    public Game update(@PathVariable String sessionId,
                       @PathVariable String gameId,
                       @RequestBody Game body) {
        body.setId(gameId);
        body.setSession(sessionId);
        return gameService.save(body);
    }

    /** 删除对局，并把它从所属场次中移除。 */
    @DeleteMapping("/{gameId}")
    public void delete(@PathVariable String sessionId, @PathVariable String gameId) {
        gameService.delete(sessionId, gameId);
    }

    /** 读取对局名称。 */
    @GetMapping("/{gameId}/name")
    public String getName(@PathVariable String sessionId, @PathVariable String gameId) {
        return gameService.get(sessionId, gameId).getName();
    }

    /** 设置对局名称。 */
    @PutMapping("/{gameId}/name/{name}")
    public void setName(@PathVariable String sessionId,
                        @PathVariable String gameId,
                        @PathVariable String name) {
        Game game = gameService.get(sessionId, gameId);
        game.setName(name);
        gameService.save(game);
    }

    /** 读取对局所用规则标识。 */
    @GetMapping("/{gameId}/rules")
    public String getRules(@PathVariable String sessionId, @PathVariable String gameId) {
        return gameService.get(sessionId, gameId).getRules();
    }

    /** 设置规则，并据此开出初始局面。 */
    @PutMapping("/{gameId}/rules/{rulesId}")
    public void setRules(@PathVariable String sessionId,
                         @PathVariable String gameId,
                         @PathVariable String rulesId) {
        gameService.applyRules(sessionId, gameId, rulesId);
    }

    /** 读取对局参与者。 */
    @GetMapping("/{gameId}/user")
    public Set<String> getPlayers(@PathVariable String sessionId, @PathVariable String gameId) {
        return gameService.get(sessionId, gameId).getPlayers();
    }

    /** 追加一个参与者。 */
    @PutMapping("/{gameId}/user/{playerId}")
    public void addPlayer(@PathVariable String sessionId,
                          @PathVariable String gameId,
                          @PathVariable String playerId) {
        Game game = gameService.get(sessionId, gameId);
        game.setPlayer(playerId);
        gameService.save(game);
    }

    /** 整组设置参与者。 */
    @PostMapping("/{gameId}/users")
    public void setPlayers(@PathVariable String sessionId,
                           @PathVariable String gameId,
                           @RequestBody Set<String> players) {
        Game game = gameService.get(sessionId, gameId);
        game.setPlayers(players);
        gameService.save(game);
    }

    /** 设置开始时间。 */
    @PutMapping("/{gameId}/starttime/{startTime}")
    public void setStartTime(@PathVariable String sessionId,
                             @PathVariable String gameId,
                             @PathVariable String startTime) {
        Game game = gameService.get(sessionId, gameId);
        game.setStartTime(Long.parseLong(startTime));
        gameService.save(game);
    }

    /** 设置结束时间。 */
    @PutMapping("/{gameId}/endtime/{endTime}")
    public void setEndTime(@PathVariable String sessionId,
                           @PathVariable String gameId,
                           @PathVariable String endTime) {
        Game game = gameService.get(sessionId, gameId);
        game.setEndTime(Long.parseLong(endTime));
        gameService.save(game);
    }

    /** 把落子记录登记到对局。 */
    @PutMapping("/{gameId}/move/{moveId}")
    public void addMove(@PathVariable String sessionId,
                        @PathVariable String gameId,
                        @PathVariable String moveId) {
        Game game = gameService.get(sessionId, gameId);
        game.setMove(moveId);
        gameService.save(game);
    }

    /** 把局面登记到对局。 */
    @PutMapping("/{gameId}/state/{stateId}")
    public void addState(@PathVariable String sessionId,
                         @PathVariable String gameId,
                         @PathVariable String stateId) {
        Game game = gameService.get(sessionId, gameId);
        game.setState(stateId);
        gameService.save(game);
    }
}
