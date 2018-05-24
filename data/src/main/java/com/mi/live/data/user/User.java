package com.mi.live.data.user;

import com.base.log.MyLog;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.region.Region;
import com.wali.live.dao.Relation;
import com.wali.live.proto.AccountProto;
import com.wali.live.proto.CommonChannelProto;
import com.wali.live.proto.LiveSummitProto;
import com.wali.live.proto.UserProto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangyuehuan on 15-12-1.
 */
public class User implements Serializable {
    private String TAG = User.class.getSimpleName();

    public static final int GENDER_MAN = 1;
    public static final int GENDER_WOMAN = 2;

    public static final int USER_TYPE_NORMAL = 0;//普通用户
    public static final int USER_TYPE_SHOP = 1;  //商铺
    public static final int USER_TYPE_TV = 2;  //电视台

    public static final int CERTIFICATION_NOT = 0;
    public static final int CERTIFICATION_WEIBO = 1; //微博
    public static final int CERTIFICATION_OFFICIAL = 2;
    public static final int CERTIFICATION_RECOMMEND = 3;
    public static final int CERTIFICATION_XIAOMI = 4; //官微认证
    public static final int CERT_TYPE_PGC = 5;      //pgc 用户
    public static final int CERTIFICATION_TV = 6; //电视台认证

    public static final int WAITING_CERTIFICATION_XIAOMI = 4;
    public static final int WAITING_CERTIFICATION_REALNAME = 5;

    public static final int REALNAME_STATUS_WAITING = 1;
    public static final int REALNAME_STATUS_FAILURE = 3;
    public static final int REALNAME_STATUS_SUCCESS = 2;

    //贵族特权等级类型
    public static final int NOBLE_LEVEL_TOP = 500;//王者
    public static final int NOBLE_LEVEL_SECOND = 400;//公爵
    public static final int NOBLE_LEVEL_THIRD = 300;//侯爵
    public static final int NOBLE_LEVEL_FOURTH = 200;//伯爵
    public static final int NOBLE_LEVEL_FIFTH = 100;//子爵

    /*以下是用户基础信息*/
    private long uid = 0;
    private long avatar;            // 头像时间戳
    private String nickname;        // 昵称
    private String sign;            // 签名
    private int gender;             // 性别
    private int level;              // 等级
    private int badge;              // 徽章
    private long updateTime;        // 更新时间，水位
    private String certification;   // 认证信息
    private boolean isFocused;      // 是否被关注
    private boolean isBlock;        // 是否被拉黑
    private boolean isBothwayFollowing;         //是否双向关注
    private int sellerStatus; //0 普通用户，1 白名单内(可以申请成为卖家) , 2. 正在申请中 3. 申请卖家成功
    //认证类型, 0表示未认证, 1表示新浪微博　2：官方账号，3：推荐认证，4：小米认证
    private int certificationType = 0;
    //等待审核的用户认证类型，目前只有4：小米认证 需要审核；审核通过或未通过后waiting_certification_type会置0
    public int waitingCertificationType = 0;
    //认证绑定信息
    public String certificationId;
    //实名认证状态，1：审核中  2：实名认证已通过  3:实名认证未通过
    public int realNameCertificationStatus;

    public String coverPhotoJson;

    public boolean firstAudit;

    private boolean isInspector = false;        //是否是巡查员 具有管理员权限
    private List<Long> managerList;

    private int mUserType = USER_TYPE_NORMAL;

    private BusinessInfo mBusinessInfo;

    /*以下是用户的详细信息*/
    private int liveTicketNum;      //星票数
    private int fansNum;            //粉丝数
    private int followNum;          //关注数
    private int vodNum;             //点播数
    private int earnNum;            //收益数
    private int diamondNum;         //钻石数
    private int goldCoinNum;         //金币数
    private int sendDiamondNum;     //送出钻石数
    private int sentVirtualDiamondNum;//送出虚拟钻石数
    private int virtualDiamondNum;  //虚拟钻数
    private boolean isLive = false;

