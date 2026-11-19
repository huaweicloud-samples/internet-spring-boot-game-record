package com.huaweicloud.scorekeep.domain;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.LinkedHashSet;
import java.util.Set;

/** 场次：一组对局的容器，记录归属者、参与者与包含的对局。 */
@DynamoDbBean
public class GameSession {

    private String id;
    private String owner;
    private String name;
    private Set<String> players;
    private Set<String> games;

    public GameSession() {
    }

    public GameSession(String id) {
        this.id = id;
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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
        this.players = players;
    }

    public Set<String> getGames() {
        return games;
    }

    public void setGames(Set<String> games) {
        this.games = games;
    }

    /** 登记一个对局；集合为空时置空，避免写入空集合。 */
    public void addGame(String gameId) {
        if (games == null) {
            games = new LinkedHashSet<>();
        }
        games.add(gameId);
    }

    public void removeGame(String gameId) {
        if (games == null) {
            return;
        }
        games.remove(gameId);
        if (games.isEmpty()) {
            games = null;
        }
    }

    public void addPlayer(String playerId) {
        if (players == null) {
            players = new LinkedHashSet<>();
        }
        players.add(playerId);
    }
}
