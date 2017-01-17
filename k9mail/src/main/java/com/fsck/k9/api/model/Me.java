
package com.fsck.k9.api.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Me {

    @SerializedName("default_tab")
    @Expose
    private String defaultTab;
    @SerializedName("adv")
    @Expose
    private Adv adv;
    @SerializedName("mail")
    @Expose
    private List<Object> mail = null;
    @SerializedName("news")
    @Expose
    private News news;
    @SerializedName("video")
    @Expose
    private Video video;
    @SerializedName("offers")
    @Expose
    private Offers offers;
    @SerializedName("age")
    @Expose
    private Integer age;

    public String getDefaultTab() {
        return defaultTab;
    }

    public void setDefaultTab(String defaultTab) {
        this.defaultTab = defaultTab;
    }

    public Adv getAdv() {
        return adv;
    }

    public void setAdv(Adv adv) {
        this.adv = adv;
    }

    public List<Object> getMail() {
        return mail;
    }

    public void setMail(List<Object> mail) {
        this.mail = mail;
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public Offers getOffers() {
        return offers;
    }

    public void setOffers(Offers offers) {
        this.offers = offers;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

}
