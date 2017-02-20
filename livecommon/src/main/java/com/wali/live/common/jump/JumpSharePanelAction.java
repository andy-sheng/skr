package com.wali.live.common.jump;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.base.activity.BaseActivity;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.jump.model.JumpFloatHomePageBean;

/**
 * Created by chengsimin on 16/9/18.
 */
public interface JumpSharePanelAction {
    void showSharePanel(Activity activity, ViewGroup parentView,String shareUrl, RoomBaseDataModel roomBaseDataModel,int clickBtn_X);

    void hideSharePanel();

    boolean isSharePanelShow();
}
