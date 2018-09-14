package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.activity.BaseActivity;
import com.mi.live.data.query.model.ViewerModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.event.UserActionEvent;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.personinfo.fragment.FloatInfoFragment;
import com.wali.live.watchsdk.watch.adapter.ViewerRankRecyclerAdapter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.ViewerRankPresenter;

import java.util.List;

public class WatchGameViewerTabView extends RelativeLayout implements IComponentView<WatchGameViewerTabView.IPresenter, WatchGameViewerTabView.IView>, ViewerRankRecyclerAdapter.OnItemClickListener, WatchGameTabView.GameTabChildView {

    RecyclerView recyclerView;
    ViewerRankRecyclerAdapter mAdapter;
    ViewerRankPresenter mPresenter;

    RoomBaseDataModel mMyRoomData;

    boolean isScroll = false;

    public WatchGameViewerTabView(Context context, WatchComponentController componentController) {
        super(context);
        mMyRoomData = componentController.getRoomBaseDataModel();
        init(context, componentController);
    }

    private void init(Context context, WatchComponentController componentController) {
        inflate(context, R.layout.watch_game_tab_viewer_layout, this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new ViewerRankRecyclerAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int totalItemCount = recyclerView.getAdapter().getItemCount();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();
                int visibleItemCount = recyclerView.getChildCount();

                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItemPosition == totalItemCount - 1
                        && visibleItemCount > 0) {
                    //加载更多
                    isScroll = false;
                    mPresenter.postAvatarEvent(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_MORE_VIEWER, mAdapter.getBasicItemCount());
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    //滚动状态,不要刷新数据
                    isScroll = true;
                } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    isScroll = false;
                }
            }
        });

        mPresenter = new ViewerRankPresenter(componentController);
        mPresenter.setView(this.getViewProxy());
        setPresenter(mPresenter);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mPresenter != null) {
            mPresenter.startPresenter();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPresenter != null) {
            mPresenter.stopPresenter();
        }
    }

    @Override
    public IView getViewProxy() {

        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @NonNull
            @Override
            public <T extends View> T getRealView() {
                return (T) WatchGameViewerTabView.this;
            }

            @Override
            public void initViewers(List<ViewerModel> viewersList) {
                if (!isScroll) {
                    mAdapter.setUserList(viewersList);
                }
            }
        }
        return new ComponentView();
    }

    @Override
    public void setPresenter(IPresenter iPresenter) {

    }

    @Override
    public void onItemClick(long uid) {
        FloatInfoFragment.openFragment((BaseActivity) getContext(), uid, mMyRoomData.getUid(), mMyRoomData.getRoomId(), mMyRoomData.getVideoUrl(), null, mMyRoomData.getEnterRoomTime(), mMyRoomData.isEnableRelationChain());
    }

    @Override
    public void select() {

    }

    @Override
    public void unselect() {

    }

    @Override
    public void stopView() {

    }


    public interface IPresenter {

        /**
         * 发送查看更多观众的event
         */
        void postAvatarEvent(int eventTypeRequestLookMoreViewer, int itemCount);
    }

    public interface IView extends IViewProxy {
        /**
         * 初始化观众列表
         */
        void initViewers(List<ViewerModel> viewersList);

    }
}
