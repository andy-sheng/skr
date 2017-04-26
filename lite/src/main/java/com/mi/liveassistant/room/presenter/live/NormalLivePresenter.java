package com.mi.liveassistant.room.presenter.live;

import com.mi.liveassistant.room.constant.LiveRoomType;
import com.mi.liveassistant.room.view.ILiveView;

/**
 * Created by chenyong on 2016/11/22.
 */
public class NormalLivePresenter extends BaseLivePresenter {
    public NormalLivePresenter(ILiveView view) {
        super(view);
        mLiveRoomType = LiveRoomType.TYPE_LIVE_PUBLIC;
    }
}
