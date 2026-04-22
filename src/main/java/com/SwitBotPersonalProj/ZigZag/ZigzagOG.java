package com.SwitBotPersonalProj.ZigZag;

import swiftbot.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class ZigzagOG {

	// Calibration data
	private static final int[] speedPicked= {20, 30, 40, 50, 60, 70, 80, 90, 100};
	private static final double[] calibartion = {2.35, 13.3, 20.15, 22.6, 24.65, 26.3, 27.4, 28.1, 29.05};

	private static final int MinimumSpeed = 30;
	private static final int MaximumSpeed = 100;

	// Tune these on your robot
	private static final long rightTurn = 500;
	private static final long leftTurn = 500;
	private static final long Turn180 = 1000;

	private static SwiftBotAPI SwiftBot;
	private static Scanner scanner = new Scanner(System.in); //Creates a Scanner object that reads text typed by the user in the console. System.in means it reads from the keyboard.
	private static Random random = new Random(); //Creates a Random object used to generate random numbers. It is used in chooseSpeed() to generate a random speed between 30 and 100. 

	private static boolean yPressed = false;
	private static boolean xPressed = false;

	private static List<Journey> journeys = new ArrayList<>(); //This creates a list that stores all the journey records for the entire session.

	public static void main(String[] args) {
		try {
			SwiftBot = SwiftBotAPI.INSTANCE;
		} catch (Exception e) {
			System.out.println("ERROR: Could not initialise SwiftBot.");
			return;
		}

		SwiftBot.enableButton(Button.Y, new ButtonFunction() { //Calls the enableButton method on the SwiftBot object. This tells the robot to start listening for button Y being pressed.
			public void run() { // Creates an anonymous class , a class with no name that is created and used in one place. ButtonFunction is an interface from the SwiftBot API that has one method called run()
				yPressed = true;
			}
		});

		SwiftBot.enableButton(Button.X, new ButtonFunction() {
			public void run() {
				xPressed = true;
			}
		});

		System.out.println("================================");
		System.out.println("|    SWIFTBOT ZIGZAG NAVIGATOR |");
		System.out.println("|         Version 1.0          |");
		System.out.println("================================");
		System.out.println("  QR Code format : Length-Sections (e.g. 20-6)");
		System.out.println("  Length: 15-85 cm , Sections: even number, max 12");

		boolean running = true;

		while (running) {
			System.out.println("\nPress Y to scan a QR code.");
			System.out.println("Press X to quit.");
			
			waitForButton(); //  it calls the waitForButton method

			if (xPressed) {
				running = false;

			} else if (yPressed) {

				System.out.println("=========================================== ");
				System.out.println("|             QR CODE SCANNING MODE        |");
				System.out.println("=========================================== ");
				System.out.println("  Hold QR code in front of the camera.");

				String qrInfo = scanQR(); //This line calls your scanQR() method and stores whatever it returns in a variable called qrText.

				if (qrInfo == null || qrInfo.isBlank()) {

				    boolean qrDone = false;

				    while (qrDone == false) {
				        System.out.println("========================================");
				        System.out.println("|               ERROR DETECTED          |");
				        System.out.println("========================================");
				        System.out.println("  QR scan failed.");
				        System.out.println("  Press Y to scan again.");
				        System.out.println("  Type 2 and press Enter to enter manually.");

				        String choice = scanner.nextLine().trim();

				        if (choice.equals("2")) {
				            System.out.print("  Enter value (e.g. 20-6): ");
				            qrInfo = scanner.nextLine().trim();
				            qrDone = true;
				        } else {
				            System.out.println("  Scanning...");
				            qrInfo = scanQR();
				            if (qrInfo != null && !qrInfo.isBlank()) {
				                System.out.println("  QR code read: " + qrInfo);
				                qrDone = true;
				            } else {
				                System.out.println("  Scan failed again.");
				                qrDone = true;
				            }
				        }
				    }
				}
				            
				     
				int[] values = parseInput(qrInfo); //Calls your parseInput() method passing in the QR text and stores the result in a variable called values. int[] means it holds an array of whole numbers. parseInput() either returns an array with two numbers or null if the input was invalid.

				if (values != null) {
					int length   = values[0]; //Gets the first number from the array and stores it in a variable called length
					int sections = values[1]; // Gets the second number from the array and stores it in a variable called sections. This is Value2 from the QR code , the number of zigzag sections.

					int speed = chooseSpeed();
					double speedCmPerSec = getSpeed(speed); //getSpeed() looks up the speed percentage in your calibration table and returns the actual speed in centimetres per second. For example if speed is 50 it looks through the speedPicked array, finds 50 and returns the matching value from calibartion which is 22.6 cm/s.
					long sectionTime = (long) ((length / speedCmPerSec) * 1000);

					double totalPath    = length * sections;
					double straightLine = calculateStraightLine(length, sections);

					System.out.println("\nLength: " + length + " cm");
					System.out.println("Sections: " + sections);
					System.out.println("Speed: " + speed + "%");
					System.out.println("Time per section: " + sectionTime + " ms");
					System.out.println("Total path: " + totalPath + " cm");
					System.out.println("Straight-line distance: " + straightLine + " cm");

					System.out.println("\nPress Y to start or X to cancel.");
					waitForButton();

					if (yPressed) {
						sleep(2000);

						long start = System.currentTimeMillis(); // System.currentTimeMillis() — calls a built in Java method that returns the current time as a number of milliseconds.
						runZigzag(sections, speed, sectionTime);
						double duration = (System.currentTimeMillis() - start) / 1000.0;
						double averageSpeed = totalPath / duration;

						Journey j = new Journey(length, sections, speed, totalPath, straightLine, duration, averageSpeed); //Creates a new Journey object and stores it in a variable called j , you are instantiating an object from the Journey class. new Journey(...) calls the constructor of the Journey class passing in all seven values as parameters. The constructor then saves each value into the object's fields using this.length = length etc. After this line j holds a complete record of everything that happened during this journey.
						journeys.add(j); //Adds the Journey object to the journeys list. The add() method is built into Java's ArrayList — it appends the object to the end of the list.
						writeToFile(j);

						System.out.println("\nJourney complete.");
						System.out.println("Duration      : " + duration + " seconds");
						System.out.println("Average speed : " + averageSpeed + " cm/s");

						System.out.println("\nPress Y for another journey or X to quit.");
						waitForButton();

						if (xPressed) {
							running = false;
						}

					} else {
						System.out.println("Journey cancelled.");
					}
				}
			}
		}

		SwiftBot.disableButton(Button.Y);
		SwiftBot.disableButton(Button.X);
		printSummary();
		System.out.println("Goodbye!");
	}

	private static void runZigzag(int sections, int speed, long sectionTime) {
		boolean[] turns = new boolean[sections - 1]; // true = right, false = left

		System.out.println("====================================");
		System.out.println("|         ZIGZAG IN PROGRESS       |");
		System.out.println("====================================");

		for (int i = 0; i < sections; i++) {
			setColour(i);
			moveForward(speed, sectionTime);

			if (i < sections - 1) {
				turns[i] = (i % 2 == 0); // first turn right, next left, etc.

				if (turns[i]) {
					turnRight();
				} else {
					turnLeft();
				}
			}
		}

		System.out.println("Turning around...");
		turnAround();

		System.out.println("=================================");
		System.out.println("|            RETRACING PATH     |");
		System.out.println("=================================");
		System.out.println("  Retracing path back to start");

		for (int i = sections - 1; i >= 0; i--) {
			setColour(i);
			moveForward(speed, sectionTime);

			if (i > 0) {
				if (turns[i - 1]) {
					turnLeft();   // undo right
				} else {
					turnRight();  // undo left
				}
			}
		}

		SwiftBot.stopMove(); // this is a method call on the SwiftBot API that immediately stops both wheels of the robot.
		setLED(0, 0, 0);
		System.out.println("Back at start.");
	}

	private static void moveForward(int speed, long time) {
		SwiftBot.startMove(speed, speed);
		sleep(time);
		SwiftBot.stopMove();
		sleep(1000);
	}

	private static void turnRight() {
		SwiftBot.startMove(60, -60);
		sleep(rightTurn);
		SwiftBot.stopMove();
		sleep(250);
	}

	private static void turnLeft() {
		SwiftBot.startMove(-60, 60);
		sleep(leftTurn);
		SwiftBot.stopMove();
		sleep(250);
	}

	private static void turnAround() {
		SwiftBot.startMove(60, -60);
		sleep(Turn180);
		SwiftBot.stopMove();
		sleep(300);
	}

	private static void setColour(int section) {
		if (section % 2 == 0) {
			setLED(0, 255, 0); // green
		} else {
			setLED(0, 0, 255); // blue
		}
	}

	private static void setLED(int r, int g, int b) {
		try {
			SwiftBot.fillUnderlights(new int[]{r, g, b});
		} catch (Exception e) {
		}
	}

	private static String scanQR() {
	    try {
	        return SwiftBot.decodeQRImage(SwiftBot.getQRImage());
	    } catch (Exception e) {
	        return null;
	    }
	}

	private static int[] parseInput(String input) { //Integer.parseInt() is a built in Java method that converts a String (text) into an int (whole number).
		if (input == null) return null;

		String[] parts = input.trim().split("-");
		if (parts.length != 2) return null;

		try {
			int length = Integer.parseInt(parts[0].trim());
			int sections = Integer.parseInt(parts[1].trim());

			if (length < 15 || length > 85) return null;
			if (sections < 2 || sections > 12 || sections % 2 != 0) return null;

			return new int[]{length, sections};
		} catch (NumberFormatException e) { // If parseInt failed because the text was not a number catches the error and returns null instead of crashing.
			return null;
		}
	}

	private static int chooseSpeed() {
		System.out.println("\n1 = random speed");
		System.out.println("2 = choose speed");
		System.out.print("Enter 1 or 2: ");
		String choice = scanner.nextLine().trim();

		if (choice.equals("2")) {
			while (true) {
				System.out.print("Enter speed (30-100): ");
				try {
					int speed = Integer.parseInt(scanner.nextLine().trim());
					if (speed >= 30 && speed <= 100) {
						return speed;
					}
				} catch (Exception e) {
				}
				System.out.println("Invalid speed.");
			}
		}

		return MinimumSpeed + random.nextInt(MaximumSpeed - MinimumSpeed + 1);
	}

	private static double getSpeed(int speedPercent) {
		for (int i = 0; i < speedPicked.length; i++) {
			if (speedPicked[i] == speedPercent) return calibartion[i];
		}

		for (int i = 0; i < speedPicked.length - 1; i++) {
			if (speedPercent > speedPicked[i] && speedPercent < speedPicked[i + 1]) {
				double ratio = (double) (speedPercent - speedPicked[i]) / (speedPicked[i + 1] - speedPicked[i]);
				return calibartion[i] + ratio * (calibartion[i + 1] - calibartion[i]);
			}
		}

		return calibartion[calibartion.length - 1];
	}

	private static double calculateStraightLine(int length, int sections) {
		double x = 0, y = 0;
		int direction = 0; // 0 up, 1 right, 2 down, 3 left

		for (int i = 0; i < sections; i++) {
			if (direction == 0) y += length;
			else if (direction == 1) x += length;
			else if (direction == 2) y -= length;
			else x -= length;

			if (i < sections - 1) {
				if (i % 2 == 0) direction = (direction + 1) % 4;
				else direction = (direction + 3) % 4;
			}
		}

		return Math.sqrt(x * x + y * y);
	}

	private static void writeToFile(Journey j) {
		try (FileWriter writer = new FileWriter("zigzag_log.txt", true)) { //FileWriter is a built in Java class that creates and writes to files. It comes from
			writer.write("===== JOURNEY =====\n");
			writer.write("Time: " + LocalDateTime.now() + "\n");
			writer.write("Section length: " + j.length + " cm\n");
			writer.write("Sections: " + j.sections + "\n");
			writer.write("Speed: " + j.speed + "%\n");
			writer.write("Total path: " + j.totalPath + " cm\n");
			writer.write("Straight-line: " + j.straightLine + " cm\n");
			writer.write("Duration: " + j.duration + " seconds\n");
			writer.write("Average speed: " + j.avgSpeed + " cm/s\n");
			writer.write("===================\n");
			System.out.println("Saved to zigzag_log.txt");
		} catch (IOException e) {
			System.out.println("Could not write log file.");
		}
	}

	private static void printSummary() {
		System.out.println("===================================");
		System.out.println("|         SESSION SUMMARY          |");
		System.out.println("====================================");
		System.out.println("Journeys completed: " + journeys.size());

		if (journeys.isEmpty()) {
			System.out.println("Log file: zigzag_log.txt");
			return;
		}

		Journey longest = journeys.get(0);
		Journey shortest = journeys.get(0);

		for (Journey j : journeys) {
			if (j.straightLine > longest.straightLine) longest = j;
			if (j.straightLine < shortest.straightLine) shortest = j;
		}

		System.out.println("\nLongest straight-line journey:");
		System.out.println("Length: " + longest.length + " cm, Sections: " + longest.sections + ", Distance: " + longest.straightLine + " cm");

		System.out.println("\nShortest straight-line journey:");
		System.out.println("Length: " + shortest.length + " cm, Sections: " + shortest.sections + ", Distance: " + shortest.straightLine + " cm");

		System.out.println("\nLog file: zigzag_log.txt");
	}

	private static void waitForButton() {
		yPressed = false;
		xPressed = false;

		while (yPressed == false && xPressed == false) { 
			sleep(100);
		}
	}

	private static void sleep(long ms) {
		try {
			Thread.sleep(ms); //This is a built in Java method that pauses the program for the number of milliseconds stored in ms. Thread refers to the current running thread
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static class Journey {
		int length, sections, speed;
		double totalPath, straightLine, duration, avgSpeed;

		Journey(int length, int sections, int speed, double totalPath,
				double straightLine, double duration, double avgSpeed) {
			this.length = length;
			this.sections = sections;
			this.speed = speed;
			this.totalPath = totalPath;
			this.straightLine = straightLine;
			this.duration = duration;
			this.avgSpeed = avgSpeed;
		}
	}
}
