
package com.fsck.k9.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Mail {

    @SerializedName("interstitial")
    @Expose
    private Boolean interstitial;
    @SerializedName("native")
    @Expose
    private Boolean _native;

    public Boolean getInterstitial() {
        return interstitial;
    }

    public void setInterstitial(Boolean interstitial) {
        this.interstitial = interstitial;
    }

    public Boolean getNative() {
        return _native;
    }

    public void setNative(Boolean _native) {
        this._native = _native;
    }

}
