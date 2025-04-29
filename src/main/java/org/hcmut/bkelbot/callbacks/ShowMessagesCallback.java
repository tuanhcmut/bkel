package org.hcmut.bkelbot.callbacks;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.hcmut.bkelbot.State;
import org.hcmut.bkelbot.UserData;
import org.hcmut.bkelbot.Utils;
import org.hcmut.bkelapi.MoodleAPI;
import org.hcmut.bkelapi.objects.MessageContent;
import org.hcmut.bkelapi.objects.MessagesList;
import org.simonscode.telegrammenulibrary.Callback;
import org.simonscode.telegrammenulibrary.ParseMode;
import org.simonscode.telegrammenulibrary.VerticalMenu;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ShowMessagesCallback implements Callback {
    private final Callback mainMenuCallback;
    private MessagesList conversation;
    public ShowMessagesCallback(Callback mainMenuCallback, MessagesList conversation) {
        this.mainMenuCallback = mainMenuCallback;
        this.conversation = conversation;
    }
    public ShowMessagesCallback(Callback mainMenuCallback) {
        this.mainMenuCallback = mainMenuCallback;
        this.conversation = null;
    }
    public void execute(AbsSender bot, CallbackQuery callbackQuery) {
        try {
            bot.execute(new SendChatAction().setChatId(callbackQuery.getMessage().getChatId()).setAction(ActionType.TYPING));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        final UserData userData = State.instance.users.get(callbackQuery.getFrom().getId());
        VerticalMenu menu = new VerticalMenu();
        menu.setParseMode(ParseMode.HTML);
        //nếu conversation chưa khởi tạo, nghĩa là không phải đang hiển thị các tin nhắn bên trong conversation, mà là hiển thị thông báo và tên các cuộc trò chuyện
        if (conversation == null) {
            //hiển thị nội dung của các thông báo và hiển thị tên của cuộc trò chuyện chứa tin nhắn mới
            try {
                //cập nhật số lượng thông báo và cuộc trò chuyện chưa đọc
                int unreadnotificationcount = MoodleAPI.getUnreadNotificationCount(userData.getToken(), (int) userData.getUserInfo().getUserid());
                int unreadconversationcount = MoodleAPI.getUnreadMessagecCount(userData.getToken(), (int) userData.getUserInfo().getUserid());
                userData.setNewinboxcount(unreadconversationcount);
                userData.setNewnotificationcount(unreadnotificationcount);
                menu.setText("<b>You have " + unreadconversationcount + " unread conversations and " + unreadnotificationcount + " unread notifications </b>");
                //count để đảm bảo sẽ không hiển thị quá nhiều button vì nếu không sẽ gây ra lỗi
                int count = 0;
                MessagesList unread_notifications = MoodleAPI.getMessages(userData.getToken(), (int) userData.getUserInfo().getUserid(), 0, "notifications", 0);
                //hiển thị các thông báo chưa đọc
                if (unread_notifications != null) {
                    for (MessageContent notification : unread_notifications.getMessages()) {
                        menu.addButton("Notification:" + Utils.makeString(notification.getText()), new MessageContentCallback(this, notification));
                        menu.nextLine();
                        if (count++ == 40) break;
                    }
                }
                //phân loại các tin nhắn (chat) chưa đọc đến từ cùng một người gửi vào một cuộc trò chuyện của người đó
                MessagesList unread_chats = MoodleAPI.getMessages(userData.getToken(), (int) userData.getUserInfo().getUserid(), 0, "conversations", 0);
                HashMap<Integer, MessagesList> conversations = new HashMap<Integer, MessagesList>();
                if (unread_chats != null) {
                    for (MessageContent messNoti : unread_chats.getMessages()) {
                            if (conversations.containsKey(messNoti.getUseridfrom()))
                                conversations.get(messNoti.getUseridfrom()).getMessages().add(messNoti);
                            else {
                                MessagesList inboxlist = new MessagesList();
                                List<MessageContent> temp = new LinkedList<MessageContent>();
                                temp.add(messNoti);
                                inboxlist.setMessages(temp);
                                conversations.put(messNoti.getUseridfrom(), inboxlist);
                            }
                    }
                }
                //chỉ hiển thị tên các cuộc trò chuyện đó và số lượng tin nhắn (chat) chưa đọc của cuộc trò chuyện đó
                if (!conversations.isEmpty()) {
                    for (Map.Entry mapElement : conversations.entrySet()) {
                        MessagesList conversation = (MessagesList) mapElement.getValue();
                        String userfrom_name = conversation.getMessages().get(0).getUserfromfullname();
                        //khi nhấn vào cuộc trò chuyện sẽ hiển thị các tin nhắn (chat) chưa đọc bên trong
                        menu.addButton(conversation.getMessages().size() + " new inbox from " + userfrom_name, new ShowMessagesCallback(this,conversation));
                        if (count++ == 41) break;
                    }
                }
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        }
        else{
            //hiển thị các tin nhắn chưa đọc chứa bên trong cuộc trò chụyện đã được truyền vào biến conversation
            int count = 0;
            menu.setText("<b> Total unread chat: " + conversation.getMessages().size()+ "\nSorted Newest to Oldest. </b>");
            for (MessageContent message : conversation.getMessages()) {
                    menu.addButton(Utils.makeString(message.getText()), new MessageContentCallback(mainMenuCallback, message));
                menu.nextLine();
                if (count++ == 40) break;
            }
        }

        menu.addButton("Go back", mainMenuCallback);
        try {
            bot.execute(menu.generateEditMessage(callbackQuery.getMessage()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
