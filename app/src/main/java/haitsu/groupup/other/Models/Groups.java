package haitsu.groupup.other.Models;

/**
 * Created by moham on 20/06/2017.
 */

public class Groups implements Comparable<Groups> {

    private String groupId;
    private String category;
    private String type;
    private String name;
    private int memberCount;
    private int memberLimit;
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

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getMemberLimit() {
        return memberLimit;
    }

    public void setMemberLimit(int memberLimit) {
        this.memberLimit = memberLimit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    @Override
    public int compareTo(Groups that) {
        long lastMessage1 = this.getLastMessage().getMessageTime();
        long lastMessage2 = that.getLastMessage().getMessageTime();

        if (lastMessage1 > lastMessage2) {
            return 1;
        } else if (lastMessage1 < lastMessage2) {
            return -1;
        } else {
            return 0;
        }
    }
}


