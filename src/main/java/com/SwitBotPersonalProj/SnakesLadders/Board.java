package com.SwitBotPersonalProj.SnakesLadders;

import java.util.LinkedHashMap;
import java.util.Map;

public class Board {

    private final int[][] grid;
    private final LinkedHashMap<Integer, Integer> snakes;
    private final LinkedHashMap<Integer, Integer> ladders;

    public Board() {
        grid = buildGrid();

        snakes = new LinkedHashMap<Integer, Integer>();
        ladders = new LinkedHashMap<Integer, Integer>();

        // Valid 5x5 task friendly positions
        // snake head > tail not same row as tail
        snakes.put(14, 7);
        snakes.put(22, 9);

        // ladder bottom < top  not same row as top
        ladders.put(3, 11);
        ladders.put(10, 18);
    }

    private int[][] buildGrid() {
        int[][] matrix = new int[5][5];
        int square = 1;

        for (int row = 4; row >= 0; row--) {
            int logicalRowFromBottom = 4 - row;

            if (logicalRowFromBottom % 2 == 0) {
                for (int col = 0; col < 5; col++) {
                    matrix[row][col] = square++;
                }
            } else {
                for (int col = 4; col >= 0; col--) {
                    matrix[row][col] = square++;
                }
            }
        }

        return matrix;
    }

    public int[][] getGrid() {
        return grid;
    }

    public int[] getCoordinates(int square) {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == square) {
                    return new int[] { row, col };
                }
            }
        }
        throw new IllegalArgumentException("Invalid square: " + square);
    }

    public boolean isSnakeHead(int square) {
        return snakes.containsKey(square);
    }

    public boolean isLadderBottom(int square) {
        return ladders.containsKey(square);
    }

    public int applySnakeOrLadder(int square) {
        if (snakes.containsKey(square)) {
            return snakes.get(square);
        }
        if (ladders.containsKey(square)) {
            return ladders.get(square);
        }
        return square;
    }

    public String getEventMessage(int square) {
        if (snakes.containsKey(square)) {
            return "Snake! Going down from " + square + " to " + snakes.get(square);
        }
        if (ladders.containsKey(square)) {
            return "Ladder! Going up from " + square + " to " + ladders.get(square);
        }
        return "";
    }

    public String snakesToString() {
        return mapToString(snakes, "Snakes");
    }

    public String laddersToString() {
        return mapToString(ladders, "Ladders");
    }

    private String mapToString(Map<Integer, Integer> map, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append(": ");
        boolean first = true;

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("->").append(entry.getValue());
            first = false;
        }

        return sb.toString();
    }

    public String renderBoard(int userSquare, int swiftBotSquare) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n================ 5x5 BOARD ================\n");

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                int square = grid[row][col];

                String marker = "";
                if (square == userSquare && square == swiftBotSquare) {
                    marker = "US";
                } else if (square == userSquare) {
                    marker = "U ";
                } else if (square == swiftBotSquare) {
                    marker = "S ";
                } else if (snakes.containsKey(square)) {
                    marker = "H "; // snake head
                } else if (ladders.containsKey(square)) {
                    marker = "L "; // ladder bottom
                } else {
                    marker = "  ";
                }

                sb.append(String.format("[%2d %s]", square, marker));
            }
            sb.append("\n");
        }

        sb.append("Legend: U=User, S=SwiftBot, H=Snake head, L=Ladder bottom\n");
        return sb.toString();
    }
}
