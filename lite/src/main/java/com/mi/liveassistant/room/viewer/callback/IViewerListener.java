package com.mi.liveassistant.room.viewer.callback;

import com.mi.liveassistant.data.model.Viewer;

import java.util.List;

/**
 * Created by lan on 17/5/4.
 */
public interface IViewerListener {
    void update(List<Viewer> viewers);
}
