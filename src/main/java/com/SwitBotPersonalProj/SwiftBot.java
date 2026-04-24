package com.SwitBotPersonalProj;
import com.SwitBotPersonalProj.DrawAShape.DrawShapesFinal;
import com.SwitBotPersonalProj.NoughtsAndCrosses.Main;
import com.SwitBotPersonalProj.SpyBot.SpyBot;
import com.SwitBotPersonalProj.TrafficLight.TrafficLights;
import com.SwitBotPersonalProj.ZigZag.ZigzagOG;
import swiftbot.*;

import java.util.Scanner;

public class SwiftBot {

    SwiftBotAPI swiftBotAPI = SwiftBotAPI.INSTANCE;
    Core core = new Core();

    Scanner userInput = new Scanner(System.in);

    public void init()
    {
        displayMenu();
        getUserInput();
        processUserInput();
    }

    public void displayMenu()
    {
        core.ui.menu();
    }

    public void getUserInput()
    {
        try {
            core.selectedTask = userInput.nextInt();

            if(core.selectedTask < 1 || core.selectedTask > core.Tasks.length)
            {
                core.ui.throwErrorOnInvalidInput();
                core.selectedTask = -1;
                getUserInput();
            }
        } catch (Exception e) {
            core.ui.throwErrorOnInvalidInput();
            core.selectedTask = -1;
            getUserInput();
        }

        core.ui.displayTaskRunConfirmation();
    }

    public void processUserInput()
    {
        //calling the tasks
        if(core.selectedTask == 1)
        {
            DrawShapesFinal.main(new String[]{});
        }
        else if (core.selectedTask == 2)
        {
            Main.main(new String[]{}); //Main class from NoughtsAndCrosses class
        }
        else if (core.selectedTask == 3)
        {
            SpyBot.main(new String[]{}); //Main class from NoughtsAndCrosses class
        }
        else if (core.selectedTask == 4)
        {
            TrafficLights.main(new String[]{}); //Main class from NoughtsAndCrosses class
        }
        else if (core.selectedTask == 5)
        {
            ZigzagOG.main(new String[]{}); //Main class from NoughtsAndCrosses class
        }

        core.ui.displayTaskCompletion();
        System.exit(10);
    }

    public static void main(String[] args) {
        SwiftBot run = new SwiftBot();
        run.init();
    }
}
