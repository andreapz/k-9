
package com.tiscali.appmail.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LatestVersion {

    @SerializedName("issue")
    @Expose
    private String issue;
    @SerializedName("upgrade_mandatory")
    @Expose
    private Boolean upgradeMandatory;

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public Boolean getUpgradeMandatory() {
        return upgradeMandatory;
    }

    public void setUpgradeMandatory(Boolean upgradeMandatory) {
        this.upgradeMandatory = upgradeMandatory;
    }

}
