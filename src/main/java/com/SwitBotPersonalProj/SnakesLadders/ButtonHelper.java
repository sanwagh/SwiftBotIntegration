package com.SwitBotPersonalProj.SnakesLadders;

import java.util.concurrent.atomic.AtomicReference;

import swiftbot.Button;
import swiftbot.SwiftBotAPI;

public class ButtonHelper {

    public static Button waitForChoice(SwiftBotAPI bot, Button... buttons) throws InterruptedException {
        final AtomicReference<Button> pressed = new AtomicReference<Button>(null);

        for (final Button button : buttons) {
            bot.enableButton(button, () -> {
                pressed.compareAndSet(null, button);
            });
        }

        while (pressed.get() == null) {
            Thread.sleep(100);
        }

        for (Button button : buttons) {
            bot.disableButton(button);
        }

        return pressed.get();
    }

    public static void waitForExactButton(SwiftBotAPI bot, Button button, String prompt) throws InterruptedException {
        System.out.println(prompt);
        Button pressed = waitForChoice(bot, button);
        if (pressed != button) {
            throw new IllegalStateException("Unexpected button press.");
        }
    }
}