    /* 这个用户对应的付费弹幕礼物id，确定这个用户开的直播的付费弹幕价格*/
    private int payBarrageGiftId = -1;

    private List<Long> mRankTopThreeList = new ArrayList<>();      //排行前三名用户
    private String mViewUrl;                                       //直播地址
    private String mRoomId = null;                                 //正在直播的id, 房间号
    private String mTVRoomId = null;                               //正在播放的电视台房间id
    private int mRoomType;

    private boolean mOnline;  //是否在线，这个只有在添加管理员的时候使用了。
    private int mAppType;                                          // app类型 4代表一直播
    private boolean certificationChanged;
    private boolean mRedName;                                      //是否被社区红名了

    private Region mRegion = new Region();//地区信息
    private List<Medal> beforeNickNameMedalList;
    private List<Medal> afterNickNameMedalList;
    private List<Medal> userCardMedalList;

    private String mNorbleMedal = null;
    // vip相关
    private int mVipLevel;                                         //vip等级
    private boolean mIsVipFrozen;                                  //vip是否被冻结
    private boolean mIsVipHide;                                    //该vip用户最后一次的隐身状态

    //贵族特权等级
    private int mNobleLevel;          //贵族特权

    //实名：手机绑定
    private boolean mIsNeedBindPhone;
    private String mPhoneNum;

    public User() {
    }

    public User(long uid, String nickname, int level, long avatar, int certificationType) {
        this.uid = uid;
        this.nickname = nickname;
        this.level = level;
        this.avatar = avatar;
        this.certificationType = certificationType;
    }

    // 只有uid,avatar,nickname默认是临时对象
    public User(long uid, long avatar, String nickname) {
        this.uid = uid;
        this.avatar = avatar;
        this.nickname = nickname;
    }

    public User(AccountProto.UserInfo protoUser) {
        parse(protoUser);
    }

    public User(UserProto.PersonalInfo protoUser) {
        parse(protoUser);
    }

    public User(CommonChannelProto.UserInfo protoUser) {
        parse(protoUser);
    }

    public User(CommonChannelProto.UserBrief protoUser) {
        parse(protoUser);
    }

    public User(LiveSummitProto.UserInfo protoUser) {
        parse(protoUser);
    }

    public void parse(LiveSummitProto.UserInfo protoUser) {
        this.uid = protoUser.getUuid();
        this.avatar = protoUser.getAvatar();
        this.nickname = protoUser.getNickname();
    }

    public void parse(AccountProto.UserInfo protoUser) {
        this.uid = protoUser.getUuid();
        this.avatar = protoUser.getAvatar();
        this.nickname = protoUser.getNickname();
        this.sign = protoUser.getSign();
        this.gender = protoUser.getGender();
        this.level = protoUser.getLevel();
        this.badge = protoUser.getBadge();
        this.updateTime = protoUser.getUpdateTime();
    }

    public void parse(UserProto.PersonalInfo protoUser) {
        if (protoUser == null) {
            return;
        }
        this.uid = protoUser.getZuid();
        this.avatar = protoUser.getAvatar();
        this.nickname = protoUser.getNickname();
        this.sign = protoUser.getSign();
        this.gender = protoUser.getGender();
        this.level = protoUser.getLevel();
        this.badge = protoUser.getBadge();
        this.updateTime = protoUser.getUpdateTime();
        this.certification = protoUser.getCertification();
        this.isFocused = protoUser.getIsFocused();
        this.isBlock = protoUser.getIsBlocked();
        this.isBothwayFollowing = protoUser.getIsBothwayFollowing();
        this.certificationType = protoUser.getCertificationType();
        this.waitingCertificationType = protoUser.getWaitingCertificationType();
        this.certificationId = protoUser.getCertificationId();
        this.realNameCertificationStatus = protoUser.getRealNameCertificationStatus();
        this.coverPhotoJson = protoUser.getCoverPhoto();
        this.firstAudit = protoUser.getIsFirstAudit();
        this.mUserType = protoUser.getUserType();
        this.isInspector = protoUser.getIsInspector();
        this.sellerStatus = protoUser.getSellerStatus();
        this.certificationChanged = false;
        this.mRedName = protoUser.getIsRedname();
        if (protoUser.getBusinessUserInfo() != null) {
            mBusinessInfo = new BusinessInfo(protoUser.getBusinessUserInfo());
        }
        this.mRegion = new Region(protoUser.getRegion());

        if (protoUser.getAdminUidsList() != null) {
            List<Long> list = new ArrayList<>();
            for (Long id : protoUser.getAdminUidsList()) {
                list.add(id);
            }
            managerList = list;
        }
        this.mVipLevel = protoUser.getVipLevel();
        this.mIsVipHide = protoUser.getVipHidden();
        this.mIsVipFrozen = protoUser.getVipDisable();
    }

