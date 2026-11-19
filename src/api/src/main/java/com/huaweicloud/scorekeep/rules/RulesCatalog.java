package com.huaweicloud.scorekeep.rules;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 规则目录：按标识装载可用规则。 */
@Component
public class RulesCatalog {

    private static final String STATE_FORMAT_HINT =
            "局面文本为 9 个字符，对应 1-9 号格位；落子文本形如 X5。";

    private final Map<String, GameRules> byId = new LinkedHashMap<>();

    public RulesCatalog() {
        register(new TicTacToe());
    }

    private void register(GameRules rules) {
        byId.put(rules.id(), rules);
    }

    public Optional<GameRules> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<GameRules.RulesInfo> describe(String id) {
        return find(id).map(RulesCatalog::toInfo);
    }

    public List<GameRules.RulesInfo> list() {
        return byId.values().stream()
                .map(RulesCatalog::toInfo)
                .toList();
    }

    private static GameRules.RulesInfo toInfo(GameRules rules) {
        return new GameRules.RulesInfo(
                rules.id(),
                rules.displayName(),
                rules.initialState(),
                STATE_FORMAT_HINT);
    }
}
