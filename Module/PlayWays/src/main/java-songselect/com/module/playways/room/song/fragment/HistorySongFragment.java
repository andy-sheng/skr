package com.module.playways.room.song.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.permission.SkrAudioPermission;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;

import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.component.busilib.callback.EmptyCallback;
import com.component.busilib.callback.ErrorCallback;
import com.component.busilib.callback.LoadingCallback;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.RouterConstants;
import com.module.playways.room.song.adapter.SongSelectAdapter;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.R;
import com.module.playways.room.song.presenter.SongTagDetailsPresenter;
import com.module.playways.room.song.view.ISongTagDetailView;
import com.module.playways.songmanager.SongManagerActivity;
import com.module.playways.songmanager.event.AddSongEvent;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class HistorySongFragment extends BaseFragment implements ISongTagDetailView {

    public static final int DEFAULT_SONG_COUNT = 30;  // 每次从服务器拉去歌曲数
    int offset = 0; //偏移量
    boolean hasMore = true; // 是否还有更多数据标记位
    List<SongModel> datas; // 歌曲的数据源

    RelativeLayout mainActContainer;
    ImageView mTopIconIv;
    TextView mTopTextTv;
    ExImageView mHistoryBack;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mHistoryRecycle;

    SongSelectAdapter songSelectAdapter;
    SongTagDetailsPresenter presenter;

    int mFrom;

    LoadService mLoadService;

    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

    @Override
    public void setData(int type, @org.jetbrains.annotations.Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mFrom = (Integer) data;
        }
    }

    @Override
    public int initView() {
        return R.layout.history_song_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mainActContainer = getRootView().findViewById(R.id.main_act_container);
        mTopIconIv = getRootView().findViewById(R.id.top_icon_iv);
        mTopTextTv = getRootView().findViewById(R.id.top_text_tv);
        mHistoryBack = getRootView().findViewById(R.id.history_back);
        mRefreshLayout = getRootView().findViewById(R.id.refreshLayout);
        mHistoryRecycle = getRootView().findViewById(R.id.history_recycle);

        if (mFrom == SongManagerActivity.TYPE_FROM_AUDITION) {
            // 练歌房
            mTopTextTv.setVisibility(View.INVISIBLE);
            mTopIconIv.setBackground(U.getDrawable(R.drawable.audition_top_icon));
            mainActContainer.setBackgroundColor(Color.parseColor("#5F3C82"));
        } else {
            // 合唱
            mTopTextTv.setText("选择合唱歌曲");
            mTopTextTv.setVisibility(View.VISIBLE);
            mTopIconIv.setBackground(U.getDrawable(R.drawable.relay_top_icon));
            mainActContainer.setBackground(U.getDrawable(R.drawable.double_room_view_bg));
        }

        int selectModel = SongSelectAdapter.AUDITION_MODE;
        if (mFrom == SongManagerActivity.TYPE_FROM_RELAY_HOME) {
            selectModel = SongSelectAdapter.RELAY_MODE;
        }
        songSelectAdapter = new SongSelectAdapter(new SongSelectAdapter.Listener() {
            @Override
            public void onClickSelect(int position, SongModel model) {
                jump(model);
            }

            @Override
            public void onClickSongName(int position, SongModel model) {

            }
        }, false, selectModel, "演唱");
        mHistoryRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mHistoryRecycle.setAdapter(songSelectAdapter);

        presenter = new SongTagDetailsPresenter(this);
        addPresent(presenter);
        presenter.getClickedMusicItmes(0, DEFAULT_SONG_COUNT, mFrom);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                presenter.getClickedMusicItmes(offset, DEFAULT_SONG_COUNT, mFrom);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });

        mHistoryBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(HistorySongFragment.this);
            }
        });

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new LoadingCallback(R.drawable.wulishigedan, "数据正在努力加载中..."))
                .addCallback(new EmptyCallback(R.drawable.wulishigedan, "你敢不敢唱首歌？", null))
                .addCallback(new ErrorCallback(R.drawable.wulishigedan, "请求出错了..."))
                .setDefaultCallback(LoadingCallback.class)
                .build();
        mLoadService = mLoadSir.register(mRefreshLayout, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                presenter.getClickedMusicItmes(offset, DEFAULT_SONG_COUNT, mFrom);
            }
        });

        U.getSoundUtils().preLoad(getTAG(), R.raw.normal_back);
    }

    void jump(SongModel songModel) {
        if (songModel == null) {
            return;
        }
        if (mFrom == SongManagerActivity.TYPE_FROM_AUDITION) {
            mSkrAudioPermission.ensurePermission(new Runnable() {
                @Override
                public void run() {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_AUDITION_ROOM)
                            .withSerializable("songModel", songModel)
                            .navigation();
                }
            }, true);
            return;
        } else {
            EventBus.getDefault().post(new AddSongEvent(songModel, mFrom));
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrAudioPermission.onBackFromPermisionManagerMaybe(getActivity());
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(getTAG());
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void loadSongsDetailItems(List<SongModel> list, int offset, boolean hasMore) {
        this.offset = offset;
        this.hasMore = hasMore;
        if (datas == null) {
            datas = new ArrayList<>();
        }
        if (songSelectAdapter != null && list != null) {
            datas.addAll(list);
            songSelectAdapter.setDataList(datas);
            songSelectAdapter.notifyDataSetChanged();
        }

        finishRefreshLoadMore();
    }

    @Override
    public void loadSongsDetailItemsFail() {
        finishRefreshLoadMore();
    }

    private void finishRefreshLoadMore() {
        mRefreshLayout.finishLoadMore();
        mRefreshLayout.finishRefresh();
        mRefreshLayout.setEnableLoadMore(hasMore);

        if (songSelectAdapter != null && songSelectAdapter.getDataList() != null && songSelectAdapter.getDataList().size() > 0) {
            mLoadService.showSuccess();
        } else {
            mLoadService.showCallback(EmptyCallback.class);
        }
    }
}
