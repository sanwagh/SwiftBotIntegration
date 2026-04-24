package com.SwitBotPersonalProj.DrawAShape;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class DrawShapesFinal {

    private static final int MOVE_SPEED = 50;
    private static final int TURN_SPEED = 50;
    private static final double MS_PER_CM = 50.0;
    private static final int TURN_MS_90 = 700;
    private static final int GREEN_BLINK_MS = 1000;

    private static final int MIN_SIDE = 15;
    private static final int MAX_SIDE = 85;
    private static final int MAX_SHAPES_PER_QR = 5;
    private static final int BACKWARD_GAP_CM = 15;

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    private static final List<DrawnShapeRecord> drawnShapes = new ArrayList<>();
    private static int squareCount = 0;
    private static int triangleCount = 0;
    private static boolean exitRequested = false;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==============================================");
        System.out.println("         SwiftBot Draw Shapes Program         ");
        System.out.println("==============================================");
        System.out.println("Console simulation version");
        System.out.println("Examples:");
        System.out.println("S:20");
        System.out.println("T:16:30:24");
        System.out.println("S:20&T:16:30:24");
        System.out.println("Type X to exit.");
        System.out.println("==============================================");

        while (!exitRequested) {
            try {
                System.out.print("\nEnter QR input: ");
                String qrInput = scanner.nextLine().trim();

                if (qrInput.equalsIgnoreCase("X")) {
                    exitRequested = true;
                    break;
                }

                if (qrInput.isEmpty()) {
                    System.out.println("Error: input cannot be empty.");
                    continue;
                }

                List<ShapeCommand> commands = parseQrInput(qrInput);
                displayCommands(qrInput, commands);

                System.out.print("Do you want to draw these shape(s)? (Y/N): ");
                String confirm = scanner.nextLine().trim();

                if (!confirm.equalsIgnoreCase("Y")) {
                    System.out.println("Drawing cancelled.");
                    continue;
                }

                drawCommands(commands);

            } catch (InvalidInputException e) {
                System.out.println("Input error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }

        try {
            Path logPath = writeLogFile();
            System.out.println("\nLog file saved to: " + logPath.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Could not write log file: " + e.getMessage());
        }

        System.out.println("Program terminated.");
        scanner.close();
    }

    private static void displayCommands(String qrInput, List<ShapeCommand> commands) {
        System.out.println("\nDecoded QR code: " + qrInput);
        System.out.println("Shapes to be drawn:");

        for (int i = 0; i < commands.size(); i++) {
            ShapeCommand command = commands.get(i);

            if (command.type.equals("S")) {
                System.out.println((i + 1) + ". Square with side " + command.sides[0] + " cm");
            } else {
                System.out.println((i + 1) + ". Triangle with sides "
                        + command.sides[0] + ", "
                        + command.sides[1] + ", "
                        + command.sides[2] + " cm");
            }
        }
    }

    private static void drawCommands(List<ShapeCommand> commands) {
        for (int i = 0; i < commands.size() && !exitRequested; i++) {
            ShapeCommand command = commands.get(i);

            if (command.type.equals("S")) {
                int side = command.sides[0];
                long totalTime = drawSquare(side);
                double area = side * side;

                drawnShapes.add(new DrawnShapeRecord(
                        "Square",
                        "Square: " + side + " (time: " + totalTime + " ms)",
                        "Square: " + side,
                        totalTime,
                        area
                ));

                squareCount++;

            } else if (command.type.equals("T")) {
                int a = command.sides[0];
                int b = command.sides[1];
                int c = command.sides[2];

                double[] angles = calculateTriangleAngles(a, b, c);
                long totalTime = drawTriangle(a, b, c, angles);
                double area = calculateTriangleArea(a, b, c);

                drawnShapes.add(new DrawnShapeRecord(
                        "Triangle",
                        "Triangle: " + a + ", " + b + ", " + c
                                + " (angles: " + DF.format(angles[0]) + ", "
                                + DF.format(angles[1]) + ", "
                                + DF.format(angles[2]) + "; time: " + totalTime + " ms)",
                        "Triangle: " + a + ", " + b + ", " + c,
                        totalTime,
                        area
                ));

                triangleCount++;
            }

            if (i < commands.size() - 1) {
                System.out.println("Moving backward " + BACKWARD_GAP_CM + " cm before next shape...");
                moveBackward(distanceToTimeMs(BACKWARD_GAP_CM));
            }
        }
    }

    private static long drawSquare(int sideCm) {
        System.out.println("\nDrawing square with side " + sideCm + " cm...");
        long sideTime = distanceToTimeMs(sideCm);
        long totalTime = 0;

        for (int i = 0; i < 4; i++) {
            moveForward(sideTime);
            totalTime += sideTime;

            turnRight90();
            totalTime += TURN_MS_90;
        }

        blinkGreen();
        System.out.println("Square completed.");
        return totalTime;
    }

    private static long drawTriangle(int a, int b, int c, double[] angles) {
        System.out.println("\nDrawing triangle with sides " + a + ", " + b + ", " + c + " cm...");
        System.out.println("Triangle angles: " + DF.format(angles[0]) + ", "
                + DF.format(angles[1]) + ", " + DF.format(angles[2]));

        int[] sides = {a, b, c};
        double[] exteriorTurns = {
                180.0 - angles[0],
                180.0 - angles[1],
                180.0 - angles[2]
        };

        long totalTime = 0;

        for (int i = 0; i < 3; i++) {
            long sideTime = distanceToTimeMs(sides[i]);
            moveForward(sideTime);
            totalTime += sideTime;

            long turnTime = angleToTurnMs(exteriorTurns[i]);
            turnRight(turnTime);
            totalTime += turnTime;
        }

        blinkGreen();
        System.out.println("Triangle completed.");
        return totalTime;
    }

    private static List<ShapeCommand> parseQrInput(String input) throws InvalidInputException {
        String[] shapeParts = input.split("&");

        if (shapeParts.length == 0) {
            throw new InvalidInputException("No shapes found in QR input.");
        }

        if (shapeParts.length > MAX_SHAPES_PER_QR) {
            throw new InvalidInputException("A maximum of 5 shapes is allowed in one QR code.");
        }

        List<ShapeCommand> commands = new ArrayList<>();

        for (String part : shapeParts) {
            String trimmed = part.trim();

            if (trimmed.isEmpty()) {
                throw new InvalidInputException("Empty shape command found.");
            }

            String[] tokens = trimmed.split(":");

            if (tokens.length < 2) {
                throw new InvalidInputException("Invalid format for command: " + trimmed);
            }

            String shapeType = tokens[0].trim().toUpperCase(Locale.ROOT);

            if (shapeType.equals("S")) {
                if (tokens.length != 2) {
                    throw new InvalidInputException("Square must have exactly 1 side length.");
                }

                int side = parseValidLength(tokens[1].trim());
                commands.add(new ShapeCommand("S", new int[] { side }));

            } else if (shapeType.equals("T")) {
                if (tokens.length != 4) {
                    throw new InvalidInputException("Triangle must have exactly 3 side lengths.");
                }

                int a = parseValidLength(tokens[1].trim());
                int b = parseValidLength(tokens[2].trim());
                int c = parseValidLength(tokens[3].trim());

                if (!isValidTriangle(a, b, c)) {
                    throw new InvalidInputException(
                            "Triangle with sides " + a + ", " + b + ", " + c + " cannot be formed."
                    );
                }

                commands.add(new ShapeCommand("T", new int[] { a, b, c }));

            } else {
                throw new InvalidInputException("Unknown shape code: " + shapeType);
            }
        }

        return commands;
    }

    private static int parseValidLength(String token) throws InvalidInputException {
        try {
            int value = Integer.parseInt(token);

            if (value < MIN_SIDE || value > MAX_SIDE) {
                throw new InvalidInputException(
                        "Length must be between " + MIN_SIDE + " and " + MAX_SIDE + " cm. Got: " + value
                );
            }

            return value;

        } catch (NumberFormatException e) {
            throw new InvalidInputException("Length must be a valid integer. Got: " + token);
        }
    }

    private static boolean isValidTriangle(int a, int b, int c) {
        return a + b > c && a + c > b && b + c > a;
    }

    private static double[] calculateTriangleAngles(int a, int b, int c) {
        double angleA = Math.toDegrees(Math.acos((b * b + c * c - a * a) / (2.0 * b * c)));
        double angleB = Math.toDegrees(Math.acos((a * a + c * c - b * b) / (2.0 * a * c)));
        double angleC = 180.0 - angleA - angleB;
        return new double[] { angleA, angleB, angleC };
    }

    private static double calculateTriangleArea(int a, int b, int c) {
        double s = (a + b + c) / 2.0;
        return Math.sqrt(s * (s - a) * (s - b) * (s - c));
    }

    private static long distanceToTimeMs(int distanceCm) {
        return Math.round(distanceCm * MS_PER_CM);
    }

    private static long angleToTurnMs(double degrees) {
        return Math.round((degrees / 90.0) * TURN_MS_90);
    }

    private static void moveForward(long durationMs) {
        System.out.println("[BOT] Move forward at speed " + MOVE_SPEED + " for " + durationMs + " ms");
        sleep(200);
    }

    private static void moveBackward(long durationMs) {
        System.out.println("[BOT] Move backward at speed " + MOVE_SPEED + " for " + durationMs + " ms");
        sleep(200);
    }

    private static void turnRight90() {
        System.out.println("[BOT] Turn right 90 degrees at speed " + TURN_SPEED + " for " + TURN_MS_90 + " ms");
        sleep(200);
    }

    private static void turnRight(long durationMs) {
        System.out.println("[BOT] Turn right at speed " + TURN_SPEED + " for " + durationMs + " ms");
        sleep(200);
    }

    private static void blinkGreen() {
        System.out.println("[BOT] Blink underlights green for " + GREEN_BLINK_MS + " ms");
        sleep(200);
    }

    private static void sleep(long durationMs) {
        try {
            Thread.sleep(Math.min(durationMs, 200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static Path writeLogFile() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path logPath = Paths.get("draw_log_" + timestamp + ".txt");

        try (BufferedWriter writer = Files.newBufferedWriter(logPath)) {
            writer.write("Draw Shapes Log");
            writer.newLine();
            writer.write("==============================");
            writer.newLine();

            writer.write("Shapes drawn:");
            writer.newLine();

            if (drawnShapes.isEmpty()) {
                writer.write("No shapes were drawn.");
                writer.newLine();
            } else {
                for (DrawnShapeRecord record : drawnShapes) {
                    writer.write(record.logEntry);
                    writer.newLine();
                }
            }

            writer.newLine();
            writer.write("Largest shape by area:");
            writer.newLine();
            writer.write(getLargestShapeSummary());
            writer.newLine();

            writer.newLine();
            writer.write("Most frequent shape:");
            writer.newLine();
            writer.write(getMostFrequentShapeSummary());
            writer.newLine();

            writer.newLine();
            writer.write("Average time:");
            writer.newLine();
            writer.write(getAverageTimeSummary());
            writer.newLine();
        }

        return logPath;
    }

    private static String getLargestShapeSummary() {
        if (drawnShapes.isEmpty()) {
            return "None";
        }

        DrawnShapeRecord largest = drawnShapes.get(0);

        for (DrawnShapeRecord record : drawnShapes) {
            if (record.area > largest.area) {
                largest = record;
            }
        }

        return largest.logSizeSummary;
    }

    private static String getMostFrequentShapeSummary() {
        if (squareCount == 0 && triangleCount == 0) {
            return "None";
        }

        if (squareCount >= triangleCount) {
            return "Square: " + squareCount + " time(s)";
        } else {
            return "Triangle: " + triangleCount + " time(s)";
        }
    }

    private static String getAverageTimeSummary() {
        if (drawnShapes.isEmpty()) {
            return "0 ms";
        }

        long total = 0;

        for (DrawnShapeRecord record : drawnShapes) {
            total += record.timeMs;
        }

        long average = Math.round((double) total / drawnShapes.size());
        return average + " ms";
    }

    private static class ShapeCommand {
        private final String type;
        private final int[] sides;

        public ShapeCommand(String type, int[] sides) {
            this.type = type;
            this.sides = sides;
        }
    }

    private static class DrawnShapeRecord {
        private final String shapeType;
        private final String logEntry;
        private final String logSizeSummary;
        private final long timeMs;
        private final double area;

        public DrawnShapeRecord(String shapeType, String logEntry, String logSizeSummary, long timeMs, double area) {
            this.shapeType = shapeType;
            this.logEntry = logEntry;
            this.logSizeSummary = logSizeSummary;
            this.timeMs = timeMs;
            this.area = area;
        }
    }

    private static class InvalidInputException extends Exception {
        public InvalidInputException(String message) {
            super(message);
        }
    }
}