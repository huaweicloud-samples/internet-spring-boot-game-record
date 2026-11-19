package com.huaweicloud.scorekeep.repository;

import com.huaweicloud.scorekeep.domain.Game;
import com.huaweicloud.scorekeep.domain.GameSession;
import com.huaweicloud.scorekeep.domain.GameState;
import com.huaweicloud.scorekeep.domain.Move;
import com.huaweicloud.scorekeep.domain.Player;
import com.huaweicloud.scorekeep.domain.TableNames;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;

/**
 * 宽表存储访问层。
 *
 * <p>表名经 {@link TableNames} 解析，按实体各持一个表句柄；按场次或对局检索走二级索引。</p>
 */
@Repository
public class ScorekeepRepository {

    private final DynamoDbTable<Player> players;
    private final DynamoDbTable<GameSession> sessions;
    private final DynamoDbTable<Game> games;
    private final DynamoDbTable<Move> moves;
    private final DynamoDbTable<GameState> states;

    public ScorekeepRepository(DynamoDbEnhancedClient enhancedClient) {
        this.players = enhancedClient.table(TableNames.PLAYER, TableSchema.fromBean(Player.class));
        this.sessions = enhancedClient.table(TableNames.SESSION, TableSchema.fromBean(GameSession.class));
        this.games = enhancedClient.table(TableNames.GAME, TableSchema.fromBean(Game.class));
        this.moves = enhancedClient.table(TableNames.MOVE, TableSchema.fromBean(Move.class));
        this.states = enhancedClient.table(TableNames.STATE, TableSchema.fromBean(GameState.class));
    }

    // ------------------------------------------------------------------
    // 参与者
    // ------------------------------------------------------------------

    public void savePlayer(Player player) {
        players.putItem(player);
    }

    public Optional<Player> findPlayer(String id) {
        return Optional.ofNullable(players.getItem(Key.builder().partitionValue(id).build()));
    }

    public List<Player> listPlayers() {
        return players.scan().items().stream().toList();
    }

    public void deletePlayer(String id) {
        players.deleteItem(Key.builder().partitionValue(id).build());
    }

    // ------------------------------------------------------------------
    // 场次
    // ------------------------------------------------------------------

    public void saveSession(GameSession session) {
        sessions.putItem(session);
    }

    public Optional<GameSession> findSession(String id) {
        return Optional.ofNullable(sessions.getItem(Key.builder().partitionValue(id).build()));
    }

    public List<GameSession> listSessions() {
        return sessions.scan().items().stream().toList();
    }

    public void deleteSession(String id) {
        sessions.deleteItem(Key.builder().partitionValue(id).build());
    }

    // ------------------------------------------------------------------
    // 对局
    // ------------------------------------------------------------------

    public void saveGame(Game game) {
        games.putItem(game);
    }

    public Optional<Game> findGame(String id) {
        return Optional.ofNullable(games.getItem(Key.builder().partitionValue(id).build()));
    }

    /** 按场次检索对局（走 {@code session-index}）。 */
    public List<Game> listGamesBySession(String sessionId) {
        DynamoDbIndex<Game> index = games.index(TableNames.GAME_SESSION_INDEX);
        QueryConditional condition = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(sessionId).build());
        return index.query(condition).stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }

    public void deleteGame(String id) {
        games.deleteItem(Key.builder().partitionValue(id).build());
    }

    // ------------------------------------------------------------------
    // 落子
    // ------------------------------------------------------------------

    public void saveMove(Move move) {
        moves.putItem(move);
    }

    public Optional<Move> findMove(String id) {
        return Optional.ofNullable(moves.getItem(Key.builder().partitionValue(id).build()));
    }

    /** 按对局检索落子（走 {@code game-index}）。 */
    public List<Move> listMovesByGame(String gameId) {
        DynamoDbIndex<Move> index = moves.index(TableNames.MOVE_GAME_INDEX);
        QueryConditional condition = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(gameId).build());
        return index.query(condition).stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }

    // ------------------------------------------------------------------
    // 局面
    // ------------------------------------------------------------------

    public void saveState(GameState state) {
        states.putItem(state);
    }

    public Optional<GameState> findState(String id) {
        return Optional.ofNullable(states.getItem(Key.builder().partitionValue(id).build()));
    }

    /** 按对局检索局面（走 {@code game-index}）。 */
    public List<GameState> listStatesByGame(String gameId) {
        DynamoDbIndex<GameState> index = states.index(TableNames.STATE_GAME_INDEX);
        QueryConditional condition = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(gameId).build());
        return index.query(condition).stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }
}
