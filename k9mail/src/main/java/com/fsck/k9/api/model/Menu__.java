
package com.fsck.k9.api.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Menu__ {

    @SerializedName("section_id")
    @Expose
    private String sectionId;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("fav")
    @Expose
    private Boolean fav;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("visibility")
    @Expose
    private Boolean visibility;
    @SerializedName("sections")
    @Expose
    private List<Object> sections = null;

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getFav() {
        return fav;
    }

    public void setFav(Boolean fav) {
        this.fav = fav;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

    public List<Object> getSections() {
        return sections;
    }

    public void setSections(List<Object> sections) {
        this.sections = sections;
    }

}
