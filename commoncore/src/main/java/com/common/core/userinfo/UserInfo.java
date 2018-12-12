package com.common.core.userinfo;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.common.log.MyLog;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wali.live.proto.User.BusinessUserInfo;
import com.wali.live.proto.User.PersonalData;
import com.wali.live.proto.User.PersonalInfo;
import com.wali.live.proto.User.Region;
import com.wali.live.proto.User.UserEcoAttr;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.common.core.userinfo.UserInfoManager.BOTH_FOLLOWED;
import static com.common.core.userinfo.UserInfoManager.MY_FOLLOWER;
import static com.common.core.userinfo.UserInfoManager.MY_FOLLOWING;
import static com.common.core.userinfo.UserInfoManager.NO_RELATION;
import static com.common.core.userinfo.UserInfoManager.QUERY_BLOCKER_LIST;
import static com.common.core.userinfo.UserInfoManager.QUERY_FOLLOWED_LIST;
import static com.common.core.userinfo.UserInfoManager.QUERY_FOLLOWER_LIST;

/**
 * 个人信息(基础类)
 */
@Entity(
        indexes = {
                @Index(value = "userId DESC", unique = true)
        }
)
public class UserInfo {
    @Id
    private Long id;
    @NonNull
    private Long userId;
    private Long avatar;   // 头像时间戳
    private String userNickname;    // 昵称
    private String userDisplayname; // 备注
    private String letter;          // 昵称或备注的首字母
    private String signature;       // 签名
    private Integer gender;         // 性别
    private Integer level;          // 等级
    private Integer badge;          // 徽章
    private Long updateTime;        //更新时间，水位

    // 认证
    private Integer certificationType; //认证类型, 0表示未认证, 1表示新浪微博　2：官方账号，3：推荐认证，4：小米认证
    private String certification;   //认证信息
    public Integer waitingCertificationType;  //等待审核的用户认证类型，目前只有4：小米认证 需要审核；审核通过或未通过后waiting_certification_type会置0
    public String certificationId;  //认证绑定信息
    public Integer realNameCertificationStatus;  //实名认证状态，1：审核中  2：实名认证已通过  3:实名认证未通过

    // 关系
    private Integer relative;  //0为双方未关注, 1为我关注该用户, 2为该用户关注我, 3为双方关注
    private Boolean block;  // 是否拉黑,默认为false

    // 管理
    private Boolean isInspector;        //是否是巡查员 具有管理员权限
    private String adminList;           //管理员
    private Boolean isUnionAdmin;       //是否是工会管理员

    // vip相关
    private Integer vipLevel;                                         //vip等级
    private Boolean isVipFrozen;                                  //vip是否被冻结
    private Boolean isVipHide;                                    //该vip用户最后一次的隐身状态
    //贵族特权等级
    private Integer nobleLevel;          //贵族特权

    // 个人主页的封面图片，格式如下 {"ts":"1465295801" , "img":"xxx"}
    public String coverPhotoJson;

    // 身份
    private Integer userType; //默认0表示普通用户，1为商铺类型
    private String businessInfo; // mUserType = 1 时为商铺信息, 否则为空
    private Integer sellerStatus; //0 普通用户，1 白名单内(可以申请成为卖家) , 2. 正在申请中 3. 申请卖家成功
    private Boolean isRedName;                                     //是否被社区红名了

    private Boolean isLive; //是否在直播
    private Boolean isFirstAudit;  //头像昵称是否先审后发

    /* 这个用户对应的付费弹幕礼物id，确定这个用户开的直播的付费弹幕价格*/
    private Integer payBarrageGiftId;

    //地区信息
    private String regions;

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

    private String ext; //待扩展

