
package com.tiscali.appmail.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Fave {

    @SerializedName("test_team")
    @Expose
    private Boolean faveKey;

    public Boolean getFaveKey() {
        return faveKey;
    }

    public void setFaveKey(Boolean faveKey) {
        this.faveKey = faveKey;
    }
}
