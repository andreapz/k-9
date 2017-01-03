
package com.fsck.k9.api.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Menu {

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
    @SerializedName("customizable")
    @Expose
    private Boolean customizable;
    @SerializedName("sections")
    @Expose
    private List<Section> sections = null;
    @SerializedName("ico")
    @Expose
    private String ico;
    @SerializedName("js")
    @Expose
    private String js;

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

    public Boolean getCustomizable() {
        return customizable;
    }

    public void setCustomizable(Boolean customizable) {
        this.customizable = customizable;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public String getIco() {
        return ico;
    }

    public void setIco(String ico) {
        this.ico = ico;
    }

    public String getJs() {
        return js;
    }

    public void setJs(String js) {
        this.js = js;
    }

}
