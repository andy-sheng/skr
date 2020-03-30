package com.module.post;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.alibaba.android.arouter.facade.template.IProvider;

public interface IPostModuleService extends IProvider {

    Fragment getFragment();

    IPersonPostsWall getPostsWall(Object activity, Object userInfo, Object requestCall);

    IDynamicPostsView getDynamicPostsView(FragmentActivity activity, int type);
}