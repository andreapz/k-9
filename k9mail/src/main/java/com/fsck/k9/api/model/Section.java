
package com.fsck.k9.api.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Section {

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
    private List<Section_> sections = null;

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

    public List<Section_> getSections() {
        return sections;
    }

    public void setSections(List<Section_> sections) {
        this.sections = sections;
    }

}
