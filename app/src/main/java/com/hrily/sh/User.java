package com.hrily.sh;

/**
 * Created by hrishi on 6/6/16.
 */
public class User {
    private String name;
    private String email;
    private String pass;
    private String friend_email;

    public User(){}

    public User(String name, String email, String pass, String friend_email) {
        this.name = name;
        this.email = email;
        this.pass = pass;
        this.friend_email = friend_email;
    }

    public String getFriend_email() {
        return friend_email;
    }

    public void setFriend_email(String friend_email) {
        this.friend_email = friend_email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
