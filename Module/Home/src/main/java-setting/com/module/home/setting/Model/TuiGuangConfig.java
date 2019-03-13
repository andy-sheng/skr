package com.module.home.setting.Model;

import java.io.Serializable;

public class TuiGuangConfig implements Serializable {
    /**
     * Url : string
     * configID : 0
     * configName : string
     * configStatus : UnknownStatus
     */

    private String Url;
    private int configID;
    private String configName;
    private String configStatus;

    public String getUrl() {
        return Url;
    }

    public void setUrl(String Url) {
        this.Url = Url;
    }

    public int getConfigID() {
        return configID;
    }

    public void setConfigID(int configID) {
        this.configID = configID;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigStatus() {
        return configStatus;
    }

    public void setConfigStatus(String configStatus) {
        this.configStatus = configStatus;
    }

    @Override
    public String toString() {
        return "TuiGuangConfig{" +
                "Url='" + Url + '\'' +
                ", configID=" + configID +
                ", configName='" + configName + '\'' +
                ", configStatus='" + configStatus + '\'' +
                '}';
    }
}
