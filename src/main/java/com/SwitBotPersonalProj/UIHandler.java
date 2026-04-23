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
        System.out.println("SWIFTBOT - MAIN MENU");
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

    public void throwErrorOnInvalidInput()
    {

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
