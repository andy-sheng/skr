package com.wali.live.watchsdk.fans.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.thornbirds.component.presenter.IEventPresenter;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.adapter.FansMemberAdapter;
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;

import java.util.List;

/**
 * Created by yangli on 2017/11/13.
 *
 * @module 粉丝团成员页视图
 */
public class FansMemberView extends RelativeLayout
        implements IComponentView<FansMemberView.IPresenter, FansMemberView.IView> {
    private static final String TAG = "FansMemberView";

    @Nullable
    protected IPresenter mPresenter;

    private final FansMemberAdapter mAdapter = new FansMemberAdapter();
    private boolean mHasInflated = false;

    private View mEmptyView;
    private RecyclerView mRecyclerView;

    private View mManagerMemberArea; // view in view stub

    private final Runnable mHideLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            MyLog.d(TAG, "mHideLoadingRunnable");
            mAdapter.hideLoading();
            mEmptyView.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        }
    };

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public FansMemberView(Context context) {
        this(context, null);
    }

    public FansMemberView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FansMemberView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void inflateContent() {
        if (mHasInflated) {
            return;
        }
        mHasInflated = true;
        inflate(getContext(), R.layout.fans_member_view, this);

        mEmptyView = $(R.id.empty_view);
        mRecyclerView = $(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (!ViewCompat.canScrollVertically(recyclerView, 1)) {
                        mPresenter.pullMore();
                    }
                }
            }
        });

        mPresenter.startPresenter();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        inflateContent();
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) FansMemberView.this;
            }

            @Override
            public void onNewDataSet(List<FansMemberModel> memberList) {
                mAdapter.addItemData(memberList);
            }

            @Override
            public void onLoadingStarted() {
                MyLog.d(TAG, "onLoadingStarted");
                mAdapter.showLoading();
                removeCallbacks(mHideLoadingRunnable);
                mEmptyView.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingDone(boolean hasMore) {
                MyLog.d(TAG, "onLoadingDone");
                mAdapter.onLoadingDone(hasMore);
                if (mAdapter.isEmpty() || hasMore) {
                    postDelayed(mHideLoadingRunnable, 400);
                }
            }

            @Override
            public void onLoadingFailed() {
                MyLog.d(TAG, "onLoadingFailed");
                mAdapter.onLoadingFailed();
                postDelayed(mHideLoadingRunnable, 400);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter extends IEventPresenter<IView> {
        /**
         * 拉取成员数据
         */
        void syncMemberData();

        /**
         * 拉取更多成员数据
         */
        void pullMore();
    }

    public interface IView extends IViewProxy {
        /**
         * 拉取到成员数据
         */
        void onNewDataSet(List<FansMemberModel> memberList);

        /**
         * 拉取开始
         */
        void onLoadingStarted();

        /**
         * 拉取结束
         */
        void onLoadingDone(boolean hasMore);

        /**
         * 拉取失败
         */
        void onLoadingFailed();
    }
}
