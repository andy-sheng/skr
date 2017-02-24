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
 * @module 基础架构组件控制器
 */
public class ComponentController implements ComponentPresenter.IComponentController {
    private static final String TAG = "ComponentController";

    public static final int MSG_DEFAULT = 0;

    // 系统消息
    private static final int MSG_SYSTEM_FIRST = 10000;
    public static final int MSG_ON_BACK_PRESSED = MSG_SYSTEM_FIRST; // 返回键
    public static final int MSG_ON_ORIENTATION  = MSG_SYSTEM_FIRST + 1; // 转屏

    // UI消息
    private static final int MSG_LIVE_FIRST = 20000;
    // 输入框相关消息
    public static final int MSG_CTRL_INPUT_VIEW    = MSG_LIVE_FIRST; // 弹起/隐藏 输入框
    public static final int MSG_STAT_INPUT_VIEW    = MSG_LIVE_FIRST + 1; // 输入框 已显示/已隐藏
    public static final int MSG_CTRL_FLY_BARRAGE   = MSG_LIVE_FIRST + 2; // 显示/隐藏 飘屏弹幕开关
    // 底部面板相关消息
    public static final int MSG_SHOW_SETTING_PANEL = MSG_LIVE_FIRST + 3; // 显示 设置面板

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
    public boolean onEvent(int source, @Nullable ComponentPresenter.Params params) {
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
