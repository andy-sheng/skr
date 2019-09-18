package com.module.feeds;

import android.support.v4.app.Fragment;

import com.alibaba.android.arouter.facade.template.IProvider;

public interface  IFeedsModuleService extends IProvider {
    Fragment getFragment();

    IPersonFeedsWall getPersonFeedsWall(Object basefragment, Object userInfo, Object requestCall);

    Fragment getLikeWorkFragment();

    Fragment getRefuseCommentFragment();
}