package com.SwitBotPersonalProj;
import com.SwitBotPersonalProj.Dance.TaskDance;
import com.SwitBotPersonalProj.DrawAShape.DrawShapesFinal;
import com.SwitBotPersonalProj.NoughtsAndCrosses.Main;
import com.SwitBotPersonalProj.SnakesLadders.SLMain;
import com.SwitBotPersonalProj.SpyBot.SpyBot;
import com.SwitBotPersonalProj.TrafficLight.TrafficLights;
import com.SwitBotPersonalProj.ZigZag.ZigzagOG;
import swiftbot.*;

import java.util.Scanner;

public class SwiftBot {

    Core core = new Core();
    Scanner userInput = new Scanner(System.in);

    public void init() throws Exception {
        displayMenu();
        getUserInput();
        core.ui.displayTaskRunConfirmation();

        processUserInput();
        core.ui.displayTaskCompletion();
        System.exit(10);
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
            userInput.next();
            return;
        }

    }

    public void processUserInput() throws Exception {
        if(core.selectedTask == 1)
        {
            TaskDance.main(new String[]{});
        }
       else if(core.selectedTask == 2)
        {
            DrawShapesFinal.main(new String[]{});
        }
        else if (core.selectedTask == 3)
        {
            Main.main(new String[]{}); //Main class from NoughtsAndCrosses class
        }
        else if (core.selectedTask == 4)
        {
            SLMain.main(new String[]{});
        }
        else if (core.selectedTask == 5)
        {
            SpyBot.main(new String[]{});
        }
        else if (core.selectedTask == 6)
        {
            TrafficLights.main(new String[]{});
        }
        else if (core.selectedTask == 7)
        {
            ZigzagOG.main(new String[]{});
        }
    }

    public static void main(String[] args) throws Exception {
        SwiftBot run = new SwiftBot();
        run.init();
    }
}
