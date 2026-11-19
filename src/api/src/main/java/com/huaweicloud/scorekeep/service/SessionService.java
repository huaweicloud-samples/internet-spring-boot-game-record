package com.huaweicloud.scorekeep.service;

import com.huaweicloud.scorekeep.domain.GameSession;
import com.huaweicloud.scorekeep.repository.ScorekeepRepository;
import com.huaweicloud.scorekeep.util.Identifiers;
import com.huaweicloud.scorekeep.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/** 场次相关业务逻辑。 */
@Service
public class SessionService {

    private final ScorekeepRepository repository;

    public SessionService(ScorekeepRepository repository) {
        this.repository = repository;
    }

    public GameSession create() {
        GameSession session = new GameSession(Identifiers.random());
        repository.saveSession(session);
        return session;
    }

    public GameSession get(String sessionId) {
        return repository.findSession(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("场次", sessionId));
    }

    public List<GameSession> list() {
        return repository.listSessions();
    }

    public GameSession save(GameSession session) {
        repository.saveSession(session);
        return session;
    }

    public void delete(String sessionId) {
        get(sessionId);
        repository.deleteSession(sessionId);
    }

    /** 把对局登记到场次下。 */
    public void addGame(String sessionId, String gameId) {
        GameSession session = get(sessionId);
        session.addGame(gameId);
        repository.saveSession(session);
    }

    /** 把对局从场次下移除；移除后场次不再持有任何对局时，该字段置空。 */
    public void removeGame(String sessionId, String gameId) {
        GameSession session = get(sessionId);
        session.removeGame(gameId);
        repository.saveSession(session);
    }
}
