
package com.fsck.k9.api.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Menu_ {

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("section_id")
    @Expose
    private String sectionId;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("sections")
    @Expose
    private List<Object> sections = null;
    @SerializedName("ico")
    @Expose
    private String ico;
    @SerializedName("visibility")
    @Expose
    private Boolean visibility;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Object> getSections() {
        return sections;
    }

    public void setSections(List<Object> sections) {
        this.sections = sections;
    }

    public String getIco() {
        return ico;
    }

    public void setIco(String ico) {
        this.ico = ico;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

}
