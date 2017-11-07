package com.wali.live.watchsdk.videothird;

import android.content.Context;
import android.support.annotation.NonNull;

import com.base.global.GlobalData;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.engine.player.engine.GalileoPlayer;
import com.wali.live.component.BaseSdkController;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.videothird.data.ThirdStreamerPresenter;
import com.wali.live.watchsdk.videothird.presenter.ThirdPlayerPresenter;
import com.wali.live.watchsdk.videothird.view.ThirdPlayerView;

/**
 * Created by yangli on 2017/8/28.
 */
public class ThirdVideoController extends BaseSdkController {
    private static final String TAG = "ThirdVideoController";

    protected RoomBaseDataModel mMyRoomData;

    protected ThirdStreamerPresenter mStreamerPresenter;
    protected ThirdPlayerPresenter mPlayerPresenter;
    protected ThirdPlayerView mPlayerView;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public ThirdVideoController(@NonNull RoomBaseDataModel myRoomData) {
        mMyRoomData = myRoomData;
    }

    public void setupController(Context context) {
        mPlayerView = new ThirdPlayerView(context);
        mPlayerView.setId(R.id.video_view);

        mStreamerPresenter = new ThirdStreamerPresenter(new WatchComponentController.EventPlayerCallback(this));
        mStreamerPresenter.setIsRealTime(false);
        GalileoPlayer player = new GalileoPlayer(GlobalData.app(), UserAccountManager.getInstance().getUuid(),
                MiLinkClientAdapter.getsInstance().getClientIp());
        player.setCallback(mStreamerPresenter.getInnerPlayerCallback());
        mStreamerPresenter.setStreamer(player);

        mPlayerPresenter = new ThirdPlayerPresenter(this, mStreamerPresenter);
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
