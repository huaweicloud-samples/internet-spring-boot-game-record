package com.huaweicloud.scorekeep.web;

import com.huaweicloud.scorekeep.domain.Game;
import com.huaweicloud.scorekeep.domain.GameState;
import com.huaweicloud.scorekeep.domain.Move;
import com.huaweicloud.scorekeep.domain.Player;
import com.huaweicloud.scorekeep.rules.GameRules;
import com.huaweicloud.scorekeep.rules.InvalidMoveException;
import com.huaweicloud.scorekeep.rules.RulesCatalog;
import com.huaweicloud.scorekeep.service.GameService;
import com.huaweicloud.scorekeep.service.MoveService;
import com.huaweicloud.scorekeep.service.PlayerService;
import com.huaweicloud.scorekeep.service.SessionService;
import com.huaweicloud.scorekeep.service.StateService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 接口契约：路径、请求体形态、响应结构与错误语义。 */
@WebMvcTest(controllers = {
        RulesController.class,
        PlayerController.class,
        SessionController.class,
        GameController.class,
        MoveController.class,
        StateController.class,
})
class ApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RulesCatalog rulesCatalog;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private GameService gameService;

    @MockBean
    private MoveService moveService;

    @MockBean
    private StateService stateService;

    @Test
    void listsRules() throws Exception {
        given(rulesCatalog.list()).willReturn(List.of(
                new GameRules.RulesInfo("TicTacToe", "三子棋", ".........", "局面文本为 9 个字符")));

        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("TicTacToe"))
                .andExpect(jsonPath("$[0].name").value("三子棋"))
                .andExpect(jsonPath("$[0].initialState").value("........."));
    }

    @Test
    void rejectsUnknownRule() throws Exception {
        given(rulesCatalog.describe("Chess")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/rules/Chess"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void createsPlayerWithGeneratedNameWhenBodyAbsent() throws Exception {
        given(playerService.createWithGeneratedName())
                .willReturn(new Player("PLAYER01", "Emma"));

        mockMvc.perform(post("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PLAYER01"))
                .andExpect(jsonPath("$.name").value("Emma"));
    }

    @Test
    void createsPlayerWithGivenName() throws Exception {
        given(playerService.create("小明")).willReturn(new Player("PLAYER02", "小明"));

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"小明\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("小明"));
    }

    @Test
    void reportsMissingPlayerAsNotFound() throws Exception {
        given(playerService.get("NOPE"))
                .willThrow(new ResourceNotFoundException("参与者", "NOPE"));

        mockMvc.perform(get("/api/user/NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("参与者 不存在：NOPE"));
    }

    @Test
    void deletesPlayer() throws Exception {
        mockMvc.perform(delete("/api/user/PLAYER01"))
                .andExpect(status().isOk());
    }

    @Test
    void listsGamesOfSession() throws Exception {
        Game game = new Game("GAME01", "SESSION01");
        game.setName("第一局");
        given(gameService.list("SESSION01")).willReturn(List.of(game));

        mockMvc.perform(get("/api/game/SESSION01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("GAME01"))
                .andExpect(jsonPath("$[0].session").value("SESSION01"))
                .andExpect(jsonPath("$[0].name").value("第一局"));
    }

    @Test
    void acceptsMoveAsPlainText() throws Exception {
        given(moveService.apply(anyString(), anyString(), anyString(), anyString()))
                .willReturn(new Move("MOVE01", "SESSION01", "GAME01", "PLAYER01", "X5"));

        mockMvc.perform(post("/api/move/SESSION01/GAME01/PLAYER01")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("X5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.move").value("X5"));

        assertEquals("X5", capturedMoveText());
    }

    @Test
    void acceptsMoveAsJsonString() throws Exception {
        given(moveService.apply(anyString(), anyString(), anyString(), anyString()))
                .willReturn(new Move("MOVE02", "SESSION01", "GAME01", "PLAYER02", "O7"));

        mockMvc.perform(post("/api/move/SESSION01/GAME01/PLAYER02")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"O7\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.move").value("O7"));

        assertEquals("O7", capturedMoveText());
    }

    @Test
    void reportsInvalidMoveAsBadRequest() throws Exception {
        willThrow(new InvalidMoveException("格位已被占用：5"))
                .given(moveService).apply(anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/move/SESSION01/GAME01/PLAYER01")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("X5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_request"));
    }

    @Test
    void exposesLatestState() throws Exception {
        given(stateService.get("SESSION01", "GAME01", "STATE01"))
                .willReturn(new GameState("STATE01", "SESSION01", "GAME01", ".........",
                        Set.of("PLAYER01", "PLAYER02")));

        mockMvc.perform(get("/api/state/SESSION01/GAME01/STATE01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("STATE01"))
                .andExpect(jsonPath("$.state").value("........."));
    }

    private String capturedMoveText() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(moveService).apply(anyString(), anyString(), anyString(), captor.capture());
        return captor.getValue();
    }
}
