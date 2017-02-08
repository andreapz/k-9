
package com.tiscali.appmail.api.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// http://www.jsonschema2pojo.org/
public class Config {

    @SerializedName("age")
    @Expose
    private Integer age;
    @SerializedName("latest_version")
    @Expose
    private LatestVersion latestVersion;
    @SerializedName("last_message")
    @Expose
    private LastMessage lastMessage;
    @SerializedName("walled_garden")
    @Expose
    private List<String> walledGarden = null;
    @SerializedName("onboarding")
    @Expose
    private Onboarding onboarding;
    @SerializedName("registration_web")
    @Expose
    private String registrationWeb;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LatestVersion getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(LatestVersion latestVersion) {
        this.latestVersion = latestVersion;
    }

    public LastMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(LastMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public List<String> getWalledGarden() {
        return walledGarden;
    }

    public void setWalledGarden(List<String> walledGarden) {
        this.walledGarden = walledGarden;
    }

    public Onboarding getOnboarding() {
        return onboarding;
    }

    public void setOnboarding(Onboarding onboarding) {
        this.onboarding = onboarding;
    }

    public String getRegistrationWeb() {
        return registrationWeb;
    }

    public void setRegistrationWeb(String registrationWeb) {
        this.registrationWeb = registrationWeb;
    }

}
