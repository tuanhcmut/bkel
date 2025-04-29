package org.hcmut.bkelbot;

import org.hcmut.bkelapi.MoodleAPI;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main {
    public static void main(String[] args) throws TelegramApiRequestException {
        MoodleAPI.setMoodleAddresses(State.instance.moodleAddress, State.instance.moodleHost);
        ApiContextInitializer.init();
        TelegramBotsApi api = new TelegramBotsApi();
        api.registerBot(new Bot());
    }
}
