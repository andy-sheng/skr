package com.module.msg.model;

import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import com.common.log.MyLog;
import com.common.utils.U;

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
@MessageTag(value = "app:CustomChatRoomMsg", flag = MessageTag.NONE)
public class CustomChatRoomMsg extends MessageContent {
    public final static String TAG = "CustomChatRoomMsg";

    int messageType;
    String contentJsonStr;
    byte[] data;

    public CustomChatRoomMsg() {

    }

    public CustomChatRoomMsg(byte[] data) {
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
            if (jsonObj.has("data")){
                String dataString = jsonObj.optString("data");
                this.data = U.getBase64Utils().decode(dataString);
            }

        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    public CustomChatRoomMsg(Parcel source) {
        messageType = source.readInt();
        contentJsonStr = source.readString();
        data = source.createByteArray();
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

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("type", messageType);
            jsonObj.put("content", contentJsonStr);
            String dataString = U.getBase64Utils().encode(data);
            jsonObj.put("data", dataString);
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
        parcel.writeByteArray(this.data);
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<CustomChatRoomMsg> CREATOR = new Creator<CustomChatRoomMsg>() {

        @Override
        public CustomChatRoomMsg createFromParcel(Parcel source) {
            return new CustomChatRoomMsg(source);
        }

        @Override
        public CustomChatRoomMsg[] newArray(int size) {
            return new CustomChatRoomMsg[size];
        }
    };
}
