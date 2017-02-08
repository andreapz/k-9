
package com.tiscali.appmail.api.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Section_ {

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("section_id")
    @Expose
    private String sectionId;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("fav")
    @Expose
    private Object fav;
    @SerializedName("visibility")
    @Expose
    private Object visibility;
    @SerializedName("sections")
    @Expose
    private List<Object> sections = null;

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

    public Object getFav() {
        return fav;
    }

    public void setFav(Object fav) {
        this.fav = fav;
    }

    public Object getVisibility() {
        return visibility;
    }

    public void setVisibility(Object visibility) {
        this.visibility = visibility;
    }

    public List<Object> getSections() {
        return sections;
    }

    public void setSections(List<Object> sections) {
        this.sections = sections;
    }

}
