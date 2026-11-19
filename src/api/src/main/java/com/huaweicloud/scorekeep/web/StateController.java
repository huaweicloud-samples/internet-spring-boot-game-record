package com.huaweicloud.scorekeep.web;

import com.huaweicloud.scorekeep.domain.GameState;
import com.huaweicloud.scorekeep.service.StateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 局面接口：局面由落子过程产生，此处只提供读取。 */
@RestController
@RequestMapping("/api/state/{sessionId}/{gameId}")
public class StateController {

    private final StateService stateService;

    public StateController(StateService stateService) {
        this.stateService = stateService;
    }

    /** 列出该对局下的全部局面。 */
    @GetMapping
    public List<GameState> list(@PathVariable String sessionId, @PathVariable String gameId) {
        return stateService.list(sessionId, gameId);
    }

    /** 查询单个局面。 */
    @GetMapping("/{stateId}")
    public GameState get(@PathVariable String sessionId,
                         @PathVariable String gameId,
                         @PathVariable String stateId) {
        return stateService.get(sessionId, gameId, stateId);
    }
}
