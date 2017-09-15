package com.mi.live.data.query.model;

import com.mi.live.data.location.Location;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LivePKProto;

import java.util.List;

/**
 * Created by chengsimin on 16/9/8.
 */
public class EnterRoomInfo {
    int retCode;
    int viewerCount;
    List<ViewerModel> viewerModelList;
    boolean isManager = false;
    boolean banSpeak = false;
    Location location;
    int type;
    String shareUrl;

    String downStreamUrl;

    long enterTs;

    LiveCover liveCover;
    String liveTitle;
    int messageMode;
    MessageRule messageRule;
    boolean isShop;
    boolean hideGift;
    boolean supportMgicFace;

    LiveCommonProto.MicInfo micInfo;
    LivePKProto.NewPKInfo pkInfo;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public int getViewerCount() {
        return viewerCount;
    }

    public void setViewerCount(int viewerCount) {
        this.viewerCount = viewerCount;
    }

    public List<ViewerModel> getViewerModelList() {
        return viewerModelList;
    }

    public void setViewerModelList(List<ViewerModel> viewerModelList) {
        this.viewerModelList = viewerModelList;
    }

    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }

    public boolean isBanSpeak() {
        return banSpeak;
    }

    public void setBanSpeak(boolean banSpeak) {
        this.banSpeak = banSpeak;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getDownStreamUrl() {
        return downStreamUrl;
    }

    public void setDownStreamUrl(String downStreamUrl) {
        this.downStreamUrl = downStreamUrl;
    }

    public long getEnterTs() {
        return enterTs;
    }

    public void setEnterTs(long enterTs) {
        this.enterTs = enterTs;
    }

    public LiveCover getLiveCover() {
        return liveCover;
    }

    public void setLiveCover(LiveCover liveCover) {
        this.liveCover = liveCover;
    }

    public String getLiveTitle() {
        return liveTitle;
    }

    public void setLiveTitle(String liveTitle) {
        this.liveTitle = liveTitle;
    }

    public int getMessageMode() {
        return messageMode;
    }

    public void setMessageMode(int messageMode) {
        this.messageMode = messageMode;
    }

    public MessageRule getMessageRule() {
        return messageRule;
    }

    public void setMessageRule(MessageRule messageRule) {
        this.messageRule = messageRule;
    }

    public boolean isShop() {
        return isShop;
    }

    public void setShop(boolean shop) {
        isShop = shop;
    }

    public boolean isHideGift() {
        return hideGift;
    }

    public void setHideGift(boolean hideGift) {
        this.hideGift = hideGift;
    }

    public boolean isSupportMgicFace() {
        return supportMgicFace;
    }

    public void setSupportMgicFace(boolean supportMgicFace) {
        this.supportMgicFace = supportMgicFace;
    }

    public void setMicInfo(LiveCommonProto.MicInfo micInfo) {
        this.micInfo = micInfo;
    }

    public LiveCommonProto.MicInfo getMicInfo() {
        return micInfo;
    }

    public void setPkInfo(LivePKProto.NewPKInfo pkInfo) {
        this.pkInfo = pkInfo;
    }

    public LivePKProto.NewPKInfo getPkInfo() {
        return this.pkInfo;
    }
}
