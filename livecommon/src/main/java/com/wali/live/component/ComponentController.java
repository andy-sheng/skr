package com.wali.live.component;

import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by yangli on 2017/2/17.
 *
 * @module 基础架构组件控制器
 */
public abstract class ComponentController implements ComponentPresenter.IComponentController {
    protected final String TAG = getTAG();

    public static final int MSG_DEFAULT = 0;

    // 系统消息
    private static final int MSG_SYSTEM_FIRST = 10000;
    public static final int MSG_ON_BACK_PRESSED = MSG_SYSTEM_FIRST;     // 返回键
    public static final int MSG_ON_ORIENT_PORTRAIT = MSG_SYSTEM_FIRST + 1;  // 竖屏
    public static final int MSG_ON_ORIENT_LANDSCAPE = MSG_SYSTEM_FIRST + 2; // 横屏
    public static final int MSG_ON_ACTIVITY_RESUMED = MSG_SYSTEM_FIRST + 3; // APP回到前台

    // 推拉流相关消息
    private static final int MSG_STREAM_FIRST = 11000;
    public static final int MSG_END_LIVE_UNEXPECTED = MSG_STREAM_FIRST;         // 异常结束直播/观看
    public static final int MSG_END_LIVE_FOR_TIMEOUT = MSG_STREAM_FIRST + 1;    // 长时间退到后台后结束直播
    public static final int MSG_OPEN_CAMERA_FAILED = MSG_STREAM_FIRST + 2;      // 打开相机失败
    public static final int MSG_OPEN_MIC_FAILED = MSG_STREAM_FIRST + 3;         // 打开麦克风失败
    public static final int MSG_ON_STREAM_SUCCESS = MSG_STREAM_FIRST + 4;       // 推/拉流成功
    public static final int MSG_ON_STREAM_RECONNECT = MSG_STREAM_FIRST + 5;     // 开始重连
    public static final int MSG_ON_LIVE_SUCCESS = MSG_STREAM_FIRST + 6;         // 开房间/进房间成功

    // UI消息
    // 复合消息(多个View同时响应的消息)
    private static final int MSG_COMPOUND_FIRST = 20000;
    public static final int MSG_INPUT_VIEW_SHOWED = MSG_COMPOUND_FIRST;             // 输入框 已显示
    public static final int MSG_INPUT_VIEW_HIDDEN = MSG_COMPOUND_FIRST + 1;         // 输入框 已隐藏
    public static final int MSG_BOTTOM_POPUP_SHOWED = MSG_COMPOUND_FIRST + 2;       // 底部按钮/礼物页面等显示时，通知 底部按钮和弹幕区 隐藏
    public static final int MSG_BOTTOM_POPUP_HIDDEN = MSG_COMPOUND_FIRST + 3;       // 底部按钮/礼物页面等隐藏时，通知 底部按钮和弹幕区 显示
    // 触摸相关消息
    private static final int MSG_TOUCH_FIRST = 21000;
    public static final int MSG_ENABLE_MOVE_VIEW = MSG_TOUCH_FIRST;     // 开启滑动
    public static final int MSG_DISABLE_MOVE_VIEW = MSG_TOUCH_FIRST + 1; // 禁止滑动
    public static final int MSG_BACKGROUND_CLICK = MSG_TOUCH_FIRST + 2; // 背景点击
    // 输入框相关消息
    private static final int MSG_INPUT_FIRST = 30000;
    public static final int MSG_SHOW_INPUT_VIEW = MSG_INPUT_FIRST;     // 请求弹起 输入框
    public static final int MSG_HIDE_INPUT_VIEW = MSG_INPUT_FIRST + 1; // 请求隐藏 输入框
    public static final int MSG_SHOW_GAME_INPUT = MSG_INPUT_FIRST + 2; // 请求显示 游戏输入框
    public static final int MSG_HIDE_GAME_INPUT = MSG_INPUT_FIRST + 3; // 请求隐藏 游戏输入框
    public static final int MSG_SHOW_BARRAGE_SWITCH = MSG_INPUT_FIRST + 4; // 显示 飘屏弹幕开关
    public static final int MSG_HIDE_BARRAGE_SWITCH = MSG_INPUT_FIRST + 5; // 隐藏 飘屏弹幕开关
    public static final int MSG_SHOW_GAME_BARRAGE = MSG_INPUT_FIRST + 6; // 显示 游戏弹幕
    public static final int MSG_HIDE_GAME_BARRAGE = MSG_INPUT_FIRST + 7; // 隐藏 游戏弹幕
    // 弹出页面相关消息
    private static final int MSG_POPUP_FIRST = 31000;
    public static final int MSG_SHOW_SETTING_PANEL = MSG_POPUP_FIRST;           // 显示 设置面板
    public static final int MSG_SHOW_MAGIC_PANEL = MSG_POPUP_FIRST + 1;         // 显示 美妆面板
    public static final int MSG_SHOW_PLUS_PANEL = MSG_POPUP_FIRST + 2;          // 显示 直播加面板
    public static final int MSG_SHOW_GIFT_PANEL = MSG_POPUP_FIRST + 3;          // 显示 礼物面板
    public static final int MSG_HIDE_BOTTOM_PANEL = MSG_POPUP_FIRST + 4;        // 隐藏 底部面板
    public static final int MSG_SHOW_ATMOSPHERE_VIEW = MSG_POPUP_FIRST + 5;     // 显示 氛围面板
    public static final int MSG_SHOE_GAME_ICON = MSG_POPUP_FIRST + 6;           // 展示 游戏中心Icon
    public static final int MSG_SHOW_GAME_DOWNLOAD = MSG_POPUP_FIRST + 7;       // 展示 游戏中心下载框
    public static final int MSG_SHOW_SHARE_PANEL = MSG_POPUP_FIRST + 8;         // 显示 分享面板
    public static final int MSG_SHOW_PERSONAL_INFO = MSG_POPUP_FIRST + 9;       // 显示 个人信息页
    public static final int MSG_SHOW_FOLLOW_GUIDE = MSG_POPUP_FIRST + 10;       // 显示 游戏引导页面
    public static final int MSG_FOLLOW_COUNT_DOWN = MSG_POPUP_FIRST + 11;       // 显示 游戏引导页面之前的倒计时

