package com.wali.live.livesdk.live.component.data;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mi.live.engine.streamer.IStreamer;
import com.wali.live.component.presenter.ComponentPresenter;

/**
 * Created by yangli on 2017/03/08.
 * <p>
 * Generated using create_component_data.py
 *
 * @module 推流器数据
 */
public class StreamerPresenter extends ComponentPresenter {
    private static final String TAG = "StreamerPresenter";

    private IStreamer mStreamer;

    public void setStreamer(IStreamer streamer) {
        mStreamer = streamer;
    }

    public StreamerPresenter(
            @NonNull IComponentController componentController) {
        super(componentController);
    }

    @Override
    public void destroy() {
        super.destroy();
        mStreamer = null;
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            switch (source) {
                default:
                    break;
            }
            return false;
        }
    }
}
