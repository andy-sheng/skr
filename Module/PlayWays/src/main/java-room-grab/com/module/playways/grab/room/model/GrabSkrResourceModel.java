package com.module.playways.grab.room.model;

import java.io.Serializable;

public class GrabSkrResourceModel implements Serializable {
    private String audioURL;
    private int itemID;
    private String midiURL;
    private String resourceID;
    private int sysScore;

    public String getAudioURL() {
        return audioURL;
    }

    public void setAudioURL(String audioURL) {
        this.audioURL = audioURL;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getMidiURL() {
        return midiURL;
    }

    public void setMidiURL(String midiURL) {
        this.midiURL = midiURL;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public int getSysScore() {
        return sysScore;
    }

    public void setSysScore(int sysScore) {
        this.sysScore = sysScore;
    }
}
