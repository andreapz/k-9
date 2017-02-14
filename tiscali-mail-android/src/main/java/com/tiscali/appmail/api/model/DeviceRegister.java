
package com.tiscali.appmail.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceRegister {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("device")
    @Expose
    private Device device;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

}
