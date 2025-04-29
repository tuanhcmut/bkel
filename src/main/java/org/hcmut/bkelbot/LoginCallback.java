package org.hcmut.bkelbot;

import org.hcmut.bkelbot.callbacks.SendMessageCallback;
import org.simonscode.telegrammenulibrary.Callback;
import org.simonscode.telegrammenulibrary.GotoCallback;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class LoginCallback implements Callback {
    public LoginCallback(GotoCallback mainMenu) {
    }

    @Override
    public void execute(AbsSender bot, CallbackQuery callbackQuery) {
        new SendMessageCallback("Please use the command /login <token> in private chat.").execute(bot, callbackQuery);
    }
}
