package org.hcmut.bkelbot.callbacks;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.hcmut.bkelbot.UserData;
import org.hcmut.bkelapi.MoodleAPI;
import org.hcmut.bkelapi.objects.MessageContent;
import org.hcmut.bkelbot.State;
import org.simonscode.telegrammenulibrary.Callback;
import org.simonscode.telegrammenulibrary.ParseMode;
import org.simonscode.telegrammenulibrary.VerticalMenu;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.hcmut.bkelbot.Utils.makeString;

public class MessageContentCallback implements Callback {
    private final Callback showmessagecallback;
    private MessageContent messNoti;
    public MessageContentCallback (Callback showmessagecallback, MessageContent messNoti){
        this.showmessagecallback = showmessagecallback;
        this.messNoti = messNoti;
    }

    @Override
    public void execute(AbsSender bot, CallbackQuery callbackQuery) {
        VerticalMenu menu = new VerticalMenu();
        menu.setParseMode(ParseMode.HTML);
        final UserData userData = State.instance.users.get(callbackQuery.getFrom().getId());
        StringBuilder sb = new StringBuilder();
        sb.append("<b>From user: " + messNoti.getUserfromfullname()+ "\n");

        //sb.append("Time: "+ zdt+"</b>\n");

        long epoch=(messNoti.getTimecreated())*1000;
        LocalDateTime date=Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDateTime();
        sb.append("DateTime:"+date+"\n");
        sb.append("Content:</b>" + makeString(messNoti.getText()));
        menu.setText(sb.toString());
        //đánh dấu tin nhắn, thông báo là đã đọc
        try {
            if (messNoti.getNotification() == 1)
                MoodleAPI.markNotificationAsRead(userData.getToken(),messNoti.getId());
            else
                MoodleAPI.markMessageAsRead(userData.getToken(), messNoti.getId());
        }
        catch (UnirestException e) {
            e.printStackTrace();
        }

        menu.addButton("Go back", showmessagecallback);

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
