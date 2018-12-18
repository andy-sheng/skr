package com.common.core.userinfo;

import android.support.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

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
    private long id;
    @NonNull
    private long userId;
    private String avatar;   // 头像时间戳
    private String userNickname;    // 昵称
    private String userDisplayname; // 备注
    private String letter;          // 昵称或备注的首字母
    private long updateTime=-1;        //更新时间，水位

    private int sex = -1;         // 性别
    private String birthday; // 生日
    // 关系
    private int relative = -1;  //0为双方未关注, 1为我关注该用户, 2为该用户关注我, 3为双方关注
    private int block = -1;  // 是否拉黑,1为拉黑 0为没有
    private int isSystem = -1; // 是否是系统

    private String ext; //待扩展

@Generated(hash = 1700347813)
public UserInfo(long id, long userId, String avatar, String userNickname,
        String userDisplayname, String letter, long updateTime, int sex,
        String birthday, int relative, int block, int isSystem, String ext) {
    this.id = id;
    this.userId = userId;
    this.avatar = avatar;
    this.userNickname = userNickname;
    this.userDisplayname = userDisplayname;
    this.letter = letter;
    this.updateTime = updateTime;
    this.sex = sex;
    this.birthday = birthday;
    this.relative = relative;
    this.block = block;
    this.isSystem = isSystem;
    this.ext = ext;
}

@Generated(hash = 1279772520)
public UserInfo() {
}

public long getId() {
    return this.id;
}

public void setId(long id) {
    this.id = id;
}

public long getUserId() {
    return this.userId;
}

public void setUserId(long userId) {
    this.userId = userId;
}

public String getAvatar() {
    return this.avatar;
}

public void setAvatar(String avatar) {
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

public long getUpdateTime() {
    return this.updateTime;
}

public void setUpdateTime(long updateTime) {
    this.updateTime = updateTime;
}

public int getSex() {
    return this.sex;
}

public void setSex(int sex) {
    this.sex = sex;
}

public String getBirthday() {
    return this.birthday;
}

public void setBirthday(String birthday) {
    this.birthday = birthday;
}

public int getRelative() {
    return this.relative;
}

public void setRelative(int relative) {
    this.relative = relative;
}

public int getBlock() {
    return this.block;
}

public void setBlock(int block) {
    this.block = block;
}

public int getIsSystem() {
    return this.isSystem;
}

public void setIsSystem(int isSystem) {
    this.isSystem = isSystem;
}

public String getExt() {
    return this.ext;
}

public void setExt(String ext) {
    this.ext = ext;
}

}

