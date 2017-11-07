package com.wali.live.watchsdk.videodetail;

import android.content.Context;
import android.support.annotation.NonNull;

import com.base.global.GlobalData;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.engine.player.engine.GalileoPlayer;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.BaseSdkController;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.videodetail.data.PullStreamerPresenter;
import com.wali.live.watchsdk.videodetail.presenter.DetailPlayerPresenter;
import com.wali.live.watchsdk.videodetail.view.DetailPlayerView;

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

    @Override
    protected final String getTAG() {
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

        mStreamerPresenter = new PullStreamerPresenter(new WatchComponentController.EventPlayerCallback(this));
        mStreamerPresenter.setIsRealTime(false);
        GalileoPlayer player = new GalileoPlayer(GlobalData.app(), UserAccountManager.getInstance().getUuid(),
                MiLinkClientAdapter.getsInstance().getClientIp());
        player.setCallback(mStreamerPresenter.getInnerPlayerCallback());
        mStreamerPresenter.setStreamer(player);

        mPlayerPresenter = new DetailPlayerPresenter(this, mStreamerPresenter);
        mPlayerPresenter.setView(mPlayerView.getViewProxy());
        mPlayerView.setPresenter(mPlayerPresenter);
        mPlayerPresenter.startPresenter();
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
