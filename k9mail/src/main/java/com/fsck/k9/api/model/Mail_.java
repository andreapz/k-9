
package com.fsck.k9.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Mail_ {

    @SerializedName("delay")
    @Expose
    private Integer delay;
    @SerializedName("showtime")
    @Expose
    private Integer showtime;
    @SerializedName("interval")
    @Expose
    private Integer interval;

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Integer getShowtime() {
        return showtime;
    }

    public void setShowtime(Integer showtime) {
        this.showtime = showtime;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

}
