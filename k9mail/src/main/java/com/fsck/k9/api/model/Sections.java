
package com.fsck.k9.api.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sections {

    @SerializedName("fave")
    @Expose
    private List<Fave> faves;

    @SerializedName("visibility")
    @Expose
    private List<Object> visibility = null;

//    public Fave getFave() {
//        return fave;
//    }
//
//    public void setFave(Fave fave) {
//        this.fave = fave;
//    }


    public List<Fave> getFaves() {
        return faves;
    }

    public void setFaves(List<Fave> faves) {
        this.faves = faves;
    }

    public List<Object> getVisibility() {
        return visibility;
    }

    public void setVisibility(List<Object> visibility) {
        this.visibility = visibility;
    }

}