    @Generated(hash = 703274252)
    public UserInfo(Long id, @NonNull Long userId, Long avatar, String userNickname, String userDisplayname,
                    String letter, String signature, Integer gender, Integer level, Integer badge, Long updateTime,
                    Integer certificationType, String certification, Integer waitingCertificationType,
                    String certificationId, Integer realNameCertificationStatus, Integer relative, Boolean block,
                    Boolean isInspector, String adminList, Boolean isUnionAdmin, Integer vipLevel, Boolean isVipFrozen,
                    Boolean isVipHide, Integer nobleLevel, String coverPhotoJson, Integer userType, String businessInfo,
                    Integer sellerStatus, Boolean isRedName, Boolean isLive, Boolean isFirstAudit,
                    Integer payBarrageGiftId, String regions, Integer liveTicketNum, Integer fansNum, Integer followNum,
                    Integer vodNum, Integer earnNum, Integer diamondNum, Integer goldCoinNum, Integer sendDiamondNum,
                    Integer sentVirtualDiamondNum, Integer virtualDiamondNum, String ext) {
        this.id = id;
        this.userId = userId;
        this.avatar = avatar;
        this.userNickname = userNickname;
        this.userDisplayname = userDisplayname;
        this.letter = letter;
        this.signature = signature;
        this.gender = gender;
        this.level = level;
        this.badge = badge;
        this.updateTime = updateTime;
        this.certificationType = certificationType;
        this.certification = certification;
        this.waitingCertificationType = waitingCertificationType;
        this.certificationId = certificationId;
        this.realNameCertificationStatus = realNameCertificationStatus;
        this.relative = relative;
        this.block = block;
        this.isInspector = isInspector;
        this.adminList = adminList;
        this.isUnionAdmin = isUnionAdmin;
        this.vipLevel = vipLevel;
        this.isVipFrozen = isVipFrozen;
        this.isVipHide = isVipHide;
        this.nobleLevel = nobleLevel;
        this.coverPhotoJson = coverPhotoJson;
        this.userType = userType;
        this.businessInfo = businessInfo;
        this.sellerStatus = sellerStatus;
        this.isRedName = isRedName;
        this.isLive = isLive;
        this.isFirstAudit = isFirstAudit;
        this.payBarrageGiftId = payBarrageGiftId;
        this.regions = regions;
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
        this.ext = ext;
    }

    @Generated(hash = 1279772520)
    public UserInfo() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAvatar() {
        return this.avatar;
    }

    public void setAvatar(Long avatar) {
        this.avatar = avatar;
    }

