package com.module.msg.custom.club;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.common.RLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;

@MessageTag(value = "SKR:ClubInviteMsg", flag = MessageTag.ISPERSISTED)
public class ClubInviteMsg extends MessageContent {
    private static final String TAG = "InviteJoinClubMsg";
    public static final Creator<ClubInviteMsg> CREATOR = new Creator<ClubInviteMsg>() {
        public ClubInviteMsg createFromParcel(Parcel source) {
            return new ClubInviteMsg(source);
        }

        public ClubInviteMsg[] newArray(int size) {
            return new ClubInviteMsg[size];
        }
    };

    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {
            if (this.getJSONUserInfo() != null) {
                jsonObj.putOpt("user", this.getJSONUserInfo());
            }
            if (this.getJsonMentionInfo() != null) {
                jsonObj.putOpt("mentionedInfo", this.getJsonMentionInfo());
            }
            jsonObj.put("isBurnAfterRead", this.isDestruct());
            jsonObj.put("burnDuration", this.getDestructTime());
        } catch (JSONException var4) {
            RLog.e("TextMessage", "JSONException " + var4.getMessage());
        }

        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException var3) {
            RLog.e("TextMessage", "UnsupportedEncodingException ", var3);
            return null;
        }
    }

    protected ClubInviteMsg() {
    }

    public static ClubInviteMsg obtain(String text) {
        ClubInviteMsg model = new ClubInviteMsg();
        return model;
    }

    public ClubInviteMsg(byte[] data) {
        String jsonStr = null;

        try {
            if (data != null && data.length >= 40960) {
                RLog.e("TextMessage", "TextMessage length is larger than 40KB, length :" + data.length);
            }

            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException var5) {
            RLog.e("TextMessage", "UnsupportedEncodingException ", var5);
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            if (jsonObj.has("user")) {
                this.setUserInfo(this.parseJsonToUserInfo(jsonObj.getJSONObject("user")));
            }

            if (jsonObj.has("mentionedInfo")) {
                this.setMentionedInfo(this.parseJsonToMentionInfo(jsonObj.getJSONObject("mentionedInfo")));
            }

            if (jsonObj.has("isBurnAfterRead")) {
                this.setDestruct(jsonObj.getBoolean("isBurnAfterRead"));
            }

            if (jsonObj.has("burnDuration")) {
                this.setDestructTime(jsonObj.getLong("burnDuration"));
            }
        } catch (JSONException var4) {
            RLog.e("TextMessage", "JSONException " + var4.getMessage());
        }
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, this.getUserInfo());
        ParcelUtils.writeToParcel(dest, this.getMentionedInfo());
        ParcelUtils.writeToParcel(dest, this.isDestruct() ? 1 : 0);
        ParcelUtils.writeToParcel(dest, this.getDestructTime());
    }

    public ClubInviteMsg(Parcel in) {
        this.setUserInfo((UserInfo)ParcelUtils.readFromParcel(in, UserInfo.class));
        this.setMentionedInfo((MentionedInfo)ParcelUtils.readFromParcel(in, MentionedInfo.class));
        this.setDestruct(ParcelUtils.readIntFromParcel(in) == 1);
        this.setDestructTime(ParcelUtils.readLongFromParcel(in));
    }

    public String toString() {
        return "ClubInviteMsg{content=}";
    }

}
