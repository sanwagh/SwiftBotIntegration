package com.SwitBotPersonalProj.NoughtsAndCrosses;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameLogger {

    private String filePath;
    private BufferedWriter writer;

    public GameLogger() {
        try {
            // use the current date and time to give the log file a unique name
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String timestamp = LocalDateTime.now().format(formatter);
            filePath = "NoughtsAndCrosses_Log_" + timestamp + ".txt";

            writer = new BufferedWriter(new FileWriter(filePath, true));

        } catch (IOException e) {
            System.out.println("Error creating log file: " + e.getMessage());
        }
    }

    public void log(String message) {
        if (writer == null) {
            System.out.println("Logger not initialised.");
            return;
        }
        

        try {
            writer.write(message);
            writer.newLine();
            writer.flush(); // flush after every write so nothing gets lost if the program crashes
        } catch (IOException e) {
            System.out.println("Error writing to log file: " + e.getMessage());
        }
        
    }

    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing log file: " + e.getMessage());
        }
        
    }

    public String getFilePath() {
        return filePath;
    }
}