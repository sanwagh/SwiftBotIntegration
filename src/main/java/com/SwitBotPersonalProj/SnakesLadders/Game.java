package com.SwitBotPersonalProj.SnakesLadders;

import java.io.IOException;
import java.util.Scanner;

import swiftbot.Button;
import swiftbot.SwiftBotAPI;

public class Game {

    private final Scanner scanner;
    private final SwiftBotAPI bot;
    private final Dice dice;
    private final Board board;
    private final SwiftBotNavigator navigator;

    private Player user;
    private Player swiftBotPlayer;
    private Mode mode;

    public Game() {
        scanner = new Scanner(System.in);
        bot = SwiftBotAPI.INSTANCE;
        dice = new Dice();
        board = new Board();
        navigator = new SwiftBotNavigator(bot, board);
    }

    public void start() throws Exception {
        System.out.println("========================================");
        System.out.println("      SNAKES AND LADDERS - SWIFTBOT     ");
        System.out.println("========================================");
        System.out.println("Press Button Y on the SwiftBot to start.");

        ButtonHelper.waitForExactButton(bot, Button.Y, "Waiting for Button Y...");

        setupPlayers();
        chooseMode();
        showBoardSetup();
        Player currentPlayer = decideFirstPlayer();

        boolean running = true;

        while (running) {
            System.out.println(board.renderBoard(user.getPosition(), swiftBotPlayer.getPosition()));
            System.out.println("Current turn: " + currentPlayer.getName());

            if (currentPlayer == user) {
                running = handleUserTurn();
            } else {
                running = handleSwiftBotTurn();
            }

            if (!running) {
                break;
            }

            if (user.getPosition() == 25) {
                System.out.println(user.getName() + " wins!");
                break;
            }

            if (swiftBotPlayer.getPosition() == 25) {
                System.out.println(swiftBotPlayer.getName() + " wins!");
                break;
            }

            currentPlayer = (currentPlayer == user) ? swiftBotPlayer : user;
        }

        finishGame();
    }

    private void setupPlayers() {
        System.out.print("Enter your name: ");
        String userName = scanner.nextLine().trim();

        if (userName.isEmpty()) {
            userName = "User";
        }

        user = new Player(userName);
        swiftBotPlayer = new Player("SwiftBot");
        navigator.resetToStart();
    }

    private void chooseMode() {
        while (true) {
            System.out.print("Choose mode (A/B): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if ("A".equals(input)) {
                mode = Mode.A;
                return;
            }
            if ("B".equals(input)) {
                mode = Mode.B;
                return;
            }

            System.out.println("Invalid mode. Please enter A or B.");
        }
    }

    private void showBoardSetup() {
        System.out.println(board.snakesToString());
        System.out.println(board.laddersToString());
        System.out.println("Players start at square 1.");
        System.out.println("A player must land exactly on square 25 to win.");
    }

    private Player decideFirstPlayer() throws InterruptedException {
        while (true) {
            System.out.println("\nDeciding first player...");

            ButtonHelper.waitForExactButton(bot, Button.A,
                    user.getName() + ": press Button A to roll for first turn.");
            int userRoll = dice.roll();
            int botRoll = dice.roll();

            System.out.println(user.getName() + " rolled " + userRoll);
            System.out.println(swiftBotPlayer.getName() + " rolled " + botRoll);

            if (userRoll > botRoll) {
                System.out.println(user.getName() + " goes first.");
                return user;
            } else if (botRoll > userRoll) {
                System.out.println(swiftBotPlayer.getName() + " goes first.");
                return swiftBotPlayer;
            } else {
                System.out.println("Tie. Rolling again...");
            }
        }
    }

    private boolean handleUserTurn() throws InterruptedException {
        ButtonHelper.waitForExactButton(bot, Button.A,
                user.getName() + ": press Button A to roll.");

        int roll = dice.roll();
        int from = user.getPosition();
        int provisional = calculateDestination(from, roll);

        System.out.println("[" + user.getName() + "] rolled a [" + roll + "] and moved from square ["
                + from + "] to [" + provisional + "].");

        user.setPosition(provisional);

        int adjusted = board.applySnakeOrLadder(provisional);
        if (adjusted != provisional) {
            System.out.println(board.getEventMessage(provisional));
            user.setPosition(adjusted);
        }

        return checkQuitCheckpoint();
    }

    private boolean handleSwiftBotTurn() throws InterruptedException {
        int from = swiftBotPlayer.getPosition();
        int roll = dice.roll();
        int provisional;

        if (mode == Mode.A) {
            provisional = calculateDestination(from, roll);
            System.out.println("[SwiftBot] rolled a [" + roll + "] and moved from square ["
                    + from + "] to [" + provisional + "].");
        } else {
            System.out.println("[SwiftBot] rolled a [" + roll + "].");
            provisional = getModeBOverrideOrNormalMove(from, roll);
            System.out.println("[SwiftBot] will move from square [" + from + "] to [" + provisional + "].");
        }

        if (provisional != from) {
            navigator.moveToSquare(provisional);
        }

        swiftBotPlayer.setPosition(provisional);

        int adjusted = board.applySnakeOrLadder(provisional);
        if (adjusted != provisional) {
            System.out.println(board.getEventMessage(provisional));
            navigator.moveToSquare(adjusted);
            swiftBotPlayer.setPosition(adjusted);
        }

        return checkQuitCheckpoint();
    }

    private int getModeBOverrideOrNormalMove(int currentPosition, int rolledValue) {
        int normalDestination = calculateDestination(currentPosition, rolledValue);

        System.out.println("Mode B: you may override the SwiftBot move.");
        System.out.println("Enter a destination square between " + (currentPosition + 1)
                + " and " + Math.min(currentPosition + 5, 25)
                + ", or press ENTER to keep the rolled move.");

        while (true) {
            System.out.print("Override square (blank = keep roll): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return normalDestination;
            }

            try {
                int chosenSquare = Integer.parseInt(input);
                int minSquare = currentPosition + 1;
                int maxSquare = Math.min(currentPosition + 5, 25);

                if (chosenSquare >= minSquare && chosenSquare <= maxSquare) {
                    return chosenSquare;
                }

                System.out.println("Invalid override. Must be between " + minSquare + " and " + maxSquare + ".");
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid integer square number.");
            }
        }
    }

    private int calculateDestination(int currentPosition, int roll) {
        int target = currentPosition + roll;

        // exact win rule at square 25
        if (target > 25) {
            return currentPosition;
        }

        return target;
    }

    private boolean checkQuitCheckpoint() throws InterruptedException {
        if (user.getPosition() == 5 || user.getPosition() == 25
                || swiftBotPlayer.getPosition() == 5 || swiftBotPlayer.getPosition() == 25) {

            System.out.println("Quit checkpoint reached.");
            System.out.println("Press Button X to quit, or Button Y to continue.");

            Button choice = ButtonHelper.waitForChoice(bot, Button.X, Button.Y);
            return choice != Button.X;
        }

        return true;
    }

    private void finishGame() {
        try {
            String path = LogWriter.writeFinalLog(user, swiftBotPlayer, board, mode);
            System.out.println("Log file saved to: " + path);
        } catch (IOException ex) {
            System.out.println("Could not write log file: " + ex.getMessage());
        }

        scanner.close();
        System.out.println("Game ended.");
    }
}
