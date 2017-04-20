package com.wali.live.livesdk.live.room;

import com.base.log.MyLog;
import com.mi.live.data.location.Location;
import com.wali.live.livesdk.live.room.view.IRoomView;
import com.wali.live.livesdk.live.room.presenter.RoomPresenter;

/**
 * Created by lan on 17/4/20.
 */
public class RoomManager {
    private static final String TAG = RoomManager.class.getSimpleName();

    private RoomPresenter mRoomPresenter;
    private IRoomView mView;

    public RoomManager(IRoomView view) {
        mView = view;
        mRoomPresenter = new RoomPresenter(view);
    }

    public void beginNormalLive(Location location, String title, String coverUrl) {
        MyLog.w(TAG, "beginNormalLive");
        mRoomPresenter.beginNormalLive(location, title, coverUrl);
    }

    public void beginGameLive(Location location, String title, String coverUrl) {
        MyLog.w(TAG, "beginGameLive");
        mRoomPresenter.beginGameLive(location, title, coverUrl);
    }
}
