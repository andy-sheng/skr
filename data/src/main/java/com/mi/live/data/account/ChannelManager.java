package com.mi.live.data.account;

/**
 * 缓存channelid
 *
 * Created by wuxiaoshan on 17-2-21.
 */
public class ChannelManager {

    private static ChannelManager sInstance = new ChannelManager();

    private volatile String mChannelId;

    private ChannelManager(){

    }

    public static ChannelManager getInstance(){
        return sInstance;
    }

    public String getChannelId(){
        return mChannelId;
    }

    public synchronized void setChannelId(String channelId){
        mChannelId = channelId;
    }

}
