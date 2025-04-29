package org.hcmut.bkelbot.autoupdate.callbacks;

import org.hcmut.bkelbot.Bot;
import org.hcmut.bkelbot.State;
import org.hcmut.bkelbot.UserData;
import org.hcmut.bkelbot.autoupdate.SchedulerManager;
import org.simonscode.telegrammenulibrary.Callback;
import org.simonscode.telegrammenulibrary.ParseMode;
import org.simonscode.telegrammenulibrary.VerticalMenu;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SetSchedulerCallback implements Callback {
    private final Callback schedulerCallback;
    private  String key;
    public SetSchedulerCallback(Callback schedulerCallback, String key) {
        this.schedulerCallback = schedulerCallback;
        this.key = key;
    }

    @Override
    public void execute(AbsSender bot, CallbackQuery callbackQuery) {
        if (this.key=="key"){
            getKeyScheduler(bot, callbackQuery);
            return;
        }
        final UserData userData = State.instance.users.get(callbackQuery.getFrom().getId());
        VerticalMenu menu = new VerticalMenu();
        menu.setParseMode(ParseMode.HTML);
        if (!userData.hasSchedulerwithKey(this.key)) {
            menu.setText("Select all " + this.key + " courses. Please select the schedulers you'd like to add:");
            if (this.key.length() >= 6)
                menu.addButton("every 15 seconds", new ActuallySetSchedulerCallback(15, key));
            if (this.key.length() >= 4)
            menu.addButton("every 1 minute", new ActuallySetSchedulerCallback(1 * 60, key));
            menu.addButton("every 5 minute", new ActuallySetSchedulerCallback(5 * 60, key));
            menu.addButton("every 15 minutes", new ActuallySetSchedulerCallback(15 * 60, key));
            menu.addButton("every 30 minutes", new ActuallySetSchedulerCallback(30 * 60, key));
            menu.addButton("every 1 hour", new ActuallySetSchedulerCallback(3600, key));
            menu.addButton("every 3 hours", new ActuallySetSchedulerCallback(3 * 3600, key));
            menu.addButton("every 6 hours", new ActuallySetSchedulerCallback(6 * 3600, key));
            menu.addButton("every 12 hours", new ActuallySetSchedulerCallback(12 * 3600, key));
            menu.addButton("everyday", new ActuallySetSchedulerCallback(24 * 3600, key));
            menu.addButton("every 3 days", new ActuallySetSchedulerCallback(3 * 24 * 3600, key));
        }
        else  menu.setText("Already exists scheduler updating all " + this.key + " courses. Please delete it or try another key!");
        menu.addButton("Go back", schedulerCallback);
        try {
            bot.execute(menu.generateEditMessage(callbackQuery.getMessage()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void getKeyScheduler(AbsSender bot, CallbackQuery callbackQuery){
        Bot.receivemessage(callbackQuery, this);
        try {
            bot.execute(new EditMessageText()
                    .setMessageId(callbackQuery.getMessage().getMessageId())
                    .setChatId(callbackQuery.getMessage().getChatId())
                    .setText("Type identified keyword, e.g: course's short name, course's id, teacher's name, semester HK2XX...")
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public void setkeyScheduler(String key){
        this.key = key;
    }

    private class ActuallySetSchedulerCallback implements Callback {
        private final int delay;
        private final String key;

        private ActuallySetSchedulerCallback(int delay, String key) {
            this.delay = delay;
            this.key = key;
        }

        @Override
        public void execute(AbsSender bot, CallbackQuery callbackQuery) {
            SchedulerManager.setScheduler(callbackQuery.getFrom().getId(), key, delay,0);
            schedulerCallback.execute(bot,callbackQuery);
        }
    }
}
