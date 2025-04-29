package org.hcmut.bkelbot.autoupdate;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.hcmut.bkelbot.Bot;
import org.hcmut.bkelbot.State;
import org.hcmut.bkelbot.UserData;
import org.hcmut.bkelapi.MoodleAPI;
import org.hcmut.bkelapi.objects.Course;
import org.hcmut.bkelapi.objects.CourseUpdate;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.util.*;

public class  SchedulerManager {
    private static final Timer timer = new Timer();
    private static Bot bot;

    public static void restartSchedulers(Bot bot) {
        SchedulerManager.bot = bot;
        for (UserData user : State.instance.users.values()) {
            if (!user.getSchedulers().isEmpty())
                for (Scheduler scheduler : user.getSchedulers())
                    enableScheduler(scheduler);

        }
    }

    public static void setScheduler(int userId, String key, int delay, long lastchecked) {
        final Scheduler scheduler = new Scheduler(userId , key, delay, lastchecked);
        State.instance.users.get(userId).getSchedulers().add(scheduler);
        enableScheduler(scheduler);
    }

    public static void enableScheduler(Scheduler scheduler) {
        //timer.schedule(new SchedulerTask(scheduler), new Date(Instant.ofEpochSecond(scheduler.getEpochSecond()).toEpochMilli()));
        timer.schedule(new SchedulerTask(scheduler), scheduler.getDelay()*1000);
    }

    private static class SchedulerTask extends TimerTask {
        private final Scheduler scheduler;

        public SchedulerTask(Scheduler scheduler) {
            this.scheduler = scheduler;
        }

        @Override
        public void run() {
            final StringBuilder sb = new StringBuilder();
            try {
                final UserData userData = State.instance.users.get(scheduler.getUserId());
                if (!userData.getSchedulers().contains(scheduler)) return;
                try {
                    Course[] courses = MoodleAPI.getCourses(userData.getToken(), userData.getUserInfo().getUserid());
                    if (courses != null) {
                        for (Course course : courses) {
                            if (!(course.getShortname().contains(scheduler.getKey())|course.getFullname().contains(scheduler.getKey())|scheduler.getKey().equals(Long.toString(course.getId()))))
                                continue;
                            CourseUpdate courseupdate =  MoodleAPI.getUpdateSince(userData.getToken(),course.getId(),(scheduler.getLastchecked()>0)?scheduler.getLastchecked():course.getLastaccess());
                            if (courseupdate.getInstances().size()==0) continue;
                            sb.append("<b>"+course.getShortname()+": "+ courseupdate.getInstances().size()+" updates found </b> \n") ;
                        }
                        if (sb.length()>0) sb.append("Go check it <b>in /start menu -> See Course Updates</b>");
                    } else {
                        sb.append("Request failed!");
                    }
                } catch (UnirestException e) {
                    e.printStackTrace();
                    sb.append("There was an error: " + e.getMessage());
                }

                bot.execute(new SendChatAction().setChatId(String.valueOf(scheduler.getUserId())).setAction(ActionType.TYPING));
                if (sb.length()>0)
                bot.execute(new SendMessage()
                        .setParseMode("HTML")
                        .setText(sb.toString())
                        .setChatId(String.valueOf(scheduler.getUserId()))
                        .enableNotification());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
           if (State.instance.users.get(scheduler.getUserId()).getSchedulers().contains(scheduler)) {
               setScheduler(scheduler.getUserId(), scheduler.getKey(), scheduler.getDelay(), Instant.now().getEpochSecond());
               State.instance.users.get(scheduler.getUserId()).getSchedulers().remove(scheduler);
           }
        }
    }
}
