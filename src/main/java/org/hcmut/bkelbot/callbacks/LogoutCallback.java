package org.hcmut.bkelbot.callbacks;

import org.hcmut.bkelbot.State;
import org.simonscode.telegrammenulibrary.Callback;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.IOException;

public class LogoutCallback implements Callback {
    @Override
    public void execute(AbsSender bot, CallbackQuery callbackQuery) {
        State.instance.users.remove(callbackQuery.getFrom().getId());
        try {
            State.save();
            new SendMessageCallback("Logout success!").execute(bot, callbackQuery);
        } catch (IOException e) {
            e.printStackTrace();
            new SendMessageCallback("Logout error:\n" + e.getMessage()).execute(bot, callbackQuery);
        }
    }
}
