package com.hotel.models;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String password;
    private String mobileNumber;
    private String email;

    public User(String username, String password, String mobileNumber, String email) {
        this.username = username;
        this.password = password;
        this.mobileNumber = mobileNumber;
        this.email = email;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getMobileNumber() { return mobileNumber; }
    public String getEmail() { return email; }
}