    public void parse(UserProto.PersonalData protoData) {
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
            UserProto.UserEcoAttr userEcoAttr = UserProto.UserEcoAttr.parseFrom(bs);
            if (userEcoAttr != null) {
                this.payBarrageGiftId = userEcoAttr.getBulletGiftId();
                this.virtualDiamondNum = userEcoAttr.getUsableVirtualGemCnt();
                this.sentVirtualDiamondNum = userEcoAttr.getConsumVirtualGemCnt();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void parse(CommonChannelProto.UserInfo protoUser) {
        if (protoUser == null) {
            return;
        }
        this.uid = protoUser.getZuid();
        this.avatar = protoUser.getAvatar();
        this.nickname = protoUser.getNickname();
        this.sign = protoUser.getSign();
        this.gender = protoUser.getGender();
        this.level = protoUser.getLevel();
        this.badge = protoUser.getBadge();
        this.updateTime = protoUser.getUpdateTime();
        this.certification = protoUser.getCertification();
        this.certificationType = protoUser.getCertificationType();
        this.fansNum = protoUser.getFansCount();
    }

    public void parse(CommonChannelProto.UserBrief protoUser) {
        if (protoUser == null) {
            return;
        }
        this.uid = protoUser.getUId();
        this.avatar = protoUser.getAvatar();
        this.nickname = protoUser.getNickname();
        this.level = protoUser.getLevel();
        this.certificationType = protoUser.getCertType();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String country = "";
        if (mRegion != null) {
            country = mRegion.getCountry();
        }
        sb.append("[")
                .append("uid == " + uid)
                .append(" avatar == " + avatar)
                .append(" nickname == " + nickname)
                .append(" sign == " + sign)
                .append(" gender == " + gender)
                .append(" level == " + level)
                .append(" badge == " + badge)
                .append(" updateTime == " + updateTime)
                .append(" certification == " + certification)
                .append(" isFocused == " + isFocused)
                .append(" isBlock == " + isBlock)
                .append(" isBothwayFollowing == " + isBothwayFollowing)
                .append(" certificationType == " + certificationType)
                .append(" waitingCertificationType == " + waitingCertificationType)
                .append(" certificationId == " + certificationId)
                .append(" realNameCertificationStatus == " + realNameCertificationStatus)
                .append(" liveTicketNum == " + liveTicketNum)
                .append("region == " + country)
                .append(" fansNum == " + fansNum)
                .append(" followNum == " + followNum)
                .append(" sendDiamondNum == " + sendDiamondNum)
                .append(" vodNum == " + vodNum)
                .append(" earnNum == " + earnNum)
                .append(" diamondNum == " + diamondNum)
                .append(" sentVirtualDiamondNum == " + sentVirtualDiamondNum)
                .append(" virtualDiamondNum == " + virtualDiamondNum)
                .append(" viewUrl == " + mViewUrl)
                .append(" roomId == " + mRoomId)
                .append(" coverPhotoJson == " + coverPhotoJson)
                .append(" sellerStatus == " + sellerStatus)
                .append(" firstAudit == " + firstAudit)
                .append(" mBusinessInfo == " + mBusinessInfo)
                .append(" appType == " + mAppType)
                .append("]");

        return sb.toString();
    }

    /**
     * 设置排行前三名用户
     *
     * @param data
     */
    public void setRankTopThreeList(List<Long> data) {
        if (data == null) {
            return;
        }

        mRankTopThreeList = data;
        MyLog.d(TAG, " setRankTopThreeList callback,size = " + mRankTopThreeList.size());
    }

    public boolean isManager(long uid) {
        if (managerList != null) {
            return managerList.contains(uid);
        }
        return false;
    }

    /**
     * 得到排名前三名用户
     *
     * @return
     */
    public List<Long> getRankTopThreeList() {
        if (mRankTopThreeList == null) {
            return new ArrayList<>();
        }

        return mRankTopThreeList;
    }

    public int getVirtualDiamondNum() {
        return virtualDiamondNum;
    }

    public void setVirtualDiamondNum(int virtualDiamondNum) {
        this.virtualDiamondNum = virtualDiamondNum;
    }

    public void setSellerStatus(int status) {
        this.sellerStatus = status;
    }

    public int getSellerStatus() {
        return sellerStatus;
    }

    public void setRoomType(int mRoomType) {
        this.mRoomType = mRoomType;
    }

    /**
     * Live.proto里的UserRoomInfo.type 对应我们的LiveManager里定义的常量
     *
     * @return
     */
    public int getRoomType() {
        return mRoomType;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getAvatar() {
        return avatar;
    }

    public void setAvatar(long avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getLiveTicketNum() {
        return liveTicketNum;
    }

    public void setLiveTicketNum(int liveTicketNum) {
        this.liveTicketNum = liveTicketNum;
    }

    public int getFansNum() {
        return fansNum;
    }

    public void setFansNum(int fansNum) {
        this.fansNum = fansNum;
    }

    public int getFollowNum() {
        return followNum;
    }

    public void setFollowNum(int followNum) {
        this.followNum = followNum;
    }

    public int getSendDiamondNum() {
        return sendDiamondNum;
    }

    public void setSendDiamondNum(int sendDiamondNum) {
        this.sendDiamondNum = sendDiamondNum;
    }

    public int getVodNum() {
        return vodNum;
    }

    public void setVodNum(int vodNum) {
        this.vodNum = vodNum;
    }

    public int getEarnNum() {
        return earnNum;
    }

    public void setEarnNum(int earnNum) {
        this.earnNum = earnNum;
    }

    public int getDiamondNum() {
        return diamondNum;
    }

    public void setDiamondNum(int diamondNum) {
        this.diamondNum = diamondNum;
    }

    public int getSentVirtualDiamondNum() {
        return sentVirtualDiamondNum;
    }

    public void setSentVirtualDiamondNum(int sentVirtualDiamondNum) {
        this.sentVirtualDiamondNum = sentVirtualDiamondNum;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setIsFocused(boolean isFocused) {
        this.isFocused = isFocused;
    }

    public boolean isBlock() {
        return isBlock;
    }

    public void setIsBlock(boolean isBlock) {
        this.isBlock = isBlock;
    }

    public void setIsBothwayFollowing(boolean isBothwayFollowing) {
        this.isBothwayFollowing = isBothwayFollowing;
    }

    public void setCertificationType(int certificationType) {
        this.certificationType = certificationType;
    }

    public boolean isInspector() {
        return isInspector;
    }

    public void setInspector(boolean isInspector) {
        this.isInspector = isInspector;
    }

    public boolean isBothwayFollowing() {
        return isBothwayFollowing;
    }

    public int getCertificationType() {
        return certificationType;
    }

    public String getViewUrl() {
        return mViewUrl;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public void setViewUrl(String mViewUrl) {
        this.mViewUrl = mViewUrl;
    }

    public void setRoomId(String mRoomId) {
        this.mRoomId = mRoomId;
    }

    public int getPayBarrageGiftId() {
        return payBarrageGiftId;
    }

    public boolean isRedName() {
        return mRedName;
    }

    public void setRedName(boolean mRedName) {
        this.mRedName = mRedName;
    }

    public Relation getRelation() {

        Relation relation = new Relation();
        relation.setUserId(this.getUid());
        relation.setAvatar(this.avatar);
        relation.setUserNickname(this.nickname);
        relation.setSignature(this.sign);

        relation.setGender(this.gender);
        relation.setCertificationType(this.certificationType);
        relation.setLevel(this.level);
        relation.setMTicketNum(this.liveTicketNum);

        relation.setIsFollowing(this.isFocused);
        relation.setIsBothway(this.isBothwayFollowing);
        return relation;
    }

    public int getUserType() {
        return mUserType;
    }

    public void setUserType(int mUserType) {
        this.mUserType = mUserType;
    }

    public BusinessInfo getBusinessInfo() {
        return mBusinessInfo;
    }

    public void setBusinessInfo(BusinessInfo businessInfo) {
        this.mBusinessInfo = businessInfo;
    }

    public boolean isCertificationChanged() {
        return certificationChanged;
    }

    public void setCertificationChanged(boolean certificationChanged) {
        this.certificationChanged = certificationChanged;
    }

    public List<Medal> getBeforeNickNameMedalList() {
        return beforeNickNameMedalList;
    }

    public void setBeforeNickNameMedal(List<Medal> beforeNickNameMedalList) {
        this.beforeNickNameMedalList = beforeNickNameMedalList;
    }

    public List<Medal> getAfterNickNameMedalList() {
        return afterNickNameMedalList;
    }

    public void setAfterNickNameMedalList(List<Medal> afterNickNameMedalList) {
        this.afterNickNameMedalList = afterNickNameMedalList;
    }

    public int getVipLevel() {
        return mVipLevel;
    }

    public void setVipLevel(int vipLevel) {
        mVipLevel = vipLevel;
    }

    public boolean isVipFrozen() {
        return mIsVipFrozen;
    }

    public void setVipFrozen(boolean vipFrozen) {
        mIsVipFrozen = vipFrozen;
    }

    public boolean isVipHide() {
        return mIsVipHide;
    }

    public void setVipHide(boolean vipHide) {
        mIsVipHide = vipHide;
    }

    public int getAppType() {
        return mAppType;
    }

    public void setAppType(int mAppType) {
        this.mAppType = mAppType;
    }

    public void setTVRoomId(String tvRoomId) {
        mTVRoomId = tvRoomId;
    }

    public String getTVRoomId() {
        return mTVRoomId;
    }

    public Region getRegion() {
        if (mRegion != null) {
            return mRegion;
        } else {
            return null;
            /*if (LocaleUtil.getSelectedLanguageIndexFromPreference() == LocaleUtil.INDEX_ENGLISH) {
                return UserProto.Region.newBuilder().setCountry("China").setCountryCode("en").build();
            } else {
                return UserProto.Region.newBuilder().setCountry("中国").setCountryCode("cn").build();
            }*/
        }
    }

    public void setRegion(Region mRegion) {
        this.mRegion = mRegion;
    }

    public int getNobleLevel() {
        return mNobleLevel;
    }

    public void setNobleLevel(int nobleLevel) {
        mNobleLevel = nobleLevel;
    }

    public boolean isNoble() {
        return this.mNobleLevel == User.NOBLE_LEVEL_FIFTH || this.mNobleLevel == User.NOBLE_LEVEL_FOURTH
                || this.mNobleLevel == User.NOBLE_LEVEL_THIRD || this.mNobleLevel == User.NOBLE_LEVEL_SECOND
                || this.mNobleLevel == User.NOBLE_LEVEL_TOP;
    }

    public boolean ismIsNeedBindPhone() {
        return mIsNeedBindPhone;
    }

    public void setmIsNeedBindPhone(boolean mIsNeedBindPhone) {
        this.mIsNeedBindPhone = mIsNeedBindPhone;
    }

    public String getmPhoneNum() {
        return mPhoneNum;
    }

    public void setmPhoneNum(String mPhoneNum) {
        this.mPhoneNum = mPhoneNum;
    }
}
