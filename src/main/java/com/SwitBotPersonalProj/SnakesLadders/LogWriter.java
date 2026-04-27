package com.SwitBotPersonalProj.SnakesLadders;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogWriter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss");

    public static String writeFinalLog(Player user, Player swiftBot, Board board, Mode mode) throws IOException {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String fileName = "snakes_ladders_log_" + timestamp + ".txt";

        File file = new File(fileName);
        FileWriter writer = new FileWriter(file);

        writer.write("Snakes and Ladders Final Log\n");
        writer.write("===========================\n");
        writer.write("Timestamp: " + timestamp + "\n");
        writer.write("Mode: " + mode + "\n");
        writer.write("User final position: " + user.getPosition() + "\n");
        writer.write("SwiftBot final position: " + swiftBot.getPosition() + "\n");
        writer.write(board.snakesToString() + "\n");
        writer.write(board.laddersToString() + "\n");

        writer.close();

        return file.getAbsolutePath();
    }
}