    public String getUserNickname() {
        return this.userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getUserDisplayname() {
        return this.userDisplayname;
    }

    public void setUserDisplayname(String userDisplayname) {
        this.userDisplayname = userDisplayname;
    }

    public String getLetter() {
        return this.letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
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

    public Long getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
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

    public Integer getWaitingCertificationType() {
        return this.waitingCertificationType;
    }

    public void setWaitingCertificationType(Integer waitingCertificationType) {
        this.waitingCertificationType = waitingCertificationType;
    }

    public String getCertificationId() {
        return this.certificationId;
    }

    public void setCertificationId(String certificationId) {
        this.certificationId = certificationId;
    }

    public Integer getRealNameCertificationStatus() {
        return this.realNameCertificationStatus;
    }

    public void setRealNameCertificationStatus(Integer realNameCertificationStatus) {
        this.realNameCertificationStatus = realNameCertificationStatus;
    }

    public Integer getRelative() {
        return this.relative;
    }

    public void setRelative(Integer relative) {
        this.relative = relative;
    }

    public Boolean getBlock() {
        return this.block;
    }

    public void setBlock(Boolean block) {
        this.block = block;
    }

    public Boolean getIsInspector() {
        return this.isInspector;
    }

    public void setIsInspector(Boolean isInspector) {
        this.isInspector = isInspector;
    }

    public String getAdminList() {
        return this.adminList;
    }

    public void setAdminList(String adminList) {
        this.adminList = adminList;
    }

    public Boolean getIsUnionAdmin() {
        return this.isUnionAdmin;
    }

    public void setIsUnionAdmin(Boolean isUnionAdmin) {
        this.isUnionAdmin = isUnionAdmin;
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

    public String getCoverPhotoJson() {
        return this.coverPhotoJson;
    }

    public void setCoverPhotoJson(String coverPhotoJson) {
        this.coverPhotoJson = coverPhotoJson;
    }

    public Integer getUserType() {
        return this.userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getBusinessInfo() {
        return this.businessInfo;
    }

    public void setBusinessInfo(String businessInfo) {
        this.businessInfo = businessInfo;
    }

    public Integer getSellerStatus() {
        return this.sellerStatus;
    }

    public void setSellerStatus(Integer sellerStatus) {
        this.sellerStatus = sellerStatus;
    }

    public Boolean getIsRedName() {
        return this.isRedName;
    }

    public void setIsRedName(Boolean isRedName) {
        this.isRedName = isRedName;
    }

    public Boolean getIsLive() {
        return this.isLive;
    }

    public void setIsLive(Boolean isLive) {
        this.isLive = isLive;
    }

    public Boolean getIsFirstAudit() {
        return this.isFirstAudit;
    }

    public void setIsFirstAudit(Boolean isFirstAudit) {
        this.isFirstAudit = isFirstAudit;
    }

    public Integer getPayBarrageGiftId() {
        return this.payBarrageGiftId;
    }

    public void setPayBarrageGiftId(Integer payBarrageGiftId) {
        this.payBarrageGiftId = payBarrageGiftId;
    }

    public String getRegions() {
        return this.regions;
    }

    public void setRegions(String regions) {
        this.regions = regions;
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

    public String getExt() {
        return this.ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public UserInfo(long userId, String userNickname, long avatar) {
        this.userId = userId;
        this.userNickname = userNickname;
        this.avatar = avatar;
    }


    public static UserInfo loadFrom(com.zq.live.proto.Common.UserInfo userInfo) {
        UserInfo user = new UserInfo();
        if (userInfo == null) {
            return user;
        }

        user.setUserId(Long.valueOf(userInfo.getUserID()));
        user.setUserNickname(userInfo.getNickName());
        // TODO: 2018/12/12 头像怎么处理，待完善
        // TODO: 2018/12/12  等级和是否为系统 待完善
        user.setGender(userInfo.getSex().getValue());
        user.setSignature(userInfo.getDescription());

        return user;
    }


    /**
     * 对数据中更新数据进行校验
     *
     * @param userInfoDB 数据库存储
     */
    public void fill(UserInfo userInfoDB) {
        if (this.userId == null) {
            setUserId(userInfoDB.getUserId());
        }

        if (this.avatar == null) {
            setAvatar(userInfoDB.getAvatar());
        }

        if (TextUtils.isEmpty(this.userNickname)) {
            setUserNickname(userInfoDB.getUserNickname());
        }

        if (TextUtils.isEmpty(this.userDisplayname)){
            setUserDisplayname(userInfoDB.getUserDisplayname());
        }

        if (TextUtils.isEmpty(this.letter)){
            setLetter(userInfoDB.letter);
        }

        if (TextUtils.isEmpty(this.signature)) {
            setSignature(userInfoDB.getSignature());
        }

        if (this.gender == null) {
            setGender(userInfoDB.getGender());
        }

        if (this.level == null) {
            setLevel(userInfoDB.getLevel());
        }

        if (this.badge == null) {
            setBadge(userInfoDB.getBadge());
        }

        if (this.certificationType == null) {
            setCertificationType(userInfoDB.getCertificationType());
        }

        if (this.relative == null) {
            setRelative(userInfoDB.getRelative());
        }

        if (this.block == null) {
            setBlock(userInfoDB.getBlock());
        }

        if (this.updateTime < userInfoDB.getUpdateTime()) {
            setUpdateTime(userInfoDB.getUpdateTime());
        }

        if (this.vipLevel == null) {
            setVipLevel(userInfoDB.getVipLevel());
        }

        if (this.isVipFrozen == null) {
            setIsVipFrozen(userInfoDB.getIsVipFrozen());
        }

        if (this.isVipFrozen == null) {
            setIsVipHide(userInfoDB.getIsVipHide());
        }

        if (this.nobleLevel == null) {
            setNobleLevel(userInfoDB.getNobleLevel());
        }

        if (this.certificationType == null) {
            setCertificationType(userInfoDB.getCertificationType());
        }

        if (TextUtils.isEmpty(this.certification)) {
            setCertification(userInfoDB.getCertification());
        }

        if (this.waitingCertificationType == null) {
            setWaitingCertificationType(userInfoDB.getWaitingCertificationType());
        }

        if (TextUtils.isEmpty(this.certificationId)) {
            setCertificationId(userInfoDB.getCertificationId());
        }

        if (this.realNameCertificationStatus == null) {
            setRealNameCertificationStatus(userInfoDB.getRealNameCertificationStatus());
        }

        if (this.isInspector == null) {
            setIsInspector(userInfoDB.getIsInspector());
        }

        if (TextUtils.isEmpty(this.adminList)) {
            setAdminList(userInfoDB.getAdminList());
        }

        if (this.isUnionAdmin == null) {
            setIsUnionAdmin(userInfoDB.getIsUnionAdmin());
        }

        if (TextUtils.isEmpty(this.coverPhotoJson)) {
            setCoverPhotoJson(userInfoDB.getCoverPhotoJson());
        }

        if (this.userType == null) {
            setUserType(userInfoDB.getUserType());
        }

        if (TextUtils.isEmpty(this.businessInfo)) {
            setBusinessInfo(userInfoDB.getBusinessInfo());
        }

        if (this.sellerStatus == null) {
            setSellerStatus(userInfoDB.getSellerStatus());
        }

        if (this.isRedName == null) {
            setIsRedName(userInfoDB.getIsRedName());
        }

        if (this.isLive == null) {
            setIsLive(userInfoDB.getIsLive());
        }

        if (this.isFirstAudit == null) {
            setIsFirstAudit(userInfoDB.getIsFirstAudit());
        }

        if (this.payBarrageGiftId == null) {
            setPayBarrageGiftId(userInfoDB.getPayBarrageGiftId());
        }

        if (TextUtils.isEmpty(this.regions)) {
            setRegions(userInfoDB.getRegions());
        }

        if (this.liveTicketNum == null) {
            setLiveTicketNum(userInfoDB.getLiveTicketNum());
        }

        if (this.fansNum == null) {
            setFansNum(userInfoDB.getFansNum());
        }

        if (this.followNum == null) {
            setFollowNum(userInfoDB.getFollowNum());
        }

        if (this.vodNum == null) {
            setVodNum(userInfoDB.getVodNum());
        }

        if (this.earnNum == null) {
            setEarnNum(userInfoDB.getEarnNum());
        }

        if (this.diamondNum == null) {
            setDiamondNum(userInfoDB.getDiamondNum());
        }

        if (this.goldCoinNum == null) {
            setGoldCoinNum(userInfoDB.getGoldCoinNum());
        }

        if (this.sendDiamondNum == null) {
            setSendDiamondNum(userInfoDB.getSendDiamondNum());
        }

        if (this.sentVirtualDiamondNum == null) {
            setSentVirtualDiamondNum(userInfoDB.getSentVirtualDiamondNum());
        }

        if (this.virtualDiamondNum == null) {
            setVirtualDiamondNum(userInfoDB.getVirtualDiamondNum());
        }
    }

    private String packetAdminList(List<Long> list) {
        if (list == null || list.size() == 0) {
            return "";
        }

        JSONArray jsonArray = new JSONArray(list);
        return jsonArray.toString();
    }

    // 重写方法
    public List<Long> getRealAdminList() {
        List<Long> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(this.adminList);
            int i = 0;
            for (; i < jsonArray.length(); i++) {
                list.add((Long) jsonArray.get(i));
            }
        } catch (Exception e) {
            MyLog.e(e);
        }

        return null;
    }

    public void setRealAdminList(List<Long> list) {
        if (list == null || list.size() == 0) {
            return;
        }

        JSONArray jsonArray = new JSONArray(list);
        setAdminList(jsonArray.toString());
    }

    public Region getRealRegions() {
        try {
            JSONObject jsonObject = new JSONObject(this.regions);
            String countryName = jsonObject.optString(JSON_KEY_COUNTRY_NAME);
            String countryCode = jsonObject.optString(JSON_KEY_COUNTRY_CODE);
            Integer sourceType = jsonObject.optInt(JSON_KEY_SOURCE_TYPE);
            Region region = new Region.Builder()
                    .setCountry(countryName)
                    .setCountryCode(countryCode)
                    .setSourceType(sourceType).build();
            return region;
        } catch (JSONException e) {
            MyLog.e(e);
        }
        return null;
    }

    public void setRealRegions(Region region) {
        if (region == null) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_KEY_COUNTRY_NAME, region.getCountry());
            jsonObject.put(JSON_KEY_COUNTRY_CODE, region.getCountryCode());
            jsonObject.put(JSON_KEY_SOURCE_TYPE, region.getSourceType());
            setRegions(jsonObject.toString());
        } catch (Exception e) {
            MyLog.e(e);
        }
    }


    public BusinessUserInfo getRealBusinessInfo() {
        try {
            JSONObject jsonObject = new JSONObject(this.businessInfo);
            JSONArray jsonArray = jsonObject.getJSONArray(JSON_KEY_BUSINESS_PHONES);
            List<String> phones = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                phones.add((String) jsonArray.get(i));
            }
            BusinessUserInfo businessUserInfo = new BusinessUserInfo.Builder()
                    .addAllServicePhone(phones)
                    .setAddress(jsonObject.optString(JSON_KEY_BUSINESS_ADDRESS))
                    .setBusinessHours(jsonObject.optString(JSON_KEY_BUSINESS_HOURS))
                    .setIntro(jsonObject.optString(JSON_KEY_BUSINESS_INTRO))
                    .build();
            return businessUserInfo;
        } catch (JSONException e) {
            MyLog.e(e);
        }
        return null;
    }

    public void setRealBusinessInfo(BusinessUserInfo businessInfo) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray(businessInfo.getServicePhoneList());
        try {
            jsonObject.put(JSON_KEY_BUSINESS_PHONES, jsonArray);
            jsonObject.put(JSON_KEY_BUSINESS_ADDRESS, businessInfo.getAddress());
            jsonObject.put(JSON_KEY_BUSINESS_HOURS, businessInfo.getBusinessHours());
            jsonObject.put(JSON_KEY_BUSINESS_INTRO, businessInfo.getIntro());
            setBusinessInfo(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private static final String JSON_KEY_COUNTRY_NAME = "country_name";
    private static final String JSON_KEY_COUNTRY_CODE = "country_code";
    private static final String JSON_KEY_SOURCE_TYPE = "source_type";

    private static final String JSON_KEY_BUSINESS_PHONES = "business_phones";
    private static final String JSON_KEY_BUSINESS_ADDRESS = "business_address";
    private static final String JSON_KEY_BUSINESS_HOURS = "business_hours";
    private static final String JSON_KEY_BUSINESS_INTRO = "business_intro";
}

