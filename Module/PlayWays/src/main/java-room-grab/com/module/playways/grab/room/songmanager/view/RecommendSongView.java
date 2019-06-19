package com.module.playways.grab.room.songmanager.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.ex.ExFrameLayout;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomServerApi;
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

public class RecommendSongView extends FrameLayout {
    public final static String TAG = "GrabSongManageView";
    private RecyclerView mRecyclerView;
    private RecommendTagModel mRecommendTagModel;
    private boolean isOwner;
    RecommendSongAdapter mRecommendSongAdapter;
    GrabRoomServerApi mGrabRoomServerApi;
    SmartRefreshLayout mRefreshLayout;
    Disposable mDisposable;
    int mOffset = 0;
    int mLimit = 20;
    boolean hasInit = false;
    MakeGamePanelView mMakeGamePanelView;

    public RecommendSongView(Context context, boolean isOwner) {
        super(context);
        this.isOwner = isOwner;
        initView();
    }

    public RecommendSongView(Context context) {
        super(context);
        initView();
    }

    public RecommendSongView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RecommendSongView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        inflate(getContext(), R.layout.recommend_song_fragment_layout, this);
        initData();
    }

    public void initData() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRefreshLayout = (SmartRefreshLayout) findViewById(R.id.refreshLayout);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecommendSongAdapter = new RecommendSongAdapter(isOwner, new RecyclerOnItemClickListener<SongModel>() {
            @Override
            public void onItemClicked(View view, int position, SongModel model) {
                    if(isOwner && model!=null && model.getItemID() == SongModel.ID_CUSTOM_GAME){
                        // 弹出录音面板
                        if (mMakeGamePanelView == null) {
                            mMakeGamePanelView = new MakeGamePanelView(getContext());
                        }
                        mMakeGamePanelView.showByDialog(1);
                    }else{
                        EventBus.getDefault().post(new AddSongEvent(model));
                    }
            }
        });
        mRecyclerView.setAdapter(mRecommendSongAdapter);
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false);
        mRefreshLayout.setEnableOverScrollDrag(false);

        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getSongList();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });
    }

    public void setData(RecommendTagModel recommendTagModel) {
        mRecommendTagModel = recommendTagModel;
    }

    public void initSongList() {
        if (!hasInit) {
            getSongList();
        }
    }

    private void getSongList() {
        if (mRecommendTagModel == null) {
            MyLog.e(TAG, "getSongList mRecommendTagModel is null");
            return;
        }

        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mDisposable = ApiMethods.subscribe(mGrabRoomServerApi.getListStandBoards(mRecommendTagModel.getType(), mOffset, mLimit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                mRefreshLayout.finishLoadMore();

                if (result.getErrno() == 0) {
                    hasInit = true;
                    List<SongModel> recommendTagModelArrayList = JSONObject.parseArray(result.getData().getString("items"), SongModel.class);
                    if (recommendTagModelArrayList == null || recommendTagModelArrayList.size() == 0) {
                        mRefreshLayout.setEnableLoadMore(false);
                        return;
                    }
                    if(mOffset ==0 && mRecommendTagModel.getType()==4){
                        // 是双人游戏那一例
                        SongModel songModel = new SongModel();
                        songModel.setItemID(SongModel.ID_CUSTOM_GAME);
                        songModel.setItemName("自制小游戏");
                        mRecommendSongAdapter.getDataList().addAll(recommendTagModelArrayList);
                    }
                    mRecommendSongAdapter.getDataList().addAll(recommendTagModelArrayList);
                    mOffset = mRecommendSongAdapter.getDataList().size();
                    mRecommendSongAdapter.notifyDataSetChanged();
                } else {
                    U.getToastUtil().showShort(result.getErrmsg() + "");
                }

            }
        });
    }

    public void destroy() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }
}
