package com.wali.live.livesdk.live.liveshow;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.fragment.FragmentDataListener;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.BaseSdkView;
import com.wali.live.livesdk.live.component.BaseLiveController;
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

    // 美妆参数拉取
    protected MagicParamPresenter mMagicParamPresenter =
            new MagicParamPresenter(this, GlobalData.app());

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
    }

    @Override
    public void enterPreparePage(
            @NonNull BaseComponentSdkActivity fragmentActivity,
            int requestCode,
            FragmentDataListener listener) {
        MyLog.w(TAG, "prepareShowLive");
        PrepareLiveFragment.openFragment(fragmentActivity, requestCode, listener, mMagicParamPresenter);
        mRoomChatMsgManager.setIsGameLiveMode(false);
    }

    @Override
    public BaseSdkView createSdkView(Activity activity) {
        return new LiveSdkView(activity, this);
    }
}
