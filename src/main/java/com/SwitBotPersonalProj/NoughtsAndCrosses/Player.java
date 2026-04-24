package com.SwitBotPersonalProj.NoughtsAndCrosses;

public class Player {

    private String name;
    private char piece;
    private int score;

    public Player(String name, char piece) {
        this.name = name;
        this.piece = piece;
        this.score = 0; // score always starts at 0
    }

    public void addScore() {
        score++;
    }

    public String getName() {
        return name;
    }

    public char getPiece() {
        return piece;
    }

    public int getScore() {
        return score;
    }

    public void setPiece(char piece) {
        // piece can change each round depending on who wins the dice roll
        this.piece = piece;
    }
}