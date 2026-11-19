package com.huaweicloud.scorekeep.service;

import com.huaweicloud.scorekeep.domain.Player;
import com.huaweicloud.scorekeep.notification.EventNotifier;
import com.huaweicloud.scorekeep.repository.ScorekeepRepository;
import com.huaweicloud.scorekeep.util.Identifiers;
import com.huaweicloud.scorekeep.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/** 参与者相关业务逻辑。 */
@Service
public class PlayerService {

    private static final List<String> SAMPLE_NAMES = List.of("Billy", "Jake", "Emma", "Ralph", "Lucy");

    private static final Random RANDOM = new Random();

    private final ScorekeepRepository repository;

    private final EventNotifier notifier;

    public PlayerService(ScorekeepRepository repository, EventNotifier notifier) {
        this.repository = repository;
        this.notifier = notifier;
    }

    /** 创建参与者并随机分配名称，同时发出事件通知。 */
    public Player createWithGeneratedName() {
        Player player = create(pickName());
        notifier.notify("参与者已创建", "名称：" + player.getName());
        return player;
    }

    public Player create(String name) {
        Player player = new Player(Identifiers.random(), name);
        repository.savePlayer(player);
        return player;
    }

    public Player get(String playerId) {
        return repository.findPlayer(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("参与者", playerId));
    }

    public List<Player> list() {
        return repository.listPlayers();
    }

    public Player save(Player player) {
        repository.savePlayer(player);
        return player;
    }

    public void delete(String playerId) {
        get(playerId);
        repository.deletePlayer(playerId);
    }

    private static String pickName() {
        return SAMPLE_NAMES.get(RANDOM.nextInt(SAMPLE_NAMES.size()));
    }
}
