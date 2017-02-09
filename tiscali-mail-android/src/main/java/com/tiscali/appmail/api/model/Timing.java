
package com.tiscali.appmail.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Timing {

    @SerializedName("mail")
    @Expose
    private Mail_ mail;

    public Mail_ getMail() {
        return mail;
    }

    public void setMail(Mail_ mail) {
        this.mail = mail;
    }

}
