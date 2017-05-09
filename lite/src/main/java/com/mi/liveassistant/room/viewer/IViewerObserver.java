package com.mi.liveassistant.room.viewer;

import com.mi.liveassistant.data.model.Viewer;
import com.mi.liveassistant.room.viewer.callback.IViewerListener;

import java.util.List;

/**
 * Created by lan on 17/5/8.
 */
public interface IViewerObserver {
    void registerListener(IViewerRegister register, IViewerListener listener);

    void observerOnList(List<Viewer> viewers);
}
