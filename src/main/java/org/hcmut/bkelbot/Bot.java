package org.hcmut.bkelbot;

import org.glassfish.grizzly.utils.Pair;
import org.hcmut.bkelbot.autoupdate.AutoCheckMessageManager;
import org.hcmut.bkelbot.callbacks.GetCourseUpdateCallback;
import org.hcmut.bkelbot.callbacks.LogoutCallback;
import org.hcmut.bkelbot.callbacks.ShowMessagesCallback;
import org.hcmut.bkelapi.MoodleAPI;
import org.hcmut.bkelbot.autoupdate.SchedulerManager;
import org.hcmut.bkelbot.autoupdate.callbacks.SchedulerCallback;
import org.hcmut.bkelbot.autoupdate.callbacks.SetSchedulerCallback;
import org.simonscode.telegrammenulibrary.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class Bot extends TelegramLongPollingBot {

    private static SimpleMenu mainMenu = new SimpleMenu();
    private static GotoCallback mainMenuCallback = new GotoCallback(mainMenu);
    private static List<List<MenuButton>> loggedInMarkup = new LinkedList<>();
    private static List<List<MenuButton>> loggedOutMarkup = new LinkedList<>();
    private static HashMap<Long, Pair<CallbackQuery,SetSchedulerCallback>> keyneedreceiving = new HashMap<Long, Pair<CallbackQuery,SetSchedulerCallback>>();

    static {
        loggedInMarkup.add(Collections.singletonList(new CallbackButton("See Course Updates", new GetCourseUpdateCallback(mainMenuCallback))));
        loggedInMarkup.add(Collections.singletonList(new CallbackButton("Schedule Course Updates", new SchedulerCallback(mainMenuCallback))));
        loggedInMarkup.add(Collections.singletonList(new CallbackButton("See Unread Messages",new ShowMessagesCallback(mainMenuCallback))));
        loggedInMarkup.add(Collections.singletonList(new CallbackButton("Logout", new LogoutCallback())));
        loggedOutMarkup.add(Collections.singletonList(new CallbackButton("Login", new LoginCallback(mainMenuCallback))));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                State.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        UpdateHook.setCleanupSchedule(
                Duration.of(1, ChronoUnit.HOURS),
                Duration.of(12, ChronoUnit.HOURS));
    }

    public Bot() {
        try {
            State.load();
        } catch (IOException e) {
            e.printStackTrace();
        }


        SchedulerManager.restartSchedulers(this);
        AutoCheckMessageManager.restartSchedulers(this);
        try {
            State.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void receivemessage (CallbackQuery callbackQuery, SetSchedulerCallback callback){
         keyneedreceiving.put(callbackQuery.getMessage().getChatId(),new Pair<>(callbackQuery,callback));
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (UpdateHook.onUpdateReceived(this, update)) {
            return;
        }

        final Message message = update.getMessage();
        if (!update.hasMessage()) {
            return;
        }



        if (message.hasText()) {
            if (message.getText().startsWith("/start")) {
                if (!State.instance.users.containsKey(message.getFrom().getId())) {
                    State.instance.users.put(message.getFrom().getId(), new UserData());
                }

                final UserData userData = State.instance.users.get(message.getFrom().getId());
                if (userData != null && userData.getToken() != null && !userData.getToken().isBlank()) {
                    mainMenu.setParseMode(ParseMode.HTML);
                    mainMenu.setText("Logged in as <b>"+userData.getUserInfo().getFullname() + "</b>\n"+ "Student ID: <b>" +userData.getUserInfo().getUsername()+ "</b>");
                    mainMenu.setMarkup(loggedInMarkup);
                } else {
                    mainMenu.setText("Status: not logged in. Please /login");
                    mainMenu.setMarkup(loggedOutMarkup);
                }
                try {
                    this.execute(mainMenu.generateSendMessage(message.getChatId()));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (message.getText().startsWith("/login")) {
                String[] parts = message.getText().split(" ");
                if (parts.length != 2) {
                    reply(message, "Invalid usage\n" +
                            "Usage: /login <token>. You get token in your Bkel site (security keys service Moodle mobile web service)");
                } else {
                    try {
                        UserData userdata = new UserData();
                        userdata.setToken(parts[1]);
                        userdata.setUserInfo(MoodleAPI.getUserInfo(userdata.getToken()));
                        reply(message, (userdata.getToken() != null && !userdata.getToken().isEmpty()) ? "Login successful! Welcome " + userdata.getUserInfo().getFullname() + ". Use /start to open menu"
                                : "Login failed! Please check your token in site Bkel type: Moodle mobile web service");
                        State.instance.users.put(message.getFrom().getId(), userdata);
                    } catch (Exception e) {
                        reply(message, "Login failed!");
                        e.printStackTrace();
                    }
                }
            }
            else { //có thể tin nhắn vừa được gửi đang chứa một từ khóa dùng cho việc tìm kiếm khóa học
               // nếu đúng thì trong keyneedreceiving sẽ chứa chat id của người dùng, chứng tỏ có gọi lệnh tìm
                final Pair<CallbackQuery,SetSchedulerCallback> temp  = keyneedreceiving.remove(message.getChatId());
                if (temp != null) {
                    CallbackQuery callbackQuery = temp.getFirst();
                    SetSchedulerCallback setSchedulerCallback = temp.getSecond();
                    setSchedulerCallback.setkeyScheduler(message.getText());
                    setSchedulerCallback.execute(this,callbackQuery);
                }
            }
        }
    }

    private void reply(Message message, String text) {
        try {
            this.execute(new SendMessage()
                    .enableMarkdown(true)
                    .setChatId(message.getChatId())
                    .setText(text)
                    .setReplyToMessageId(message.getMessageId())
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "BkelBot";
    }

    @Override
    public String getBotToken() {
        return "1255998355:AAFelwZd5PLmWn4YNVEAhUKLmIo_B6b479k";
    }

    @Override
    public void onClosing() {
        super.onClosing();
        try {
            State.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
