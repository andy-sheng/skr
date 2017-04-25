package com.mi.liveassistant.room.view;

import com.mi.liveassistant.common.mvp.IView;

/**
 * Created by lan on 17/4/24.
 */
public interface ILifecycleView extends IView {
    void pause();

    void resume();
}
