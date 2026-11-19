package com.huaweicloud.scorekeep.domain;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 对局：一场在某个场次下进行的比赛。 */
@DynamoDbBean
public class Game {

    private String id;
    private String session;
    private String name;
    private Set<String> players;
    private String rules;
    private Long startTime;
    private Long endTime;
    private List<String> states;
    private List<String> moves;

    public Game() {
    }

    public Game(String id, String session) {
        this.id = id;
        this.session = session;
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** 场次标识，同时是 {@code session-index} 的分区键。 */
    @DynamoDbSecondaryPartitionKey(indexNames = TableNames.GAME_SESSION_INDEX)
    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getPlayers() {
        return players;
    }

    public void setPlayers(Set<String> players) {
        this.players = players == null || players.isEmpty() ? null : new LinkedHashSet<>(players);
    }

    public void setPlayer(String playerId) {
        if (players == null) {
            players = new LinkedHashSet<>();
        }
        players.add(playerId);
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    /** 开始时间（Unix 秒）。 */
    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    /** 结束时间戳，取值口径由调用方给定，服务端只做原样存取。 */
    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public List<String> getStates() {
        return states;
    }

    public void setStates(List<String> states) {
        this.states = states == null || states.isEmpty() ? null : new ArrayList<>(states);
    }

    public void setState(String stateId) {
        if (states == null) {
            states = new ArrayList<>();
        }
        states.add(stateId);
    }

    public List<String> getMoves() {
        return moves;
    }

    public void setMoves(List<String> moves) {
        this.moves = moves == null || moves.isEmpty() ? null : new ArrayList<>(moves);
    }

    public void setMove(String moveId) {
        if (moves == null) {
            moves = new ArrayList<>();
        }
        moves.add(moveId);
    }
}
