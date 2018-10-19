package com.common.core.myinfo;

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

@Entity(
        indexes = {
                @Index(value = "uid DESC", unique = true)
        }
)
public class MyUserInfo {
    @Id
    private Long id;
    @NotNull
    private Long uid; // 用户id
    private Long avatar;// 用户头像时间戳
    private String nickName;// 用户昵称
    private String sign;// 个性签名
    private Integer gender;// 用户性别
    private Integer level;// 用户等级
    private Integer badge;// 用户勋章

    private Long updateTs; // 上次更新的时间戳

    private Integer certificationType = 0; // 认证类型
    private String certification; // 认证信息
    private Boolean certificationChanged; // 认证改变

    private Boolean isFocused;      // 是否被关注
    private Boolean isBlock;        // 是否被拉黑
    private Boolean isBothwayFollowing;  //是否双向关注

    /*以下是用户的详细信息*/
    private Integer liveTicketNum;      //星票数
    private Integer fansNum;            //粉丝数
    private Integer followNum;          //关注数
    private Integer vodNum;             //点播数
    private Integer earnNum;            //收益数
    private Integer diamondNum;         //钻石数
    private Integer goldCoinNum;         //金币数
    private Integer sendDiamondNum;     //送出钻石数
    private Integer sentVirtualDiamondNum;//送出虚拟钻石数
    private Integer virtualDiamondNum;  //虚拟钻数
    private Boolean isLive = false;

    private String viewUrl;                                       //直播地址
    private String roomId = null;                                 //正在直播的id, 房间号
    private String tvRoomId = null;                               //正在播放的电视台房间id
    private Integer roomType;

    private Boolean online;  // 是否在线，这个只有在添加管理员的时候使用了。
    private Integer appType; // app类型 4代表一直播
    private Boolean redName; // 是否被社区红名了

    private String norbleMedal = null;

    // vip相关
    private Integer vipLevel;                                         //vip等级
    private Boolean isVipFrozen;                                  //vip是否被冻结
    private Boolean isVipHide;                                    //该vip用户最后一次的隐身状态

    //贵族特权等级
    private Integer nobleLevel;          //贵族特权

    //实名：手机绑定
    private Boolean isNeedBindPhone;
    private String phoneNum;

    private String ext;

    @Generated(hash = 2079621641)
    public MyUserInfo(Long id, @NotNull Long uid, Long avatar, String nickName,
                      String sign, Integer gender, Integer level, Integer badge,
                      Long updateTs, Integer certificationType, String certification,
                      Boolean certificationChanged, Boolean isFocused, Boolean isBlock,
                      Boolean isBothwayFollowing, Integer liveTicketNum, Integer fansNum,
                      Integer followNum, Integer vodNum, Integer earnNum, Integer diamondNum,
                      Integer goldCoinNum, Integer sendDiamondNum,
                      Integer sentVirtualDiamondNum, Integer virtualDiamondNum,
                      Boolean isLive, String viewUrl, String roomId, String tvRoomId,
                      Integer roomType, Boolean online, Integer appType, Boolean redName,
                      String norbleMedal, Integer vipLevel, Boolean isVipFrozen,
                      Boolean isVipHide, Integer nobleLevel, Boolean isNeedBindPhone,
                      String phoneNum, String ext) {
        this.id = id;
        this.uid = uid;
        this.avatar = avatar;
        this.nickName = nickName;
        this.sign = sign;
        this.gender = gender;
        this.level = level;
        this.badge = badge;
        this.updateTs = updateTs;
        this.certificationType = certificationType;
        this.certification = certification;
        this.certificationChanged = certificationChanged;
        this.isFocused = isFocused;
        this.isBlock = isBlock;
        this.isBothwayFollowing = isBothwayFollowing;
        this.liveTicketNum = liveTicketNum;
        this.fansNum = fansNum;
        this.followNum = followNum;
        this.vodNum = vodNum;
        this.earnNum = earnNum;
        this.diamondNum = diamondNum;
        this.goldCoinNum = goldCoinNum;
        this.sendDiamondNum = sendDiamondNum;
        this.sentVirtualDiamondNum = sentVirtualDiamondNum;
        this.virtualDiamondNum = virtualDiamondNum;
        this.isLive = isLive;
        this.viewUrl = viewUrl;
        this.roomId = roomId;
        this.tvRoomId = tvRoomId;
        this.roomType = roomType;
        this.online = online;
        this.appType = appType;
        this.redName = redName;
        this.norbleMedal = norbleMedal;
        this.vipLevel = vipLevel;
        this.isVipFrozen = isVipFrozen;
        this.isVipHide = isVipHide;
        this.nobleLevel = nobleLevel;
        this.isNeedBindPhone = isNeedBindPhone;
        this.phoneNum = phoneNum;
        this.ext = ext;
    }

