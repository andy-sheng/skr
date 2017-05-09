package com.mi.liveassistant.room.presenter.live;

import android.support.annotation.NonNull;

import com.mi.liveassistant.room.constant.LiveRoomType;
import com.mi.liveassistant.room.view.ILiveView;

import component.IEventController;

/**
 * Created by chenyong on 2016/11/22.
 */
public class NormalLivePresenter extends BaseLivePresenter {

    public NormalLivePresenter(@NonNull IEventController controller, ILiveView view) {
        super(controller, view);
        mLiveRoomType = LiveRoomType.TYPE_LIVE_PUBLIC;
    }

}
