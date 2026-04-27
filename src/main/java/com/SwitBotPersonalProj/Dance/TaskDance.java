package com.SwitBotPersonalProj.Dance;

import swiftbot.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;

public class TaskDance {


    static SwiftBotAPI bot;
    
    static final int TOP_SPEED = 100;

    static volatile boolean yPressed = false;
    static volatile boolean xPressed = false;

    //main

    public static void main(String[] args) {

        bot = SwiftBotAPI.INSTANCE;

        //disable any previously registered buttons and set up Y and X
        bot.disableAllButtons();
        bot.enableButton(Button.Y, () -> yPressed = true);
        bot.enableButton(Button.X, () -> xPressed = true);

        //stores every valid hex value entered across all scans for the final log
        ArrayList<String> hexHistory = new ArrayList<>();
        boolean keepGoing = true;

        //start screen banner
        System.out.println("  +=========================================+");
        System.out.println("  |           Little Party Dancer           |");
        System.out.println("  +=========================================+");

        //main program loop
        while (keepGoing) {

            //prompt the user to scan a QR code
            System.out.println("\nGet the QR code in front of the camera!");
            String scanned = readQRCode();
            //if nothing was scanned try again
            if (scanned == null || scanned.isEmpty()) {
                continue;
            }
            System.out.println("Got you! I read: " + scanned);

            //split the scanned string to get individual hex values
            String[] chunks = scanned.split("&");
            //reject if more than 5 values were provided
            if (chunks.length > 5) {
                System.out.println("Too many to handle! Max is 5 so try again! :(");
                continue;
            }

            //separate valid from invalid
            ArrayList<String> goodValues = new ArrayList<>();
            ArrayList<String> badValues  = new ArrayList<>();

            for (String chunk : chunks) {
                String trimmed = chunk.trim();
                if (looksLikeHex(trimmed)) {
                    goodValues.add(trimmed.toUpperCase());
                } else {
                    badValues.add(trimmed);
                }
            }

            //skipped invalid values
            if (!badValues.isEmpty()) {
                System.out.print("Skipping invalid values: ");
                for (int i = 0; i < badValues.size(); i++) {
                    System.out.print(badValues.get(i));
                    if (i < badValues.size() - 1) System.out.print(", ");
                }
                System.out.println();
            }

            //if there are no valid values scan again
            if (goodValues.isEmpty()) {
                System.out.println("No valid hex values found. Give it another go!");
                continue;
            }

            //storing the speed and color from the last hex for the reverse dance
            ArrayList<String> allMoves = new ArrayList<>();
            int[] lastSettings = null;

            // run the dance routine for each valid hex value one by one
            for (String hex : goodValues) {
                try {
                    lastSettings = danceRoutine(hex, allMoves);
                    hexHistory.add(hex);
                } catch (InterruptedException e) {
                    System.err.println("Uh oh, dance got interrupted: " + e.getMessage());
                }
            }

            //once all dances are done, offer the user to replay everything in reverse
            if (lastSettings != null && !allMoves.isEmpty()) {
                String[] fullRoutine = allMoves.toArray(new String[0]);
                try {
                    offerReversed(fullRoutine,
                        lastSettings[0], // speed
                        lastSettings[1], // forward duration
                        lastSettings[2], // spin duration
                        lastSettings[3], // r
                        lastSettings[4], // g
                        lastSettings[5]  // b
                    );
                } catch (InterruptedException e) { }
            }

            // ask the user whether to scan another QR code or end
            System.out.println("Press Y to scan another QR code");
            System.out.println("or press X to wrap up and save.");

            try {
                boolean again = waitForChoice();
                if (again) {
                    System.out.println("\nLet's go again!\n");
                    keepGoing = true;
                } else {
                    System.out.println("\nWrapping everything up...\n");
                    keepGoing = false;
                }
            } catch (InterruptedException e) {
                keepGoing = false;
            }
        }

        //print all hex values entered
        System.out.print("Values entered: ");
        for (int i = 0; i < hexHistory.size(); i++) {
            System.out.print(hexHistory.get(i));
            if (i < hexHistory.size() - 1) System.out.print(", ");
        }
        System.out.println();

        //sort all entered hex values in ascending order
        sortHexList(hexHistory);

        //print the sorted list
        System.out.print("Sorted: ");
        for (int i = 0; i < hexHistory.size(); i++) {
            System.out.print(hexHistory.get(i));
            if (i < hexHistory.size() - 1) System.out.print(", ");
        }
        System.out.println();

        //write the sorted hex values to a text file and display the file path
        String logFile = "swiftbot_log.txt";
        try (FileWriter writer = new FileWriter(logFile)) {
            writer.write("Sorted hex values (ascending):\n");
            for (String hex : hexHistory) {
                writer.write(hex + "\n");
            }
        } catch (IOException e) {
            System.err.println("Couldn't save the log: " + e.getMessage());
        }

        System.out.println("Log saved to: " + new java.io.File(logFile).getAbsolutePath());
        System.out.println("\nThanks for using the service!\n");
    }

