package com.SwitBotPersonalProj.SpyBot;

import java.awt.image.BufferedImage;

public class SpyBot_Authenticator {

    final int maxAttempts = 3;

    SpyBot_Core core;

   private char agentLocation;
   private String agentCallSign;
   private int attemptsTaken;

    public SpyBot_Authenticator(SpyBot_Core core)
    {
        this.core = core;
    }

    Boolean authenticateUser()
    {
        // Give the user 3 attempts to verify
        //NOTE: This loop is irrelevant if the system status is STANDBY as the system is locked into a loop asking for authentication indefinitely
       for(int i = 1; i<=maxAttempts; i++)
       {
           if(core.systemStatus.equalsIgnoreCase("STANDBY"))
           {
               core.ui.introScreen();
           }

           System.out.println("Attempt: " + i + "/3");

           if(tryAuthentication())
           {
               attemptsTaken = i;
               return true;
           }
           System.out.println("[ERROR]: Invalid QR code detected. Please try again.");
           System.out.println("Place the QR code and press ENTER.");
           //core.ui.introScreen();
       }
        if (core.systemStatus.equalsIgnoreCase("DELIVER"))
        {
            core.ui.authFailed("Invalid QR Code", attemptsTaken);
            core.logger.logOnFailedAuth();
        }
        return false;
    }

    private Boolean tryAuthentication()
    {
        BufferedImage qrImage;
        String qrData = "";
        int qrDataLength;

        core.userInput.nextLine(); // Waits for the user to press enter

        try{
            qrImage = core.swiftBotAPI.getQRImage();
            qrData = core.swiftBotAPI.decodeQRImage(qrImage);

        }
        catch (Exception e) {e.printStackTrace();}

         qrDataLength = qrData.length();

        if (qrDataLength == 0) {
            return false;
        }

        // Extract the location and the call sign

        agentLocation = qrData.charAt(qrDataLength - 1); // Get the last char in the string
        agentCallSign = extractCallSign(qrData);

        if(validCallSign() && validLocation() && validCallSignLocationCombination())
        {
            if(core.systemStatus.equalsIgnoreCase("STANDBY") && agentLocation == 'A') //Assuming A owns the bot
            {
                return true;
            }
            else if(core.systemStatus.equalsIgnoreCase("DELIVER"))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private String extractCallSign(String qrData)
    {
        String callSign = " ";

        if (qrData.charAt(qrData.length()-2) == ':')
        {
            for(int i = 0; i<qrData.length()-2; i++)
            {
                callSign += qrData.charAt(i);
            }
        }

        return callSign.trim().toUpperCase();
    }

    private boolean validCallSignLocationCombination()
    {
        for(int i = 0; i< core.validCallSigns.length; i++)
        {
            String qrConcat = agentCallSign.concat(Character.toString(agentLocation));
            String combination = core.validCallSigns[i].concat(Character.toString(core.validLocations[i]));

            if (combination.equals(qrConcat))
            {
                return true;
            }
        }

        return false;
    }

    private boolean validCallSign()
    {
        for(int i = 0; i< core.validCallSigns.length; i++)
        {
            if (core.validCallSigns[i].equalsIgnoreCase(agentCallSign))
            {
                return true;
            }

        }
        return false;
    }

    private boolean validLocation()
    {
        for(int i = 0; i< core.validLocations.length; i++)
        {
            if (core.validLocations[i] == agentLocation)
            {

                return true;
            }
        }

        return false;
    }

    //Getter methods
    public char getAgentLocation()
    {
        return agentLocation;
    }

    public String getAgentCallSign()
    {
        return agentCallSign;
    }

    public int getAuthAttempts()
    {
        return attemptsTaken;
    }

    public void resetAuthenticator()
    {
        agentLocation = 0;
        agentCallSign = "";
        attemptsTaken = 0;
    }
}
