package com.wali.live.watchsdk.ipc.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wuxiaoshan on 17-3-31.
 */
public class ThirdPartLoginData implements Parcelable {

    int channelId;

    String xuid;

    int sex;

    String nickname;

    String headUrl;

    String sign;


    public ThirdPartLoginData() {
    }

    public ThirdPartLoginData(int channelId, String xuid, int sex, String nickname, String headUrl, String sign){
        this.channelId = channelId;
        this.xuid = xuid;
        this.sex = sex;
        this.nickname = nickname;
        this.headUrl = headUrl;
        this.sign = sign;
    }

    public ThirdPartLoginData(Parcel source){
        readFromParcel(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(channelId);
        dest.writeString(xuid);
        dest.writeInt(sex);
        dest.writeString(nickname);
        dest.writeString(headUrl);
        dest.writeString(sign);
    }

    public void readFromParcel(Parcel source){
        channelId = source.readInt();
        xuid = source.readString();
        sex = source.readInt();
        nickname = source.readString();
        headUrl = source.readString();
        sign = source.readString();
    }

    public String getXuid() {
        return xuid;
    }

    public void setXuid(String xuid) {
        this.xuid = xuid;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public static final Creator<ThirdPartLoginData> CREATOR = new Creator<ThirdPartLoginData>(){
        @Override
        public ThirdPartLoginData createFromParcel(Parcel source) {
            return new ThirdPartLoginData(source);
        }

        @Override
        public ThirdPartLoginData[] newArray(int size) {
            return new ThirdPartLoginData[0];
        }
    };
}
