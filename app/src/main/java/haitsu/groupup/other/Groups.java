package haitsu.groupup.other;

/**
 * Created by moham on 20/06/2017.
 */

public class Groups {

    private String category;
    private String name;
    private Boolean admin;

    public Groups(){

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
}
