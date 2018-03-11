package haitsu.groupup_test.other.Models;

import java.util.Date;

/**
 * Created by moham on 04/06/2017.
 */

public class UserRequest {
    private String userId;
    private String groupId;
    private String username;
    private String gender;
    private String groupname;
    private String groupCategory;
    private String age;
    private String city;
    private String country;
    private long timeOfRequest;
    private double latitude;
    private double longitude;

    public UserRequest() {
        timeOfRequest = new Date().getTime();
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserRequest(String username, String gender, String groupname, String age, String city, String country, double latitude, double longitude) {
        this.username = username;
        this.gender = gender;
        this.groupname = groupname;
        this.age = age;
        this.city = city;
        this.country = country;
        // Initialize to current time
        timeOfRequest = new Date().getTime();
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGroupName() {
        return groupname;
    }

    public void setGroupName(String groupname) {
        this.groupname = groupname;
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

    public long getTimeOfRequest() {
        return timeOfRequest;
    }

    public void setTimeOfRequest(long timeOfRequest) {
        this.timeOfRequest = timeOfRequest;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
