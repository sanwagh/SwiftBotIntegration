package com.SwitBotPersonalProj.SpyBot;

import com.SwitBotPersonalProj.SpyBot.SpyBot_Core;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class SpyBot_Logger {
    SpyBot_Core core;

    private int currentMsgNumber = 0;

    public SpyBot_Logger(SpyBot_Core core)
    {
        this.core = core;
    }

    public void logMessage()
    {
        currentMsgNumber++;

        ArrayList<String> log = new ArrayList<>();

        log.add("[SEND MESSAGE]");
        log.add(String.format("[SENDER]: %1$s:%2$s", core.authenticator.getAgentCallSign(), core.authenticator.getAgentLocation()));
        log.add("[MSG-ID]: MSG-"+currentMsgNumber);
        log.add("[MESSAGE-TIME] " + LocalDateTime.now());
        log.add("[VERIFICATION ATTEMPTS]: " + core.authenticator.getAuthAttempts());
        log.add("[PRIORITY]: " + core.priorityLevel);
        log.add("[MESSAGE]: " + core.messageHandler.getMessage());
        log.add(" ");

        flushToFile(log);
    }

    public void logOnDelivery()
    {
        ArrayList<String> log = new ArrayList<>();

        log.add("[DELIVERED MESSAGE]");
        log.add(String.format("[RECEIVER]: %1$s:%2$s", core.authenticator.getAgentCallSign(), core.authenticator.getAgentLocation()));
        log.add("[MESSAGE-TIME]: " + LocalDateTime.now());
        log.add("[MSG-ID]: MSG-"+currentMsgNumber);
        log.add(" ");

        flushToFile(log);
    }

    public void logOnFailedAuth()
    {
        ArrayList<String> log = new ArrayList<>();

        log.add("[DELIVERY FAILED]");
        log.add("[MSG-ID]: MSG-"+currentMsgNumber);
        log.add("[REASON]: Authentication failed after 3 attempts");
        log.add("[ACTION]: Return to sender");
        log.add("[DETECTION_TIME]: " + LocalDateTime.now());

        flushToFile(log);
    }


    private void flushToFile(ArrayList<String> log) {
        Path logFile = Paths.get("SpyBotLogs.txt");
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter("SpyBotLogs.txt",true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        if(Files.notExists(logFile))
        {
            createLogFile(logFile);
        }

        for(int i = 0; i<log.size(); i++)
        {
            try
            {
                writer.write(log.get(i));
                writer.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createLogFile(Path logFile)
    {
        try {
            Files.createFile(logFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
