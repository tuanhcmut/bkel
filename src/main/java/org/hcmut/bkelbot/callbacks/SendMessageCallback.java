package org.hcmut.bkelbot.callbacks;

import org.simonscode.telegrammenulibrary.Callback;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SendMessageCallback implements Callback {
    private final String text;

    public SendMessageCallback(String text) {
        this.text = text;
    }

    @Override
    public void execute(AbsSender bot, CallbackQuery callbackQuery) {
        try {
            bot.execute(new SendMessage()
                    .setChatId(callbackQuery.getMessage().getChatId())
                    .setText(text)
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
