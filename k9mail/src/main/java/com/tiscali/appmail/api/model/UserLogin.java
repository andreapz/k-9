
package com.tiscali.appmail.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserLogin {

    @SerializedName("me")
    @Expose
    private Me me;
    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("mail_auth_token")
    @Expose
    private String mailAuthToken;

    public Me getMe() {
        return me;
    }

    public void setMe(Me me) {
        this.me = me;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMailAuthToken() {
        return mailAuthToken;
    }

    public void setMailAuthToken(String mailAuthToken) {
        this.mailAuthToken = mailAuthToken;
    }

}