    //qr code scanner
    //uses the SwiftBot camera to capture and decode a QR code every 500ms
    static String readQRCode() {
        System.out.println("Scanning QR code...");
        while (true) {
            BufferedImage photo = bot.getQRImage();
            if (photo != null) {
                String decoded = bot.decodeQRImage(photo);
                if (decoded != null && !decoded.trim().isEmpty()) {
                    return decoded.trim();
                }
            }
            try { Thread.sleep(500); } catch (InterruptedException e) { }
        }
    }

    //checking input
    static boolean looksLikeHex(String input) {
        if (input == null || input.length() == 0 || input.length() > 2) return false;
        for (int i = 0; i < input.length(); i++) {
            char ch = Character.toUpperCase(input.charAt(i));
            if (!((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F'))) return false;
        }
        return true;
    }

    //conversions

    //hex to deci
    static int hexToNumber(String hex) {
        hex = hex.toUpperCase();
        int total = 0;
        for (int i = 0; i < hex.length(); i++) {
            char ch = hex.charAt(i);
            int digit;
            if (ch >= '0' && ch <= '9') {
                digit = ch - '0';
            } else if (ch >= 'A' && ch <= 'F') {
                digit = 10 + (ch - 'A');
            } else {
                throw new IllegalArgumentException("Not a valid hex character: " + ch);
            }
            total = total * 16 + digit;
        }
        return total;
    }

    //deci to oct
    static String numberToOctal(int number) {
        if (number == 0) return "0";
        StringBuilder digits = new StringBuilder();
        int n = number;
        while (n > 0) {
            digits.insert(0, (char) ('0' + (n % 8)));
            n /= 8;
        }
        return digits.toString();
    }

    //deci to bi
    static String numberToBinary(int number) {
        if (number == 0) return "0";
        StringBuilder digits = new StringBuilder();
        int n = number;
        while (n > 0) {
            digits.insert(0, (char) ('0' + (n % 2)));
            n /= 2;
        }
        return digits.toString();
    }

    //oct to a deci
    static int octalToNumber(String octal) {
        int total = 0;
        for (int i = 0; i < octal.length(); i++) {
            total = total * 8 + (octal.charAt(i) - '0');
        }
        return total;
    }

    //calculates speed and color, builds the move list, then performs the dance
    //returns the dance settings for reverse dance
    static int[] danceRoutine(String hexValue, ArrayList<String> sharedMoves) throws InterruptedException {
        hexValue = hexValue.trim().toUpperCase();
        boolean isTwoDigits = hexValue.length() == 2;

        // convert hex to decimal, octal and binary
        int asNumber    = hexToNumber(hexValue);
        String asOctal  = numberToOctal(asNumber);
        String asBinary = numberToBinary(asNumber);
        int octalNumber = octalToNumber(asOctal);

        //calculate speed from octal value
        //if octal is less than 50, add 50 to ensure a visible movement speed
        int danceSpeed;
        if (octalNumber < 50) {
            danceSpeed = octalNumber + 50;
        } else {
            danceSpeed = octalNumber;
        }
        if (danceSpeed > TOP_SPEED) {
            danceSpeed = TOP_SPEED;
        }

        //calculate LED color from the decimal value
        int red   = asNumber;
        int green = (asNumber % 80) * 3;
        int blue  = Math.max(red, green);

        //forward duration depends on hex length
        int forwardTime = isTwoDigits ? 500 : 1000;
        int spinTime    = 750;

        // build the move sequence by reading the binary string right to left
        String[] moves = new String[asBinary.length()];
        for (int i = 0; i < asBinary.length(); i++) {
            char bit = asBinary.charAt(asBinary.length() - 1 - i);
            moves[i] = (bit == '1') ? "forward" : "spin";
        }

        for (String move : moves) {
            sharedMoves.add(move);
        }

        StringBuilder moveDisplay = new StringBuilder();
        for (int i = 0; i < moves.length; i++) {
            moveDisplay.append(moves[i].equals("forward") ? "Forward" : "Spin");
            if (i < moves.length - 1) moveDisplay.append(" ... ");
        }

        //b4 dancing, display all conversion results and movement 
        System.out.println("\n----------------------------------------");
        System.out.println("Hex:      " + hexValue);
        System.out.println("Octal:    " + asOctal);
        System.out.println("Decimal:  " + asNumber);
        System.out.println("Binary:   " + asBinary);
        System.out.println("----------------------------------------");
        System.out.println("Speed:    " + danceSpeed);
        System.out.println("Lights:   R:" + red + "  G:" + green + "  B:" + blue);
        System.out.println("Duration: " + (forwardTime / 1000.0) + "s forward / " + (spinTime / 1000.0) + "s spin");
        System.out.println("Moves:    " + moveDisplay);
        System.out.println("----------------------------------------\n");

        LEDglow(red, green, blue);
        System.out.println("Let's go! Dancing for: " + hexValue);
        Thread.sleep(3000); // 3 second delay before moving so user have time to place the bot on the floor
        performMoves(moves, danceSpeed, forwardTime, spinTime);
        lightsOut();
        System.out.println("Done with " + hexValue + "!");
        Thread.sleep(1000); //1 second pause for efficiency

        //return the dance settings for the reverse dance to use
        return new int[]{danceSpeed, forwardTime, spinTime, red, green, blue};
    }

    //movement

    //forward
    static void driveForward(int speed, int milliseconds) throws InterruptedException {
        bot.move(speed, speed, milliseconds);
        Thread.sleep(milliseconds + 200); // extra 200ms gap before next move
    }

    //spinning
    static void spinAround(int speed, int milliseconds) throws InterruptedException {
        bot.move(speed, -speed, milliseconds);
        Thread.sleep(milliseconds + 200); // extra 200ms gap before next move
    }

    //sets the underlight color using rgb array
    static void LEDglow(int red, int green, int blue) {
        int[] colour = {red, green, blue};
        bot.fillUnderlights(colour);
    }

    //turns all underlights off
    static void lightsOut() {
        bot.disableUnderlights();
    }

    static void performMoves(String[] moves, int speed,
                             int forwardTime, int spinTime)
                             throws InterruptedException {
        for (int i = 0; i < moves.length; i++) {
            if (moves[i].equals("forward")) {
                System.out.println("---Get moving!!---");
                driveForward(speed, forwardTime);
            } else {
                System.out.println(">>>Spinning time<<<");
                spinAround(speed, spinTime);
            }
        }
    }

    //reverse dance

    static void offerReversed(String[] allMoves, int speed,
                            int forwardTime, int spinTime,
                            int red, int green, int blue)
                            throws InterruptedException {

    System.out.println("\nWanna see that in reverse? (Y = Yes or X = No)");
    boolean wantsReversed = waitForChoice();

        if (wantsReversed) {
            //copy the move list in reverse order
            String[] flipped = new String[allMoves.length];
            for (int i = 0; i < allMoves.length; i++) {
                flipped[i] = allMoves[allMoves.length - 1 - i];
            }
            System.out.println("\n  ** Reverse dance Time! **");
            LEDglow(red, green, blue);
            Thread.sleep(3000);//3 second wait time
            performMoves(flipped, speed, forwardTime, spinTime);
            lightsOut();
            System.out.println("Reverse dance complete!\n");

        } else {
            System.out.println("No worries, moving on!\n");
        }
    }

    //buttons

    static boolean waitForChoice() throws InterruptedException {
        yPressed = false;
        xPressed = false;
        while (!yPressed && !xPressed) {
            Thread.sleep(100);
        }
        return yPressed;
    }

    //sorts the hex list in ascending order
    //compares by decimal value so the sort is numerically correct
    static void sortHexList(ArrayList<String> hexList) {
        int size = hexList.size();
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - i - 1; j++) {
                int a = hexToNumber(hexList.get(j));
                int b = hexToNumber(hexList.get(j + 1));
                //swap if left value is greater than right
                if (a > b) {
                    String temp = hexList.get(j);
                    hexList.set(j, hexList.get(j + 1));
                    hexList.set(j + 1, temp);
                }
            }
        }
    }
}