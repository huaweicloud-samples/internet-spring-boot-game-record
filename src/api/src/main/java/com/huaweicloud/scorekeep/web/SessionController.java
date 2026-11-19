package com.huaweicloud.scorekeep.web;

import com.huaweicloud.scorekeep.domain.GameSession;
import com.huaweicloud.scorekeep.service.SessionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 场次接口。 */
@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /** 新建场次。 */
    @PostMapping
    public GameSession create() {
        return sessionService.create();
    }

    /** 列出全部场次。 */
    @GetMapping
    public List<GameSession> list() {
        return sessionService.list();
    }

    /** 查询单个场次。 */
    @GetMapping("/{sessionId}")
    public GameSession get(@PathVariable String sessionId) {
        return sessionService.get(sessionId);
    }

    /** 覆盖式更新场次：名称、归属者与参与者集合均以请求体为准。 */
    @PutMapping("/{sessionId}")
    public GameSession update(@PathVariable String sessionId, @RequestBody GameSession body) {
        body.setId(sessionId);
        return sessionService.save(body);
    }

    /** 删除场次。 */
    @DeleteMapping("/{sessionId}")
    public void delete(@PathVariable String sessionId) {
        sessionService.delete(sessionId);
    }

    /** 设置归属者。 */
    @PutMapping("/{sessionId}/owner/{ownerId}")
    public GameSession setOwner(@PathVariable String sessionId, @PathVariable String ownerId) {
        GameSession session = sessionService.get(sessionId);
        session.setOwner(ownerId);
        return sessionService.save(session);
    }

    /** 把对局登记到场次下。 */
    @PutMapping("/{sessionId}/game/{gameId}")
    public void addGame(@PathVariable String sessionId, @PathVariable String gameId) {
        sessionService.addGame(sessionId, gameId);
    }
}
