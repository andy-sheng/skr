package com.mi.live.data.query.model;

import com.mi.live.data.location.Location;
import com.mi.live.data.push.model.BarrageMsgExt;

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

    BarrageMsgExt.MicBeginInfo micInfo;
    BarrageMsgExt.PkStartInfo pkInfo;

    //contest冲顶大会新加字段
    boolean ableContest;//可以参加答题 true：可以
    int revivalNum;//复活卡数量
    boolean isLate;//是否迟到 true：是
    String contestId;//场次id

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

    public void setMicBeginInfo(BarrageMsgExt.MicBeginInfo micBeginInfo) {
        this.micInfo = micBeginInfo;
    }

    public BarrageMsgExt.MicBeginInfo getMicBeginInfo() {
        return micInfo;
    }

    public void setPkStartInfo(BarrageMsgExt.PkStartInfo pkStartInfo) {
        this.pkInfo = pkStartInfo;
    }

    public BarrageMsgExt.PkStartInfo getPkStartInfo() {
        return this.pkInfo;
    }

    public boolean isAbleContest() {
        return ableContest;
    }

    public void setAbleContest(boolean ableContest) {
        this.ableContest = ableContest;
    }

    public int getRevivalNum() {
        return revivalNum;
    }

    public void setRevivalNum(int revivalNum) {
        this.revivalNum = revivalNum;
    }

    public boolean isLate() {
        return isLate;
    }

    public void setLate(boolean late) {
        isLate = late;
    }

    public String getContestId() {
        return contestId;
    }

    public void setContestId(String contestId) {
        this.contestId = contestId;
    }
}
