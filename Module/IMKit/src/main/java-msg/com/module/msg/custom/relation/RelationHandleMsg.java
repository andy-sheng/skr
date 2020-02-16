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

/**
 * 是的，建议您在将您的自定义消息，设置成不入库的，也就是MessageTag 的注解设置为  MessageTag.STATUS ，
 * 否者的话，在进入会话界面也会获取历史消息，就会加载出来的
 * 不入库，只是不计入本地数据库中，在服务端还是可以查询到此消息的
 * 消息是不会丢失的
 */
@MessageTag(value = "SKR:RelationHandleMsg", flag = (MessageTag.ISCOUNTED|MessageTag.ISPERSISTED))
public class RelationHandleMsg extends MessageContent {
    private static final String TAG = "RelationHandleMsg";
    private String content;
    private String msgUid;
    private int handle;

    public static final Creator<RelationHandleMsg> CREATOR = new Creator<RelationHandleMsg>() {
        public RelationHandleMsg createFromParcel(Parcel source) {
            return new RelationHandleMsg(source);
        }

        public RelationHandleMsg[] newArray(int size) {
            return new RelationHandleMsg[size];
        }
    };

    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.putOpt("content", content);
            jsonObj.putOpt("handle", handle);
            jsonObj.putOpt("msgUid", msgUid);
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

    protected RelationHandleMsg() {
    }

    public static RelationHandleMsg obtain() {
        RelationHandleMsg model = new RelationHandleMsg();
        return model;
    }

    // 不能没有!!!! 不然会发包失败!!!
    public RelationHandleMsg(byte[] data) {
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

            if (jsonObj.has("handle")) {
                this.setHandle(jsonObj.optInt("handle"));
            }

            if (jsonObj.has("msgUid")) {
                this.setMsgUid(jsonObj.optString("msgUid"));
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getHandle() {
        return handle;
    }

    public void setHandle(int handle) {
        this.handle = handle;
    }

    public String getMsgUid() {
        return msgUid;
    }

    public void setMsgUid(String content) {
        this.msgUid = content;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, this.content);
        ParcelUtils.writeToParcel(dest, this.handle);
        ParcelUtils.writeToParcel(dest, this.msgUid);
        ParcelUtils.writeToParcel(dest, this.getUserInfo());
        ParcelUtils.writeToParcel(dest, this.getMentionedInfo());
        ParcelUtils.writeToParcel(dest, this.isDestruct() ? 1 : 0);
        ParcelUtils.writeToParcel(dest, this.getDestructTime());
    }

    public RelationHandleMsg(Parcel in) {
        this.setContent(ParcelUtils.readFromParcel(in));
        this.setHandle(ParcelUtils.readIntFromParcel(in));
        this.setMsgUid(ParcelUtils.readFromParcel(in));
        this.setUserInfo((UserInfo)ParcelUtils.readFromParcel(in, UserInfo.class));
        this.setMentionedInfo((MentionedInfo)ParcelUtils.readFromParcel(in, MentionedInfo.class));
        this.setDestruct(ParcelUtils.readIntFromParcel(in) == 1);
        this.setDestructTime(ParcelUtils.readLongFromParcel(in));
    }


}
