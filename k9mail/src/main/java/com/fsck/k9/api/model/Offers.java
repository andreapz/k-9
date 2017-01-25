
package com.fsck.k9.api.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Offers {

    @SerializedName("webview")
    @Expose
    private Webview webview;
    @SerializedName("menu")
    @Expose
    private List<TiscaliMenuItem> tiscaliMenuItem = null;

    public Webview getWebview() {
        return webview;
    }

    public void setWebview(Webview webview) {
        this.webview = webview;
    }

    public List<TiscaliMenuItem> getTiscaliMenuItem() {
        return tiscaliMenuItem;
    }

    public void setTiscaliMenuItem(List<TiscaliMenuItem> tiscaliMenuItem) {
        this.tiscaliMenuItem = tiscaliMenuItem;
    }

}
