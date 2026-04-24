package com.SwitBotPersonalProj;
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

            if(core.selectedTask < 1 || core.selectedTask > core.Tasks.length+1)
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
    }

    public void processUserInput()
    {
        //calling the tasks
        if(core.selectedTask == 1)
        {
            
        }
    }
}
