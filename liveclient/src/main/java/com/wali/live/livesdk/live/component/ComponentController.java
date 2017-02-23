package com.wali.live.livesdk.live.component;

import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.livesdk.live.component.presenter.ComponentPresenter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by yangli on 2017/2/17.
 *
 * @module
 */
public class ComponentController implements ComponentPresenter.IComponentController {
    private static final String TAG = "ComponentController";

    public static final int MSG_DEFAULT = 0;

    private static final int MSG_SYSTEM_FIRST = 10000;
    public static final int MSG_ON_BACK_PRESSED = MSG_SYSTEM_FIRST;
    public static final int MSG_ON_ORIENTATION = MSG_SYSTEM_FIRST + 1;


    private static final int MSG_LIVE_FIRST = 20000;
    public static final int MSG_SHOW_INPUT_VIEW = MSG_LIVE_FIRST;
    public static final int MSG_SHOW_SETTING_PANEL = MSG_LIVE_FIRST + 1;

    private final Map<Integer, Set<ComponentPresenter.IAction>> mEventActionMap = new HashMap<>();

    @Override
    public void registerAction(int event, @Nullable ComponentPresenter.IAction action) {
        if (action == null) {
            return;
        }
        Set<ComponentPresenter.IAction> actionSet = mEventActionMap.get(event);
        if (actionSet == null) {
            actionSet = new HashSet<>();
            mEventActionMap.put(event, actionSet);
        }
        actionSet.add(action);
    }

    @Override
    public void unregisterAction(int event, @Nullable ComponentPresenter.IAction action) {
        if (action == null) {
            return;
        }
        Set<ComponentPresenter.IAction> actionSet = mEventActionMap.get(event);
        if (actionSet != null) {
            actionSet.remove(action);
        }
    }

    @Override
    public void unregisterAction(ComponentPresenter.IAction action) {
        if (action == null) {
            return;
        }
        for (Set<ComponentPresenter.IAction> actionSet : mEventActionMap.values()) {
            actionSet.remove(action);
        }
    }

    @Override
    public boolean onEvent(int source) {
        return onEvent(source, null);
    }

    @Override
    public boolean onEvent(int source, ComponentPresenter.Params params) {
        Set<ComponentPresenter.IAction> actionSet = mEventActionMap.get(source);
        if (actionSet == null || actionSet.isEmpty()) {
            MyLog.e(TAG, "no action registered for source " + source);
            return false;
        }
        boolean result = false;
        for (ComponentPresenter.IAction action : actionSet) {
            result |= action.onAction(source, params);
        }
        return result;
    }

}
