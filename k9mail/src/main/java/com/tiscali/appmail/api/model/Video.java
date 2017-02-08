
package com.tiscali.appmail.api.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Video {

    @SerializedName("menu")
    @Expose
    private List<TiscaliMenuItem> tiscaliMenuItem = null;

    public List<TiscaliMenuItem> getTiscaliMenuItem() {
        return tiscaliMenuItem;
    }

    public void setTiscaliMenuItem(List<TiscaliMenuItem> tiscaliMenuItem) {
        this.tiscaliMenuItem = tiscaliMenuItem;
    }
}
