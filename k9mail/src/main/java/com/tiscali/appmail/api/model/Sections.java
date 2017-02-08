
package com.tiscali.appmail.api.model;

import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sections {

    @SerializedName("fave")
    @Expose
    private Map<String, Boolean> faves;

    @SerializedName("visibility")
    @Expose
    private Map<String, Boolean> visibility = null;

    // public Fave getFave() {
    // return fave;
    // }
    //
    // public void setFave(Fave fave) {
    // this.fave = fave;
    // }


    public Map<String, Boolean> getFaves() {
        return faves;
    }

    public void setFaves(Map<String, Boolean> faves) {
        this.faves = faves;
    }

    public Map<String, Boolean> getVisibility() {
        return visibility;
    }

    public void setVisibility(Map<String, Boolean> visibility) {
        this.visibility = visibility;
    }

}
