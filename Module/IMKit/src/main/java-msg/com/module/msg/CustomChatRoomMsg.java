package com.module.msg;

import android.annotation.SuppressLint;
import android.os.Parcel;

import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/*
 * 注解名：MessageTag ；属性：value ，flag； value 即 ObjectName 是消息的唯一标识不可以重复，
 * 开发者命名时不能以 RC 开头，避免和融云内置消息冲突；flag 是用来定义消息的可操作状态。
 *如下面代码段，自定义消息名称 CustomizeMessage ，vaule 是 app:custom ，
 * flag 是 MessageTag.ISCOUNTED | MessageTag.ISPERSISTED 表示消息计数且存库。
 * app:RedPkgMsg: 这是自定义消息类型的名称，测试的时候用"app:RedPkgMsg"；
 * */
@MessageTag(value = "app:CustomChatRoomMsg", flag = MessageTag.NONE)
public class CustomChatRoomMsg extends MessageContent {
    public CustomChatRoomMsg() {
    }

    public CustomChatRoomMsg(byte[] data) {
        super(data);
    }

    public CustomChatRoomMsg(Parcel source) {

    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

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
