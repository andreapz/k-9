
package com.fsck.k9.api.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class News_ {

    @SerializedName("refresh-timeout")
    @Expose
    private Integer refreshTimeout;
    @SerializedName("menu")
    @Expose
    private List<Menu> menu = null;

    public Integer getRefreshTimeout() {
        return refreshTimeout;
    }

    public void setRefreshTimeout(Integer refreshTimeout) {
        this.refreshTimeout = refreshTimeout;
    }

    public List<Menu> getMenu() {
        return menu;
    }

    public void setMenu(List<Menu> menu) {
        this.menu = menu;
    }

}
