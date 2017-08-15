package com.wali.live.watchsdk.editinfo.fragment.presenter;

import com.base.mvp.IRxView;

/**
 * Created by lan on 2017/8/15.
 */
public interface IEditNameView extends IRxView {
    void editSuccess(String name);

    void editFailure(int code);
}
