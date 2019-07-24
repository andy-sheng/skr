package com.module.playways.room.prepare.model;

import com.zq.live.proto.Common.ResourceInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResourceInfoModel implements Serializable {
    int resourceID = 0;//资源id
    int itemID = 0;//音乐条目id
    String audioURL = "";//声音资源URL
    String midiURL = ""; //打分资源URL

    public ResourceInfoModel(int resourceID, int itemID, String audioURL, String midiURL) {
        this.resourceID = resourceID;
        this.itemID = itemID;
        this.audioURL = audioURL;
        this.midiURL = midiURL;
    }

    public static ResourceInfoModel parse(ResourceInfo resourceInfo){

        return new ResourceInfoModel(resourceInfo.getResourceID(), resourceInfo.getItemID(), resourceInfo.getAudioURL(), resourceInfo.getMidiURL());
    }

    public static List<ResourceInfoModel> parse(List<ResourceInfo> resourceInfoList){
        ArrayList<ResourceInfoModel> resourceInfos = new ArrayList<>(resourceInfoList.size());

        for (ResourceInfo res :
                resourceInfoList) {
            resourceInfos.add(parse(res));
        }

        return resourceInfos;
    }

    public int getResourceID() {
        return resourceID;
    }

    public int getItemID() {
        return itemID;
    }

    public String getAudioURL() {
        return audioURL;
    }

    public String getMidiURL() {
        return midiURL;
    }

    @Override
    public String toString() {
        return "ResourceInfoModel{" +
                "resourceID=" + resourceID +
                ", itemID=" + itemID +
                ", audioURL='" + audioURL + '\'' +
                ", midiURL='" + midiURL + '\'' +
                '}';
    }
}
