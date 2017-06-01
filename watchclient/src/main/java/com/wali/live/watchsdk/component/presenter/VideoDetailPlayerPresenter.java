package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.dns.IStreamReconnect;
import com.wali.live.watchsdk.component.view.VideoDetailPlayerView;

/**
 * Created by zyh on 2017/05/31.
 *
 * @module 回放详情页的播放view的presenter
 */
public class VideoDetailPlayerPresenter extends ComponentPresenter<VideoDetailPlayerView.IView>
        implements VideoDetailPlayerView.IPresenter, IStreamReconnect {
    private static final String TAG = "VideoDetailPlayerPresenter";
    private RoomBaseDataModel mMyRoomData;

    public VideoDetailPlayerPresenter(@NonNull IComponentController componentController, RoomBaseDataModel myRoomData) {
        super(componentController);
        mMyRoomData = myRoomData;
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    @Override
    public void onDnsReady() {

    }

    @Override
    public boolean ipSelect() {
        return false;
    }

    @Override
    public void startReconnect(int code) {

    }

    public class Action implements ComponentPresenter.IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                default:
                    break;
            }
            return false;
        }
    }
}
