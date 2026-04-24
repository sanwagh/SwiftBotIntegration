package com.SwitBotPersonalProj.NoughtsAndCrosses;

import java.util.Random;
import swiftbot.Button;
import swiftbot.SwiftBotAPI;
import swiftbot.Underlight;

public class SwiftBotPlayer extends Player {

	private SwiftBotAPI swiftbot = SwiftBotAPI.INSTANCE;

	private static final int Speed = 70;
	private static final int Move_Time = 810;
	private static final int Turn_Time = 352;

	// tracks where the robot currently is on the board
	private int currentRow = 1;
	private int currentCol = 1;
	private int facing = 0; // 0 = right, 1 = up, 2 = left, 3 = down

	public SwiftBotPlayer(String name, char piece) {
		super(name, piece);
	}

	public int[] chooseMove(Board board) {
		Random random = new Random();
		int row, col;
		// keep picking random squares until we find one that is empty
		do {
			row = random.nextInt(3) + 1;
			col = random.nextInt(3) + 1;
		} while (board.getCell(row, col) != ' ');
		return new int[] { row, col };
	}

	public void moveToSquare(int targetRow, int targetCol) {
		// System.out.println("SwiftBot moving to square [" + targetRow + "," + targetCol + "]..."); - Debug

		int rowDiff = targetRow - currentRow;
		int colDiff = targetCol - currentCol;

		if (rowDiff > 0) {
			turnToFace(3); // swap: 3 = towards you (down)
			moveForward(rowDiff);
		} else if (rowDiff < 0) {
			turnToFace(1); // swap: 1 = away from you (up)
			moveForward(Math.abs(rowDiff));
		}

		if (colDiff > 0) {
			turnToFace(0);
			moveForward(colDiff);
		} else if (colDiff < 0) {
			turnToFace(2);
			moveForward(Math.abs(colDiff));
		}
		currentRow = targetRow;
		currentCol = targetCol;

		blinkGreen();

		returnToStart();
	}

	public void moveToSquareNoReturn(int targetRow, int targetCol) {
		// System.out.println("SwiftBot moving to square [" + targetRow + "," + targetCol + "]..."); - Debug
		int rowDiff = targetRow - currentRow;
		int colDiff = targetCol - currentCol;

		if (rowDiff > 0) {
			turnToFace(3);
			moveForward(rowDiff);
		} else if (rowDiff < 0) {
			turnToFace(1);
			moveForward(Math.abs(rowDiff));
		}

		if (colDiff > 0) {
			turnToFace(0);
			moveForward(colDiff);
		} else if (colDiff < 0) {
			turnToFace(2);
			moveForward(Math.abs(colDiff));
		}

		currentRow = targetRow;
		currentCol = targetCol;
		blinkGreen();
	}

	private void turnToFace(int targetDirection) {
		// System.out.println("Turning from " + facing + " to " + targetDirection); - Debug
		// use modulo 4 to work out the shortest turn needed
		int diff = (targetDirection - facing + 4) % 4;
		// System.out.println("Diff: " + diff); - Debug

		if (diff == 1) {
			swiftbot.move(-68, 70, Turn_Time - 4); // left 90
			sleep(500);
		} else if (diff == 2) {
			swiftbot.move(69, -70, Turn_Time * 2); // 180
			sleep(500);
		} else if (diff == 3) {
			swiftbot.move(66, -72, Turn_Time - 4);
			sleep(500);
		}

		facing = targetDirection;
	}

	private void moveForward(int squares) {
		// move one square at a time so the robot stays accurate
		for (int i = 0; i < squares; i++) {
			swiftbot.move(80, 79, Move_Time);
			sleep(1000);
		}
	}

	private void returnToStart() {
		// System.out.println("SwiftBot returning to start..."); - Debug
		int rowDiff = 1 - currentRow;
		int colDiff = 1 - currentCol;

		if (rowDiff > 0) {
			turnToFace(3);
			moveForward(rowDiff);
		} else if (rowDiff < 0) {
			turnToFace(1);
			moveForward(Math.abs(rowDiff));
		}
		if (colDiff > 0) {
			turnToFace(0);
			moveForward(colDiff);
		} else if (colDiff < 0) {
			turnToFace(2);
			moveForward(Math.abs(colDiff));
		}
		currentRow = 1;
		currentCol = 1;
		turnToFace(0);
		facing = 0;
	}

