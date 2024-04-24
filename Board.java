import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class Board extends JFrame {
    private static final Color BG_COLOR = new Color(250, 248, 239);
    private static final Color TILE_COLOR = new Color(205, 193, 180);
    private static final Color FONT_COLOR = new Color(224,255,255);
    private static final Font TILE_FONT = new Font("Arial", Font.BOLD, 32);
    private static final Color[] TILE_COLORS = {
            new Color(255, 255, 255), 	// White
            new Color(240,128,128), 	// (2) Light Coral
            new Color(255, 153, 102), 	// (4) Orange
            new Color(255, 102, 51),  	// (8) Dark Orange
            new Color(106,90,205), 		// (16) Slate Blue
            new Color(0,0,255), 		// (32) Dodger Blue
            new Color(148,0,211),   	// (64) Dark Violet
            new Color(64,224,208), 		// (128) Turquoise
            new Color(72,61,139), 		// (256) Dark Slate Blue
            new Color(0,128,128),   	// (512) Teal
            new Color(102, 204, 255)  	// (1024) Light Blue
    };

    private final int SIZE = 4;
    private final JLabel[][] board;
    private final int[][] gridCell;
    private boolean compress;
    private boolean merge;
    private boolean moved;
    private int score;

    public Board() {
        super("2048 Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//frame vanishes from the memory on clicking 'close'

        JPanel gameArea = new JPanel(new GridLayout(SIZE, SIZE));
        gameArea.setBackground(BG_COLOR);

        board = new JLabel[SIZE][SIZE];
        gridCell = new int[SIZE][SIZE];
        compress = false;
        merge = false;
        moved = false;
        score = 0;

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                JLabel label = new JLabel("", SwingConstants.CENTER);
                label.setFont(TILE_FONT);
                label.setPreferredSize(new Dimension(80, 80));
                label.setBackground(TILE_COLOR);
                label.setOpaque(true);
                gameArea.add(label);
                board[i][j] = label;
            }
        }

        setContentPane(gameArea);
        setVisible(true);

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_UP) {
                    transpose();
                    compressGrid();
                    mergeGrid();
                    moved = compress || merge;
                    compressGrid();
                    transpose();
                } else if (key == KeyEvent.VK_DOWN) {
                    transpose();
                    reverse();
                    compressGrid();
                    mergeGrid();
                    moved = compress || merge;
                    compressGrid();
                    reverse();
                    transpose();
                } else if (key == KeyEvent.VK_LEFT) {
                    compressGrid();
                    mergeGrid();
                    moved = compress || merge;
                    compressGrid();
                } else if (key == KeyEvent.VK_RIGHT) {
                    reverse();
                    compressGrid();
                    mergeGrid();
                    moved = compress || merge;
                    compressGrid();
                    reverse();
                }
                paintGrid();
                if (moved) {
                    randomCell(); // Generate a new random cell if any move is made
                    paintGrid(); // Update the grid after adding the new cell
                }
                if (isGameOver()) {
                    int maxTile = getMaxTile();
                    score = (int) Math.pow(2, maxTile);
                    JOptionPane.showMessageDialog(null, "Game Over! Your score: " + score);
                    dispose(); // Close the window when the game is over
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }

    private void reverse() {
        for (int[] row : gridCell) {
            for (int i = 0; i < row.length / 2; i++) {
                int temp = row[i];
                row[i] = row[row.length - i - 1];
                row[row.length - i - 1] = temp;
            }
        }
    }

    private void transpose() {
        int[][] temp = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                temp[i][j] = gridCell[j][i];
            }
        }
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(temp[i], 0, gridCell[i], 0, SIZE);
        }
    }

    private void compressGrid() {
        compress = false;
        int[][] temp = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            int cnt = 0;
            for (int j = 0; j < SIZE; j++) {
                if (gridCell[i][j] != 0) {
                    temp[i][cnt] = gridCell[i][j];
                    if (cnt != j) {
                        compress = true;
                    }
                    cnt++;
                }
            }
        }
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(temp[i], 0, gridCell[i], 0, SIZE);
        }
    }

    private void mergeGrid() {
        merge = false;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE - 1; j++) {
                if (gridCell[i][j] == gridCell[i][j + 1] && gridCell[i][j] != 0) {
                    gridCell[i][j] *= 2;
                    gridCell[i][j + 1] = 0;
                    score += gridCell[i][j];
                    merge = true;
                }
            }
        }
    }

    private void randomCell() {
        ArrayList<Point> cells = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (gridCell[i][j] == 0) {
                    cells.add(new Point(i, j));
                }
            }
        }
        Point curr = cells.get(new Random().nextInt(cells.size()));
        int i = curr.x;
        int j = curr.y;
        gridCell[i][j] = 2;
    }

    private boolean isGameOver() {
        return isBoardFull() && !canMerge();
    }

    private boolean isBoardFull() {
        for (int[] row : gridCell) {
            for (int cell : row) {
                if (cell == 0) {
                    return false; // Found an empty cell, so the board is not full
                }
            }
        }
        return true; // No empty cells found, board is full
    }

    private boolean canMerge() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE - 1; j++) {
                if (gridCell[i][j] == gridCell[i][j + 1]) {
                    return true;
                }
            }
        }
        for (int i = 0; i < SIZE - 1; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (gridCell[i][j] == gridCell[i + 1][j]) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getMaxTile() {
        int maxTile = 0;
        for (int[] row : gridCell) {
            for (int cell : row) {
                if (cell > maxTile) {
                    maxTile = cell;
                }
            }
        }
        return (int) (Math.log(maxTile) / Math.log(2));
    }

    private void paintGrid() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (gridCell[i][j] == 0) {
                    board[i][j].setText("");
                    board[i][j].setBackground(TILE_COLOR);
                } else {
                    board[i][j].setText(String.valueOf(gridCell[i][j]));
                    int value = (int) (Math.log(gridCell[i][j]) / Math.log(2));
                    board[i][j].setBackground(TILE_COLORS[value]);
                    board[i][j].setForeground(FONT_COLOR);
                }
            }
        }
    }

    public static void main(String[] args) {
        Board board = new Board();
        board.randomCell();
        board.randomCell();
        board.paintGrid();
    }
}
