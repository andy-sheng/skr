package com.wali.live.watchsdk.videodetail.view;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.base.log.MyLog;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.watchsdk.R;

/**
 * Created by yangli on 2017/05/31.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 详情播放视图
 */
public class DetailBottomView implements View.OnClickListener,
        IComponentView<DetailBottomView.IPresenter, DetailBottomView.IView> {
    private static final String TAG = "DetailBottomView";

    @Nullable
    protected IPresenter mPresenter;

    private View mContentView;
    private View mPraiseBtn;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) mContentView.findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View v) {
        if (mPresenter == null) {
            return;
        }
        int i = v.getId();
        if (i == R.id.text_editor) {
            mPresenter.showInputView();
        } else if (i == R.id.praise_button) {
            mPresenter.praiseVideo(!v.isSelected());
        } else if (i == R.id.share_button) {
            mPresenter.showSharePanel();
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public DetailBottomView(@NonNull View contentView) {
        mContentView = contentView;
        mPraiseBtn = $(R.id.praise_button);
        $click($(R.id.text_editor), this);
        $click($(R.id.share_button), this);
        $click(mPraiseBtn, this);
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) mContentView;
            }

            @Override
            public void updateLikeStatus(boolean isLike) {
                mPraiseBtn.setSelected(isLike);
            }

            @Override
            public void onPraiseDone(boolean isLike) {
                MyLog.w(TAG, "onPraiseDone, isLike=" + isLike);
                mPraiseBtn.setSelected(isLike);
            }

            @Override
            public void onPraiseFailed(boolean isLike) {
                MyLog.w(TAG, "onPraiseFailed, isLike=" + isLike);
                // mPraiseBtn.setSelected(!isLike);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 显示输入框
         */
        void showInputView();

        /**
         * 点赞
         *
         * @param isLike true-点赞，false-取消点赞
         */
        void praiseVideo(boolean isLike);

        /**
         * 显示分享面板
         */
        void showSharePanel();
    }

    public interface IView extends IViewProxy {
        /**
         * 更新点赞按钮状态
         */
        void updateLikeStatus(boolean isLike);

        /**
         * 点赞/取消点赞成功
         *
         * @param isLike true-点赞，false-取消点赞
         */
        void onPraiseDone(boolean isLike);

        /**
         * 点赞/取消点赞失败
         *
         * @param isLike true-点赞，false-取消点赞
         */
        void onPraiseFailed(boolean isLike);
    }
}
