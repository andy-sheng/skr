package com.wali.live.livesdk.live.component.view;

import android.support.annotation.Nullable;

/**
 * Created by yangli on 2017/2/17.
 *
 * @module 基础架构视图
 */
public interface IComponentView<PRESENTER, VIEW extends IViewProxy> {
    VIEW getViewProxy();

    void setPresenter(@Nullable PRESENTER presenter);
}
