package com.mi.live.room;

import com.base.log.MyLog;
import com.mi.live.data.location.Location;
import com.mi.live.room.presenter.LivePresenter;
import com.mi.live.room.view.ILiveView;
import com.wali.live.proto.LiveCommonProto;

import java.util.List;

/**
 * Created by lan on 17/4/20.
 */
public class LiveManager implements ILiveView {
    private static final String TAG = RoomConstant.LOG_PREFIX + LiveManager.class.getSimpleName();

    private LivePresenter mLivePresenter;

    private String mLiveId;

    public LiveManager() {
        mLivePresenter = new LivePresenter(this);
    }

    @Override
    public void beginNormalLive(Location location, String title, String coverUrl) {
        MyLog.w(TAG, "beginNormalLive");
        mLivePresenter.beginNormalLive(location, title, coverUrl);
    }

    @Override
    public void beginGameLive(Location location, String title, String coverUrl) {
        MyLog.w(TAG, "beginGameLive");
        mLivePresenter.beginGameLive(location, title, coverUrl);
    }

    @Override
    public void notifyBeginLiveFail(int errCode) {
        MyLog.d(TAG, "notifyBeginLiveFail errCode=" + errCode);
    }

    @Override
    public void notifyBeginLiveSuccess(String liveId, List<LiveCommonProto.UpStreamUrl> upStreamUrlList, String udpUpStreamUrl) {
        MyLog.d(TAG, "notifyBeginLiveSuccess liveId=" + liveId);
        mLiveId = liveId;
    }

    @Override
    public void endLive() {
        MyLog.w(TAG, "endLive");
        mLivePresenter.endLive(mLiveId);
    }

    @Override
    public void notifyEndLiveFail(int errCode) {
        MyLog.d(TAG, "notifyEndLiveFail errCode=" + errCode);
    }

    @Override
    public void notifyEndLiveSuccess() {
        MyLog.d(TAG, "notifyEndLiveSuccess");
    }
}
