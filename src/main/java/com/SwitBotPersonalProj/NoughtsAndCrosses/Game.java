package com.SwitBotPersonalProj.NoughtsAndCrosses;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Game {

    private Board board;
    private Player humanPlayer;
    private SwiftBotPlayer swiftBot;
    private GameLogger logger;
    private Scanner scanner;
    private int roundNumber;
    private ArrayList<String> movesThisRound;

    // ANSI colour codes for console output
    private String reset  = "\u001B[0m";
    private String cyan   = "\u001B[36m";
    private String yellow = "\u001B[33m";
    private String green  = "\u001B[32m";
    private String red    = "\u001B[31m";
    private String purple = "\u001B[35m";

    public Game(Player humanPlayer, SwiftBotPlayer swiftBot, GameLogger logger) {
        this.board = new Board();
        this.humanPlayer = humanPlayer;
        this.swiftBot = swiftBot;
        this.logger = logger;
        this.scanner = new Scanner(System.in);
        this.roundNumber = 0;
        this.movesThisRound = new ArrayList<>();
    }

    private int rollDice() {
        Random random = new Random();
        return random.nextInt(6) + 1; // nextInt(6) gives 0-5 so we add 1 to get 1-6
    }

    private Player decideWhoGoesFirst() {
        System.out.println(yellow + "\nRolling dice to decide who goes first..." + reset);

        int humanRoll, botRoll;
        // keep rolling if it's a tie
        do {
            humanRoll = rollDice();
            botRoll = rollDice();
            System.out.println(cyan + humanPlayer.getName() + " rolled: " + humanRoll + reset);
            System.out.println(red + swiftBot.getName() + " rolled: " + botRoll + reset);
            if (humanRoll == botRoll) {
                System.out.println(yellow + "It's a tie! Rolling again..." + reset);
            }
        } while (humanRoll == botRoll);

        if (humanRoll > botRoll) {
            System.out.println(green + humanPlayer.getName() + " goes first and plays as O!" + reset);
            humanPlayer.setPiece('O');
            swiftBot.setPiece('X');
            return humanPlayer;
        } else {
            System.out.println(red + swiftBot.getName() + " goes first and plays as O!" + reset);
            swiftBot.setPiece('O');
            humanPlayer.setPiece('X');
            return swiftBot;
        }
    }

    private void humanTurn() {
        int row, col;
        boolean validMove = false;
        while (!validMove) {
            System.out.print(cyan + "\n" + humanPlayer.getName() + " - Enter your move [row,col]: " + reset);
            String input = scanner.nextLine().trim();
            // regex check to make sure input is in the correct format
            if (!input.matches("[1-3],[1-3]")) {
                System.out.println(yellow + "Invalid format! Please enter row and column as [row,col] e.g. 1,2" + reset);
                continue;
            }
            String[] parts = input.split(",");
            row = Integer.parseInt(parts[0].trim());
            col = Integer.parseInt(parts[1].trim());
            validMove = board.placePiece(row, col, humanPlayer.getPiece());
            if (validMove) {
                System.out.println(green + "[" + humanPlayer.getName() + " - " + humanPlayer.getPiece() + "] moved to square [" + row + "," + col + "]" + reset);
                String move = humanPlayer.getName() + " | Piece: " + humanPlayer.getPiece() + " | Position: [" + row + "," + col + "]";
                movesThisRound.add(move);
            }
        }
    }

    private void swiftBotTurn() {
        int[] move = swiftBot.chooseMove(board);
        int row = move[0];
        int col = move[1];

        System.out.println(red + swiftBot.getName() + " is choosing square [" + row + "," + col + "]..." + reset);
        swiftBot.moveToSquare(row, col);
        board.placePiece(row, col, swiftBot.getPiece());
        System.out.println(red + "[" + swiftBot.getName() + " - " + swiftBot.getPiece() + "] moved to square [" + row + "," + col + "]" + reset);
        String movelog = swiftBot.getName() + " | Piece: " + swiftBot.getPiece() + " | Position: [" + row + "," + col + "]";
        movesThisRound.add(movelog);
    }

    private void displayScoreboard() {
        System.out.println(yellow + "\n--- SCOREBOARD ---" + reset);
        System.out.println(green + humanPlayer.getName() + ": " + humanPlayer.getScore() + reset);
        System.out.println(red + swiftBot.getName() + ": " + swiftBot.getScore() + reset);
        System.out.println(yellow + "------------------" + reset);
    }

    public boolean playRound() {
        roundNumber++;
        movesThisRound = new ArrayList<>();

        System.out.println(purple + "\n========== ROUND " + roundNumber + " ==========" + reset);

        board.reset();
        Player currentPlayer = decideWhoGoesFirst();

        while (true) {
            board.display();

            if (currentPlayer == humanPlayer) {
                humanTurn();
            } else {
                swiftBotTurn();
            }

            if (board.checkWin(currentPlayer.getPiece())) {
                board.display();
                if (currentPlayer == humanPlayer) {
                    System.out.println(green + "\n*** " + currentPlayer.getName() + " wins this round! ***" + reset);
                } else {
                    System.out.println(red + "\n*** " + currentPlayer.getName() + " wins this round! ***" + reset);
                }
                currentPlayer.addScore();
                logger.log(currentPlayer.getName() + " won round " + roundNumber);
                displayScoreboard();
                int[][] winLine = board.getWinningLine(currentPlayer.getPiece());
                if (winLine != null) {
                    swiftBot.traceWinningLine(winLine[0], winLine[1], currentPlayer.getPiece());
                }
                break;
            }

            if (board.isFull()) {
                board.display();
                System.out.println(purple + "\n*** It's a draw! ***" + reset);
                logger.log("Round " + roundNumber + " ended in a draw");
                displayScoreboard();
                swiftBot.drawCelebration();
                break;
            }

            // swap to the other player for the next turn
            if (currentPlayer == humanPlayer) {
                currentPlayer = swiftBot;
            } else {
                currentPlayer = humanPlayer;
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = LocalDateTime.now().format(formatter);

        // write round details to the log file
        logger.log("========== ROUND " + roundNumber + " ==========");
        logger.log("Date/Time: " + dateTime);
        logger.log("Players: " + humanPlayer.getName() + " (vs) " + swiftBot.getName());
        logger.log("Moves in order:");
        for (int i = 0; i < movesThisRound.size(); i++) {
            logger.log("  Move " + (i + 1) + ": " + movesThisRound.get(i));
        }
        logger.log("Scores - " + humanPlayer.getName() + ": " + humanPlayer.getScore() + ", " + swiftBot.getName() + ": " + swiftBot.getScore());
        logger.log("=========================================");

        return swiftBot.waitForContinueOrQuit();
    }
}