package com.huaweicloud.scorekeep.rules;

/**
 * 三子棋规则。
 *
 * <p>局面文本为 9 个字符，对应棋盘 1-9 号格位，取值 {@code .} / {@code X} / {@code O}。
 * 落子文本形如 {@code X5}：首个字符为落子标记，其后为格位号。终局时在局面文本前加结果前缀。</p>
 */
public class TicTacToe implements GameRules {

    private static final int CELLS = 9;
    private static final char EMPTY = '.';
    private static final String INITIAL = ".........";

    private static final int[][] LINES = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6},
    };

    @Override
    public String id() {
        return "TicTacToe";
    }

    @Override
    public String displayName() {
        return "三子棋";
    }

    @Override
    public String initialState() {
        return INITIAL;
    }

    @Override
    public String move(String state, String moveText) throws InvalidMoveException {
        String board = normalize(state);
        if (moveText == null || moveText.length() < 2) {
            throw new InvalidMoveException("落子描述需形如“标记+格位号”，例如 X5");
        }

        char mark = Character.toUpperCase(moveText.charAt(0));
        if (mark != 'X' && mark != 'O') {
            throw new InvalidMoveException("落子标记只能是 X 或 O");
        }

        int cell;
        try {
            cell = Integer.parseInt(moveText.substring(1).trim());
        } catch (NumberFormatException e) {
            throw new InvalidMoveException("格位号需为 1-9 的整数");
        }
        if (cell < 1 || cell > CELLS) {
            throw new InvalidMoveException("格位号超出范围：" + cell);
        }

        int index = cell - 1;
        if (board.charAt(index) != EMPTY) {
            throw new InvalidMoveException("格位已被占用：" + cell);
        }

        char expected = expectedMark(board);
        if (mark != expected) {
            throw new InvalidMoveException("当前应由 " + expected + " 落子");
        }

        char[] next = board.toCharArray();
        next[index] = mark;
        String nextBoard = new String(next);

        String winner = winnerOf(nextBoard);
        if (winner != null) {
            String prefix = winner.equals("X") ? OUTCOME_FIRST_WINS : OUTCOME_SECOND_WINS;
            return prefix + nextBoard;
        }
        if (nextBoard.indexOf(EMPTY) < 0) {
            return OUTCOME_DRAW + nextBoard;
        }
        return nextBoard;
    }

    /** 由已落子数量推导当前应落子的一方。 */
    static char expectedMark(String board) {
        long filled = board.chars().filter(c -> c != EMPTY).count();
        return filled % 2 == 0 ? 'X' : 'O';
    }

    /** 返回获胜标记；无胜者返回 {@code null}。 */
    static String winnerOf(String board) {
        for (int[] line : LINES) {
            char a = board.charAt(line[0]);
            if (a != EMPTY && a == board.charAt(line[1]) && a == board.charAt(line[2])) {
                return String.valueOf(a);
            }
        }
        return null;
    }

    /** 去除结果前缀并校验长度。 */
    static String normalize(String state) throws InvalidMoveException {
        if (state == null || state.isEmpty()) {
            return INITIAL;
        }
        String board = GameRules.isTerminal(state) ? state.substring(1) : state;
        if (board.length() != CELLS) {
            throw new InvalidMoveException("局面文本长度应为 " + CELLS);
        }
        return board;
    }
}