    @Generated(hash = 198622815)
    public MyUserInfo() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUid() {
        return this.uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getAvatar() {
        return this.avatar;
    }

    public void setAvatar(Long avatar) {
        this.avatar = avatar;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getSign() {
        return this.sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Integer getGender() {
        return this.gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getLevel() {
        return this.level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getBadge() {
        return this.badge;
    }

    public void setBadge(Integer badge) {
        this.badge = badge;
    }

    public Long getUpdateTs() {
        return this.updateTs;
    }

    public void setUpdateTs(Long updateTs) {
        this.updateTs = updateTs;
    }

    public Integer getCertificationType() {
        return this.certificationType;
    }

    public void setCertificationType(Integer certificationType) {
        this.certificationType = certificationType;
    }

    public String getCertification() {
        return this.certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }

    public Boolean getCertificationChanged() {
        return this.certificationChanged;
    }

    public void setCertificationChanged(Boolean certificationChanged) {
        this.certificationChanged = certificationChanged;
    }

    public Boolean getIsFocused() {
        return this.isFocused;
    }

    public void setIsFocused(Boolean isFocused) {
        this.isFocused = isFocused;
    }

    public Boolean getIsBlock() {
        return this.isBlock;
    }

    public void setIsBlock(Boolean isBlock) {
        this.isBlock = isBlock;
    }

    public Boolean getIsBothwayFollowing() {
        return this.isBothwayFollowing;
    }

    public void setIsBothwayFollowing(Boolean isBothwayFollowing) {
        this.isBothwayFollowing = isBothwayFollowing;
    }

    public Integer getLiveTicketNum() {
        return this.liveTicketNum;
    }

    public void setLiveTicketNum(Integer liveTicketNum) {
        this.liveTicketNum = liveTicketNum;
    }

    public Integer getFansNum() {
        return this.fansNum;
    }

    public void setFansNum(Integer fansNum) {
        this.fansNum = fansNum;
    }

    public Integer getFollowNum() {
        return this.followNum;
    }

    public void setFollowNum(Integer followNum) {
        this.followNum = followNum;
    }

    public Integer getVodNum() {
        return this.vodNum;
    }

    public void setVodNum(Integer vodNum) {
        this.vodNum = vodNum;
    }

    public Integer getEarnNum() {
        return this.earnNum;
    }

    public void setEarnNum(Integer earnNum) {
        this.earnNum = earnNum;
    }

    public Integer getDiamondNum() {
        return this.diamondNum;
    }

    public void setDiamondNum(Integer diamondNum) {
        this.diamondNum = diamondNum;
    }

    public Integer getGoldCoinNum() {
        return this.goldCoinNum;
    }

    public void setGoldCoinNum(Integer goldCoinNum) {
        this.goldCoinNum = goldCoinNum;
    }

    public Integer getSendDiamondNum() {
        return this.sendDiamondNum;
    }

    public void setSendDiamondNum(Integer sendDiamondNum) {
        this.sendDiamondNum = sendDiamondNum;
    }

    public Integer getSentVirtualDiamondNum() {
        return this.sentVirtualDiamondNum;
    }

    public void setSentVirtualDiamondNum(Integer sentVirtualDiamondNum) {
        this.sentVirtualDiamondNum = sentVirtualDiamondNum;
    }

    public Integer getVirtualDiamondNum() {
        return this.virtualDiamondNum;
    }

    public void setVirtualDiamondNum(Integer virtualDiamondNum) {
        this.virtualDiamondNum = virtualDiamondNum;
    }

    public Boolean getIsLive() {
        return this.isLive;
    }

    public void setIsLive(Boolean isLive) {
        this.isLive = isLive;
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
        return this.vipLevel;
    }

    public void setVipLevel(Integer vipLevel) {
        this.vipLevel = vipLevel;
    }

    public Boolean getIsVipFrozen() {
        return this.isVipFrozen;
    }

    public void setIsVipFrozen(Boolean isVipFrozen) {
        this.isVipFrozen = isVipFrozen;
    }

    public Boolean getIsVipHide() {
        return this.isVipHide;
    }

    public void setIsVipHide(Boolean isVipHide) {
        this.isVipHide = isVipHide;
    }

    public Integer getNobleLevel() {
        return this.nobleLevel;
    }

    public void setNobleLevel(Integer nobleLevel) {
        this.nobleLevel = nobleLevel;
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

    public static MyUserInfo loadFrom(GetOwnInfoRsp rsp) {
        MyUserInfo user = new MyUserInfo();
        if (rsp.getPersonalInfo() != null) {
            user.parse(rsp.getPersonalInfo());
        }
        if (rsp.getPersonalData() != null) {
            user.parse(rsp.getPersonalData());
        }
        return user;
    }

    private void parse(PersonalInfo protoUser) {
        if (protoUser == null) {
            return;
        }
        this.uid = protoUser.getZuid();
        this.avatar = protoUser.getAvatar();
        this.nickName = protoUser.getNickname();
        this.sign = protoUser.getSign();
        this.gender = protoUser.getGender();
        this.level = protoUser.getLevel();
        this.badge = protoUser.getBadge();
        this.updateTs = protoUser.getUpdateTime();
        this.certification = protoUser.getCertification();
        this.isFocused = protoUser.getIsFocused();
        this.isBlock = protoUser.getIsBlocked();
        this.isBothwayFollowing = protoUser.getIsBothwayFollowing();
        this.certificationType = protoUser.getCertificationType();
        this.certificationChanged = false;
        this.redName = protoUser.getIsRedname();

        this.vipLevel = protoUser.getVipLevel();
        this.isVipHide = protoUser.getVipHidden();
        this.isVipFrozen = protoUser.getVipDisable();
    }

    public void parse(PersonalData protoData) {
        if (protoData == null) {
            return;
        }
        this.liveTicketNum = protoData.getMliveTicketNum();
        this.fansNum = protoData.getFansNum();
        this.followNum = protoData.getFollowNum();
        this.sendDiamondNum = protoData.getSendDiamondNum();
        this.vodNum = protoData.getVodNum();
        this.earnNum = protoData.getEarnNum();
        this.diamondNum = protoData.getDiamondNum();

        ByteString bs = protoData.getUserEcoAttr();
        try {
            UserEcoAttr userEcoAttr = UserEcoAttr.parseFrom(bs.toByteArray());
            if (userEcoAttr != null) {
                this.virtualDiamondNum = userEcoAttr.getUsableVirtualGemCnt();
                this.sentVirtualDiamondNum = userEcoAttr.getConsumVirtualGemCnt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
