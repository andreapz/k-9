
package com.tiscali.appmail.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Endpoints {

    @SerializedName("account_authorize")
    @Expose
    private AccountAuthorize accountAuthorize;
    @SerializedName("account_register")
    @Expose
    private AccountRegister accountRegister;
    @SerializedName("account_user_login")
    @Expose
    private AccountUserLogin accountUserLogin;
    @SerializedName("pushnotification_register")
    @Expose
    private PushnotificationRegister pushnotificationRegister;
    @SerializedName("pushnotification_activate")
    @Expose
    private PushnotificationActivate pushnotificationActivate;
    @SerializedName("user_me")
    @Expose
    private UserMe userMe;
    @SerializedName("section_visibility")
    @Expose
    private SectionVisibility sectionVisibility;
    @SerializedName("section_fave")
    @Expose
    private SectionFave sectionFave;
    @SerializedName("action_track")
    @Expose
    private ActionTrack actionTrack;
    @SerializedName("info_about")
    @Expose
    private InfoAbout infoAbout;

    public AccountAuthorize getAccountAuthorize() {
        return accountAuthorize;
    }

    public void setAccountAuthorize(AccountAuthorize accountAuthorize) {
        this.accountAuthorize = accountAuthorize;
    }

    public AccountRegister getAccountRegister() {
        return accountRegister;
    }

    public void setAccountRegister(AccountRegister accountRegister) {
        this.accountRegister = accountRegister;
    }

    public AccountUserLogin getAccountUserLogin() {
        return accountUserLogin;
    }

    public void setAccountUserLogin(AccountUserLogin accountUserLogin) {
        this.accountUserLogin = accountUserLogin;
    }

    public PushnotificationRegister getPushnotificationRegister() {
        return pushnotificationRegister;
    }

    public void setPushnotificationRegister(PushnotificationRegister pushnotificationRegister) {
        this.pushnotificationRegister = pushnotificationRegister;
    }

    public PushnotificationActivate getPushnotificationActivate() {
        return pushnotificationActivate;
    }

    public void setPushnotificationActivate(PushnotificationActivate pushnotificationActivate) {
        this.pushnotificationActivate = pushnotificationActivate;
    }

    public UserMe getUserMe() {
        return userMe;
    }

    public void setUserMe(UserMe userMe) {
        this.userMe = userMe;
    }

    public SectionVisibility getSectionVisibility() {
        return sectionVisibility;
    }

    public void setSectionVisibility(SectionVisibility sectionVisibility) {
        this.sectionVisibility = sectionVisibility;
    }

    public SectionFave getSectionFave() {
        return sectionFave;
    }

    public void setSectionFave(SectionFave sectionFave) {
        this.sectionFave = sectionFave;
    }

    public ActionTrack getActionTrack() {
        return actionTrack;
    }

    public void setActionTrack(ActionTrack actionTrack) {
        this.actionTrack = actionTrack;
    }

    public InfoAbout getInfoAbout() {
        return infoAbout;
    }

    public void setInfoAbout(InfoAbout infoAbout) {
        this.infoAbout = infoAbout;
    }

}
