package com.mi.liveassistant.milink.callback;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.milink.event.MiLinkEvent;
import com.mi.milink.sdk.client.MiLinkObserver;
import com.mi.milink.sdk.data.Const;

import org.greenrobot.eventbus.EventBus;

import java.util.Observable;

/**
 * 此类负责监听mlink的状态
 * Created by MK on 15-3-30.
 */
public class MiLinkStatusObserver extends MiLinkObserver {
    protected final String TAG = getTAG();

    protected int mConnectState;    //当前连接状态
    protected int mLoginState;      //当前登录状态

    public MiLinkStatusObserver() {
        mConnectState = Const.SessionState.Disconnected;
        mLoginState = Const.LoginState.NotLogin;
    }

    protected String getTAG() {
        return "MLinkStatusObserver";
    }

    /**
     * 事件：MNS服务请求重启 <br>
     * <br>
     * <i> "Hello, Client. I wanna play a game. In the past 12 hours, you get
     * tickets and transfer data via the Mns Service. You never care the state
     * of the Service, and assume Service's guilt ... Now you've got a occasion
     * to reset all these. The Process Id of the Service is provided as
     * parameter. You can kill her process by
     * {@code android.os.Process.killProcess()} or give her a chance to proceed
     * working —— <b>Live or Die, make your choice." <b></i>
     *
     * @param servicePid MNS服务的进程ID
     */
    @Override
    public void onSuicideTime(int servicePid) {

    }

    /**
     * 事件：全局错误，主要是服务器3000错误 <br>
     * <br>
     *
     * @param errCode 错误码
     * @param errMsg  错误信息
     */
    @Override
    public void onInternalError(int errCode, String errMsg) {
    }

    /**
     * 事件：服务初始化成功 <br>
     * <br>
     *
     * @param timePoint 初始化完成时间点，单位ms
     * @see System#currentTimeMillis()
     */
    @Override
    public void onServiceConnected(long timePoint) {
        MyLog.w(TAG + "onServiceConnected");
        // service绑定成功了，初始化一下，保证能连就连上
//        MiLinkClientAdapter.getsInstance().initCallBack();
    }

    /**
     * 服务器连接状态更新
     *
     * @param oldState 参考Const.ServerState Connected = 2 Connecting = 1 Disconnected = 0;
     * @param newState 参考Const.ServerState
     */
    @Override
    public void onServerStateUpdate(int oldState, int newState) {
        MyLog.w(TAG + " onServerStateUpdate, oldState=" + oldState + ", newState=" + newState);
        mConnectState = newState;
        if (mConnectState == Const.SessionState.Connected) {
            EventBus.getDefault().post(new MiLinkEvent.StatusConnected());
        } else if (mConnectState == Const.SessionState.Disconnected) {
            EventBus.getDefault().post(new MiLinkEvent.StatusDisConnected());
        }
    }


    @Override
    public void onLoginStateUpdate(int i) {
        MyLog.w(TAG + " onLoginStateUpdate ,i=" + i);
        mLoginState = i;
        if (mLoginState == Const.LoginState.NotLogin) {
            EventBus.getDefault().post(new MiLinkEvent.StatusNotLogin());
        } else if (mLoginState == Const.LoginState.Logined) {
            EventBus.getDefault().post(new MiLinkEvent.StatusLogined());
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        MyLog.w(TAG + " update ,data=" + data);
        super.update(observable, data);
    }
}
