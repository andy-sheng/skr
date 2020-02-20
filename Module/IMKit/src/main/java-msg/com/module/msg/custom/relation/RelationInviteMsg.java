package com.module.msg.custom.relation;

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

@MessageTag(value = "SKR:RelationInviteMsg", flag = (MessageTag.ISCOUNTED|MessageTag.ISPERSISTED))
public class RelationInviteMsg extends MessageContent {
    private static final String TAG = "RelationInviteMsg";
    private String content = "";
    private String uniqID = "";
    private long expireAt;

    public static final Creator<RelationInviteMsg> CREATOR = new Creator<RelationInviteMsg>() {
        public RelationInviteMsg createFromParcel(Parcel source) {
            return new RelationInviteMsg(source);
        }

        public RelationInviteMsg[] newArray(int size) {
            return new RelationInviteMsg[size];
        }
    };

    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.putOpt("content", content);
            jsonObj.putOpt("uniqID", uniqID);
            jsonObj.putOpt("expireAt", expireAt);
            if (this.getJSONUserInfo() != null) {
                jsonObj.putOpt("user", this.getJSONUserInfo());
            }
            if (this.getJsonMentionInfo() != null) {
                jsonObj.putOpt("mentionedInfo", this.getJsonMentionInfo());
            }
            jsonObj.put("isBurnAfterRead", this.isDestruct());
            jsonObj.put("burnDuration", this.getDestructTime());
        } catch (JSONException var4) {
            RLog.e(TAG, "JSONException " + var4.getMessage());
        }
        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException var3) {
            RLog.e(TAG, "UnsupportedEncodingException ", var3);
            return null;
        }
    }

    protected RelationInviteMsg() {
    }

    public static RelationInviteMsg obtain() {
        RelationInviteMsg model = new RelationInviteMsg();
        return model;
    }

    // 不能没有!!!! 不然会发包失败!!!
    public RelationInviteMsg(byte[] data) {
        String jsonStr = null;

        try {
            if (data != null && data.length >= 40960) {
                RLog.e(TAG, "length is larger than 40KB, length :" + data.length);
            }

            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException var5) {
            RLog.e(TAG, "UnsupportedEncodingException ", var5);
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            if (jsonObj.has("content")) {
                this.setContent(jsonObj.optString("content"));
            }

            if (jsonObj.has("uniqID")) {
                this.setUniqID(jsonObj.optString("uniqID"));
            }

            if (jsonObj.has("expireAt")) {
                this.setExpireAt(jsonObj.optLong("expireAt"));
            }

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
            RLog.e(TAG, "JSONException " + var4.getMessage());
        }
    }

    public long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    public String getUniqID() {
        return uniqID;
    }

    public void setUniqID(String uniqID) {
        this.uniqID = uniqID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, this.content);
        ParcelUtils.writeToParcel(dest, this.uniqID);
        ParcelUtils.writeToParcel(dest, this.expireAt);
        ParcelUtils.writeToParcel(dest, this.getUserInfo());
        ParcelUtils.writeToParcel(dest, this.getMentionedInfo());
        ParcelUtils.writeToParcel(dest, this.isDestruct() ? 1 : 0);
        ParcelUtils.writeToParcel(dest, this.getDestructTime());
    }

    public RelationInviteMsg(Parcel in) {
        this.setContent(ParcelUtils.readFromParcel(in));
        this.setUniqID(ParcelUtils.readFromParcel(in));
        this.setExpireAt(ParcelUtils.readLongFromParcel(in));
        this.setUserInfo((UserInfo)ParcelUtils.readFromParcel(in, UserInfo.class));
        this.setMentionedInfo((MentionedInfo)ParcelUtils.readFromParcel(in, MentionedInfo.class));
        this.setDestruct(ParcelUtils.readIntFromParcel(in) == 1);
        this.setDestructTime(ParcelUtils.readLongFromParcel(in));
    }


}
