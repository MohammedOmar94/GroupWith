package haitsu.groupup.other.Models;

/**
 * Created by moham on 20/06/2017.
 */

public class Groups {

    private String category;
    private String name;
    private Boolean admin;
    private Boolean userApproved;
    private ChatMessage lastMessage;

    public Groups() {

    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ChatMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(ChatMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Boolean getUserApproved() {
        return userApproved;
    }

    public void setUserApproved(Boolean userApproved) {
        this.userApproved = userApproved;
    }
}
