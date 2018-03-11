package haitsu.groupup_test.other.Models;

import com.google.firebase.database.DataSnapshot;

/**
 * Created by moham on 11/03/2018.
 */

public class DataModel {

    private DataSnapshot userSnapshot;
    private String groupId;
    private String groupCategory;

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
}
