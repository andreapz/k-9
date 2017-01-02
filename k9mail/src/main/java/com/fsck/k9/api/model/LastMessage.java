
package com.fsck.k9.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LastMessage {

    @SerializedName("last_modified")
    @Expose
    private String lastModified;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("onboarding")
    @Expose
    private Object onboarding;

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Object getOnboarding() {
        return onboarding;
    }

    public void setOnboarding(Object onboarding) {
        this.onboarding = onboarding;
    }

}
