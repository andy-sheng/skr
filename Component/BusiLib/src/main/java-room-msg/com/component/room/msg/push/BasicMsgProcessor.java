package com.component.room.msg.push;

import com.common.utils.U;
import com.module.msg.CustomMsgType;
import com.module.msg.IPushMsgProcess;

/**
 * 基础消息处理器，几乎每个房间内模块都要使用
 */
public class BasicMsgProcessor implements IPushMsgProcess {
    public final String TAG = "BasicMsgProcessor";

    @Override
    public void process(int messageType, byte[] data) {
        U.getToastUtil().showShort(TAG + "在处理 process pb" + " messageType=" + messageType);
        switch (messageType) {
            case CustomMsgType.MSG_TYPE_TEXT:
                break;
            case CustomMsgType.MSG_TYPE_ENTER:
                break;
            case CustomMsgType.MSG_TYPE_QUIT:
                break;
            default:
                break;
        }
    }

    @Override
    public int[] acceptType() {
        return new int[]{
                CustomMsgType.MSG_TYPE_TEXT,
                CustomMsgType.MSG_TYPE_ENTER,
                CustomMsgType.MSG_TYPE_QUIT,

        };
    }
}
