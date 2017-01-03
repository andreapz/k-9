
package com.fsck.k9.api.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Video {

    @SerializedName("menu")
    @Expose
    private List<Menu_> menu = null;

    public List<Menu_> getMenu() {
        return menu;
    }

    public void setMenu(List<Menu_> menu) {
        this.menu = menu;
    }

}
