package com.huaweicloud.scorekeep.web;

import com.huaweicloud.scorekeep.rules.GameRules;
import com.huaweicloud.scorekeep.rules.RulesCatalog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 规则查询接口。 */
@RestController
@RequestMapping("/api/rules")
public class RulesController {

    private final RulesCatalog rulesCatalog;

    public RulesController(RulesCatalog rulesCatalog) {
        this.rulesCatalog = rulesCatalog;
    }

    /** 列出全部可用规则。 */
    @GetMapping
    public List<GameRules.RulesInfo> list() {
        return rulesCatalog.list();
    }

    /** 查询单条规则。 */
    @GetMapping("/{rulesId}")
    public GameRules.RulesInfo get(@PathVariable String rulesId) {
        return rulesCatalog.describe(rulesId)
                .orElseThrow(() -> new ResourceNotFoundException("规则", rulesId));
    }
}
