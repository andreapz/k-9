
package com.tiscali.appmail.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Options {

    @SerializedName("allowskip")
    @Expose
    private Boolean allowskip;
    @SerializedName("otheroption")
    @Expose
    private Integer otheroption;

    public Boolean getAllowskip() {
        return allowskip;
    }

    public void setAllowskip(Boolean allowskip) {
        this.allowskip = allowskip;
    }

    public Integer getOtheroption() {
        return otheroption;
    }

    public void setOtheroption(Integer otheroption) {
        this.otheroption = otheroption;
    }

}
