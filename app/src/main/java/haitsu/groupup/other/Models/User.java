package haitsu.groupup.other.Models;

/**
 * Created by moham on 04/06/2017.
 */

public class User {

    private String username;
    private String gender;
    private String email;
    private String age;
    private String city;
    private String country;
    private Groups group;
    private double latitude;
    private double longitude;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String gender, String email, String age, String city, String country, Groups group, double latitude, double longitude) {
        this.username = username;
        this.gender = gender;
        this.email = email;
        this.age = age;
        this.city = city;
        this.country = country;
        this.group = group;
        this.latitude = latitude;
        this.longitude = longitude;
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


    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Groups getGroup() {
        return group;
    }

    public void setGroup(Groups group) {
        this.group = group;
    }
}
