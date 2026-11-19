package com.huaweicloud.scorekeep.domain;

/**
 * 表名与索引名常量。
 *
 * <p>表名前缀可经环境变量 {@code TABLE_PREFIX} 覆盖，便于多环境隔离。</p>
 */
public final class TableNames {

    private static final String PREFIX = System.getenv().getOrDefault("TABLE_PREFIX", "scorekeep");

    public static final String PLAYER = PREFIX + "-user";
    public static final String SESSION = PREFIX + "-session";
    public static final String GAME = PREFIX + "-game";
    public static final String MOVE = PREFIX + "-move";
    public static final String STATE = PREFIX + "-state";

    /** 对局表上按场次检索的二级索引。 */
    public static final String GAME_SESSION_INDEX = "session-index";

    /** 落子表、局面表上按对局检索的二级索引。 */
    public static final String MOVE_GAME_INDEX = "game-index";

    public static final String STATE_GAME_INDEX = "game-index";

    /** 承载二级索引分区键的属性名。 */
    public static final String GAME_SESSION_INDEX_ATTRIBUTE = "session";

    public static final String GAME_INDEX_ATTRIBUTE = "game";

    private TableNames() {
    }
}
