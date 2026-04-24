package com.SwitBotPersonalProj.SpyBot;

import swiftbot.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class SpyBot_MessageHandler
{
    SpyBot_Core core;

    List<String> morseDictionary;
    private ArrayList<Character> morseInput =  new ArrayList<Character>();
    private ArrayList<Character> locationMorse = new ArrayList<>();

    private char senderLocation;
    private String message = " ";

    public SpyBot_MessageHandler(SpyBot_Core core)
    {
        this.core = core;
    }

    public void inputMessageInMorse()
    {
        core.ui.sendMessage();

        morseInput.clear();
        boolean endOfInput = false;

        AtomicReference<Boolean> pressed = new AtomicReference<>(false);

        // Loop until the end of input signal (0) is detected then stop accepting input and process message
        while(!endOfInput)
        {
            core.swiftBotAPI.enableButton(Button.X,() ->{
                morseInput.add('.');
                System.out.print(".");
                pressed.set(true);
            });

            core.swiftBotAPI.enableButton(Button.Y,() ->{
                morseInput.add('-');
                System.out.print("-");
                pressed.set(true);
            });

            core.swiftBotAPI.enableButton(Button.A,() ->{
                morseInput.add('2');
                System.out.print("/");
                pressed.set(true);
            });

            core.swiftBotAPI.enableButton(Button.B,() ->{
                morseInput.add('3');
                System.out.print(" ");
                pressed.set(true);
            });

            while(pressed.get() == false)
            {

            }
            pressed.set(false);
            endOfInput = isEndOfInput();
            System.out.println();
            core.swiftBotAPI.disableAllButtons();
        }
        endOfInput = false;
        core.swiftBotAPI.disableAllButtons();

        core.ui.prioritySelection();
        getPriorityLevel();

        processMessage();
    }

    public void displayMessageInMorse()
    {
        removeLocation();
        trimInputEnd(); //Remove the 0 from the end of the message input
        locationMorse = getSenderLocationMorse(); //Display sender location while delivering message
        morseInput.addLast('4');

        displayMorse(locationMorse);
        displayMorse(morseInput);
        resetMessageHandler();
    }

    private void getPriorityLevel()
    {
        while (true)
        {
            try {
                core.priorityLevel = core.userInput.nextInt();
                core.userInput.nextLine();

                if (core.priorityLevel > 4 ||core.priorityLevel < 1)
                {
                    System.out.println("--> [ERROR]: Enter an integer from 1-4");
                }
                else
                {
                    break;
                }
            } catch (Exception e) {
                System.out.println("--> [ERROR]: Enter an integer");
                core.userInput.next();
            }
        }
    }

    private void processMessage()
    {
      convertToPlainText();
      core.deliveryLocation = extractDeliveryLocation();

      if(core.deliveryLocation == 0 ||!validLocation()) {
          System.out.println("--> [ERROR]: Invalid delivery location or message");
          resetMessageHandler();
          inputMessageInMorse();
          return;
      }

        senderLocation = core.botOwner;

        core.ui.messageRecorded();
        core.logger.logMessage();
    }

    // Checks for end of input by checking if the last 5 chars are '-'
    private Boolean isEndOfInput()
    {
        int morseInputEnd = morseInput.size();

        if (morseInputEnd < 5) return false;

        return morseInput.get(morseInputEnd - 1) == '-'
                && morseInput.get(morseInputEnd - 2) == '-'
                && morseInput.get(morseInputEnd - 3) == '-'
                && morseInput.get(morseInputEnd - 4) == '-'
                && morseInput.get(morseInputEnd - 5) == '-';
    }

    private void convertToPlainText()
    {
        ArrayList<Character> charArray = new ArrayList<>(); //Hold the morse for the current character
        morseDictionary = loadMorseDictionary();

        if(morseInput.size() == 0 || morseInput.size() == 4)
        {
            message = " ";
            return;
        }

        for(int i = 0; i<morseInput.size()-5; i++)
        {
            if(morseInput.get(i) == '2')
            {
                message += convertCharMorseToChar(charArray);
                charArray.clear();
                continue;
            }
            else if (morseInput.get(i) == '3')
            {
                message += convertCharMorseToChar(charArray)+ " ";
                charArray.clear();
                continue;
            }
            charArray.add(morseInput.get(i));
        }

        message = message.trim();
    }

    // Compare the character morse and all the morse codes from the dictionary
    // Converts both into a string and equates them
    private char convertCharMorseToChar(ArrayList<Character> charArray)
    {
        char charProcessed = 0;

        for(int i = 0; i< morseDictionary.size(); i++)
        {
            String line = morseDictionary.get(i);
            String morseLine = "";
            String charLine = "";

            // 0 and 1 are ignored are 0 is the character and 1 is a space
            for(int j = 2; j<line.length(); j++)
            {
                morseLine += line.charAt(j);
            }

            for(int j = 0; j<charArray.size(); j++)
            {
                charLine += charArray.get(j);
            }

            if(morseLine.equals(charLine))
            {
                charProcessed = line.charAt(0);
                break;
            }
        }
        System.out.println(charProcessed);
        return charProcessed;
    }

    private List<String> loadMorseDictionary()
    {
        List<String> fileLines;

        try
        {
            Path path = Paths.get("MorseCodeDictionary.txt");
            fileLines = Files.readAllLines(path);

        }
        catch (IOException e)
        {
            System.out.println("[WARNING]: Error occurred while trying to access the main Morse Code Dictionary.");
            System.out.println("[WARNING]: Loading Fallback Dictionary...");

            fileLines = core.fallbackMorseDictionary;
        }

        return fileLines;

    }

    private char extractDeliveryLocation()
    {
        if (message.isBlank() || message.length() <= 1)
        {
            return 0;
        }
        if (message.charAt(1) == ' ')
        {
            return message.charAt(0);
        }

        return 0;
    }

    private boolean validLocation()
    {
        for(int i = 0; i<core.validLocations.length; i++)
        {
            if (core.deliveryLocation == core.validLocations[i])
            {
                if (core.deliveryLocation == 'A') {return false;}
                return true;
            }
        }

        return false;
    }

    private void removeLocation()
    {
       while (morseInput.getFirst() != '3')
       {
           morseInput.removeFirst();
       }
        morseInput.removeFirst();
    }

    private void trimInputEnd()
    {
        while (morseInput.getLast() != '3')
        {
            morseInput.removeLast();
        }
    }

    private ArrayList<Character> getSenderLocationMorse()
    {
        ArrayList<Character> morse = new ArrayList<>();

        for(int i = 0; i<morseDictionary.size(); i++)
        {
            String line = morseDictionary.get(i);

            if (line.charAt(0) == senderLocation)
            {
                for (int j = 2; j<line.length(); j++)
                {
                    morse.add(line.charAt(j));
                }
            }
        }
        morse.add('3');
        return morse;
    }

    private void displayMorse(ArrayList<Character> morseValues)
    {
        int cooldown = 500;
        System.out.println();

        for(int i = 0; i<morseValues.size(); i++)
        {
            if(morseValues.get(i) == '.')
            {
                core.swiftBotAPI.fillUnderlights(core.morseLightsRGB[0]);
                core.wait(cooldown);

                core.swiftBotAPI.disableUnderlights();

            }
            else if (morseValues.get(i) == '-')
            {
                core.swiftBotAPI.fillUnderlights(core.morseLightsRGB[1]);
                core.wait(cooldown);

                core.swiftBotAPI.disableUnderlights();

            }
            else if (morseValues.get(i) == '2')
            {

                core.swiftBotAPI.fillUnderlights(core.morseLightsRGB[2]);
                core.wait(cooldown);

                core.swiftBotAPI.disableUnderlights();

            }
            else if (morseValues.get(i) == '3')
            {
                core.swiftBotAPI.fillUnderlights(core.morseLightsRGB[3]);
                core.wait(cooldown);

                core.swiftBotAPI.disableUnderlights();

            }
            else if (morseValues.get(i) == '4')
            {
                core.swiftBotAPI.fillUnderlights(core.morseLightsRGB[4]);

                core.wait(cooldown);

                core.swiftBotAPI.disableUnderlights();
            }
        }
    }

    // Getter
    public String getMessage()
    {
        return message;
    }

    public void resetMessageHandler()
    {
        message = "";
        core.deliveryLocation = ' ';
        morseInput.clear();
    }
}
