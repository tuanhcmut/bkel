package org.hcmut.bkelbot.autoupdate.callbacks;

import org.hcmut.bkelbot.State;
import org.hcmut.bkelbot.autoupdate.Scheduler;
import org.simonscode.telegrammenulibrary.Callback;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class RemoveSchedulerCallback implements Callback {
    private final Callback schedulersCallback;
    private final Scheduler scheduler;

    public RemoveSchedulerCallback(Callback schedulersCallback, Scheduler scheduler) {
        this.schedulersCallback = schedulersCallback;
        this.scheduler = scheduler;
    }

    @Override
    public void execute(AbsSender bot, CallbackQuery callbackQuery) {
        State.instance.users.get(callbackQuery.getFrom().getId()).getSchedulers().remove(scheduler);
        schedulersCallback.execute(bot, callbackQuery);
    }
}
