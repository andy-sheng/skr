package com.wali.live.watchsdk.videodetail;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.view.EmptyView;

/**
 * Created by zyh on 2017/06/06.
 *
 * @module 详情页底下的详情view
 */
public class DetailIntroduceView extends RelativeLayout
        implements IComponentView<DetailIntroduceView.IPresenter, DetailIntroduceView.IView> {
    private static final String TAG = "DetailIntroduceView";

    private EmptyView mEmptyView;
    private TextView mContentTv;

    @Nullable
    protected IPresenter mPresenter;


    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    public DetailIntroduceView(Context context) {
        this(context, null, 0);
    }

    public DetailIntroduceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DetailIntroduceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.feeds_detail_vedio_info_view, this);
        mEmptyView = $(R.id.empty_view);
        mContentTv = $(R.id.content_tv);
    }

    public void setData(String title, String desc) {
        if (!TextUtils.isEmpty(desc)) {
            mContentTv.setText(desc);
        } else if (!TextUtils.isEmpty(title)) {
            mContentTv.setText(title);
        } else {
            mContentTv.setVisibility(GONE);
            mEmptyView.setVisibility(VISIBLE);
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) DetailIntroduceView.this;
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy<View> {
    }
}
