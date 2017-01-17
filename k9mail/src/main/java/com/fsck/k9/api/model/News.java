
package com.fsck.k9.api.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class News {

    @SerializedName("refresh-timeout")
    @Expose
    private Integer refreshTimeout;
    @SerializedName("menu")
    @Expose
    private List<TiscaliMenuItem> tiscaliMenuItem = null;

    public Integer getRefreshTimeout() {
        return refreshTimeout;
    }

    public void setRefreshTimeout(Integer refreshTimeout) {
        this.refreshTimeout = refreshTimeout;
    }

    public List<TiscaliMenuItem> getTiscaliMenuItem() {
        return tiscaliMenuItem;
    }

    public void setTiscaliMenuItem(List<TiscaliMenuItem> tiscaliMenuItem) {
        this.tiscaliMenuItem = tiscaliMenuItem;
    }

}
