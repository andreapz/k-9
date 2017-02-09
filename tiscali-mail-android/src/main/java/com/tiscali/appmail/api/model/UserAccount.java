package com.tiscali.appmail.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by andreaputzu on 03/01/17.
 */

public class UserAccount {
    @SerializedName("username")
    private String username;
    @SerializedName("password")
    private String password;

    public UserAccount(String username, String password) {
        this.password = password;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String account) {
        this.username = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
