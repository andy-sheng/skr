package com.wali.live.livesdk.live.component.presenter;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.presenter.RxLifeCyclePresenter;
import com.wali.live.livesdk.live.component.view.IViewProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 2017/2/17.
 *
 * @module 基础架构表现
 */
public abstract class ComponentPresenter<VIEW extends IViewProxy> extends RxLifeCyclePresenter {

    protected @NonNull IComponentController mComponentController;

    @Nullable
    protected VIEW mView;

    @Nullable
    protected IAction mAction;

    protected final void registerAction(int event) {
        if (mAction == null) {
            mAction = createAction();
        }
        mComponentController.registerAction(event, mAction);
    }

    protected final void unregisterAction(int event) {
        mComponentController.unregisterAction(event, mAction);
    }

    protected final void unregisterAction() {
        mComponentController.unregisterAction(mAction);
    }

    public ComponentPresenter(@NonNull IComponentController componentController) {
        this.mComponentController = componentController;
    }

    /**
     * 设置该Presenter控制的view对象
     */
    public void setComponentView(@Nullable VIEW view) {
        mView = view;
    }

    /**
     * 实例化该Presenter响应其他Presenter发出的EVENT_*事件时的响应处理对象
     */
    @Nullable
    @CheckResult
    protected abstract IAction createAction();

    /**
     * 事件参数
     */
    public static class Params {
        private List<Object> params;
        private int accessIndex = 0;

        public Params putItem(Object object) {
            if (params == null) {
                params = new ArrayList<>();
            }
            params.add(object);
            return this;
        }

        public @Nullable <T extends Object> T fetchItem() {
            if (params == null || accessIndex >= params.size()) {
                return null;
            }
            try {
                T elem = (T) params.get(accessIndex);
                ++accessIndex;
                return elem;
            } catch (ClassCastException e) {
                // just ignore
            }
            return null;
        }
    }

    /**
     * 事件响应处理接口
     */
    public interface IAction {
        /**
         * 处理source事件，params为事件参数
         */
        boolean onAction(int source, @Nullable Params params);

    }

    public interface IComponentController {
        /**
         * 向控制器注册action对象响应source事件
         */
        void registerAction(int source, @Nullable IAction action);

        /**
         * 向控制器取消注册action对象响应source事件
         */
        void unregisterAction(int event, @Nullable IAction action);

        /**
         * 向控制器取消注册action对象响应任意事件
         */
        void unregisterAction(@Nullable IAction action);

        /**
         * 向控制器发送source事件，不携带参数
         */
        boolean onEvent(int source);

        /**
         * 向控制器发送source事件，并传递参数params
         */
        boolean onEvent(int source, @Nullable Params params);
    }
}
