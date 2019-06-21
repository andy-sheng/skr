package com.module.playways.grab.room.songmanager.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.songmanager.SongManageData;
import com.module.playways.grab.room.songmanager.adapter.RecommendSongAdapter;
import com.module.playways.grab.room.songmanager.customgame.MakeGamePanelView;
import com.module.playways.grab.room.songmanager.event.AddSongEvent;
import com.module.playways.grab.room.songmanager.model.RecommendTagModel;
import com.module.playways.room.song.model.SongModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * 推荐歌曲view
 */
public class RecommendSongView extends FrameLayout {
    public final static String TAG = "GrabSongManageView";
    private RecyclerView mRecyclerView;
    private RecommendTagModel mRecommendTagModel;
    RecommendSongAdapter mRecommendSongAdapter;
    GrabRoomServerApi mGrabRoomServerApi;
    SmartRefreshLayout mRefreshLayout;
    Disposable mDisposable;
    int mOffset = 0;
    int mLimit = 20;
    //    boolean hasInit = false;
    MakeGamePanelView mMakeGamePanelView;

    SongManageData mRoomData;

    public RecommendSongView(Context context, SongManageData roomData, RecommendTagModel recommendTagModel) {
        super(context);
        this.mRoomData = roomData;
        this.mRecommendTagModel = recommendTagModel;
        initView();
    }

    public void initView() {
        inflate(getContext(), R.layout.recommend_song_fragment_layout, this);
        initData();
    }

    public void initData() {
        mRecyclerView = findViewById(R.id.recycler_view);
        mRefreshLayout = findViewById(R.id.refreshLayout);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (mRoomData.isGrabRoom()) {
            mRecommendSongAdapter = new RecommendSongAdapter(mRoomData.isOwner(), new RecyclerOnItemClickListener<SongModel>() {
                @Override
                public void onItemClicked(View view, int position, SongModel model) {
                    if (mRoomData.isOwner() && model != null && model.getItemID() == SongModel.ID_CUSTOM_GAME) {
                        if (mMakeGamePanelView == null) {
                            mMakeGamePanelView = new MakeGamePanelView(getContext());
                        }
                        mMakeGamePanelView.showByDialog(mRoomData.getGameId());
                    } else {
                        EventBus.getDefault().post(new AddSongEvent(model));
                    }
                }
            });
        } else {
            /**
             * 双人房默认是直接 点唱
             */
            mRecommendSongAdapter = new RecommendSongAdapter(true, new RecyclerOnItemClickListener<SongModel>() {
                @Override
                public void onItemClicked(View view, int position, SongModel model) {
                    EventBus.getDefault().post(new AddSongEvent(model));
                }
            });
        }

        mRecyclerView.setAdapter(mRecommendSongAdapter);
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false);
        mRefreshLayout.setEnableOverScrollDrag(false);

        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getSongList(mOffset);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getSongList(0);
            }
        });
        getSongList(0);
    }

    public void tryLoad() {
        if (mRecommendSongAdapter.getDataList().isEmpty()) {
            getSongList(0);
        }
    }

    private void getSongList(int offset) {
        if (mRecommendTagModel == null) {
            MyLog.e(TAG, "getSongList mRecommendTagModel is null");
            return;
        }

        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mDisposable = ApiMethods.subscribe(mGrabRoomServerApi.getListStandBoards(mRecommendTagModel.getType(), offset, mLimit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                mRefreshLayout.finishLoadMore();

                if (result.getErrno() == 0) {
                    List<SongModel> recommendTagModelArrayList = JSONObject.parseArray(result.getData().getString("items"), SongModel.class);
                    if (recommendTagModelArrayList == null || recommendTagModelArrayList.size() == 0) {
                        mRefreshLayout.setEnableLoadMore(false);
                        return;
                    }
                    if (offset == 0) {
                        mRecommendSongAdapter.getDataList().clear();
                        if (mRecommendTagModel.getType() == 4 && mRoomData.isOwner()) {
                            // 是双人游戏那一例
                            SongModel songModel = new SongModel();
                            songModel.setItemID(SongModel.ID_CUSTOM_GAME);
                            songModel.setItemName("自制小游戏");
                            mRecommendSongAdapter.getDataList().addAll(recommendTagModelArrayList);
                        }
                    }
                    mRecommendSongAdapter.getDataList().addAll(recommendTagModelArrayList);
                    mOffset = mRecommendSongAdapter.getDataList().size();
                    if (mOffset > 0 && mRecommendSongAdapter.getDataList().get(0).getItemID() == SongModel.ID_CUSTOM_GAME) {
                        mOffset--;
                    }
                    mRecommendSongAdapter.notifyDataSetChanged();
                } else {
                    U.getToastUtil().showShort(result.getErrmsg() + "");
                }

            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    public void destroy() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}
