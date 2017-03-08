package com.wali.live.livesdk.live.component;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.base.fragment.FragmentDataListener;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.BaseSdkView;
import com.wali.live.component.ComponentController;
import com.wali.live.livesdk.live.LiveSdkActivity;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 直播组件控制器基类
 */
public abstract class BaseLiveController extends ComponentController {

    /**
     * 进入准备页
     */
    public abstract void enterPreparePage(
            BaseComponentSdkActivity fragmentActivity,
            int requestCode,
            FragmentDataListener listener);

    /**
     * 创建SdkView
     */
    public abstract BaseSdkView createSdkView(Activity activity);
}
