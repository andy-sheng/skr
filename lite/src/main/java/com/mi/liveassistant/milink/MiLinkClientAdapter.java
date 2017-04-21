package com.mi.liveassistant.milink;

import android.text.TextUtils;

import com.mi.live.data.account.HostChannelManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.common.preference.PreferenceUtils;
import com.mi.liveassistant.milink.callback.MiLinkChannelStatusObserver;
import com.mi.liveassistant.milink.callback.MiLinkPacketDispatcher;
import com.mi.liveassistant.milink.callback.MiLinkStatusObserver;
import com.mi.liveassistant.milink.command.MiLinkCommand;
import com.mi.liveassistant.milink.event.MiLinkEventListener;
import com.mi.liveassistant.milink.handler.ExtraDataHandler;
import com.mi.milink.sdk.aidl.PacketData;
import com.mi.milink.sdk.client.MiLinkChannelClient;
import com.mi.milink.sdk.client.SendPacketListener;
import com.mi.milink.sdk.client.ipc.MiLinkClientIpc;
import com.mi.milink.sdk.data.Const;

/**
 * Created by linjinbin on 15/2/15.
 */
public class MiLinkClientAdapter {
    private final static String TAG = MiLinkClientAdapter.class.getSimpleName();

    private static MiLinkClientAdapter sInstance;

    MiLinkPacketDispatcher mMiLinkPacketDispatcher = new MiLinkPacketDispatcher();
    MiLinkEventListener mMiLinkEventListener = new MiLinkEventListener();
    MiLinkStatusObserver mMiLinkStatusObserver = new MiLinkStatusObserver();
    // 重新new MiLinkChannelStatusObserver，取消登录事件
    MiLinkChannelStatusObserver mMiLinkChannelStatusObserver = new MiLinkChannelStatusObserver();

    private String clientIp;

    private boolean mIsTouristMode = false;

    MiLinkChannelClient mMiLinkChannelClient;

    private MiLinkClientAdapter() {
        MiLinkClientIpc.setMilinkStateObserver(mMiLinkStatusObserver);
        MiLinkClientIpc.setEventListener(mMiLinkEventListener);
        MiLinkClientIpc.setPacketListener(mMiLinkPacketDispatcher);
        addPacketDataHandler(new ExtraDataHandler());

        mMiLinkChannelClient = new MiLinkChannelClient();
        mMiLinkChannelClient.setEventListener(mMiLinkEventListener);
        mMiLinkChannelClient.setMilinkStateObserver(mMiLinkChannelStatusObserver);
        mMiLinkChannelClient.setPacketListener(mMiLinkPacketDispatcher);
    }

    public static synchronized MiLinkClientAdapter getsInstance() {
        if (null == sInstance) {
            sInstance = new MiLinkClientAdapter();
        }
        return sInstance;
    }

    public void initCallBack() {
        initMilinkSdkByLoginMode(false);
    }

    public void initCallBackFirst() {
        initMilinkSdkByLoginMode(true);
    }

    private void initMilinkSdkByLoginMode(boolean first) {
        if (UserAccountManager.getInstance().hasAccount()) {
            String vuid = UserAccountManager.getInstance().getUuid();
            String serviceToken = UserAccountManager.getInstance().getServiceToken();
            String sSecurity = UserAccountManager.getInstance().getSSecurity();
            MyLog.d("MiLinkClientAdapter initUseLoginByAppLogin voipId=" + vuid + ",serviceToken=" + serviceToken + ",securit=" + sSecurity + ",first=" + first);

            if (!TextUtils.isEmpty(serviceToken) && !TextUtils.isEmpty(sSecurity)) {
                String inputIp = PreferenceUtils.getSettingString("spKeyIpManual", "");
                String inputPort = PreferenceUtils.getSettingString("spKeyPortManual", "");
                MyLog.w(TAG + " initMilinkSdkByLoginMode inputIp=" + inputIp + " inputPort=" + inputPort);
                MiLinkClientIpc.init(vuid, serviceToken, sSecurity, null, first);

                if (!TextUtils.isEmpty(inputIp) && !TextUtils.isEmpty(inputPort)) {   //ip不为空
                    MyLog.w(TAG + " enableConnectModeManual true");
                    MiLinkClientIpc.enableConnectModeManual(true);
                    MiLinkClientIpc.setIpAndPortInManualMode(inputIp, Integer.valueOf(inputPort));
                } else {
                    MyLog.w(TAG + " enableConnectModeManual false");
                    enableConnectModeManual(false);
                }
            }
        }
    }

    public void forceReconnet() {
        if (mIsTouristMode) {
            mMiLinkChannelClient.forceReconnect();
        } else {
            MiLinkClientIpc.forceReconnet();
        }
    }

