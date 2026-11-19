package com.huaweicloud.scorekeep.rules;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 规则目录的装载与查询。 */
class RulesCatalogTest {

    private final RulesCatalog catalog = new RulesCatalog();

    @Test
    void exposesRegisteredRules() {
        List<GameRules.RulesInfo> rules = catalog.list();
        assertEquals(1, rules.size());
        assertEquals("TicTacToe", rules.get(0).id());
        assertEquals(".........", rules.get(0).initialState());
        assertFalse(rules.get(0).name().isBlank());
    }

    @Test
    void findsRuleByIdentifier() {
        assertTrue(catalog.find("TicTacToe").isPresent());
        assertFalse(catalog.find("Chess").isPresent());
    }

    @Test
    void describesKnownRuleAndRejectsUnknown() {
        assertTrue(catalog.describe("TicTacToe").isPresent());
        assertTrue(catalog.describe("Chess").isEmpty());
    }
}
