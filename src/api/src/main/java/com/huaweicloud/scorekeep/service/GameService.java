package com.huaweicloud.scorekeep.service;

import com.huaweicloud.scorekeep.domain.Game;
import com.huaweicloud.scorekeep.domain.GameState;
import com.huaweicloud.scorekeep.repository.ScorekeepRepository;
import com.huaweicloud.scorekeep.rules.GameRules;
import com.huaweicloud.scorekeep.rules.RulesCatalog;
import com.huaweicloud.scorekeep.util.Identifiers;
import com.huaweicloud.scorekeep.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/** 对局相关业务逻辑。 */
@Service
public class GameService {

    private final ScorekeepRepository repository;

    private final SessionService sessionService;

    private final StateService stateService;

    private final RulesCatalog rulesCatalog;

    public GameService(ScorekeepRepository repository,
                       SessionService sessionService,
                       StateService stateService,
                       RulesCatalog rulesCatalog) {
        this.repository = repository;
        this.sessionService = sessionService;
        this.stateService = stateService;
        this.rulesCatalog = rulesCatalog;
    }

    /** 新建对局，并登记到场次下。 */
    public Game create(String sessionId) {
        sessionService.get(sessionId);
        Game game = new Game(Identifiers.random(), sessionId);
        repository.saveGame(game);
        sessionService.addGame(sessionId, game.getId());
        return game;
    }

    public Game get(String sessionId, String gameId) {
        sessionService.get(sessionId);
        return repository.findGame(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("对局", gameId));
    }

    public List<Game> list(String sessionId) {
        sessionService.get(sessionId);
        return repository.listGamesBySession(sessionId);
    }

    public Game save(Game game) {
        sessionService.get(game.getSession());
        repository.saveGame(game);
        return game;
    }

    /** 设置规则，并以规则给出的初始局面开出一份新局面。 */
    public Game applyRules(String sessionId, String gameId, String rulesId) {
        Game game = get(sessionId, gameId);
        GameRules rules = rulesCatalog.find(rulesId)
                .orElseThrow(() -> new IllegalArgumentException("规则不可用：" + rulesId));
        game.setRules(rulesId);
        GameState state = stateService.create(sessionId, gameId, rules.initialState(), game.getPlayers());
        game.setState(state.getId());
        repository.saveGame(game);
        return game;
    }

    public void delete(String sessionId, String gameId) {
        repository.findGame(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("对局", gameId));
        repository.deleteGame(gameId);
        sessionService.removeGame(sessionId, gameId);
    }
}
