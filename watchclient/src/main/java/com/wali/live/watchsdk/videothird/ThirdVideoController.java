package com.wali.live.watchsdk.videothird;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.global.GlobalData;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.BaseSdkController;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.videodetail.presenter.VideoDetailPlayerPresenter;
import com.wali.live.watchsdk.videodetail.view.VideoDetailPlayerView;
import com.wali.live.watchsdk.videothird.data.PullStreamerPresenter;
import com.wali.live.watchsdk.videothird.data.engine.GalileoPlayer;
import com.wali.live.watchsdk.videothird.data.engine.IPlayer;

/**
 * Created by yangli on 2017/8/28.
 */
public class ThirdVideoController extends BaseSdkController {
    private static final String TAG = "ThirdVideoController";

    protected RoomBaseDataModel mMyRoomData;
    protected PullStreamerPresenter mStreamerPresenter;

//    protected VideoDetailPlayerView mPlayerView;
//    protected VideoDetailPlayerPresenter mPlayerPresenter;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public ThirdVideoController(@NonNull RoomBaseDataModel myRoomData) {
        mMyRoomData = myRoomData;
    }

    public void setupController(Context context) {
//        if (mPlayerView == null) {
//            mPlayerView = new VideoDetailPlayerView(context);
//            mPlayerView.setId(R.id.video_view);
//            mPlayerView.setMyRoomData(mMyRoomData);
//            mPlayerPresenter = new VideoDetailPlayerPresenter(this, mMyRoomData, (Activity) context);
//            mPlayerPresenter.setView(mPlayerView.getViewProxy());
//            mPlayerView.setPresenter(mPlayerPresenter);
//        }
//        mPlayerPresenter.startPresenter();

        mStreamerPresenter = new PullStreamerPresenter();
        IPlayer player = new GalileoPlayer(GlobalData.app(), UserAccountManager.getInstance().getUuid(),
                MiLinkClientAdapter.getsInstance().getClientIp());
        mStreamerPresenter.setStreamer(player);
        mStreamerPresenter.setOriginalStreamUrl(mMyRoomData.getVideoUrl());
    }

    @Override
    public void release() {
        super.release();
//        mPlayerPresenter.stopPresenter();
//        mPlayerPresenter.destroy();

        mStreamerPresenter.stopWatch();
        mStreamerPresenter.destroy();
    }
}
