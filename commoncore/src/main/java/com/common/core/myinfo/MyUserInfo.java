package com.common.core.myinfo;

import com.common.core.userinfo.UserInfo;
import com.wali.live.proto.User.GetOwnInfoRsp;
import com.wali.live.proto.User.PersonalData;
import com.wali.live.proto.User.PersonalInfo;
import com.wali.live.proto.User.UserEcoAttr;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.IOException;

import okio.ByteString;


public class MyUserInfo {
    private UserInfo mUserInfo;

    private Boolean certificationChanged; // 认证改变

    private String viewUrl;                                       //直播地址
    private String roomId = null;                                 //正在直播的id, 房间号
    private String tvRoomId = null;                               //正在播放的电视台房间id
    private Integer roomType;

    private Boolean online;  // 是否在线，这个只有在添加管理员的时候使用了。
    private Integer appType; // app类型 4代表一直播
    private Boolean redName; // 是否被社区红名了

    private String norbleMedal = null;

    //实名：手机绑定
    private Boolean isNeedBindPhone;
    private String phoneNum;

    private String ext;

    public MyUserInfo() {
        if (mUserInfo == null) {
            mUserInfo = new UserInfo();
        }
    }

    public Long getUid() {
        return this.mUserInfo.getUserId();
    }

    public void setUid(Long uid) {
        this.mUserInfo.setUserId(uid);
    }

    public Long getAvatar() {
        return this.mUserInfo.getAvatar();
    }

    public void setAvatar(Long avatar) {
        this.mUserInfo.setAvatar(avatar);
    }

    public String getNickName() {
        return this.mUserInfo.getUserNickname();
    }

    public void setNickName(String nickName) {
        this.mUserInfo.setUserNickname(nickName);
    }

    public String getSign() {
        return this.mUserInfo.getSignature();
    }

    public void setSign(String sign) {
        this.mUserInfo.setSignature(sign);
    }

    public Integer getGender() {
        return this.mUserInfo.getGender();
    }

    public void setGender(Integer gender) {
        this.mUserInfo.setGender(gender);
    }

    public Integer getLevel() {
        return this.mUserInfo.getLevel();
    }

    public void setLevel(Integer level) {
        this.mUserInfo.setLevel(level);
    }

    public Integer getBadge() {
        return this.mUserInfo.getBadge();
    }

    public void setBadge(Integer badge) {
        this.mUserInfo.setBadge(badge);
    }

    public Long getUpdateTs() {
        return this.mUserInfo.getUpdateTime();
    }

    public void setUpdateTs(Long updateTs) {
        this.mUserInfo.setUpdateTime(updateTs);
    }

    public Integer getCertificationType() {
        return this.mUserInfo.getCertificationType();
    }

    public void setCertificationType(Integer certificationType) {
        this.mUserInfo.setCertificationType(certificationType);
    }

    public String getCertification() {
        return this.mUserInfo.getCertification();
    }

    public void setCertification(String certification) {
        this.mUserInfo.setCertification(certification);
    }

    public Boolean getCertificationChanged() {
        return this.certificationChanged;
    }

    public void setCertificationChanged(Boolean certificationChanged) {
        this.certificationChanged = certificationChanged;
    }

    public Boolean getIsBlock() {
        return this.mUserInfo.getBlock();
    }

    public void setIsBlock(Boolean isBlock) {
        this.mUserInfo.setBlock(isBlock);
    }

    public Integer getLiveTicketNum() {
        return this.mUserInfo.getLiveTicketNum();
    }

    public void setLiveTicketNum(Integer liveTicketNum) {
        this.mUserInfo.setLiveTicketNum(liveTicketNum);
    }

    public Integer getFansNum() {
        return this.mUserInfo.getFansNum();
    }

    public void setFansNum(Integer fansNum) {
        this.mUserInfo.setFansNum(fansNum);
    }

    public Integer getFollowNum() {
        return this.mUserInfo.getFansNum();
    }

    public void setFollowNum(Integer followNum) {
        this.mUserInfo.setFollowNum(followNum);
    }

    public Integer getVodNum() {
        return this.mUserInfo.getVodNum();
    }

    public void setVodNum(Integer vodNum) {
        this.mUserInfo.setVodNum(vodNum);
    }

