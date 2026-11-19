package com.huaweicloud.scorekeep.rules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 三子棋规则的行为校验。 */
class TicTacToeTest {

    private final TicTacToe rules = new TicTacToe();

    @Test
    void initialStateIsEmptyBoard() {
        assertEquals(".........", rules.initialState());
        assertFalse(GameRules.isTerminal(rules.initialState()));
    }

    @Test
    void firstPlayerWinsOnTopRow() {
        String state = rules.initialState();
        state = rules.move(state, "X1");
        state = rules.move(state, "O4");
        state = rules.move(state, "X2");
        state = rules.move(state, "O5");
        state = rules.move(state, "X3");

        assertTrue(GameRules.isTerminal(state));
        assertEquals(GameRules.OUTCOME_FIRST_WINS, String.valueOf(state.charAt(0)));
        assertTrue(state.substring(1).startsWith("XXX"));
    }

    @Test
    void secondPlayerWinsOnAntiDiagonal() {
        String state = rules.initialState();
        state = rules.move(state, "X1");
        state = rules.move(state, "O3");
        state = rules.move(state, "X2");
        state = rules.move(state, "O5");
        state = rules.move(state, "X9");
        state = rules.move(state, "O7");

        assertTrue(GameRules.isTerminal(state));
        assertEquals(GameRules.OUTCOME_SECOND_WINS, String.valueOf(state.charAt(0)));
    }

    @Test
    void fullBoardWithoutWinnerIsDraw() {
        String state = rules.initialState();
        for (String move : new String[]{"X1", "O2", "X3", "O5", "X4", "O6", "X8", "O7", "X9"}) {
            state = rules.move(state, move);
        }

        assertTrue(GameRules.isTerminal(state));
        assertEquals(GameRules.OUTCOME_DRAW, String.valueOf(state.charAt(0)));
    }

    @Test
    void rejectsOccupiedCell() {
        String state = rules.move(rules.initialState(), "X5");
        InvalidMoveException error = assertThrows(InvalidMoveException.class,
                () -> rules.move(state, "O5"));
        assertTrue(error.getMessage().contains("已被占用"));
    }

    @Test
    void rejectsOutOfOrderMark() {
        InvalidMoveException error = assertThrows(InvalidMoveException.class,
                () -> rules.move(rules.initialState(), "O5"));
        assertTrue(error.getMessage().contains("应由"));
    }

    @Test
    void rejectsCellOutsideBoard() {
        assertThrows(InvalidMoveException.class, () -> rules.move(rules.initialState(), "X0"));
        assertThrows(InvalidMoveException.class, () -> rules.move(rules.initialState(), "X10"));
    }

    @Test
    void rejectsUnparseableMove() {
        assertThrows(InvalidMoveException.class, () -> rules.move(rules.initialState(), "X"));
        assertThrows(InvalidMoveException.class, () -> rules.move(rules.initialState(), "Xa"));
    }
}
