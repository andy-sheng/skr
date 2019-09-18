package com.module.post;

import android.support.v4.app.Fragment;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.module.feeds.IPersonFeedsWall;

public interface IPostModuleService extends IProvider {
    Fragment getFragment();
}