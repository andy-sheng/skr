package com.mi.live.data.api.request;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;

/**
 * Created by lan on 16-3-18.
 *
 * @module milink请求
 * 将MiLink请求分为:生成request,构造ReqData,发送ReqData三个步骤
 * 需要注意的是,同步请求还需要解析Response数据的步骤
 * </p>
 * 主要目的是简化代码,方便增删接口
 * </p>
 * 目前只用于LiveManager相关代码,试试效果
 */
public abstract class BaseRequest {
    protected String TAG = getTAG();

    protected String mCommand;
    protected String mAction;
    protected String mChannelId;

    protected GeneratedMessage mRequest;
    protected GeneratedMessage mResponse;

    protected String getTAG() {
        return getClass().getSimpleName();
    }

    public BaseRequest(String command,String action,String channelId){
        mCommand = command;
        mAction = action;
        mChannelId = channelId;
    }

    /**
     * 生成请求数据
     */
    protected PacketData generateReqData() {
        PacketData reqData = new PacketData();
        reqData.setCommand(mCommand);
        reqData.setData(mRequest.toByteArray());
        if(!TextUtils.isEmpty(mChannelId)){
            reqData.setChannelId(mChannelId);
        }
        MyLog.d(TAG, mAction + " request : \n" + mRequest.toString());
        return reqData;
    }

    /**
     * 发送同步请求
     */
    protected GeneratedMessage sendSync() {
        if (mRequest == null) {
            MyLog.d(TAG, mAction + " request is null");
            return null;
        }
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(generateReqData(), MiLinkConstant.TIME_OUT);
        if (rspData != null) {
            try {
                mResponse = parse(rspData.getData());
                MyLog.d(TAG, mAction + " response : \n" + mResponse.toString());
            } catch (InvalidProtocolBufferException e) {
                MyLog.d(TAG, e);
            }
        } else {
            MyLog.d(TAG, mAction + " response is null");
        }
        return mResponse;
    }

    /**
     * 同步请求解析response的数据
     */
    protected abstract GeneratedMessage parse(byte[] bytes) throws InvalidProtocolBufferException;

    /**
     * 发送异步请求
     */
    protected boolean sendAsync() {
        if (mRequest == null) {
            MyLog.d(TAG, mAction + " request is null");
            return false;
        }
        MiLinkClientAdapter.getsInstance().sendAsync(generateReqData(), MiLinkConstant.TIME_OUT);
        return true;
    }


    public <T extends GeneratedMessage> T syncRsp() {
        return (T) sendSync();
    }

    public void async() {
        sendAsync();
    }
}
