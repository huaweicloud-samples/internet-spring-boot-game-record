package com.huaweicloud.scorekeep.web;

import com.huaweicloud.scorekeep.domain.Player;
import com.huaweicloud.scorekeep.service.PlayerService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 参与者接口。
 *
 * <p>不带名称创建时由服务端随机分配名称并发出事件通知；
 * 显式提供名称时直接落库。</p>
 */
@RestController
@RequestMapping("/api/user")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /** 创建参与者。 */
    @PostMapping
    public Player create(@RequestBody(required = false) Player body) {
        if (body == null || body.getName() == null || body.getName().isBlank()) {
            return playerService.createWithGeneratedName();
        }
        return playerService.create(body.getName());
    }

    /** 列出全部参与者。 */
    @GetMapping
    public List<Player> list() {
        return playerService.list();
    }

    /** 查询单个参与者。 */
    @GetMapping("/{playerId}")
    public Player get(@PathVariable String playerId) {
        return playerService.get(playerId);
    }

    /** 覆盖式更新参与者。 */
    @PutMapping("/{playerId}")
    public Player update(@PathVariable String playerId, @RequestBody Player body) {
        body.setId(playerId);
        return playerService.save(body);
    }

    /** 删除参与者。 */
    @DeleteMapping("/{playerId}")
    public void delete(@PathVariable String playerId) {
        playerService.delete(playerId);
    }
}
