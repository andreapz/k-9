
package com.tiscali.appmail.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Options_ {

    @SerializedName("connect_timeout")
    @Expose
    private Integer connectTimeout;
    @SerializedName("retry")
    @Expose
    private Integer retry;

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getRetry() {
        return retry;
    }

    public void setRetry(Integer retry) {
        this.retry = retry;
    }

}
