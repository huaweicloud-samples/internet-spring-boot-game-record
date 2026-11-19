package com.huaweicloud.scorekeep.service;

import com.huaweicloud.scorekeep.domain.Game;
import com.huaweicloud.scorekeep.domain.GameState;
import com.huaweicloud.scorekeep.domain.Move;
import com.huaweicloud.scorekeep.notification.EventNotifier;
import com.huaweicloud.scorekeep.repository.ScorekeepRepository;
import com.huaweicloud.scorekeep.rules.GameRules;
import com.huaweicloud.scorekeep.rules.RulesCatalog;
import com.huaweicloud.scorekeep.util.Identifiers;
import com.huaweicloud.scorekeep.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 落子相关业务逻辑。
 *
 * <p>一次落子会依次完成：读取当前局面、按规则推导新局面、落库新局面、
 * 把落子与局面登记回对局；局面进入终局时发出事件通知。</p>
 */
@Service
public class MoveService {

    private final ScorekeepRepository repository;

    private final GameService gameService;

    private final StateService stateService;

    private final RulesCatalog rulesCatalog;

    private final EventNotifier notifier;

    public MoveService(ScorekeepRepository repository,
                       GameService gameService,
                       StateService stateService,
                       RulesCatalog rulesCatalog,
                       EventNotifier notifier) {
        this.repository = repository;
        this.gameService = gameService;
        this.stateService = stateService;
        this.rulesCatalog = rulesCatalog;
        this.notifier = notifier;
    }

    public List<Move> list(String sessionId, String gameId) {
        gameService.get(sessionId, gameId);
        return repository.listMovesByGame(gameId);
    }

    public Move get(String sessionId, String gameId, String moveId) {
        gameService.get(sessionId, gameId);
        return repository.findMove(moveId)
                .orElseThrow(() -> new ResourceNotFoundException("落子", moveId));
    }

    /** 应用一步落子，返回落库后的落子记录。 */
    public Move apply(String sessionId, String gameId, String playerId, String moveText) {
        Game game = gameService.get(sessionId, gameId);

        List<String> stateIds = game.getStates();
        if (stateIds == null || stateIds.isEmpty()) {
            throw new IllegalArgumentException("对局尚未开始，缺少可用局面");
        }
        GameState previous = stateService.get(sessionId, gameId, stateIds.get(stateIds.size() - 1));

        GameRules rules = rulesCatalog.find(game.getRules())
                .orElseThrow(() -> new IllegalArgumentException("规则不可用：" + game.getRules()));
        String nextStateText = rules.move(previous.getState(), moveText);

        Set<String> nextTurn = nextTurn(game.getPlayers(), playerId);
        GameState next = stateService.create(sessionId, gameId, nextStateText, nextTurn);

        if (GameRules.isTerminal(nextStateText)) {
            notifier.notify("对局结束", "末步参与者：" + playerId);
        }

        Move move = new Move(Identifiers.random(), sessionId, gameId, playerId, moveText);
        move.setCreatedTime(Instant.now().getEpochSecond());
        repository.saveMove(move);

        game.setMove(move.getId());
        game.setState(next.getId());
        repository.saveGame(game);

        return move;
    }

    /**
     * 推导下一步待落子的一方：单人局保持原样，多人局从参与者中排除刚完成落子的一方。
     */
    private static Set<String> nextTurn(Set<String> players, String playerId) {
        Set<String> turn = players == null ? new LinkedHashSet<>() : new LinkedHashSet<>(players);
        if (turn.size() != 1) {
            turn.remove(playerId);
        }
        return turn;
    }
}
