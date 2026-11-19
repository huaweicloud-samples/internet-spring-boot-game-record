package com.huaweicloud.scorekeep.domain;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

/** 落子记录。 */
@DynamoDbBean
public class Move {

    private String id;
    private String game;
    private String session;
    private String player;
    private String move;
    private Long createdTime;

    public Move() {
    }

    public Move(String id, String session, String game, String player, String move) {
        this.id = id;
        this.session = session;
        this.game = game;
        this.player = player;
        this.move = move;
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** 所属对局，同时是 {@code game-index} 的分区键。 */
    @DynamoDbSecondaryPartitionKey(indexNames = TableNames.MOVE_GAME_INDEX)
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

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getMove() {
        return move;
    }

    public void setMove(String move) {
        this.move = move;
    }

    /** 记录时间（Unix 秒）。 */
    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }
}
