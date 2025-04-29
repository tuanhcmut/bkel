package org.hcmut.bkelbot.callbacks;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.hcmut.bkelbot.UserData;
import org.hcmut.bkelapi.MoodleAPI;
import org.hcmut.bkelapi.objects.Course;
import org.hcmut.bkelapi.objects.CourseUpdate;
import org.hcmut.bkelbot.State;
import org.simonscode.telegrammenulibrary.Callback;
import org.simonscode.telegrammenulibrary.GotoCallback;
import org.simonscode.telegrammenulibrary.ParseMode;
import org.simonscode.telegrammenulibrary.SimpleMenu;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class GetCourseUpdateCallback implements Callback{
    private final Callback mainMenuCallback;

    public GetCourseUpdateCallback(Callback mainMenuCallback) {
        this.mainMenuCallback = mainMenuCallback;
    }

    @Override
    public void execute(AbsSender bot, CallbackQuery callbackQuery) {
        try {
            bot.execute(new SendChatAction().setChatId(callbackQuery.getMessage().getChatId()).setAction(ActionType.TYPING));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        SimpleMenu menu = new SimpleMenu();
        menu.setParseMode(ParseMode.HTML);
        final UserData userData = State.instance.users.get(callbackQuery.getFrom().getId());
        try {
            if (userData.getSchedulers().size() > 0) {
                Course[] courses = MoodleAPI.getCourses(userData.getToken(), userData.getUserInfo().getUserid());
                final StringBuilder sb = new StringBuilder();
                for (Course course : courses) {
                    //kiểm tra ở id khóa học, tên khóa học, tên rút gọn khóa học có chứa key đã đặt lệnh không
                    if (!(userData.checkContainsKey(course.getShortname())|userData.checkContainsKey(course.getFullname())|userData.hasSchedulerwithKey(Long.toString(course.getId()))))
                        continue;
                    CourseUpdate courseupdate =  MoodleAPI.getUpdateSince(userData.getToken(),course.getId(),course.getLastaccess());
                    if (courseupdate.getInstances().size()==0) continue;
                    sb.append("<b>"+course.getShortname()+"</b>: "+ courseupdate.getInstances().size()+" updates found \n") ;
                    menu.addButton(course.getShortname(), new UpdateDetailsCallback(new GotoCallback(menu), courseupdate,course));
                    menu.nextLine();
                }
                if (sb.length()==0) sb.append ("No new updates on the courses you have scheduled to check");
                menu.setText(sb.toString());
            } else {
                menu.setText("Request failed! Please set at least a Scheduler to check Course Updates");
            }
        } catch (UnirestException e) {
            e.printStackTrace();
            menu.setText("There was an error: " + e.getMessage());
        }
        menu.addButton("Go back", mainMenuCallback);
        try {
            bot.execute(menu.generateEditMessage(callbackQuery.getMessage()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
