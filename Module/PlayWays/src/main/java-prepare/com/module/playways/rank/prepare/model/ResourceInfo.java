package com.module.playways.rank.prepare.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResourceInfo implements Serializable {
    int resourceID = 0;//资源id
    int itemID = 0;//音乐条目id
    String audioURL = "";//声音资源URL
    String midiURL = ""; //打分资源URL

    public ResourceInfo(int resourceID, int itemID, String audioURL, String midiURL) {
        this.resourceID = resourceID;
        this.itemID = itemID;
        this.audioURL = audioURL;
        this.midiURL = midiURL;
    }

    public static ResourceInfo parse(com.zq.live.proto.Common.ResourceInfo resourceInfo){

        return new ResourceInfo(resourceInfo.resourceID, resourceInfo.itemID, resourceInfo.audioURL, resourceInfo.midiURL);
    }

    public static List<ResourceInfo> parse(List<com.zq.live.proto.Common.ResourceInfo> resourceInfoList){
        ArrayList<ResourceInfo> resourceInfos = new ArrayList<>(resourceInfoList.size());

        for (com.zq.live.proto.Common.ResourceInfo res :
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
        return "ResourceInfo{" +
                "resourceID=" + resourceID +
                ", itemID=" + itemID +
                ", audioURL='" + audioURL + '\'' +
                ", midiURL='" + midiURL + '\'' +
                '}';
    }
}
