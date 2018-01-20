package haitsu.groupup.other.Models;

import java.util.Date;

/**
 * Created by moham on 15/07/2017.
 */

public class ChatMessage {

    private String groupName;
    private String messageText;
    private String messageUser;
    private long messageTime;
    private String imageUrl;

    public ChatMessage(String groupName, String messageText, String messageUser, String imageUrl) {
        this.groupName = groupName;
        this.messageText = messageText;
        this.messageUser = messageUser;

        // Initialize to current time
        messageTime = new Date().getTime();
        this.imageUrl = imageUrl;
    }

    public ChatMessage() {

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