	private void blinkGreen() {
		for (int i = 0; i < 3; i++) {
			swiftbot.setUnderlight(Underlight.FRONT_LEFT, new int[] { 0, 255, 0 });
			swiftbot.setUnderlight(Underlight.FRONT_RIGHT, new int[] { 0, 255, 0 });
			sleep(500);
			swiftbot.setUnderlight(Underlight.FRONT_LEFT, new int[] { 0, 0, 0 });
			swiftbot.setUnderlight(Underlight.FRONT_RIGHT, new int[] { 0, 0, 0 });
			sleep(500);
		}
	}

	private void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			System.out.println("Sleep interrupted: " + e.getMessage());
		}
	}

	// Waits for the user to press button A to start
	public void waitForButtonA() {
		System.out.println("Press button A on the SwiftBot to start...");
		final boolean[] pressed = { false };
		swiftbot.enableButton(Button.A, () -> {
			pressed[0] = true;
			swiftbot.disableButton(Button.A);
		});
		// Wait until button is pressed
		while (!pressed[0]) {
			sleep(100);
		}
	}

	public boolean waitForContinueOrQuit() {
		System.out.println("Press Y on SwiftBot to play again, or X to quit.");
		final boolean[] yPressed = { false };
		final boolean[] xPressed = { false };

		swiftbot.enableButton(Button.Y, () -> {
			yPressed[0] = true;
			swiftbot.disableButton(Button.Y);
			swiftbot.disableButton(Button.X);
		});
		swiftbot.enableButton(Button.X, () -> {
			xPressed[0] = true;
			swiftbot.disableButton(Button.X);
			swiftbot.disableButton(Button.Y);
		});

		while (!yPressed[0] && !xPressed[0]) {
			sleep(100);
		}
		return yPressed[0]; // true = play again, false = quit
	}

	public void traceWinningLine(int[] winRow, int[] winCol, char winnerPiece) {
		// blink the winner's colour before and after tracing the line
		if (winnerPiece == 'O') {
			blinkColour(0, 255, 0, 3);
		} else {
			blinkColour(255, 0, 0, 3);
		}

		for (int i = 0; i < 3; i++) {
			moveToSquareNoReturn(winRow[i], winCol[i]);
			sleep(500);
		}

		returnToStart();

		if (winnerPiece == 'O') {
			blinkColour(0, 255, 0, 3);
		} else {
			blinkColour(255, 0, 0, 3);
		}
	}

	public void drawCelebration() {
		blinkColour(0, 0, 255, 3);
		swiftbot.move(Speed, -Speed, Turn_Time * 4); // spin in place
		sleep(500);
		blinkColour(0, 0, 255, 3);
	}

	private void blinkColour(int r, int g, int b, int times) {
		for (int i = 0; i < times; i++) {
			swiftbot.setUnderlight(Underlight.FRONT_LEFT, new int[] { r, g, b });
			swiftbot.setUnderlight(Underlight.FRONT_RIGHT, new int[] { r, g, b });
			swiftbot.setUnderlight(Underlight.BACK_LEFT, new int[] { r, g, b });
			swiftbot.setUnderlight(Underlight.BACK_RIGHT, new int[] { r, g, b });
			sleep(500);
			swiftbot.setUnderlight(Underlight.FRONT_LEFT, new int[] { 0, 0, 0 });
			swiftbot.setUnderlight(Underlight.FRONT_RIGHT, new int[] { 0, 0, 0 });
			swiftbot.setUnderlight(Underlight.BACK_LEFT, new int[] { 0, 0, 0 });
			swiftbot.setUnderlight(Underlight.BACK_RIGHT, new int[] { 0, 0, 0 });
			sleep(500);
		}
	}
}