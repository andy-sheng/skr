package com.common.core.account;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.Date;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;


@Entity(
        indexes = {
                @Index(value = "uid DESC", unique = true)
        }
)
public class UserAccount {
    @Id
    private long id;
    @NotNull
    private String uid;
    private String nickName;
    private String avatar;
    private String password;
    private boolean isLogOff;
    private String thirdId;
    @NotNull
    private int channelId;
    private boolean needEditUserInfo;
    private String serviceToken;
    private String secretToken;
    private int sex;
    private String birthday;
    private String ext;

    @Generated(hash = 575886058)
    public UserAccount(long id, @NotNull String uid, String nickName, String avatar,
            String password, boolean isLogOff, String thirdId, int channelId,
            boolean needEditUserInfo, String serviceToken, String secretToken, int sex,
            String birthday, String ext) {
        this.id = id;
        this.uid = uid;
        this.nickName = nickName;
        this.avatar = avatar;
        this.password = password;
        this.isLogOff = isLogOff;
        this.thirdId = thirdId;
        this.channelId = channelId;
        this.needEditUserInfo = needEditUserInfo;
        this.serviceToken = serviceToken;
        this.secretToken = secretToken;
        this.sex = sex;
        this.birthday = birthday;
        this.ext = ext;
    }

    @Generated(hash = 1029142458)
    public UserAccount() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIsLogOff() {
        return this.isLogOff;
    }

    public void setIsLogOff(Boolean isLogOff) {
        this.isLogOff = isLogOff;
    }

    public String getThirdId() {
        return this.thirdId;
    }

    public void setThirdId(String thirdId) {
        this.thirdId = thirdId;
    }

    public Integer getChannelId() {
        return this.channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public Boolean getNeedEditUserInfo() {
        return this.needEditUserInfo;
    }

    public void setNeedEditUserInfo(Boolean needEditUserInfo) {
        this.needEditUserInfo = needEditUserInfo;
    }

    public String getServiceToken() {
        return this.serviceToken;
    }

    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }

    public String getSecretToken() {
        return this.secretToken;
    }

    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    public String getExt() {
        return this.ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public Integer getSex() {
        return this.sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getBirthday() {
        return this.birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setIsLogOff(boolean isLogOff) {
        this.isLogOff = isLogOff;
    }

    public void setNeedEditUserInfo(boolean needEditUserInfo) {
        this.needEditUserInfo = needEditUserInfo;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

}
