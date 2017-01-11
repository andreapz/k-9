
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
    private List<Menu__> menu = null;

    public Webview getWebview() {
        return webview;
    }

    public void setWebview(Webview webview) {
        this.webview = webview;
    }

    public List<Menu__> getMenu() {
        return menu;
    }

    public void setMenu(List<Menu__> menu) {
        this.menu = menu;
    }

}
