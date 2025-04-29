package org.hcmut.bkelbot.autoupdate;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.hcmut.bkelbot.UserData;
import org.hcmut.bkelapi.MoodleAPI;
import org.hcmut.bkelbot.Bot;
import org.hcmut.bkelbot.State;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class AutoCheckMessageManager {
    private static final Timer timer = new Timer();
    private static Bot bot;

    public static void restartSchedulers(Bot bot) {
        AutoCheckMessageManager.bot = bot;
        for (Map.Entry mapElement : State.instance.users.entrySet()) {
            int id = (int) mapElement.getKey();
            UserData user = (UserData) mapElement.getValue();
            user.setNewnotificationcount(0);
            user.setNewinboxcount(0);
            enableAutoScheduler(id);
        }
    }

    public static void enableAutoScheduler(int userid) {
        timer.schedule(new CheckerMessageTask(userid),5000);
    }

    private static class CheckerMessageTask extends TimerTask {
        private final int userId ;

        public CheckerMessageTask(int id){
            this.userId = id;
        }

        @Override
        public void run() {
            if (!State.instance.users.get(userId).getToken().isBlank())
            try {
                final UserData userData = State.instance.users.get(userId);
                int beforeunreadnotificationcount = userData.getNewnotificationcount();
                int beforeunreadconversationcount = userData.getNewinboxcount();
                int unreadnotificationcount = MoodleAPI.getUnreadNotificationCount(userData.getToken(), (int) userData.getUserInfo().getUserid());
                int unreadconversationcount = MoodleAPI.getUnreadMessagecCount(userData.getToken(), (int) userData.getUserInfo().getUserid());
                userData.setNewinboxcount(unreadconversationcount);
                userData.setNewnotificationcount(unreadnotificationcount);
                StringBuilder sb = new StringBuilder();
                if (beforeunreadconversationcount < unreadconversationcount) {
                    sb.append("Received <b>");
                    sb.append(unreadconversationcount - beforeunreadconversationcount);
                    sb.append(" new incoming inbox.</b> Total unread: <b>");
                    sb.append(unreadconversationcount);
                    sb.append("</b>\n");
                }
                if (beforeunreadnotificationcount < unreadnotificationcount){
                    sb.append("Received <b>");
                    sb.append(unreadnotificationcount - beforeunreadnotificationcount );
                    sb.append(" new incoming notification.</b> Total unread: <b>");
                    sb.append(unreadnotificationcount);
                    sb.append("</b>\n");
                }
                try{
                if (sb.length()>0){
                        bot.execute(new SendChatAction().setChatId(String.valueOf(userId)).setAction(ActionType.TYPING));
                        sb.append("Go check it <b>in /start menu -> See Unread Messages </b>");
                        bot.execute(new SendMessage()
                                .setParseMode("HTML")
                                .setText(sb.toString())
                                .setChatId(String.valueOf(userId))
                                .enableNotification());
                    }
                } catch (TelegramApiException e) {
                e.printStackTrace();
                }
            }
            catch (UnirestException e){
                e.printStackTrace();
            }
            enableAutoScheduler(this.userId);
        }
    }
}
