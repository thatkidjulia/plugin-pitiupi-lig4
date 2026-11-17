package pitiupi.lig4.game;

/** Regras do Lig4, sem dependência da interface ou da rede. */
public final class Lig4Game {
    public static final int ROWS = 6;
    public static final int COLUMNS = 7;
    private final int[][] board = new int[ROWS][COLUMNS];
    private int currentPlayer = 1;
    private int winner;
    private boolean draw;

    /** @return linha onde a peça caiu, ou -1 se a jogada não for válida. */
    public int play(int column) {
        if (column < 0 || column >= COLUMNS || winner != 0 || draw) return -1;
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][column] == 0) {
                board[row][column] = currentPlayer;
                if (hasFour(row, column)) winner = currentPlayer;
                else if (isFull()) draw = true;
                else currentPlayer = 3 - currentPlayer;
                return row;
            }
        }
        return -1;
    }

    public int at(int row, int column) { return board[row][column]; }
    public int currentPlayer() { return currentPlayer; }
    public int winner() { return winner; }
    public boolean isDraw() { return draw; }
    public boolean isFinished() { return winner != 0 || draw; }

    public void reset() {
        for (int[] row : board) java.util.Arrays.fill(row, 0);
        currentPlayer = 1;
        winner = 0;
        draw = false;
    }

    private boolean isFull() {
        for (int c = 0; c < COLUMNS; c++) if (board[0][c] == 0) return false;
        return true;
    }

    private boolean hasFour(int row, int col) {
        int player = board[row][col];
        return line(row, col, 0, 1, player) || line(row, col, 1, 0, player)
                || line(row, col, 1, 1, player) || line(row, col, 1, -1, player);
    }

    private boolean line(int row, int col, int dr, int dc, int player) {
        int count = 1;
        count += countDirection(row, col, dr, dc, player);
        count += countDirection(row, col, -dr, -dc, player);
        return count >= 4;
    }

    private int countDirection(int row, int col, int dr, int dc, int player) {
        int count = 0, r = row + dr, c = col + dc;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLUMNS && board[r][c] == player) {
            count++; r += dr; c += dc;
        }
        return count;
    }
}
