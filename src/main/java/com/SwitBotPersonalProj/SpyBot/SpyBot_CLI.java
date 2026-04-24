package com.SwitBotPersonalProj.SpyBot;

public class SpyBot_CLI {

    SpyBot_Core core;

    public SpyBot_CLI(SpyBot_Core core)
    {
        this.core = core;
    }

    public void introScreen()
    {
        printDoubleLine();
        System.out.println("        SWIFTBOT SECURE COMMUNICATION SYSTEM");
        printDoubleLine();
        System.out.println("Status: " + core.systemStatus);
        printDashLine();

        System.out.println("Please scan your QR code to continue");
        System.out.println();
        System.out.println("Format Required:");
        System.out.println("<CALLSIGN>:<LOCATION>");
        printDashLine();

        System.out.println("Place QR code in front of the camera and press ENTER");
        System.out.println("Waiting for user input...");
    }

    public void authFailed(String failReason, int currentAttempt)
    {
        printDoubleLine();
        System.out.println("            USER AUTHENTICATION FAILED");
        printDoubleLine();

        System.out.println("ERROR:   Invalid QR Code");
        System.out.println("REASON:  " + failReason);
        System.out.println(String.format("ATTEMPTS LEFT: %1$d/%2$d", currentAttempt, core.authenticator.maxAttempts));
        printDashLine();

    }

    public void systemLock()
    {
        printDoubleLine();
        System.out.println("            SYSTEM LOCKED");
        printDoubleLine();

        System.out.println("ERROR: Multiple failed attempts");
        System.out.println("Updated Log");
        printDashLine();
        System.out.println("Returning to sender");
        //printDashLine();
    }

    public void mainMenu()
    {
        printDoubleLine();
        System.out.println("            MAIN MENU");
        printDoubleLine();

        System.out.println("Authenticated As  : " + core.authenticator.getAgentCallSign());
        System.out.println("Location          : " + core.authenticator.getAgentLocation());
        System.out.println("Status            : " + core.systemStatus);
        //printDashLine();
        System.out.println();

    }

    public void sendMessage()
    {
        printDoubleLine();
        System.out.println("            SEND MESSAGE");
        printDoubleLine();

        System.out.println("Morse Input Guide :");
        System.out.println("Dot (.)           : Button X");
        System.out.println("Dash (-)          : Button Y");
        System.out.println("End Of Character  : Button A");
        System.out.println("End Of Word       : Button B");
        System.out.println("End Of Message    : Morse Digit 0");
        printDashLine();
        System.out.println("Recording Morse input...");
    }

    public void prioritySelection()
    {
        printDoubleLine();
        System.out.println("            MESSAGE PRIORITY SELECTION");
        printDoubleLine();
        System.out.println("Select priority level for this message:");

        System.out.println("1. Low Priority");
        System.out.println("   - Speed: 50%");
        System.out.println("   - Used for: Routine communications");

        System.out.println("2. Medium Priority");
        System.out.println("   - Speed: 55%");
        System.out.println("   - Used for: Regular communications");

        System.out.println("3. High Priority");
        System.out.println("   - Speed: 60%");
        System.out.println("   - Used for: Time-Sensitive communications");

        System.out.println("4. Critical Priority");
        System.out.println("   - Speed: 75%");
        System.out.println("   - Used for: Emergency communications");

        printDashLine();
        System.out.println("Enter Priority level (1-4): ");
    }

    public void messageRecorded()
    {
        printDashLine();
        System.out.println("Message successfully recorded");
        System.out.println("Priority Level");
        System.out.println("Travel Speed");
        System.out.println("Destination: " + core.deliveryLocation);
        printDashLine();
        System.out.println("Preparing to Travel...");
    }

    public void swiftBotInTransit()
    {
        printDoubleLine();
        System.out.println("            SWIFTBOT IN TRANSIT");
        printDoubleLine();

        System.out.println("Current Location: A");
        System.out.println("Destination     : "+ core.deliveryLocation);
        System.out.println("Priority Level  : "+ core.priorityLevel);
        printDashLine();

        System.out.println("Status: Moving to Destination");
    }

    public void acceptDelivery()
    {
        System.out.println("[INFO]: New Message. Press X on the bot to proceed");

    }

    public void messageDelivery()
    {
        printDoubleLine();
        System.out.println("            MESSAGE DELIVERY");
        printDoubleLine();

        System.out.println("SwiftBot has arrived at the location");
        printDashLine();
        System.out.println("Please scan your QR code to authenticate...");
        System.out.println("Place the QR code in front of the camera and press ENTER\n");
    }

    public void deliverInProgress()
    {
        printDashLine();
        System.out.println("Success");
        System.out.println("Delivering Message");
        printDashLine();
        System.out.println("Morse Output Guide:");
        System.out.println("Dot (.)           : White");
        System.out.println("Dash (-)          : Blue");
        System.out.println("End Of Character  : Yellow");
        System.out.println("End Of Word       : Red");
        System.out.println("End Of Message    : Green");
        printDashLine();
    }

    public void returnToSender()
    {
        printDashLine();
        System.out.println("Success");
        System.out.println("Returning to sender...");
        printDashLine();
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
