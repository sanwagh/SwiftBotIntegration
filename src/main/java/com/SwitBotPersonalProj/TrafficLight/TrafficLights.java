package com.SwitBotPersonalProj.TrafficLight;

import swiftbot.SwiftBotAPI;
import swiftbot.ImageSize;
import swiftbot.Button;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TrafficLights {

    // Underlight RGB constants
    private static final int[] RGB_RED = new int[] { 255, 0, 0 };
    private static final int[] RGB_GREEN = new int[] { 0, 255, 0 };
    private static final int[] RGB_BLUE = new int[] { 0, 0, 255 };
    private static final int[] RGB_YELLOW = new int[] { 255, 255, 0 };
    private static final int[] RGB_OFF = new int[] { 0, 0, 0 };

    private enum LightColor {
        RED, GREEN, BLUE, UNKNOWN
    }

    // Buttons / control flags
    private static final AtomicBoolean startPressed = new AtomicBoolean(false);
    private static final AtomicBoolean terminatePressed = new AtomicBoolean(false);

    // Prompt choice flags: Y => yes, X => no
    private static final AtomicBoolean promptChoiceMade = new AtomicBoolean(false);
    private static final AtomicBoolean promptChoiceYes = new AtomicBoolean(false);
    private static final AtomicBoolean waitingForPrompt = new AtomicBoolean(false);

    // Stats + logs
    private static final Map<LightColor, AtomicInteger> colorCounts = new EnumMap<>(LightColor.class);
    private static final StringBuilder eventLog = new StringBuilder();

    // Debounce: require same colour twice
    private static LightColor lastCandidate = LightColor.UNKNOWN;
    private static int sameCount = 0;

    public static void main(String[] args) {
        for (LightColor c : LightColor.values()) {
            colorCounts.put(c, new AtomicInteger(0));
        }

        SwiftBotAPI bot = SwiftBotAPI.INSTANCE;
        long startTimeMs = System.currentTimeMillis();

        // ------------------- CLI configuration -------------------
        Scanner sc = new Scanner(System.in);

        int initialSpeed = readIntInRange(sc, "Enter initial forward speed (1-100) [default 20]: ", 1, 100, 20);

        int scanIntervalMs = readIntInRange(sc, "Enter scan interval ms (100-2000) [default 200]: ", 100, 2000, 200);

        int detectDistanceCm = readIntInRange(sc,
                "Enter detection distance threshold cm (10-60) [default 30]: ", 10, 60, 30);

        // Green behaviour: must pass within 2 seconds
        int passDurationMs = 2000;
        int passSpeed = clampInt(initialSpeed * 2, 1, 100);

        // Blue manoeuvre calibration inputs
        int blueTurnSpeed = readIntInRange(sc, "Enter blue turn speed (10-100) [default 40]: ", 10, 100, 40);

        int turn90Ms = readIntInRange(sc,
                "Enter calibrated 90° turn duration (ms) (200-2000) [default 650]: ", 200, 2000, 650);

        int blueForwardSpeed = readIntInRange(sc, "Enter blue sidestep forward speed (1-50) [default 15]: ", 1, 50,
                15);

        System.out.println("\nConfiguration:");
        System.out.println("  initialSpeed      = " + initialSpeed);
        System.out.println("  scanIntervalMs    = " + scanIntervalMs);
        System.out.println("  detectDistanceCm  = " + detectDistanceCm);
        System.out.println("  passSpeed/2s      = " + passSpeed + " for " + passDurationMs + " ms");
        System.out.println("  blueTurnSpeed     = " + blueTurnSpeed);
        System.out.println("  turn90Ms          = " + turn90Ms);
        System.out.println("  blueForwardSpeed  = " + blueForwardSpeed);
        System.out.println();

        // ------------------- Button callbacks -------------------
        bot.enableButton(Button.A, () -> {
            startPressed.set(true);
            System.out.println("[Button A] Start pressed.");
        });

        bot.enableButton(Button.X, () -> {
            if (waitingForPrompt.get()) {
                promptChoiceMade.set(true);
                promptChoiceYes.set(false);
                System.out.println("[Button X] No selected.");
            } else {
                terminatePressed.set(true);
                System.out.println("[Button X] Terminate pressed.");
            }
        });

        bot.enableButton(Button.Y, () -> {
            if (waitingForPrompt.get()) {
                promptChoiceMade.set(true);
                promptChoiceYes.set(true);
                System.out.println("[Button Y] Yes selected.");
            }
        });

        // Wait for start
        System.out.println("Press Button A to start. Press Button X to terminate any time.");
        while (!startPressed.get() && !terminatePressed.get()) {
            sleepQuiet(50);
        }

        if (terminatePressed.get()) {
            shutdown(bot, startTimeMs);
            return;
        }

        // Start: yellow underlights, begin moving forward at initial speed
        fillUnderlightsSafe(bot, RGB_YELLOW);
        bot.startMove(initialSpeed, initialSpeed);
        System.out.println("Starting movement. Initial speed = " + initialSpeed);
        eventLog.append(ts()).append(" START initialSpeed=").append(initialSpeed).append("\n");

        int detections = 0;

        // ------------------- Main loop -------------------
        while (!terminatePressed.get()) {
            try {
                double distance = bot.useUltrasound(); // typically cm

                if (isBadDistance(distance)) {
                    sleepQuiet(scanIntervalMs);
                    continue;
                }

                // Only consider lights when within threshold
                if (distance <= detectDistanceCm) {
                    // stop to take a stable photo
                    bot.stopMove();

                    BufferedImage img = bot.takeStill(ImageSize.SQUARE_240x240);

                    // Use ROI average (center) for better robustness
                    int[] avg = averageRGBCenterROI(img);

                    // Classify with HSV for stability
                    LightColor detected = classifyHSV(avg);

                    // Debounce: require the same non-UNKNOWN colour twice consecutively
                    updateDebounce(detected);
                    if (detected == LightColor.UNKNOWN || sameCount < 2) {
                        // resume moving; not confident yet
                        fillUnderlightsSafe(bot, RGB_YELLOW);
                        bot.startMove(initialSpeed, initialSpeed);
                        sleepQuiet(scanIntervalMs);
                        continue;
                    }

                    // We are confident: freeze candidate for this event
                    LightColor finalDetected = detected;
                    resetDebounce();

                    detections++;
                    colorCounts.get(finalDetected).incrementAndGet();

                    System.out.printf("Detected: %s | Distance: %.1f cm | AvgRGB=(%d,%d,%d)%n", finalDetected, distance,
                            avg[0], avg[1], avg[2]);

                    eventLog.append(ts()).append(" DETECTED=").append(finalDetected).append(" DistanceCm=")
                            .append(String.format("%.1f", distance)).append(" AvgRGB=(").append(avg[0]).append(",")
                            .append(avg[1]).append(",").append(avg[2]).append(")\n");

                    // -------- Behaviour for each colour --------
                    switch (finalDetected) {
                    case GREEN -> {
                        fillUnderlightsSafe(bot, RGB_GREEN);
                        bot.move(passSpeed, passSpeed, passDurationMs);
                        bot.stopMove();

                        sleepQuiet(1000);

                        fillUnderlightsSafe(bot, RGB_YELLOW);
                        bot.startMove(initialSpeed, initialSpeed);
                    }
                    case RED -> {
                        fillUnderlightsSafe(bot, RGB_RED);
                        bot.stopMove();

                        sleepQuiet(1000);

                        fillUnderlightsSafe(bot, RGB_YELLOW);
                        bot.startMove(initialSpeed, initialSpeed);
                    }
                    case BLUE -> {
                        bot.stopMove();
                        sleepQuiet(1000);

                        blinkUnderlights(bot, RGB_BLUE, 6, 200);

                        // Turn left 90°
                        bot.move(-blueTurnSpeed, blueTurnSpeed, turn90Ms);
                        bot.stopMove();

                        // Move forward slowly 1 second
                        bot.move(blueForwardSpeed, blueForwardSpeed, 1000);
                        bot.stopMove();

                        sleepQuiet(1000);

                        // Move back to original path
                        bot.move(-blueForwardSpeed, -blueForwardSpeed, 1000);
                        bot.stopMove();

                        // Face forward again (turn right 90°)
                        bot.move(blueTurnSpeed, -blueTurnSpeed, turn90Ms);
                        bot.stopMove();

                        fillUnderlightsSafe(bot, RGB_YELLOW);
                        bot.startMove(initialSpeed, initialSpeed);
                    }
                    case UNKNOWN -> {
                        fillUnderlightsSafe(bot, RGB_YELLOW);
                        sleepQuiet(300);
                        bot.startMove(initialSpeed, initialSpeed);
                    }
                    }

                    // -------- After every 3 traffic lights --------
                    if (detections % 3 == 0 && !terminatePressed.get()) {
                        bot.stopMove();
                        System.out.println("\nHandled " + detections + " traffic lights.");
                        System.out.println("Press Button Y to CONTINUE or Button X to TERMINATE.");

                        boolean cont = waitForYesNoByButtons();
                        if (!cont) {
                            terminatePressed.set(true);
                            break;
                        }

                        fillUnderlightsSafe(bot, RGB_YELLOW);
                        bot.startMove(initialSpeed, initialSpeed);
                        eventLog.append(ts()).append(" CONTINUE after 3-lights prompt\n");
                        System.out.println("Continuing...\n");
                    }
                }

                sleepQuiet(scanIntervalMs);

            } catch (Exception ex) {
                System.err.println("Loop error: " + ex.getMessage());
                eventLog.append(ts()).append(" ERROR: ").append(ex).append("\n");
                sleepQuiet(scanIntervalMs);
            }
        }

        // ------------------- Termination flow -------------------
        bot.stopMove();
        fillUnderlightsSafe(bot, RGB_OFF);

        long durationMs = System.currentTimeMillis() - startTimeMs;

        System.out.println("\nTerminating.");
        System.out.println("Press Button Y to DISPLAY the execution summary, or Button X to SKIP display.");

        boolean display = waitForYesNoByButtons();

        String summary = buildSummary(detections, durationMs);

        if (display) {
            System.out.println("\n===== EXECUTION SUMMARY =====");
            System.out.print(summary);
            System.out.println("===== END SUMMARY =====");
        }

        String filePath = writeLogToFile(summary, eventLog.toString());
        System.out.println("\nLog written to: " + filePath);

        shutdown(bot, startTimeMs);
    }

    // ------------------- Detection improvements -------------------

    // Average only center ROI, ignore very dark pixels
    private static int[] averageRGBCenterROI(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();

        int x0 = (int) (w * 0.30);
        int x1 = (int) (w * 0.70);
        int y0 = (int) (h * 0.30);
        int y1 = (int) (h * 0.70);

        long sumR = 0, sumG = 0, sumB = 0;
        long n = 0;

        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                if (r + g + b < 40)
                    continue;

                sumR += r;
                sumG += g;
                sumB += b;
                n++;
            }
        }

        if (n == 0)
            return new int[] { 0, 0, 0 };

        return new int[] { (int) (sumR / n), (int) (sumG / n), (int) (sumB / n) };
    }

    // HSV-based classification for stability
    private static LightColor classifyHSV(int[] avg) {
        int r = avg[0], g = avg[1], b = avg[2];

        if (r + g + b < 80)
            return LightColor.UNKNOWN;

        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        float hue = hsb[0] * 360f;
        float sat = hsb[1];
        float bri = hsb[2];

        if (sat < 0.25f || bri < 0.20f)
            return LightColor.UNKNOWN;

        if (hue < 20 || hue > 340)
            return LightColor.RED;

        if (hue >= 80 && hue <= 160)
            return LightColor.GREEN;

        if (hue >= 190 && hue <= 260)
            return LightColor.BLUE;

        return LightColor.UNKNOWN;
    }

    private static void updateDebounce(LightColor detected) {
        if (detected != LightColor.UNKNOWN && detected == lastCandidate) {
            sameCount++;
        } else {
            lastCandidate = detected;
            sameCount = 1;
        }
    }

    private static void resetDebounce() {
        lastCandidate = LightColor.UNKNOWN;
        sameCount = 0;
    }

    // ------------------- Movement / I/O helpers -------------------

    private static boolean isBadDistance(double d) {
        return Double.isNaN(d) || Double.isInfinite(d) || d < 0;
    }

    private static int readIntInRange(Scanner sc, String prompt, int min, int max, int def) {
        while (true) {
            try {
                System.out.print(prompt);
                String line = sc.nextLine().trim();

                if (line.isEmpty())
                    return def;

                int v = Integer.parseInt(line);
                if (v < min || v > max) {
                    System.out.println("Invalid input. Enter an integer in [" + min + ", " + max + "].");
                    continue;
                }
                return v;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    private static void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    private static void fillUnderlightsSafe(SwiftBotAPI bot, int[] rgb) {
        try {
            bot.fillUnderlights(rgb);
        } catch (Exception ex) {
            System.err.println("Underlights error: " + ex.getMessage());
        }
    }

    private static void blinkUnderlights(SwiftBotAPI bot, int[] rgb, int blinks, int intervalMs) {
        for (int i = 0; i < blinks && !terminatePressed.get(); i++) {
            fillUnderlightsSafe(bot, rgb);
            sleepQuiet(intervalMs);
            fillUnderlightsSafe(bot, RGB_OFF);
            sleepQuiet(intervalMs);
        }
    }

    // Wait for Y or X during prompts only
    private static boolean waitForYesNoByButtons() {
        promptChoiceMade.set(false);
        promptChoiceYes.set(false);
        waitingForPrompt.set(true);

        while (!promptChoiceMade.get()) {
            sleepQuiet(50);
        }

        waitingForPrompt.set(false);
        return promptChoiceYes.get();
    }

    private static String buildSummary(int detections, long durationMs) {
        LightColor[] realColours = { LightColor.RED, LightColor.GREEN, LightColor.BLUE };

        LightColor most = null;
        int mostCount = -1;

        for (LightColor c : realColours) {
            int count = colorCounts.get(c).get();
            if (count > mostCount) {
                mostCount = count;
                most = c;
            }
        }

        long sec = durationMs / 1000;
        long ms = durationMs % 1000;

        StringBuilder sb = new StringBuilder();
        sb.append("Traffic lights encountered: ").append(detections).append("\n");
        sb.append("Most frequent colour: ").append(most == null ? "None" : most).append("\n");
        sb.append("Most frequent colour count: ").append(Math.max(mostCount, 0)).append("\n");
        sb.append("Total duration: ").append(sec).append(".").append(String.format("%03d", ms))
                .append(" seconds\n\n");
        sb.append("Counts:\n");

        for (LightColor c : realColours) {
            sb.append("  ").append(c).append(": ").append(colorCounts.get(c).get()).append("\n");
        }

        return sb.toString();
    }

    private static String writeLogToFile(String summary, String details) {
        try {
            File dir = new File(System.getProperty("user.dir"), "swiftbot_logs");
            if (!dir.exists() && !dir.mkdirs())
                dir = new File(System.getProperty("user.dir"));

            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File out = new File(dir, "traffic_log_" + stamp + ".txt");

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(out))) {
                bw.write("===== SUMMARY =====\n");
                bw.write(summary);
                bw.write("\n===== EVENT LOG =====\n");
                bw.write(details);
            }
            return out.getAbsolutePath();
        } catch (Exception ex) {
            System.err.println("Failed to write log file: " + ex.getMessage());
            return "(failed to write log)";
        }
    }

    private static String ts() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static void shutdown(SwiftBotAPI bot, long startTimeMs) {
        try {
            bot.stopMove();
        } catch (Exception ignored) {
        }
        try {
            bot.disableUnderlights();
        } catch (Exception ignored) {
        }
        try {
            bot.disableAllButtons();
        } catch (Exception ignored) {
        }

        long dur = System.currentTimeMillis() - startTimeMs;
        System.out.println("Shutdown complete. Duration: " + (dur / 1000.0) + "s");
    }
}