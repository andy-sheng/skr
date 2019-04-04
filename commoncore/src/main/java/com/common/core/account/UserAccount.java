package com.common.core.account;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.Date;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

import retrofit2.http.Body;


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
    private String password;
    private Boolean isLogOff = false;
    private String thirdId;
    private String phoneNum;
    @NotNull
    private Integer channelId;
    private Boolean needEditUserInfo = false;
    private String serviceToken;
    private String secretToken;
    private String rongToken;
    private String ext;

    @Generated(hash = 15335548)
    public UserAccount(Long id, @NotNull String uid, String password,
                       Boolean isLogOff, String thirdId, String phoneNum,
                       @NotNull Integer channelId, Boolean needEditUserInfo,
                       String serviceToken, String secretToken, String rongToken, String ext) {
        this.id = id;
        this.uid = uid;
        this.password = password;
        this.isLogOff = isLogOff;
        this.thirdId = thirdId;
        this.phoneNum = phoneNum;
        this.channelId = channelId;
        this.needEditUserInfo = needEditUserInfo;
        this.serviceToken = serviceToken;
        this.secretToken = secretToken;
        this.rongToken = rongToken;
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

    public String getPhoneNum() {
        return this.phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
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

    public String getRongToken() {
        return this.rongToken;
    }

    public void setRongToken(String rongToken) {
        this.rongToken = rongToken;
    }

    public String getExt() {
        return this.ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

}
