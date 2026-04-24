package com.SwitBotPersonalProj;

public class UIHandler {

    Core core;

    public UIHandler(Core core)
    {
        this.core = core;
    }

    public void menu()
    {
        printDoubleLine();
        System.out.println("          SWIFTBOT - MAIN MENU");
        printDoubleLine();

        System.out.println("Select a task:");
        printDashLine();

        for(int i = 0; i<core.Tasks.length; i++)
        {
            System.out.println(i+1 + ". " + core.Tasks[i]);
        }
        printDashLine();

        System.out.println("Enter a number to select the task and press ENTER: ");
    }

    public void displayTaskRunConfirmation()
    {
        printDashLine();
        System.out.println("Running Task: " + core.Tasks[core.selectedTask-1]);
    }

    public void displayTaskCompletion()
    {
        printDashLine();
        System.out.println("Finished Running Task: " + core.Tasks[core.selectedTask-1]);
    }

    public void throwErrorOnInvalidInput()
    {
        System.out.println("[ERROR]: Invalid number entered. Please enter a number corresponding to the task you want to run.");
    }

    public void clearTerminalScreen()
    {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }


    //HELPER METHODS

    private void printDoubleLine()
    {
        System.out.println();
        System.out.println("=====================================================\n");
    }

    private void printDashLine()
    {
        System.out.println();
        System.out.println("------------------------------------------------------\n");
    }
}
