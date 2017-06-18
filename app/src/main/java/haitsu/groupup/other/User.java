package haitsu.groupup.other;

/**
 * Created by moham on 04/06/2017.
 */

public class User {

    private String id;
    private String username;
    private String email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email){
        this.username = username;
        this.email = email;
    }

    public User(String id, String username, String email){
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
