package com.SwitBotPersonalProj.NoughtsAndCrosses;

public class Board {

    private char[][] grid;

    public Board() {
        grid = new char[3][3];
        reset();
    }

    public void reset() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                grid[row][col] = ' ';
            }
        }
    }

    public void display() {
        // ANSI colour codes for the board display
        String reset  = "\u001B[0m";
        String blue   = "\u001B[34m";
        String yellow = "\u001B[33m";
        String green  = "\u001B[32m";
        String red    = "\u001B[31m";
        String grey   = "\u001B[90m";

        System.out.println(yellow + "\n    1   2   3" + reset);
        System.out.println(blue + "  +---+---+---+" + reset);

        for (int row = 0; row < 3; row++) {
            System.out.print(yellow + (row + 1) + reset + " " + blue + "|" + reset);
            for (int col = 0; col < 3; col++) {
                char c = grid[row][col];
                // O is green, X is red, empty is a grey dot
                if (c == 'O') {
                    System.out.print(green + " O " + reset);
                } else if (c == 'X') {
                    System.out.print(red + " X " + reset);
                } else {
                    System.out.print(grey + " . " + reset);
                }
                System.out.print(blue + "|" + reset);
            }
            System.out.println();
            System.out.println(blue + "  +---+---+---+" + reset);
        }
        System.out.println();
    }

    public boolean placePiece(int row, int col, char piece) {
        if (grid[row - 1][col - 1] != ' ') {
            System.out.println("\u001B[33m" + "That square is already taken! Try again." + "\u001B[0m");
            return false;
        }
        // minus 1 to convert from user input (1-3) to array index (0-2)
        grid[row - 1][col - 1] = piece;
        return true;
    }

    public boolean checkWin(char piece) {
        for (int row = 0; row < 3; row++) {
            if (grid[row][0] == piece && grid[row][1] == piece && grid[row][2] == piece) {
                return true;
            }
        }
        for (int col = 0; col < 3; col++) {
            if (grid[0][col] == piece && grid[1][col] == piece && grid[2][col] == piece) {
                return true;
            }
        }
        // check both diagonals
        if (grid[0][0] == piece && grid[1][1] == piece && grid[2][2] == piece) {
            return true;
        }
        if (grid[0][2] == piece && grid[1][1] == piece && grid[2][0] == piece) {
            return true;
        }
        return false;
    }

    public boolean isFull() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (grid[row][col] == ' ') {
                    return false;
                }
            }
        }
        return true;
    }

    public char getCell(int row, int col) {
        return grid[row - 1][col - 1];
    }

    public int[][] getWinningLine(char piece) {
        // returns the coordinates of the winning squares so SwiftBot can trace the line
        for (int row = 0; row < 3; row++) {
            if (grid[row][0] == piece && grid[row][1] == piece && grid[row][2] == piece) {
                return new int[][]{{row + 1, row + 1, row + 1}, {1, 2, 3}};
            }
        }
        for (int col = 0; col < 3; col++) {
            if (grid[0][col] == piece && grid[1][col] == piece && grid[2][col] == piece) {
                return new int[][]{{1, 2, 3}, {col + 1, col + 1, col + 1}};
            }
        }
        if (grid[0][0] == piece && grid[1][1] == piece && grid[2][2] == piece) {
            return new int[][]{{1, 2, 3}, {1, 2, 3}};
        }
        if (grid[0][2] == piece && grid[1][1] == piece && grid[2][0] == piece) {
            return new int[][]{{1, 2, 3}, {3, 2, 1}};
        }
        return null;
    }
}