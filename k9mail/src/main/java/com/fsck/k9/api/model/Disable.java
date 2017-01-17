
package com.fsck.k9.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Disable {

    @SerializedName("mail")
    @Expose
    private Mail mail;
    @SerializedName("news")
    @Expose
    private AdvNews advNews;

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }

    public AdvNews getAdvNews() {
        return advNews;
    }

    public void setAdvNews(AdvNews advNews) {
        this.advNews = advNews;
    }

}
