package com.mi.live.data.milink;

import android.text.TextUtils;

import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.milink.callback.MiLinkEventListener;
import com.mi.live.data.milink.callback.MiLinkPacketDispatcher;
import com.mi.live.data.milink.callback.MiLinkStatusObserver;
import com.mi.live.data.milink.handler.ExtraDataHandler;
import com.mi.milink.sdk.aidl.PacketData;
import com.mi.milink.sdk.client.MiLinkChannelClient;

/**
 * Created by zyh on 16/4/18.
 */
@Deprecated
public class MilinkChannelClientAdapter {
    private MiLinkChannelClient mMiLinkChannelClient;
    private static MilinkChannelClientAdapter sInstance;

    private final MiLinkPacketDispatcher mMiLinkPacketDispatcher = new MiLinkPacketDispatcher();
    private final MiLinkEventListener mMiLinkEventListener = new MiLinkEventListener();
    private final MiLinkStatusObserver mMiLinkStatusObserver = new MiLinkStatusObserver();
    private boolean mHasInit;

    private MilinkChannelClientAdapter() {
        mMiLinkChannelClient = new MiLinkChannelClient();
        mMiLinkChannelClient.setMilinkStateObserver(mMiLinkStatusObserver);
        mMiLinkChannelClient.setEventListener(mMiLinkEventListener);
        mMiLinkChannelClient.setPacketListener(mMiLinkPacketDispatcher);
        addPacketDataHandler(new ExtraDataHandler());
    }

    public synchronized static MilinkChannelClientAdapter getInstance() {
        if (sInstance == null) {
            sInstance = new MilinkChannelClientAdapter();
        }
        return sInstance;
    }

    public void initMilinkChannelClient() {
        if (!mHasInit) {
            mMiLinkChannelClient.initUseChannelMode();
            mHasInit = true;
        }
    }

    private void addPacketDataHandler(MiLinkPacketDispatcher.PacketDataHandler packetDataHandler) {
        mMiLinkPacketDispatcher.addPacketDataHandler(packetDataHandler);
    }

    public PacketData sendDataByChannel(final PacketData packet, final int timeout) {
        if (packet != null && TextUtils.isEmpty(packet.getChannelId()) && HostChannelManager.getInstance().getChannelId() != 0) {
            packet.setChannelId(String.valueOf(HostChannelManager.getInstance().getChannelId()));
        }
        initMilinkChannelClient();
        return mMiLinkChannelClient.sendDataBySimpleChannel(packet, timeout);
    }

    public void destroy() {
        if (mMiLinkChannelClient != null) {
            mMiLinkChannelClient.logoff();
        }
        sInstance = null;
    }

    public void forceReconnect() {
        mMiLinkChannelClient.forceReconnect();
    }

    public int getMiLinkConnectState() {
        if (mHasInit) {
            return mMiLinkChannelClient.getMiLinkConnectState();
        } else {
            return 0;
        }
    }

    public boolean isMiLinkLogined() {
        return mHasInit && mMiLinkChannelClient.isChannelLogined();
    }

    /**
     * 尝试同步通道模式id，如果进程有的话，会同步过来.
     *
     * @return
     */
    public boolean trySyncAnonymousAccountId() {
        long id = MiLinkChannelClient.getAnonymousAccountId();
        if (id != 0) {
            UserAccountManager.getInstance().setAnonymousId(id);
            return true;
        } else {
            initMilinkChannelClient();
        }
        return false;
    }
}
