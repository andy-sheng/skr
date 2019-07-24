package com.module.playways.doubleplay.pbLocalModel;

import com.component.live.proto.Common.AgoraTokenInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LocalAgoraTokenInfo implements Serializable {
    int userID; //用户id
    String token; //声网token

    private LocalAgoraTokenInfo() {

    }

    public LocalAgoraTokenInfo(int userID, String token) {
        this.userID = userID;
        this.token = token;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static List<LocalAgoraTokenInfo> toLocalAgoraTokenInfo(List<AgoraTokenInfo> agoraTokenInfo) {
        List<LocalAgoraTokenInfo> localAgoraTokenInfo = new ArrayList<>();
        if (agoraTokenInfo == null) {
            return localAgoraTokenInfo;
        }

        for (AgoraTokenInfo info : agoraTokenInfo) {
            localAgoraTokenInfo.add(toLocalAgoraTokenInfo(info));
        }

        return localAgoraTokenInfo;
    }

    public static LocalAgoraTokenInfo toLocalAgoraTokenInfo(AgoraTokenInfo agoraTokenInfo) {
        LocalAgoraTokenInfo localAgoraTokenInfo = new LocalAgoraTokenInfo();
        localAgoraTokenInfo.token = agoraTokenInfo.getToken();
        localAgoraTokenInfo.userID = agoraTokenInfo.getUserID();
        return localAgoraTokenInfo;
    }
}
