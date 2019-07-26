package com.module.feeds;

import android.support.v4.app.Fragment;
import com.alibaba.android.arouter.facade.template.IProvider;

public interface  IFeedsModuleService extends IProvider {
    Fragment getFeedsFragment();

    IPersonFeedsWall getPersonFeedsWall(Object basefragment, Object userInfo, Object requestCall);
}