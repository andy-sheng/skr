package com.wali.live.livesdk.live.component.view;

import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by yangli on 2017/2/19.
 *
 * @module 基础架构
 */
public interface IViewProxy {
    /**
     * 返回代理的真实View对象
     */
    @Nullable
    <T extends View> T getRealView();
}
