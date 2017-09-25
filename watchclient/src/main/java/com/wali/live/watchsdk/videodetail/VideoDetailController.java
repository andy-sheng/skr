package com.wali.live.watchsdk.videodetail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.BaseSdkController;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.videodetail.presenter.DetailPlayerPresenter;
import com.wali.live.watchsdk.videodetail.view.DetailPlayerView;
import com.wali.live.watchsdk.videothird.data.PullStreamerPresenter;
import com.wali.live.watchsdk.videothird.data.engine.GalileoPlayer;

/**
 * Created by yangli on 2017/5/26.
 */
public class VideoDetailController extends BaseSdkController {
    private static final String TAG = "VideoDetailController";

    protected RoomBaseDataModel mMyRoomData;
    protected LiveRoomChatMsgManager mRoomChatMsgManager; // 房间弹幕管理

    protected PullStreamerPresenter mStreamerPresenter;
    protected DetailPlayerPresenter mPlayerPresenter;
    protected DetailPlayerView mPlayerView;

    @Nullable
    @Override
    protected String getTAG() {
        return TAG;
    }

    public VideoDetailController(
            @NonNull RoomBaseDataModel myRoomData,
            @NonNull LiveRoomChatMsgManager roomChatMsgManager) {
        mMyRoomData = myRoomData;
        mRoomChatMsgManager = roomChatMsgManager;
    }

    public void setupController(Context context) {
        mPlayerView = new DetailPlayerView(context);
        mPlayerView.setId(R.id.video_view);

        mStreamerPresenter = new PullStreamerPresenter(this);
        mStreamerPresenter.setIsRealTime(false);
        GalileoPlayer player = new GalileoPlayer(GlobalData.app(), UserAccountManager.getInstance().getUuid(),
                MiLinkClientAdapter.getsInstance().getClientIp());
        player.setCallback(mStreamerPresenter.getPlayerCallback());
        mStreamerPresenter.setStreamer(player);

        mPlayerPresenter = new DetailPlayerPresenter(this, mStreamerPresenter, mMyRoomData);
        mPlayerPresenter.setView(mPlayerView.getViewProxy());
        mPlayerView.setPresenter(mPlayerPresenter);
        mPlayerPresenter.startPresenter();

        // 发送事件，通知可以播放
        if (!TextUtils.isEmpty(mMyRoomData.getVideoUrl())) {
            postEvent(MSG_PLAYER_START);
        }
    }

    @Override
    public void release() {
        super.release();

        mPlayerPresenter.stopPresenter();
        mPlayerPresenter.destroy();

        mStreamerPresenter.stopWatch();
        mStreamerPresenter.destroy();
    }
}