    public Integer getEarnNum() {
        return this.mUserInfo.getEarnNum();
    }

    public void setEarnNum(Integer earnNum) {
        this.mUserInfo.setEarnNum(earnNum);
    }

    public Integer getDiamondNum() {
        return this.mUserInfo.getDiamondNum();
    }

    public void setDiamondNum(Integer diamondNum) {
        this.mUserInfo.setDiamondNum(diamondNum);
    }

    public Integer getGoldCoinNum() {
        return this.mUserInfo.getGoldCoinNum();
    }

    public void setGoldCoinNum(Integer goldCoinNum) {
        this.setGoldCoinNum(goldCoinNum);
    }

    public Integer getSendDiamondNum() {
        return this.mUserInfo.getSendDiamondNum();
    }

    public void setSendDiamondNum(Integer sendDiamondNum) {
        this.mUserInfo.setSendDiamondNum(sendDiamondNum);
    }

    public Integer getSentVirtualDiamondNum() {
        return this.mUserInfo.getSentVirtualDiamondNum();
    }

    public void setSentVirtualDiamondNum(Integer sentVirtualDiamondNum) {
        this.mUserInfo.setSendDiamondNum(sentVirtualDiamondNum);
    }

    public Integer getVirtualDiamondNum() {
        return this.mUserInfo.getVirtualDiamondNum();
    }

    public void setVirtualDiamondNum(Integer virtualDiamondNum) {
        this.mUserInfo.setVirtualDiamondNum(virtualDiamondNum);
    }

    public Boolean getIsLive() {
        return this.mUserInfo.getIsLive();
    }

    public void setIsLive(Boolean isLive) {
        this.mUserInfo.setIsLive(isLive);
    }

    public String getViewUrl() {
        return this.viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public String getRoomId() {
        return this.roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getTvRoomId() {
        return this.tvRoomId;
    }

    public void setTvRoomId(String tvRoomId) {
        this.tvRoomId = tvRoomId;
    }

    public Integer getRoomType() {
        return this.roomType;
    }

    public void setRoomType(Integer roomType) {
        this.roomType = roomType;
    }

    public Boolean getOnline() {
        return this.online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public Integer getAppType() {
        return this.appType;
    }

    public void setAppType(Integer appType) {
        this.appType = appType;
    }

    public Boolean getRedName() {
        return this.redName;
    }

    public void setRedName(Boolean redName) {
        this.redName = redName;
    }

    public String getNorbleMedal() {
        return this.norbleMedal;
    }

    public void setNorbleMedal(String norbleMedal) {
        this.norbleMedal = norbleMedal;
    }

    public Integer getVipLevel() {
        return this.mUserInfo.getVipLevel();
    }

    public void setVipLevel(Integer vipLevel) {
        this.mUserInfo.setVipLevel(vipLevel);
    }

    public Boolean getIsVipFrozen() {
        return this.mUserInfo.getIsVipFrozen();
    }

    public void setIsVipFrozen(Boolean isVipFrozen) {
        this.mUserInfo.setIsVipFrozen(isVipFrozen);
    }

    public Boolean getIsVipHide() {
        return this.mUserInfo.getIsVipHide();
    }

    public void setIsVipHide(Boolean isVipHide) {
        this.mUserInfo.setIsVipHide(isVipHide);
    }

    public Integer getNobleLevel() {
        return this.mUserInfo.getNobleLevel();
    }

    public void setNobleLevel(Integer nobleLevel) {
        this.mUserInfo.setNobleLevel(nobleLevel);
    }

    public Boolean getIsNeedBindPhone() {
        return this.isNeedBindPhone;
    }

    public void setIsNeedBindPhone(Boolean isNeedBindPhone) {
        this.isNeedBindPhone = isNeedBindPhone;
    }

    public String getPhoneNum() {
        return this.phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getExt() {
        return this.ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
    }

    public static MyUserInfo loadFrom(GetOwnInfoRsp rsp) {
        MyUserInfo user = new MyUserInfo();
        UserInfo userInfo = UserInfo.loadFrom(rsp.getPersonalInfo(), rsp.getPersonalData());
        user.setUserInfo(userInfo);
        return user;
    }
}
