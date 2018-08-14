package com.mi.live.data.milink;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.milink.callback.MiLinkChannelStatusObserver;
import com.mi.live.data.milink.callback.MiLinkEventListener;
import com.mi.live.data.milink.callback.MiLinkPacketDispatcher;
import com.mi.live.data.milink.callback.MiLinkStatusObserver;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.handler.ExtraDataHandler;
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
        //mMilinkclient.clearNotification(-1);  清除mipsuh通知栏，传入id，id为-1时为清除所有。
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
        String vuid = null, serviceToken = null, sSecurity = null;
        if (UserAccountManager.getInstance().hasAccount()) {
            vuid = UserAccountManager.getInstance().getUuid();
            serviceToken = UserAccountManager.getInstance().getServiceToken();
            sSecurity = UserAccountManager.getInstance().getSSecurity();
            MyLog.d("MiLinkClientAdapter initUseLoginByAppLogin voipId=" + vuid + ",serviceToken=" + serviceToken + ",securit=" + sSecurity + ",first=" + first);
            if (!TextUtils.isEmpty(serviceToken) && !TextUtils.isEmpty(sSecurity)) {
                String inputIp = PreferenceUtils.getSettingString(GlobalData.app(), "spKeyIpManual", "");
                String inputPort = PreferenceUtils.getSettingString(GlobalData.app(), "spKeyPortManual", "");
                MyLog.w(TAG + " initMilinkSdkByLoginMode inputIp == " + inputIp + " inputPort == " + inputPort);
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
            return mMiLinkChannelClient.isMiLinkLogined();
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
        // 注： 这里手动init一下原因是因为MilinkChannelClient在账号登录成功后会手动logoff掉sessionManager会被置空，
        // 但是milink底层isInit并没有重置为false,因此sendDataBySimpleChannel里面根据isInit标记位为true不会调用
        //initUseChannelMode,因而sessionManager一直会为null，导致首次登陆成功，而之后登陆会失败（rsp == null）。
        //最好的是修改milink底层代码，重新build jar包，这里先手动初始化。
        mMiLinkChannelClient.initUseChannelMode();
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

    private static final String[] accessCommand = new String[]{
            MiLinkCommand.COMMAND_ACCOUNT_VERIFY_ASSISTANT,
            MiLinkCommand.COMMAND_LIVE_ROOM_INFO,
            MiLinkCommand.COMMAND_LIVE_ENTER,
            MiLinkCommand.COMMAND_LIVE_VIEWER_TOP,
            MiLinkCommand.COMMAND_LIVE_VIEWERINFO,
            MiLinkCommand.COMMAND_LIVE_ROOM_INFO_CHANGE,
            MiLinkCommand.COMMAND_LIVE_ROOM_TAG,
            MiLinkCommand.COMMAND_LIST_TAGLIVE,
            MiLinkCommand.COMMAND_RECOMMEND_ROOM,
            MiLinkCommand.COMMAND_PUSH_BARRAGE,
            MiLinkCommand.COMMAND_SYNC_SYSMSG,
            MiLinkCommand.COMMAND_PUSH_SYSMSG,
            MiLinkCommand.COMMAND_REPLAY_BARRAGE,
            MiLinkCommand.COMMAND_GET_USER_INFO_BY_ID,
            MiLinkCommand.COMMAND_GET_OWN_INFO,
            MiLinkCommand.COMMAND_GET_PERRSONAL_DATA,
            MiLinkCommand.COMMAND_GET_HOMEPAGE,
            MiLinkCommand.COMMAND_LOGIN,
            MiLinkCommand.COMMAND_GET_SERVICE_TOKEN,
            MiLinkCommand.COMMAND_ACCOUNT_XIAOMI_SSO_LOGIN,
            MiLinkCommand.COMMAND_GET_RANK_LIST_V2,
            MiLinkCommand.COMMAND_GET_CONFIG,
            MiLinkCommand.COMMAND_STAT_REPORT,
            MiLinkCommand.COMMAND_DELAY_REPORT,
            MiLinkCommand.COMMAND_IP_SELECT_QUERY,
            MiLinkCommand.COMMAND_ROOM_VIEWER,
            MiLinkCommand.COMMAND_GET_LIVE_ROOM,
            MiLinkCommand.COMMAND_PUSH_LOGLEVEL,
            MiLinkCommand.COMMAND_PUSH_GLOBAL_MSG,
            MiLinkCommand.COMMAND_EFFECT_GET,
            MiLinkCommand.COMMAND_PULL_ROOM_MESSAGE,
            MiLinkCommand.COMMAND_HOT_CHANNEL_LIST,
            MiLinkCommand.COMMAND_GIFT_GET_LIST,
            MiLinkCommand.COMMAND_LIVE_LEAVE,
            MiLinkCommand.COMMAND_ACCOUNT_3PARTSIGNLOGIN,
            MiLinkCommand.COMMAND_RECOMMEND_CHANNELLIST,
            MiLinkCommand.COMMAND_FACE_BEAUTY_PARAMS,
            MiLinkCommand.COMMAND_FEEDS_GET_FEED_INFO,
            MiLinkCommand.COMMAND_FEEDS_COMMENT_QUERY,
            MiLinkCommand.COMMAND_LIST_HISTORY,
            MiLinkCommand.COMMAND_STATISTICS_RECOMMEND_TAG,
            MiLinkCommand.COMMAND_HOT_CHANNEL_SUB_LIST,
            MiLinkCommand.COMMAND_LIST_CHANNEL,
            MiLinkCommand.COMMAND_ROOM_ATTACHMENT,
            MiLinkCommand.COMMAND_OPERATION_ANIM_RESOURCE,
            MiLinkCommand.COMMAND_GET_USER_LIST_BY_ID,
            MiLinkCommand.COMMAND_GAME_SUMMARY
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
                MiLinkClientIpc.logoff();
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
     *
     * @return
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

    public void setGlobalPushFlag(boolean enable) {
        MyLog.w(TAG, "setGlobalPushFlag enable=" + enable);
        MiLinkClientIpc.setGlobalPushFlag(enable);
    }
}
