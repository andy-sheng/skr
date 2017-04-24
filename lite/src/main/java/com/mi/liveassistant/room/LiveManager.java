package com.mi.liveassistant.room;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.data.Location;
import com.mi.liveassistant.proto.LiveCommonProto;
import com.mi.liveassistant.room.callback.ICallback;
import com.mi.liveassistant.room.presenter.LivePresenter;
import com.mi.liveassistant.room.view.ILiveView;

import java.util.List;

/**
 * Created by lan on 17/4/20.
 */
public class LiveManager implements ILiveView {
    private static final String TAG = RoomConstant.LOG_PREFIX + LiveManager.class.getSimpleName();

    private LivePresenter mLivePresenter;
    private String mLiveId;

    private ICallback mOutBeginCallback;
    private ICallback mOutEndCallback;

    public LiveManager() {
        mLivePresenter = new LivePresenter(this);
    }

    @Override
    public void beginNormalLive(Location location, String title, String coverUrl, ICallback callback) {
        MyLog.w(TAG, "beginNormalLive");
        mOutBeginCallback = callback;
        mLivePresenter.beginNormalLive(location, title, coverUrl);
    }

    @Override
    public void beginGameLive(Location location, String title, String coverUrl, ICallback callback) {
        MyLog.w(TAG, "beginGameLive");
        mOutBeginCallback = callback;
        mLivePresenter.beginGameLive(location, title, coverUrl);
    }

    @Override
    public void notifyBeginLiveFail(int errCode) {
        MyLog.d(TAG, "notifyBeginLiveFail errCode=" + errCode);
        if (mOutBeginCallback != null) {
            mOutBeginCallback.notifyFail(errCode);
        }
    }

    @Override
    public void notifyBeginLiveSuccess(String liveId, List<LiveCommonProto.UpStreamUrl> upStreamUrlList, String udpUpStreamUrl) {
        MyLog.d(TAG, "notifyBeginLiveSuccess liveId=" + liveId);
        mLiveId = liveId;
        if (mOutBeginCallback != null) {
            mOutBeginCallback.notifySuccess();
        }
    }

    @Override
    public void endLive(ICallback callback) {
        MyLog.w(TAG, "endLive");
        mOutEndCallback = callback;
        mLivePresenter.endLive(mLiveId);
    }

    @Override
    public void notifyEndLiveFail(int errCode) {
        MyLog.d(TAG, "notifyEndLiveFail errCode=" + errCode);
        if (mOutEndCallback != null) {
            mOutEndCallback.notifyFail(errCode);
        }
    }

    @Override
    public void notifyEndLiveSuccess() {
        MyLog.d(TAG, "notifyEndLiveSuccess");
        if (mOutEndCallback != null) {
            mOutEndCallback.notifySuccess();
        }
    }
}
