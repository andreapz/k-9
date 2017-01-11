
package com.fsck.k9.api.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Onboarding {

    @SerializedName("last_modified")
    @Expose
    private String lastModified;
    @SerializedName("pages")
    @Expose
    private List<Page> pages = null;
    @SerializedName("options")
    @Expose
    private Options_ options;

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public Options_ getOptions() {
        return options;
    }

    public void setOptions(Options_ options) {
        this.options = options;
    }

}