    /**
     * @return milink若已经登录，返回true，否则返回false；登录上可接收push
     */
    public boolean isMiLinkLogined() {
        if (mIsTouristMode) {
            return mMiLinkChannelClient.isChannelLogined();
        } else {
            return MiLinkClientIpc.isMiLinkLogined();
        }
    }

    /**
     * @return milink若已经连接上，返回true，否则返回false；
     */
    public boolean isMiLinkConnected() {
        if (mIsTouristMode) {
            return mMiLinkChannelClient.getMiLinkConnectState() == Const.SessionState.Connected;
        } else {
            return MiLinkClientIpc.getMiLinkConnectState() == Const.SessionState.Connected;
        }
    }

    public void setTimeoutMultiply(float f) {
        MiLinkClientIpc.setTimeoutMultiply(f);
    }

    public void enableConnectModeManual(boolean b) {
        MiLinkClientIpc.enableConnectModeManual(b);
    }

    public void setIpAndPortInManualMode(String ip, int port) {
        MiLinkClientIpc.setIpAndPortInManualMode(ip, port);
    }

    /**
     * 设置milink 日志级别
     *
     * @param logLevel
     */
    public void setMilinkLogLevel(int logLevel) {
        MiLinkClientIpc.setMilinkLogLevel(logLevel);
    }

    public PacketData sendSync(final PacketData packet, final int timeout) {
        setChannelId(packet);
        if (mIsTouristMode) {
            if (checkChannelCommand(packet)) {
                return mMiLinkChannelClient.sendDataBySimpleChannel(packet, timeout);
            } else {
                return null;
            }
        } else {
            return MiLinkClientIpc.sendSync(packet, timeout);
        }
    }

    public PacketData sendDataByChannel(PacketData packet, int timeout) {
        setChannelId(packet);
        return mMiLinkChannelClient.sendDataBySimpleChannel(packet, timeout);
    }

    private boolean checkChannelCommand(final PacketData packet) {
        for (String command : accessCommand) {
            if (packet.getCommand().equals(command)) {
                return true;
            }
        }
        MyLog.w(TAG, packet.getCommand() + " not in accessCommand");
        return false;
    }

    private static String[] accessCommand = new String[]{
            MiLinkCommand.COMMAND_LIVE_LEAVE,
    };

    public void sendAsync(PacketData packet, int timeout, final SendPacketListener l) {
        setChannelId(packet);
        MiLinkClientIpc.sendAsync(packet, timeout, l);
    }

    public void logoff() {
        MiLinkClientIpc.logoff();
    }

    /**
     * 异步发送消息，这条消息的response会在init注册的listener中返回
     *
     * @param packet 发送的业务数据
     */
    public void sendAsync(PacketData packet) {
        setChannelId(packet);
        if (mIsTouristMode) {
            if (checkChannelCommand(packet)) {
                mMiLinkChannelClient.sendAsync(packet);
            }
        } else {
            MiLinkClientIpc.sendAsync(packet);
        }
    }

    public void sendAsync(PacketData packet, int timeout) {
        setChannelId(packet);
        if (mIsTouristMode) {
            if (checkChannelCommand(packet)) {
                mMiLinkChannelClient.sendAsync(packet, timeout);
            }
        } else {
            MiLinkClientIpc.sendAsync(packet, timeout);
        }
    }

    private void setChannelId(PacketData packet) {
        if (packet != null && TextUtils.isEmpty(packet.getChannelId()) && HostChannelManager.getInstance().getChannelId() != 0) {
            packet.setChannelId(String.valueOf(HostChannelManager.getInstance().getChannelId()));
        }
    }

    public void removeMiPushNotification() {
        MiLinkClientIpc.clearNotification(-1);
    }

    public void addPacketDataHandler(MiLinkPacketDispatcher.PacketDataHandler packetDataHandler) {
        mMiLinkPacketDispatcher.addPacketDataHandler(packetDataHandler);
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setIsTouristMode(boolean isTouristMode) {
        if (isTouristMode != mIsTouristMode) {
            if (isTouristMode) {
                //如果是游客模式
                mMiLinkChannelClient.initUseChannelMode();
            } else {
                //如果不是游客模式，主动退出
                mMiLinkChannelClient.logoff();
            }
        }
        mIsTouristMode = isTouristMode;
    }

    public boolean isTouristMode() {
        return mIsTouristMode;
    }

    /**
     * 尝试同步通道模式id，如果进程有的话，会同步过来.
     */
    public boolean trySyncAnonymousAccountId() {
        long id = MiLinkChannelClient.getAnonymousAccountId();
        MyLog.d(TAG, "AnonymousId:" + id);
        if (id != 0) {
            UserAccountManager.getInstance().setAnonymousId(id);
            return true;
        } else {
            if (mIsTouristMode) {
                mMiLinkChannelClient.initUseChannelMode();
            }
        }
        return false;
    }
}
