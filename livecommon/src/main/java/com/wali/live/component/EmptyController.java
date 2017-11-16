package com.wali.live.component;

import com.thornbirds.component.IEventController;
import com.thornbirds.component.IEventObserver;
import com.thornbirds.component.IParams;

/**
 * Created by yangli on 2017/11/15.
 */
public final class EmptyController implements IEventController {
    @Override
    public void registerObserverForEvent(int event, IEventObserver observer) {
    }

    @Override
    public void unregisterObserverForEvent(int event, IEventObserver observer) {
    }

    @Override
    public void unregisterObserver(IEventObserver observer) {
    }

    @Override
    public boolean postEvent(int event) {
        return false;
    }

    @Override
    public boolean postEvent(int event, IParams params) {
        return false;
    }

    @Override
    public void release() {
    }
}
