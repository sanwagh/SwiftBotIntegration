package com.SwitBotPersonalProj.SpyBot;

public class SpyBot {

    final SpyBot_Core core = new SpyBot_Core();

    void mainHandler()
    {
        core.authenticator.resetAuthenticator();
        Boolean userAuth = core.authenticator.authenticateUser();

        if(!userAuth){
            //Lock the system and send it back to the sender if authentication fails
            if (core.systemStatus.equalsIgnoreCase("DELIVER"))
            {
                core.ui.systemLock();
                core.navigation.moveBotToSender();

                System.exit(200);
            }
            mainHandler();
        }

        //Once the auth is passed, check system status
        // Get the message input if the status is standby
        if(core.systemStatus.equalsIgnoreCase("STANDBY"))
        {
            core.ui.mainMenu();
            core.messageHandler.inputMessageInMorse();

            core.systemStatus = "DELIVER";

            core.navigation.moveBotToDeliveryLocation();
            core.ui.messageDelivery();

            mainHandler();
        }
        else if(core.systemStatus.equalsIgnoreCase("DELIVER"))
        {
            //Check if the agent receiving is the one being authenticated
            if(core.deliveryLocation == core.authenticator.getAgentLocation())
            {
                core.ui.deliverInProgress();
                core.logger.logOnDelivery();

                core.messageHandler.displayMessageInMorse();
                core.systemStatus = "STANDBY";
                System.out.println("Waiting 10 seconds before returning.");
                core.wait(10000);

                core.ui.returnToSender();

                core.navigation.moveBotToSender();
            }
            else
            {
                System.out.println("[ERROR]: Delivery location and current agent location mismatch");
            }
            core.ui.messageDelivery();
            mainHandler();

        }
        else {
            System.out.println("[CRITICAL]: Illegal System status detected. Terminating. STATUS: "+ core.systemStatus);
            System.exit(100);
        }

    }

    public static void main(String[] args)
    {
        SpyBot run = new SpyBot();
        run.mainHandler();
    }
}
