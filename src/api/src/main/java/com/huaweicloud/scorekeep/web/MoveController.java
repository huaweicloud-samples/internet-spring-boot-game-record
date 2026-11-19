package com.huaweicloud.scorekeep.web;

import com.huaweicloud.scorekeep.domain.Move;
import com.huaweicloud.scorekeep.service.MoveService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 落子接口。 */
@RestController
@RequestMapping("/api/move/{sessionId}/{gameId}")
public class MoveController {

    private final MoveService moveService;

    public MoveController(MoveService moveService) {
        this.moveService = moveService;
    }

    /** 列出该对局下的全部落子。 */
    @GetMapping
    public List<Move> list(@PathVariable String sessionId, @PathVariable String gameId) {
        return moveService.list(sessionId, gameId);
    }

    /** 由指定参与者落一子。 */
    @PostMapping("/{playerId}")
    public Move apply(@PathVariable String sessionId,
                      @PathVariable String gameId,
                      @PathVariable String playerId,
                      @RequestBody String moveText) {
        return moveService.apply(sessionId, gameId, playerId, normalize(moveText));
    }

    /** 查询单条落子记录。 */
    @GetMapping("/{moveId}")
    public Move get(@PathVariable String sessionId,
                    @PathVariable String gameId,
                    @PathVariable String moveId) {
        return moveService.get(sessionId, gameId, moveId);
    }

    /**
     * 兼容两种请求体形态：以 JSON 字符串提交时去掉外层引号，
     * 以纯文本提交时原样使用。
     */
    private static String normalize(String moveText) {
        if (moveText == null) {
            return "";
        }
        String trimmed = moveText.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }
}
