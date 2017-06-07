package com.wali.live.watchsdk.videodetail.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.event.UserActionEvent;
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
                    UserActionEvent.post(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, uid, null);
                }

                @Override
                public void onItemClick(DetailCommentAdapter.CommentItem item) {
                    mPresenter.showCommentInput(item);
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

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && mPresenter != null) {
                    if (!ViewCompat.canScrollVertically(recyclerView, 1)) {
                        mPresenter.pullFeedsComment();
                    }
                }
            }
        });
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
            public void onPullCommentDone(List<DetailCommentAdapter.CommentItem> commentItemList) {
                mCommentItemList.addAll(commentItemList);
                mAdapter.setItemData(mCommentItemList);
                mEmptyView.setVisibility(mCommentItemList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onSendCommentDone(DetailCommentAdapter.CommentItem commentItem) {
                mCommentItemList.add(commentItem);
                mAdapter.setItemData(mCommentItemList);
            }

            @Override
            public void onPullCommentFailed() {
                if (mCommentItemList.isEmpty()) {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onShowLoadingView(boolean isShow) {
                mLoadingView.setVisibility(isShow ? View.VISIBLE : ViewGroup.GONE);
            }

            @Override
            public void onShowEmptyView(boolean isShow) {
                mEmptyView.setVisibility(isShow ? View.VISIBLE : ViewGroup.GONE);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 拉取更多评论
         */
        void pullFeedsComment();

        /**
         * 回复评论
         */
        void showCommentInput(DetailCommentAdapter.CommentItem commentItem);

        /**
         * 发送评论
         */
        void sendComment(String feedsId, DetailCommentAdapter.CommentItem commentItem);
    }

    public interface IView extends IViewProxy {
        /**
         * 拉取评论成功
         */
        void onPullCommentDone(List<DetailCommentAdapter.CommentItem> commentItemList);

        void onSendCommentDone(DetailCommentAdapter.CommentItem commentItem);

        /**
         * 拉取评论失败
         */
        void onPullCommentFailed();

        /**
         * 显示加载图标
         */
        void onShowLoadingView(boolean isShow);

        /**
         * 显示/隐藏空页面
         */
        void onShowEmptyView(boolean isShow);
    }
}
