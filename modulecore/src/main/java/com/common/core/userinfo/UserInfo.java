package com.common.core.userinfo;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.wali.live.proto.User.PersonalInfo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

import static com.common.core.userinfo.UserInfoManager.BOTH_FOLLOWED;
import static com.common.core.userinfo.UserInfoManager.MY_FOLLOWER;
import static com.common.core.userinfo.UserInfoManager.MY_FOLLOWING;
import static com.common.core.userinfo.UserInfoManager.NO_RELATION;
import static com.common.core.userinfo.UserInfoManager.QUERY_BLOCKER_LIST;
import static com.common.core.userinfo.UserInfoManager.QUERY_FOLLOWED_LIST;
import static com.common.core.userinfo.UserInfoManager.QUERY_FOLLOWER_LIST;

/**
 * 个人信息
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
    private Long avatar;
    private String userNickname;
    private String signature;
    private Integer gender;
    private Integer level;
    private Integer badge;
    private Integer mTicketNum;
    private Integer certificationType;
    private Integer relative;  //0为双方未关注, 1为我关注该用户, 2为该用户关注我, 3为双方关注
    private boolean block;  // 是否拉黑,默认为false

    private String ext; //待扩展

    @Generated(hash = 1263741178)
    public UserInfo(Long id, @NonNull Long userId, Long avatar, String userNickname,
                    String signature, Integer gender, Integer level, Integer badge,
                    Integer mTicketNum, Integer certificationType, Integer relative,
                    boolean block, String ext) {
        this.id = id;
        this.userId = userId;
        this.avatar = avatar;
        this.userNickname = userNickname;
        this.signature = signature;
        this.gender = gender;
        this.level = level;
        this.badge = badge;
        this.mTicketNum = mTicketNum;
        this.certificationType = certificationType;
        this.relative = relative;
        this.block = block;
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

    public Integer getMTicketNum() {
        return this.mTicketNum;
    }

    public void setMTicketNum(Integer mTicketNum) {
        this.mTicketNum = mTicketNum;
    }

    public Integer getCertificationType() {
        return this.certificationType;
    }

    public void setCertificationType(Integer certificationType) {
        this.certificationType = certificationType;
    }

    public Integer getRelative() {
        return this.relative;
    }

    public void setRelative(Integer relative) {
        this.relative = relative;
    }

    public boolean getBlock() {
        return this.block;
    }

    public void setBlock(boolean block) {
        this.block = block;
    }

    public String getExt() {
        return this.ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public static UserInfo loadFrom(com.wali.live.proto.Relation.UserInfo userInfo, int type) {
        UserInfo user = new UserInfo();
        if (userInfo == null) {
            return user;
        }

        user.setUserId(userInfo.getUserId());
        user.setUserNickname(userInfo.getNickname());
        user.setSignature(userInfo.getSignature());
        user.setAvatar(userInfo.getAvatar());
        user.setCertificationType(userInfo.getCertificationType());
        user.setGender(userInfo.getGender());
        user.setLevel(userInfo.getLevel());
        user.setBadge(userInfo.getBadge());

        if (userInfo.getIsBothway()) {
            user.setRelative(BOTH_FOLLOWED);
        } else {
            if (type == QUERY_FOLLOWED_LIST) {
                user.setRelative(MY_FOLLOWING);
            } else if (type == QUERY_FOLLOWER_LIST) {
                user.setRelative(MY_FOLLOWER);
            } else {
                user.setRelative(NO_RELATION);
            }
        }

        if (type == QUERY_BLOCKER_LIST) {
            user.setBlock(true);
        }

        return user;
    }

    public static UserInfo loadFrom(PersonalInfo personalInfo) {
        UserInfo userInfo = new UserInfo();
        if (personalInfo == null) {
            return userInfo;
        }

        userInfo.setUserId(personalInfo.getZuid());
        userInfo.setUserNickname(personalInfo.getNickname());
        userInfo.setSignature(personalInfo.getSign());
        userInfo.setAvatar(personalInfo.getAvatar());
        userInfo.setCertificationType(personalInfo.getCertificationType());
        userInfo.setGender(personalInfo.getGender());
        userInfo.setLevel(personalInfo.getLevel());
        userInfo.setBadge(personalInfo.getBadge());

        if (personalInfo.getIsBothwayFollowing()) {
            userInfo.setRelative(BOTH_FOLLOWED);
        } else {
            if (personalInfo.getIsFocused()) {
                userInfo.setRelative(MY_FOLLOWING);
            } else {
                userInfo.setRelative(NO_RELATION);
            }
        }
        userInfo.setBlock(personalInfo.getIsBlocked());

        return userInfo;
    }


    /**
     * 对数据中更新数据进行校验
     *
     * @param userInfoDB     数据库存储
     * @param relationChange 关注关系是否改变
     * @param blockChange    拉黑状态是否改变
     */
    public void fill(UserInfo userInfoDB, boolean relationChange, boolean blockChange) {
        if (this.avatar == null) {
            setAvatar(userInfoDB.getAvatar());
        }

        if (TextUtils.isEmpty(this.userNickname)) {
            setUserNickname(userInfoDB.getUserNickname());
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

        if (this.mTicketNum == null) {
            setMTicketNum(userInfoDB.getMTicketNum());
        }

        if (this.certificationType == null) {
            setCertificationType(userInfoDB.getCertificationType());
        }

        if (!relationChange) {
            setRelative(userInfoDB.getRelative());
        }

        if (!blockChange) {
            setBlock(userInfoDB.getBlock());
        }
    }
}
