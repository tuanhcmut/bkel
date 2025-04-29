package org.hcmut.bkelbot.callbacks;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.hcmut.bkelbot.UserData;
import org.hcmut.bkelapi.MoodleAPI;
import org.hcmut.bkelapi.objects.*;
import org.hcmut.bkelapi.objects.CourseModule;
import org.hcmut.bkelbot.State;
import org.simonscode.telegrammenulibrary.*;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.hcmut.bkelbot.Utils.makeString;

public class UpdateDetailsCallback implements Callback {

    private Callback getupdateCallback;
    private CourseUpdate courseUpdate;
    private Course course;
    public UpdateDetailsCallback (Callback getupdateCallback,CourseUpdate courseUpdate,Course course) {
        this.getupdateCallback = getupdateCallback;
        this.courseUpdate = courseUpdate;
        this.course = course;
    }
    @Override
    public void execute(AbsSender bot, CallbackQuery callbackQuery) {
        try {
            bot.execute(new SendChatAction().setChatId(callbackQuery.getMessage().getChatId()).setAction(ActionType.TYPING));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        final UserData userData = State.instance.users.get(callbackQuery.getFrom().getId());
        VerticalMenu menu = new VerticalMenu();
        menu.setParseMode(ParseMode.HTML);
        StringBuilder sb = new StringBuilder();
        sb.append("<b>" + course.getFullname() + "</b>\n" +
                course.getEnrolledusercount() + " students enrolled.\n");
        try {
            final CourseContent[] courseDetails = MoodleAPI.getCourseDetails(userData.getToken(), course.getId());
            for (CourseContent courseDetail : courseDetails)
                for (CourseModule module : courseDetail.getModules())
                    for (Instance instance : courseUpdate.getInstances())
                        if (module.getId() == instance.getId()) {
                            generateUpdateDetailsCallbacks(callbackQuery, userData, menu, module);
                            sb.append("In " + instance.getContextlevel() + " <b>" + module.getModname() + " " + module.getName() + "</b> has " + instance.getUpdates().size() + " update:");
                            for (Update update : instance.getUpdates())
                                sb.append(" *"+ update.getName() + " update" );
                            sb.append("\n");
                        }
        }
        catch (UnirestException e) {
            e.printStackTrace();
        }


        menu.setText(sb.toString());
        menu.addButton("Go back", getupdateCallback);

        try {
            bot.execute(menu.generateEditMessage(callbackQuery.getMessage()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



    void generateUpdateDetailsCallbacks(CallbackQuery callbackQuery, UserData userData, VerticalMenu menu , CourseModule module) {
            if (!module.isUservisible())
                return;
            menu.addButton(module.getName(), new TextMenuCallback(this, makeString((module.getDescription()!=null?module.getDescription():""))+"\nView at: " + module.getUrl()));
    }

    }
