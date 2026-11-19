package com.huaweicloud.scorekeep.service;

import com.huaweicloud.scorekeep.domain.GameState;
import com.huaweicloud.scorekeep.repository.ScorekeepRepository;
import com.huaweicloud.scorekeep.util.Identifiers;
import com.huaweicloud.scorekeep.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/** 局面相关业务逻辑：每次落子后生成一份局面快照，并按对局归集。 */
@Service
public class StateService {

    private final ScorekeepRepository repository;

    public StateService(ScorekeepRepository repository) {
        this.repository = repository;
    }

    public GameState create(String sessionId, String gameId, String stateText, Set<String> turn) {
        requireSessionAndGame(sessionId, gameId);
        GameState state = new GameState(Identifiers.random(), sessionId, gameId, stateText, turn);
        repository.saveState(state);
        return state;
    }

    public GameState get(String sessionId, String gameId, String stateId) {
        requireSessionAndGame(sessionId, gameId);
        return repository.findState(stateId)
                .orElseThrow(() -> new ResourceNotFoundException("局面", stateId));
    }

    public List<GameState> list(String sessionId, String gameId) {
        requireSessionAndGame(sessionId, gameId);
        return repository.listStatesByGame(gameId);
    }

    private void requireSessionAndGame(String sessionId, String gameId) {
        repository.findSession(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("场次", sessionId));
        repository.findGame(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("对局", gameId));
    }
}
