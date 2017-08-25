package com.wali.live.watchsdk.editinfo.fragment.presenter;

import android.support.annotation.StringRes;

import com.base.mvp.IRxView;

/**
 * Created by wangmj on 17-8-16.
 */

public interface IEditAvatarView extends IRxView {

    void showProgressDialog(@StringRes int msgRes);

    void hideProgressDialog();

    void editSuccess(long avatar);

    void editFailure(int code);
}
