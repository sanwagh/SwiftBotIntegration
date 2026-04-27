package com.SwitBotPersonalProj.SnakesLadders;

import swiftbot.SwiftBotAPI;

public class SwiftBotNavigator {

    private enum Heading {
        EAST, WEST, NORTH, SOUTH
    }

    private final SwiftBotAPI bot;
    private final Board board;

    
    private final int moveSpeed = 50;
    private final int turnSpeed = 45;
    private final int cellMoveTimeMs = 1300; 
    private final int turn90TimeMs = 650;    
    private Heading heading;
    private int currentSquare;

    public SwiftBotNavigator(SwiftBotAPI bot, Board board) {
        this.bot = bot;
        this.board = board;
        this.heading = Heading.EAST;
        this.currentSquare = 1;
    }

    public int getCurrentSquare() {
        return currentSquare;
    }

    public void resetToStart() {
        heading = Heading.EAST;
        currentSquare = 1;
    }

    public void moveToSquare(int targetSquare) throws InterruptedException {
        if (targetSquare == currentSquare) {
            return;
        }

        int step = (targetSquare > currentSquare) ? 1 : -1;
        int square = currentSquare;

        while (square != targetSquare) {
            int nextSquare = square + step;
            moveOneStep(square, nextSquare);
            square = nextSquare;
        }

        currentSquare = targetSquare;
    }

    private void moveOneStep(int fromSquare, int toSquare) throws InterruptedException {
        int[] from = board.getCoordinates(fromSquare);
        int[] to = board.getCoordinates(toSquare);

        int rowDiff = to[0] - from[0];
        int colDiff = to[1] - from[1];

        Heading desiredHeading;

        if (rowDiff == 0 && colDiff == 1) {
            desiredHeading = Heading.EAST;
        } else if (rowDiff == 0 && colDiff == -1) {
            desiredHeading = Heading.WEST;
        } else if (rowDiff == -1 && colDiff == 0) {
            desiredHeading = Heading.NORTH;
        } else if (rowDiff == 1 && colDiff == 0) {
            desiredHeading = Heading.SOUTH;
        } else {
            throw new IllegalStateException("Squares are not adjacent: " + fromSquare + " -> " + toSquare);
        }

        turnTo(desiredHeading);
        moveForwardOneSquare();
    }

    private void turnTo(Heading desiredHeading) throws InterruptedException {
        if (heading == desiredHeading) {
            return;
        }

        // 180 degree turn
        if ((heading == Heading.EAST && desiredHeading == Heading.WEST)
                || (heading == Heading.WEST && desiredHeading == Heading.EAST)
                || (heading == Heading.NORTH && desiredHeading == Heading.SOUTH)
                || (heading == Heading.SOUTH && desiredHeading == Heading.NORTH)) {
            turnRight90();
            Thread.sleep(200);
            turnRight90();
            heading = desiredHeading;
            return;
        }

        // 90 degree turn
        if ((heading == Heading.EAST && desiredHeading == Heading.NORTH)
                || (heading == Heading.NORTH && desiredHeading == Heading.WEST)
                || (heading == Heading.WEST && desiredHeading == Heading.SOUTH)
                || (heading == Heading.SOUTH && desiredHeading == Heading.EAST)) {
            turnLeft90();
            heading = desiredHeading;
            return;
        }

        if ((heading == Heading.EAST && desiredHeading == Heading.SOUTH)
                || (heading == Heading.SOUTH && desiredHeading == Heading.WEST)
                || (heading == Heading.WEST && desiredHeading == Heading.NORTH)
                || (heading == Heading.NORTH && desiredHeading == Heading.EAST)) {
            turnRight90();
            heading = desiredHeading;
            return;
        }
    }

    private void turnLeft90() throws InterruptedException {
        bot.move(-turnSpeed, turnSpeed, turn90TimeMs);
        Thread.sleep(200);
    }

    private void turnRight90() throws InterruptedException {
        bot.move(turnSpeed, -turnSpeed, turn90TimeMs);
        Thread.sleep(200);
    }

    private void moveForwardOneSquare() throws InterruptedException {
        bot.move(moveSpeed, moveSpeed, cellMoveTimeMs);
        Thread.sleep(200);
    }
}
