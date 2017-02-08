
package com.tiscali.appmail.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Adv {

    @SerializedName("disable")
    @Expose
    private Disable disable;
    @SerializedName("timing")
    @Expose
    private Timing timing;

    public Disable getDisable() {
        return disable;
    }

    public void setDisable(Disable disable) {
        this.disable = disable;
    }

    public Timing getTiming() {
        return timing;
    }

    public void setTiming(Timing timing) {
        this.timing = timing;
    }

}
