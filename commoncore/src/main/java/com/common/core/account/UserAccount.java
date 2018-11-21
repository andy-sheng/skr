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
    private Long id;
    @NotNull
    private String uid;
    private String nickName;
    private String imgUrl;
    private String serviceToken;
    private String securityKey;
    private String passToken;
    private String password;
    private String oldPwd;
    private String deviceId;
    private String pSecurity;
    private String sSecurity;
    private Integer isReset;
    private Integer isNew;
    private Boolean isLogOff;
    private String thirdId;
    @NotNull
    private Integer channelId;
    private Boolean needEditUserInfo;
    private String ext;

    @Generated(hash = 249300457)
    public UserAccount(Long id, @NotNull String uid, String nickName, String imgUrl,
                       String serviceToken, String securityKey, String passToken,
                       String password, String oldPwd, String deviceId, String pSecurity,
                       String sSecurity, Integer isReset, Integer isNew, Boolean isLogOff,
                       String thirdId, @NotNull Integer channelId, Boolean needEditUserInfo,
                       String ext) {
        this.id = id;
        this.uid = uid;
        this.nickName = nickName;
        this.imgUrl = imgUrl;
        this.serviceToken = serviceToken;
        this.securityKey = securityKey;
        this.passToken = passToken;
        this.password = password;
        this.oldPwd = oldPwd;
        this.deviceId = deviceId;
        this.pSecurity = pSecurity;
        this.sSecurity = sSecurity;
        this.isReset = isReset;
        this.isNew = isNew;
        this.isLogOff = isLogOff;
        this.thirdId = thirdId;
        this.channelId = channelId;
        this.needEditUserInfo = needEditUserInfo;
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

    public String getImgUrl() {
        return this.imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getServiceToken() {
        return this.serviceToken;
    }

    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }

    public String getSecurityKey() {
        return this.securityKey;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }

    public String getPassToken() {
        return this.passToken;
    }

    public void setPassToken(String passToken) {
        this.passToken = passToken;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOldPwd() {
        return this.oldPwd;
    }

    public void setOldPwd(String oldPwd) {
        this.oldPwd = oldPwd;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPSecurity() {
        return this.pSecurity;
    }

    public void setPSecurity(String pSecurity) {
        this.pSecurity = pSecurity;
    }

    public String getSSecurity() {
        return this.sSecurity;
    }

    public void setSSecurity(String sSecurity) {
        this.sSecurity = sSecurity;
    }

    public Integer getIsReset() {
        return this.isReset;
    }

    public void setIsReset(Integer isReset) {
        this.isReset = isReset;
    }

    public Integer getIsNew() {
        return this.isNew;
    }

    public void setIsNew(Integer isNew) {
        this.isNew = isNew;
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

    public String getExt() {
        return this.ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }


}
