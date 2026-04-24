package com.SwitBotPersonalProj.SpyBot;

import swiftbot.Button;

import java.util.concurrent.atomic.AtomicBoolean;


public class SpyBot_Navigation {

    //values in cm
    final double distance = 50.0;
    final double maxSpeed = 27.5;

    private int speedPercent;

    SpyBot_Core core;

    public SpyBot_Navigation(SpyBot_Core core)
    {
        this.core = core;
    }

    public void moveBotToDeliveryLocation()
    {
        setSpeedBasedOnPriority();

        if(core.deliveryLocation == 'B')
        {
            core.swiftBotAPI.move(100,-100,447); //time calculated based on the rotation speed of the robot. Rotates by 120
        }
        else if (core.deliveryLocation == 'C')
        {
            core.swiftBotAPI.move(-100,100,447);

        }
        else
        {
            System.out.println("SOME ISSUE WITH LOCATION: " + core.deliveryLocation);
        }

        core.ui.swiftBotInTransit();
        core.wait(600);

        core.swiftBotAPI.move(speedPercent,speedPercent,calculateTime());
        flashLightsOnDelivery();
    }

    public void moveBotToSender()
    {
        setSpeedBasedOnPriority();
        core.swiftBotAPI.move(100,-100,670); // Rotates by 180
        core.swiftBotAPI.move(speedPercent,speedPercent,calculateTime());
    }

    private void setSpeedBasedOnPriority()
    {
        speedPercent = (core.prioritySpeeds[core.priorityLevel]);
    }

    private int calculateTime()
    {
        double time =  (distance/(maxSpeed*(speedPercent/100.0)));

        return (int) time*1000;
    }

    private void flashLightsOnDelivery()
    {
        AtomicBoolean pressed = new AtomicBoolean(false);

        core.ui.acceptDelivery();

        core.swiftBotAPI.enableButton(Button.X, () ->{
            pressed.set(true);
        });

        while(!pressed.get())
        {
            core.wait(750);
            core.swiftBotAPI.fillUnderlights(new int[]{255, 0, 0});

            core.wait(500);
            core.swiftBotAPI.disableUnderlights();
        }

        core.swiftBotAPI.disableAllButtons();
    }
}
