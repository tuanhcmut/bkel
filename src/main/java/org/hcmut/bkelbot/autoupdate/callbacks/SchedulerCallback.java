package org.hcmut.bkelbot.autoupdate.callbacks;
import org.hcmut.bkelbot.State;
import org.hcmut.bkelbot.UserData;
import org.hcmut.bkelbot.autoupdate.Scheduler;
import org.simonscode.telegrammenulibrary.Callback;
import org.simonscode.telegrammenulibrary.VerticalMenu;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SchedulerCallback implements Callback {
    private final Callback mainMenuCallback;
    private String key;
    public SchedulerCallback(Callback mainMenuCallback) {
        this.mainMenuCallback = mainMenuCallback;
    }

    @Override
    public void execute(AbsSender bot, CallbackQuery callbackQuery) {
        VerticalMenu menu = new VerticalMenu();
        final UserData userData = State.instance.users.get(callbackQuery.getFrom().getId());
        menu.setText("You have set up " + userData.getSchedulers().size() + " course updates scheduler.\n\nTap a scheduler to remove it.");
        menu.addButton("Add scheduler updating keyword-courses",new SetSchedulerCallback(this,"key"));
        menu.addButton("Add scheduler updating video's courses",new SetSchedulerCallback(this,"Video"));
        menu.addButton("Add scheduler updating all courses",new SetSchedulerCallback(this,""));

        for (Scheduler scheduler : userData.getSchedulers())
        menu.addButton( "[ON] Check all "+ scheduler.getKey() + " courses every " + toHHMMSS(scheduler.getDelay()) ,  new RemoveSchedulerCallback(this, scheduler));

        menu.addButton("Go back", mainMenuCallback);

        try {
            bot.execute(menu.generateEditMessage(callbackQuery.getMessage()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private String toHHMMSS (int s){
        int sec = s % 60;
        int min = (s / 60)%60;
        int hours = (s/60)/60;

        String strSec=  Integer.toString(sec);
        String strmin=  Integer.toString(min);
        String strHours=  Integer.toString(hours);

        return ((strHours.equals("0")?"":strHours + "h " )
                + (strmin.equals("0")?"":strmin + "m ")
                + (strSec.equals("0")?"": strSec + "s"));
    }

}
