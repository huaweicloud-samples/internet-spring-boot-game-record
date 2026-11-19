package com.huaweicloud.scorekeep.rules;

/**
 * 对局规则契约。
 *
 * <p>规则以纯函数形式实现：给定当前局面文本与一步落子，返回新的局面文本。
 * 局面文本的编码由各实现自行定义，但须满足：以约定前缀开头表示终局，
 * 便于上层据此判定是否需要发出对局结束通知。</p>
 */
public interface GameRules {

    /** 终局前缀：先手方胜。 */
    String OUTCOME_FIRST_WINS = "A";

    /** 终局前缀：后手方胜。 */
    String OUTCOME_SECOND_WINS = "B";

    /** 终局前缀：和局。 */
    String OUTCOME_DRAW = "D";

    /** 规则标识，用于接口路径与对局字段。 */
    String id();

    /** 规则展示名。 */
    String displayName();

    /** 初始局面文本。 */
    String initialState();

    /**
     * 应用一步落子。
     *
     * @param state    当前局面文本
     * @param moveText 落子描述
     * @return 新的局面文本
     * @throws InvalidMoveException 落子不合法
     */
    String move(String state, String moveText) throws InvalidMoveException;

    /** 判断局面文本是否表示终局。 */
    static boolean isTerminal(String stateText) {
        if (stateText == null || stateText.isEmpty()) {
            return false;
        }
        char prefix = stateText.charAt(0);
        return prefix == 'A' || prefix == 'B' || prefix == 'D';
    }

    /** 规则描述。 */
    record RulesInfo(String id, String name, String initialState, String description) {
    }
}
