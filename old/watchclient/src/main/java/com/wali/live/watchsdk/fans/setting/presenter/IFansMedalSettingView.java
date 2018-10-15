package com.wali.live.watchsdk.fans.setting.presenter;

import com.base.mvp.IRxView;

import java.util.List;

/**
 * Created by lan on 2017/11/24.
 */
public interface IFansMedalSettingView extends IRxView {
    void setGroupMedal(List<String> list);
}
