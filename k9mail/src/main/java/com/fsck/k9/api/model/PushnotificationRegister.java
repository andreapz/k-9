
package com.fsck.k9.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PushnotificationRegister {

    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("hook")
    @Expose
    private Boolean hook;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getHook() {
        return hook;
    }

    public void setHook(Boolean hook) {
        this.hook = hook;
    }

}
