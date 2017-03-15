package com.wali.live.livesdk.live.liveshow;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.fragment.FragmentDataListener;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.engine.streamer.GalileoStreamer;
import com.mi.live.engine.streamer.IStreamer;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.BaseSdkView;
import com.wali.live.livesdk.live.component.BaseLiveController;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.liveshow.data.MagicParamPresenter;
import com.wali.live.livesdk.live.liveshow.fragment.PrepareLiveFragment;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 组件控制器, 游戏直播
 */
public class LiveComponentController extends BaseLiveController {
    private static final String TAG = "LiveComponentController";

    @NonNull
    protected RoomBaseDataModel mMyRoomData; // 房间数据
    @NonNull
    protected LiveRoomChatMsgManager mRoomChatMsgManager; // 房间弹幕管理
    @NonNull
    protected StreamerPresenter mStreamerPresenter; // 推流器
    @NonNull
    protected MagicParamPresenter mMagicParamPresenter; // 美妆参数拉取

    @Nullable
    @Override
    protected String getTAG() {
        return TAG;
    }

    public LiveComponentController(
            @NonNull RoomBaseDataModel myRoomData,
            @NonNull LiveRoomChatMsgManager roomChatMsgManager) {
        mMyRoomData = myRoomData;
        mRoomChatMsgManager = roomChatMsgManager;

        mMagicParamPresenter = new MagicParamPresenter(this, GlobalData.app());
        mStreamerPresenter = new StreamerPresenter(this);
    }

    @Override
    public void enterPreparePage(
            @NonNull BaseComponentSdkActivity fragmentActivity,
            int requestCode,
            FragmentDataListener listener) {
        MyLog.w(TAG, "prepareShowLive");
        PrepareLiveFragment.openFragment(fragmentActivity, requestCode, listener, mStreamerPresenter);
        mRoomChatMsgManager.setIsGameLiveMode(false);
    }

    @Override
    public IStreamer createStreamer(int width, int height, boolean hasMicSource) {
        IStreamer streamer = new GalileoStreamer(GlobalData.app(),
                UserAccountManager.getInstance().getUuid(), width, height, hasMicSource);
        mStreamerPresenter.setStreamer(streamer);
        return streamer;
    }

    @Override
    public BaseSdkView createSdkView(Activity activity) {
        return new LiveSdkView(activity, this);
    }
}
