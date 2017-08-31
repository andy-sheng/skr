package com.wali.live.watchsdk.videothird;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.BaseSdkController;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.videodetail.view.VideoDetailPlayerView;
import com.wali.live.watchsdk.videodetail.presenter.VideoDetailPlayerPresenter;

/**
 * Created by yangli on 2017/8/28.
 */
public class ThirdVideoController extends BaseSdkController {
    private static final String TAG = "ThirdVideoController";

    protected RoomBaseDataModel mMyRoomData;

    protected VideoDetailPlayerView mPlayerView;
    protected VideoDetailPlayerPresenter mPlayerPresenter;

    @Nullable
    @Override
    protected String getTAG() {
        return TAG;
    }

    public ThirdVideoController(@NonNull RoomBaseDataModel myRoomData) {
        mMyRoomData = myRoomData;
    }

    public void setupController(Context context) {
        if (mPlayerView == null) {
            mPlayerView = new VideoDetailPlayerView(context);
            mPlayerView.setId(R.id.video_view);
            mPlayerView.setMyRoomData(mMyRoomData);
            mPlayerPresenter = new VideoDetailPlayerPresenter(this, mMyRoomData, (Activity) context);
            mPlayerPresenter.setView(mPlayerView.getViewProxy());
            mPlayerView.setPresenter(mPlayerPresenter);
        }
        mPlayerPresenter.startPresenter();
    }

    @Override
    public void release() {
        super.release();
        mPlayerPresenter.stopPresenter();
        mPlayerPresenter.destroy();
    }
}
