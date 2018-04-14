package haitsu.groupup.other.Models;

import com.google.firebase.database.DataSnapshot;

/**
 * Created by moham on 11/03/2018.
 */

public class DataModel {

    private DataSnapshot userSnapshot;
    private String groupId;
    private String groupCategory;;
    private String type;
    private String groupAdminId;

    public DataModel() {

    }

    public DataSnapshot getUserSnapshot() {
        return userSnapshot;
    }

    public void setUserSnapshot(DataSnapshot userSnapshot) {
        this.userSnapshot = userSnapshot;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupCategory() {
        return groupCategory;
    }

    public void setGroupCategory(String groupCategory) {
        this.groupCategory = groupCategory;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroupAdminId() {
        return groupAdminId;
    }

    public void setGroupAdminId(String groupAdminId) {
        this.groupAdminId = groupAdminId;
    }
}
