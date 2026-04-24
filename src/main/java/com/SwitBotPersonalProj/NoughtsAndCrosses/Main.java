package com.SwitBotPersonalProj.NoughtsAndCrosses;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // ANSI colour codes for console output
        String reset  = "\u001B[0m";
        String cyan   = "\u001B[36m";
        String yellow = "\u001B[33m";
        String green  = "\u001B[32m";
        String red    = "\u001B[31m";

        System.out.println(yellow + "================================" + reset);
        System.out.println(cyan   + "   NOUGHTS AND CROSSES GAME    " + reset);
        System.out.println(yellow + "================================" + reset);

        System.out.println(green + "Press button A on the SwiftBot to start..." + reset);
        SwiftBotPlayer swiftBot = new SwiftBotPlayer("SwiftBot", 'X');
        swiftBot.waitForButtonA();

        System.out.print(cyan + "Enter your name: " + reset);
        String humanName = scanner.nextLine().trim();

        // keep asking until the user enters something
        while (humanName.isEmpty()) {
            System.out.print(red + "Name cannot be empty! Enter your name: " + reset);
            humanName = scanner.nextLine().trim();
        }

        Player humanPlayer = new Player(humanName, 'O');
        GameLogger logger = new GameLogger();

        System.out.println(green + "\nWelcome " + humanName + "! You are playing against SwiftBot." + reset);
        logger.log("Game started - Players: " + humanName + " vs SwiftBot");

        Game game = new Game(humanPlayer, swiftBot, logger);
        boolean playAgain = true;

        // playRound returns true if the user wants to play again, false if they want to quit
        while (playAgain) {
            playAgain = game.playRound();
        }

        System.out.println(yellow + "\n========== FINAL SCORES ==========" + reset);
        System.out.println(green + humanPlayer.getName() + ": " + humanPlayer.getScore() + reset);
        System.out.println(red   + swiftBot.getName()    + ": " + swiftBot.getScore()    + reset);

        if (humanPlayer.getScore() > swiftBot.getScore()) {
            System.out.println(green + "\nCongratulations " + humanPlayer.getName() + ", you are the overall winner!" + reset);
        } else if (swiftBot.getScore() > humanPlayer.getScore()) {
            System.out.println(red + "\nSwiftBot is the overall winner!" + reset);
        } else {
            System.out.println(cyan + "\nOverall it's a tie!" + reset);
        }

        logger.log("Game ended - Final scores: " + humanPlayer.getName() + ": " + humanPlayer.getScore() + ", SwiftBot: " + swiftBot.getScore());
        logger.close();
        System.out.println("\nGame log saved to: " + logger.getFilePath());

        scanner.close();
    }
}