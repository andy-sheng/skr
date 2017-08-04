package com.wali.live.watchsdk.videodetail.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.videodetail.adapter.DetailReplayAdapter;

import java.util.List;

/**
 * Created by zyh on 2017/06/06.
 *
 * @module 详情页底下的回放view
 */
public class DetailReplayView extends RelativeLayout implements IComponentView<DetailReplayView.IPresenter,
        DetailReplayView.IView> {
    private static final String TAG = "DetailReplayView";
    private TextView mEmptyView;
    private View mLoadingView;
    private RecyclerView mRecyclerView;
    private DetailReplayAdapter mAdapter;

    @Nullable
    protected IPresenter mPresenter;

    public void setMyRoomData(RoomBaseDataModel myRoomData) {
        if (mAdapter != null && myRoomData != null) {
            mAdapter.setMyRoomData(myRoomData);
        }
    }

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    public DetailReplayView(Context context) {
        this(context, null, 0);
    }

    public DetailReplayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DetailReplayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.replay_tab_page, this);
        mEmptyView = $(R.id.empty_view);
        mLoadingView = $(R.id.loading_view);
        mRecyclerView = $(R.id.recycler_view);

        mAdapter = new DetailReplayAdapter();
        mAdapter.setClickListener(new DetailReplayAdapter.IReplayClickListener() {
            @Override
            public void onItemClick(DetailReplayAdapter.ReplayInfoItem item) {
                mPresenter.onClickReplayItem(item);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                context, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
        if (mPresenter != null) {
            mPresenter.pullReplayList();
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
                return (T) DetailReplayView.this;
            }

            @Override
            public void onPullReplayDone(List<DetailReplayAdapter.ReplayInfoItem> itemList) {
                if (itemList != null && itemList.size() > 0) {
                    mEmptyView.setVisibility(View.GONE);
                    mAdapter.setItemData(itemList);
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPullReplayFailed() {
                mEmptyView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onShowLoadingView(boolean isShow) {
                mLoadingView.setVisibility(isShow ? View.VISIBLE : ViewGroup.GONE);
            }

            @Override
            public void onShowEmptyView(boolean isShow) {
                mEmptyView.setVisibility(isShow ? View.VISIBLE : ViewGroup.GONE);
            }

            @Override
            public void updateAllReplayView() {
                mAdapter.notifyDataSetChanged();
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 拉取回放列表
         */
        void pullReplayList();

        /**
         * 点击回放事件
         *
         * @param replayInfoItem
         */
        void onClickReplayItem(DetailReplayAdapter.ReplayInfoItem replayInfoItem);
    }

    public interface IView extends IViewProxy {
        /**
         * 拉取回放成功
         */
        void onPullReplayDone(List<DetailReplayAdapter.ReplayInfoItem> hisLiveInfoList);

        /**
         * 拉取回放失败
         */
        void onPullReplayFailed();

        /**
         * 显示加载图标
         */
        void onShowLoadingView(boolean isShow);

        /**
         * 显示/隐藏空页面
         */
        void onShowEmptyView(boolean isShow);

        /**
         * 刷新回放整体的页面
         */
        void updateAllReplayView();
    }
}
