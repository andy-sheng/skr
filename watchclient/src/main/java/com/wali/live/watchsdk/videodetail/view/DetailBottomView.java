package com.wali.live.watchsdk.videodetail.view;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.base.log.MyLog;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;

/**
 * Created by yangli on 2017/05/31.
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
    private View mShareBtn;

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
            if (AccountAuthManager.triggerActionNeedAccount(mContentView.getContext())) {
                mPresenter.showInputView();
            }
        } else if (i == R.id.praise_button) {
            if (AccountAuthManager.triggerActionNeedAccount(mContentView.getContext())) {
                mPresenter.praiseVideo(!v.isSelected());
            }
        } else if (i == R.id.share_button) {
            mPresenter.showSharePanel();
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public DetailBottomView(@NonNull View contentView, boolean enableShare) {
        mContentView = contentView;
        mPraiseBtn = $(R.id.praise_button);
        mShareBtn = $(R.id.share_button);
        $click($(R.id.text_editor), this);
        $click(mPraiseBtn, this);
        if (enableShare) {
            mShareBtn.setVisibility(View.VISIBLE);
            $click(mShareBtn, this);
        } else {
            mShareBtn.setVisibility(View.GONE);
        }
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
