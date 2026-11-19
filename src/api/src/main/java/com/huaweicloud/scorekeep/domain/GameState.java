package com.huaweicloud.scorekeep.domain;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.util.LinkedHashSet;
import java.util.Set;

/** 局面：一次落子之后的对局状态快照。 */
@DynamoDbBean
public class GameState {

    private String id;
    private String game;
    private String session;
    private String state;
    private Set<String> turn;

    public GameState() {
    }

    public GameState(String id, String session, String game, String state, Set<String> turn) {
        this.id = id;
        this.session = session;
        this.game = game;
        this.state = state;
        this.turn = turn == null || turn.isEmpty() ? null : new LinkedHashSet<>(turn);
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** 所属对局，同时是 {@code game-index} 的分区键。 */
    @DynamoDbSecondaryPartitionKey(indexNames = TableNames.STATE_GAME_INDEX)
    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    /** 局面文本，由规则实现解释。 */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /** 待落子的一方；为空表示本局已结束。 */
    public Set<String> getTurn() {
        return turn;
    }

    public void setTurn(Set<String> turn) {
        this.turn = turn == null || turn.isEmpty() ? null : new LinkedHashSet<>(turn);
    }
}
