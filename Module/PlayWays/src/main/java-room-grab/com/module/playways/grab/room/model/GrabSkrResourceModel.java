package com.module.playways.grab.room.model;

import com.zq.live.proto.Common.ResourceInfo;

import java.io.Serializable;

public class GrabSkrResourceModel implements Serializable {
    private String audioURL;
    private int itemID;
    private String midiURL;
    private int resourceID;
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

    public int getResourceID() {
        return resourceID;
    }

    public void setResourceID(int resourceID) {
        this.resourceID = resourceID;
    }

    public int getSysScore() {
        return sysScore;
    }

    public void setSysScore(int sysScore) {
        this.sysScore = sysScore;
    }

    @Override
    public String toString() {
        return "GrabSkrResourceModel{" +
                "audioURL='" + audioURL + '\'' +
                '}';
    }

    public static GrabSkrResourceModel parse(ResourceInfo resourceInfo){
        GrabSkrResourceModel grabSkrResourceModel = new GrabSkrResourceModel();
        grabSkrResourceModel.setAudioURL(resourceInfo.getAudioURL());
        grabSkrResourceModel.setItemID(resourceInfo.getItemID());
        grabSkrResourceModel.setMidiURL(resourceInfo.getMidiURL());
        grabSkrResourceModel.setResourceID(resourceInfo.getResourceID());
        grabSkrResourceModel.setSysScore(resourceInfo.getSysScore());
        return grabSkrResourceModel;
    }
}