    // 详情播放相关
    private static final int MSG_DETAIL_VIDEO_FIRST = 40000;
    public static final int MSG_UPDATE_LIKE_STATUS = MSG_DETAIL_VIDEO_FIRST; // 更新 点赞状态
    public static final int MSG_COMMENT_TOTAL_CNT = MSG_DETAIL_VIDEO_FIRST + 1; // 更新 评论总数
    public static final int MSG_REPLAY_TOTAL_CNT = MSG_DETAIL_VIDEO_FIRST + 2;  // 更新 回放总数
    public static final int MSG_SHOW_COMMENT_INPUT = MSG_DETAIL_VIDEO_FIRST + 3;  // 回复 评论
    public static final int MSG_SEND_COMMENT = MSG_DETAIL_VIDEO_FIRST + 4;  // 发送 评论
    public static final int MSG_FOLD_INFO_AREA = MSG_DETAIL_VIDEO_FIRST + 5;  // 收起 信息区
    public static final int MSG_NEW_DETAIL_REPLAY = MSG_DETAIL_VIDEO_FIRST + 6;  // 点击回放每一条
    public static final int MSG_COMPLETE_USER_INFO = MSG_DETAIL_VIDEO_FIRST + 7;  // 点击回放每一条
    public static final int MSG_UPDATE_START_TIME = MSG_DETAIL_VIDEO_FIRST + 8; // 更新 回放的录制时间(用于拉取房间消息/弹幕)
    public static final int MSG_PLAYER_ROTATE_ORIENTATION = MSG_DETAIL_VIDEO_FIRST + 9; //方向变换事件

    public static final int MSG_PLAYER_FEEDS_DETAIL = MSG_DETAIL_VIDEO_FIRST + 10;


    // 播放器相关消息
    private static final int MSG_PLAYER_FIRST = 41000;
    public static final int MSG_PLAYER_FULL_SCREEN = MSG_PLAYER_FIRST;
    public static final int MSG_PLAYER_DETAIL_SCREEN = MSG_PLAYER_FIRST + 1;
    public static final int MSG_PLAYER_START = MSG_PLAYER_FIRST + 2;
    public static final int MSG_PLAYER_PAUSE = MSG_PLAYER_FIRST + 3;

    private static final int MSG_MORE_FIRST = 90000;
    public static final int MSG_FORCE_ROTATE_SCREEN = MSG_MORE_FIRST + 1; // 强制旋转UI

    private final Map<Integer, Set<ComponentPresenter.IAction>> mEventActionMap = new HashMap<>();

    @Nullable
    protected abstract String getTAG();

    public void release() {
        mEventActionMap.clear();
    }

    @Override
    public void registerAction(int event, @Nullable ComponentPresenter.IAction action) {
        if (action == null) {
            MyLog.e(TAG, "registerAction but action is null for event=" + event);
            return;
        }
        Set<ComponentPresenter.IAction> actionSet = mEventActionMap.get(event);
        if (actionSet == null) {
            actionSet = new LinkedHashSet<>(); // 保序
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
