package haitsu.groupup.other.Models;

import java.util.Date;

/**
 * Created by moham on 28/07/2017.
 */

public class Notification {

    private String messageText;
    private long messageTime;

    public Notification(String messageText) {
        this.messageText = messageText;

        // Initialize to current time
        messageTime = new Date().getTime();
    }

    public Notification(){

    }


    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
