package com.wali.live.watchsdk.videothird;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.SurfaceView;

import com.base.global.GlobalData;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.BaseSdkController;
import com.wali.live.watchsdk.R;
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
    protected SurfaceView mPlayerView;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public ThirdVideoController(@NonNull RoomBaseDataModel myRoomData) {
        mMyRoomData = myRoomData;
    }

    public void setupController(Context context) {
        if (mPlayerView == null) {
            mPlayerView = new SurfaceView(context);
            mPlayerView.setId(R.id.video_view);
        }
        mStreamerPresenter = new PullStreamerPresenter();
        IPlayer player = new GalileoPlayer(GlobalData.app(), UserAccountManager.getInstance().getUuid(),
                MiLinkClientAdapter.getsInstance().getClientIp());
        player.setDisplay(mPlayerView.getHolder());
        mStreamerPresenter.setStreamer(player);
        mStreamerPresenter.setOriginalStreamUrl(mMyRoomData.getVideoUrl());
        mStreamerPresenter.startWatch();
    }

    @Override
    public void release() {
        super.release();
        mStreamerPresenter.stopWatch();
        mStreamerPresenter.destroy();
    }
}
