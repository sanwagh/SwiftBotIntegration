package com.SwitBotPersonalProj.SpyBot;

import swiftbot.*;

import java.util.*;

public class SpyBot_Core {
    //Handles all the shared resources between classes

    //API and UTILITIES
    SwiftBotAPI swiftBotAPI = SwiftBotAPI.INSTANCE;
    Scanner userInput = new Scanner(System.in);

    //Classes
    final SpyBot_Authenticator authenticator = new SpyBot_Authenticator(this);
    final SpyBot_MessageHandler messageHandler = new SpyBot_MessageHandler(this);
    final SpyBot_Navigation navigation = new SpyBot_Navigation(this);
    final SpyBot_CLI ui = new SpyBot_CLI(this);
    final SpyBot_Logger logger = new SpyBot_Logger(this);

    //Variables
    public String systemStatus = "STANDBY";
    public char deliveryLocation;
    public int priorityLevel = 1;

    //CONSTANTS
    final char botOwner = 'A';

    final String[] validCallSigns = {"ALPHA","ECHO","SEAL"};

    final char[] validLocations = {'A','B','C'};

    final int [] prioritySpeeds = {0,50,55,60,75};
    final int[][] morseLightsRGB = {
            {255,255,255}, // dot
            {0,0,255},     // dash
            {255,255,0},   // end of character
            {255,0,0},     // end of word
            {0,255,0}      // end of input
    };

    final List<String> fallbackMorseDictionary = new ArrayList<String>(Arrays.asList(
            "A .-",
            "B -...",
            "C -.-.",
            "D -..",
            "E .",
            "F ..-.",
            "G --.",
            "H ....",
            "I ..",
            "J .---",
            "K -.-",
            "L .-..",
            "M --",
            "N -.",
            "O ---",
            "P .--.",
            "Q --.-",
            "R .-.",
            "S ...",
            "T -",
            "U ..-",
            "V ...-",
            "W .--",
            "X -..-",
            "Y -.--",
            "Z --..",
            "0 -----")) ;


    //Shared methods

    public void wait(int time)
    {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
