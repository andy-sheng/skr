package com.module.msg.model;

import android.os.Parcel;

import com.common.log.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 注解名：MessageTag ；属性：value ，flag； value 即 ObjectName 是消息的唯一标识不可以重复，
 * 开发者命名时不能以 RC 开头，避免和融云内置消息冲突；flag 是用来定义消息的可操作状态。
 * 如下面代码段，自定义消息名称 CustomizeMessage ，vaule 是 app:custom ，
 * flag 是 MessageTag.ISCOUNTED | MessageTag.ISPERSISTED 表示消息计数且存库。
 * app:RedPkgMsg: 这是自定义消息类型的名称，测试的时候用"app:RedPkgMsg"；
 * <p>
 * MessageTag.NONE 表示不落存储
 */
@MessageTag(value = "SKR:SpecailOp", flag = MessageTag.NONE)
public class SpecailOpMsg extends MessageContent {
    public final String TAG = "CustomChatRoomMsg";

    int messageType;
    String contentJsonStr;

    public SpecailOpMsg() {

    }

    public SpecailOpMsg(byte[] data) {
        String jsonStr = null;

        try {
            jsonStr = new String(data, "UTF-8");
            JSONObject jsonObj = new JSONObject(jsonStr);

            if (jsonObj.has("content")) {
                this.contentJsonStr = jsonObj.optString("content");
            }
            if (jsonObj.has("type")) {
                this.messageType = jsonObj.optInt("type");
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    public SpecailOpMsg(Parcel source) {
        messageType = source.readInt();
        contentJsonStr = source.readString();
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getContentJsonStr() {
        return contentJsonStr;
    }

    public void setContentJsonStr(String contentJsonStr) {
        this.contentJsonStr = contentJsonStr;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("type", messageType);
            jsonObj.put("content", contentJsonStr);
        } catch (JSONException e) {
            MyLog.d(TAG, e);
        }

        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            MyLog.d(TAG, e);
        }
        return new byte[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.messageType);
        parcel.writeString(this.contentJsonStr);
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<SpecailOpMsg> CREATOR = new Creator<SpecailOpMsg>() {

        @Override
        public SpecailOpMsg createFromParcel(Parcel source) {
            return new SpecailOpMsg(source);
        }

        @Override
        public SpecailOpMsg[] newArray(int size) {
            return new SpecailOpMsg[size];
        }
    };
}
