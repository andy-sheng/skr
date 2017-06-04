package com.wali.live.watchsdk.videodetail.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.videodetail.adapter.DetailCommentAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 2017/06/02.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 评论列表页视图
 */
public class DetailCommentView extends RelativeLayout
        implements IComponentView<DetailCommentView.IPresenter, DetailCommentView.IView> {
    private static final String TAG = "DetailCommentView";

    @Nullable
    protected IPresenter mPresenter;

    private List<DetailCommentAdapter.CommentItem> mCommentItemList = new ArrayList<>();

    private TextView mEmptyView;
    private View mLoadingView;
    private RecyclerView mRecyclerView;

    private DetailCommentAdapter mAdapter;

    private final DetailCommentAdapter.ICommentClickListener mCommentClickListener =
            new DetailCommentAdapter.ICommentClickListener() {

                @Override
                public void onClickName(long uid) {
                }

                @Override
                public void onItemClick(DetailCommentAdapter.CommentItem item) {
                }

                @Override
                public void onItemLongClick(DetailCommentAdapter.CommentItem item) {
                }
            };

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
        if (mPresenter != null) {
            mPresenter.pullFeedsComment();
        }
    }

    public DetailCommentView(Context context) {
        this(context, null);
    }

    public DetailCommentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DetailCommentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.detail_tab_page, this);
        mEmptyView = $(R.id.empty_view);
        mLoadingView = $(R.id.loading_view);
        mRecyclerView = $(R.id.recycler_view);

        mAdapter = new DetailCommentAdapter();
        mAdapter.setOnClickListener(mCommentClickListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                context, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) DetailCommentView.this;
            }

            @Override
            public void onCommentItemList(List<DetailCommentAdapter.CommentItem> commentItemList) {
                mCommentItemList.addAll(commentItemList);
                mAdapter.setItemData(mCommentItemList);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        void pullFeedsComment();
    }

    public interface IView extends IViewProxy {
        void onCommentItemList(List<DetailCommentAdapter.CommentItem> commentItemList);
    }
}
