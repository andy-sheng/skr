package com.wali.live.component;

import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;

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
    public static final int MSG_ON_ORIENTATION  = MSG_SYSTEM_FIRST + 1; // 系统转屏

    // UI消息
    private static final int MSG_LIVE_FIRST = 20000;
    // 输入框相关消息
    public static final int MSG_SHOW_INPUT_VIEW     = MSG_LIVE_FIRST;     // 请求弹起输入框
    public static final int MSG_HIDE_INPUT_VIEW     = MSG_LIVE_FIRST + 1; // 请求隐藏输入框
    public static final int MSG_INPUT_VIEW_SHOWED   = MSG_LIVE_FIRST + 2; // 输入框 已显示
    public static final int MSG_INPUT_VIEW_HIDDEN   = MSG_LIVE_FIRST + 3; // 输入框 已隐藏
    public static final int MSG_SHOW_BARRAGE_SWITCH = MSG_LIVE_FIRST + 4; // 显示 飘屏弹幕开关
    public static final int MSG_HIDE_BARRAGE_SWITCH = MSG_LIVE_FIRST + 5; // 隐藏 飘屏弹幕开关
    // 弹出页面相关消息
    public static final int MSG_BOTTOM_POPUP_SHOWED = MSG_LIVE_FIRST + 6; // 底部按钮/礼物页面等显示时，通知 底部按钮和弹幕区 隐藏
    public static final int MSG_BOTTOM_POPUP_HIDDEN = MSG_LIVE_FIRST + 7; // 底部按钮/礼物页面等隐藏时，通知 底部按钮和弹幕区 显示
    public static final int MSG_SHOW_SETTING_PANEL  = MSG_LIVE_FIRST + 8; // 显示 设置面板
    public static final int MSG_SHOW_GIFT_PANEL     = MSG_LIVE_FIRST + 9; // 显示 礼物面板

    private final Map<Integer, Set<ComponentPresenter.IAction>> mEventActionMap = new HashMap<>();

    @Override
    public void registerAction(int event, @Nullable ComponentPresenter.IAction action) {
        if (action == null) {
            MyLog.e(TAG, "registerAction but action is null for event=" + event);
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
