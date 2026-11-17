package pitiupi.lig4.ui;

import pitiupi.lig4.game.Lig4Game;
import javax.swing.*;
import java.awt.*;

/** Tabuleiro e estado visível da partida. */
public final class Lig4Panel extends JPanel {
    private static final Color BOARD_RED = new Color(221, 35, 53);
    private static final Color PLAYER_YELLOW = new Color(255, 211, 35);
    private static final Color PLAYER_BLUE = new Color(53, 142, 218);
    private final Lig4Game game = new Lig4Game();
    private final JLabel status = new JLabel("Desafie alguém na lista de usuários para jogar.", SwingConstants.CENTER);
    private final JButton[][] cells = new JButton[Lig4Game.ROWS][Lig4Game.COLUMNS];
    private final JButton rematch = new JButton("Pedir revanche");
    private int localPlayer;
    private boolean active;
    private boolean rematchEnabled;
    private java.util.function.IntConsumer moveHandler;
    private Runnable rematchHandler;

    public Lig4Panel() {
        setLayout(new BorderLayout(8, 8));
        add(status, BorderLayout.NORTH);
        status.setFont(status.getFont().deriveFont(Font.BOLD, 16f));
        status.setBorder(BorderFactory.createEmptyBorder(12, 8, 8, 8));
        JPanel board = new BoardFrame();
        board.setLayout(new GridLayout(Lig4Game.ROWS, Lig4Game.COLUMNS, 8, 8));
        board.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        for (int row = 0; row < Lig4Game.ROWS; row++) for (int col = 0; col < Lig4Game.COLUMNS; col++) {
            JButton cell = new CircleCell();
            final int column = col;
            cell.addActionListener(e -> { if (moveHandler != null) moveHandler.accept(column); });
            cells[row][col] = cell;
            board.add(cell);
        }
        JPanel boardArea = new JPanel(new GridBagLayout());
        boardArea.setOpaque(false);
        boardArea.add(board);
        add(boardArea, BorderLayout.CENTER);
        rematch.setEnabled(false);
        rematch.addActionListener(e -> { if (rematchHandler != null) rematchHandler.run(); });
        add(rematch, BorderLayout.SOUTH);
    }

    public void setMoveHandler(java.util.function.IntConsumer handler) { moveHandler = handler; }
    public void setRematchHandler(Runnable handler) { rematchHandler = handler; }
    public Lig4Game game() { return game; }
    public int localPlayer() { return localPlayer; }

    public void beginMatch(int player, String opponent) {
        game.reset();
        localPlayer = player;
        active = true;
        rematchEnabled = false;
        rematch.setEnabled(false);
        clearBoard();
        status.setText("Partida contra " + opponent + " — você é " + colorName(player) + ". " + turnText());
        refreshEnabled();
    }

    public int play(int column) {
        if (!active || !canPlay()) return -1;
        int player = game.currentPlayer();
        int row = game.play(column);
        if (row >= 0) {
            paintCell(row, column, player);
            updateStatus();
        }
        return row;
    }

    public boolean applyRemoteMove(int column, int expectedPlayer) {
        if (!active || game.isFinished() || game.currentPlayer() != expectedPlayer || canPlay()) return false;
        int row = game.play(column);
        if (row < 0) return false;
        paintCell(row, column, expectedPlayer);
        updateStatus();
        return true;
    }

    public void endMatch(String message) {
        active = false;
        status.setText(message);
        refreshEnabled();
    }

    public void enableRematch() { rematchEnabled = true; rematch.setEnabled(true); }

    private boolean canPlay() { return active && !game.isFinished() && game.currentPlayer() == localPlayer; }
    private String turnText() { return canPlay() ? "Sua vez." : "Aguardando a jogada do oponente."; }
    private String colorName(int player) { return player == 1 ? "amarelo" : "azul"; }

    private void updateStatus() {
        if (game.winner() != 0) {
            active = false;
            status.setText(game.winner() == localPlayer ? "Você venceu!" : "Seu oponente venceu.");
            enableRematch();
        } else if (game.isDraw()) {
            active = false;
            status.setText("Empate!");
            enableRematch();
        } else status.setText("Sua vez: " + colorName(game.currentPlayer()) + ". " + turnText());
        refreshEnabled();
    }

    private void refreshEnabled() {
        boolean enabled = canPlay();
        for (int r = 0; r < Lig4Game.ROWS; r++) for (int c = 0; c < Lig4Game.COLUMNS; c++)
            cells[r][c].setEnabled(enabled && game.at(0, c) == 0);
        rematch.setEnabled(rematchEnabled);
    }

    private void paintCell(int row, int col, int player) {
        cells[row][col].setBackground(player == 1 ? PLAYER_YELLOW : PLAYER_BLUE);
    }

    private void clearBoard() {
        for (JButton[] row : cells) for (JButton cell : row) { cell.setBackground(null); cell.setEnabled(false); }
    }

    private static final class BoardFrame extends JPanel {
        BoardFrame() { setOpaque(false); }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setColor(new Color(0, 0, 0, 35));
            g.fillRoundRect(4, 7, w - 8, h - 10, 28, 28);
            GradientPaint paint = new GradientPaint(0, 0, new Color(244, 66, 76), 0, h, BOARD_RED);
            g.setPaint(paint);
            g.fillRoundRect(0, 0, w - 8, h - 10, 28, 28);
            g.setColor(new Color(255, 255, 255, 55));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(2, 2, w - 12, h - 14, 25, 25);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class CircleCell extends JButton {
        CircleCell() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setMargin(new Insets(0, 0, 0, 0));
            setPreferredSize(new Dimension(54, 54));
            setBackground(null);
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.max(8, Math.min(getWidth(), getHeight()) - 3);
            int x = (getWidth() - size) / 2, y = (getHeight() - size) / 2;
            Color piece = getBackground();
            g.setColor(new Color(125, 20, 30, 90));
            g.fillOval(x + 1, y + 2, size, size);
            if (piece == null) {
                g.setPaint(new GradientPaint(x, y, new Color(255, 255, 255), x + size, y + size, new Color(218, 222, 228)));
                g.fillOval(x, y, size, size);
                g.setColor(new Color(170, 177, 186));
                g.setStroke(new BasicStroke(2f));
                g.drawOval(x + 1, y + 1, size - 2, size - 2);
            } else {
                g.setPaint(new GradientPaint(x, y, piece.brighter(), x + size, y + size, piece.darker()));
                g.fillOval(x, y, size, size);
                g.setColor(new Color(255, 255, 255, 125));
                g.fillOval(x + size / 5, y + size / 7, size / 3, size / 5);
                g.setColor(piece.darker().darker());
                g.setStroke(new BasicStroke(1.3f));
                g.drawOval(x + 1, y + 1, size - 2, size - 2);
            }
            if (getModel().isRollover() && isEnabled()) {
                g.setColor(new Color(255, 255, 255, 100));
                g.setStroke(new BasicStroke(3f));
                g.drawOval(x + 3, y + 3, size - 6, size - 6);
            }
            g.dispose();
        }
    }
}
