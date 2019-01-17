package com.module.playways.rank.song.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;

import android.support.v7.widget.RecyclerView;

import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.audioroom.AudioRoomActivity;
import com.module.playways.PlayWaysActivity;
import com.module.playways.rank.prepare.fragment.AuditionFragment;
import com.module.playways.rank.prepare.fragment.AuditionPrepareResFragment;
import com.module.playways.rank.prepare.fragment.PrepareResFragment;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.song.adapter.SongSelectAdapter;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.module.playways.rank.song.presenter.SongTagDetailsPresenter;
import com.module.playways.rank.song.view.ISongTagDetailView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.module.playways.PlayWaysActivity.KEY_GAME_TYPE;

public class HistorySongFragment extends BaseFragment implements ISongTagDetailView {

    public static final int DEFAULT_SONG_COUNT = 30;  // 每次从服务器拉去歌曲数
    int offset = 0; //偏移量
    boolean hasMore = true; // 是否还有更多数据标记位
    List<SongModel> datas; // 歌曲的数据源

    RelativeLayout mMainActContainer;
    ExImageView mHistoryBack;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mHistoryRecycle;

    SongSelectAdapter songSelectAdapter;
    SongTagDetailsPresenter presenter;

    int mGameType;

    @Override
    public int initView() {
        return R.layout.history_song_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mHistoryBack = (ExImageView) mRootView.findViewById(R.id.history_back);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mHistoryRecycle = (RecyclerView) mRootView.findViewById(R.id.history_recycle);

        mHistoryRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        Bundle bundle = getArguments();
        if (bundle != null) {
            mGameType = bundle.getInt(KEY_GAME_TYPE);
        }

        songSelectAdapter = new SongSelectAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                U.getSoundUtils().play(TAG, R.raw.general_button);
                SongModel songModel = (SongModel) model;
                if (getActivity() instanceof AudioRoomActivity) {
                    U.getToastUtil().showShort("试音房");
                    if (songModel.isAllResExist()) {
                        PrepareData prepareData = new PrepareData();
                        prepareData.setSongModel(songModel);

                        mRootView.post(new Runnable() {
                            @Override
                            public void run() {
                                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), AuditionFragment.class)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(true)
                                        .addDataBeforeAdd(0, prepareData)
                                        .setFragmentDataListener(new FragmentDataListener() {
                                            @Override
                                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                                            }
                                        })
                                        .build());
                            }
                        });
                    } else {
                        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), AuditionPrepareResFragment.class)
                                .setAddToBackStack(false)
                                .setHasAnimation(true)
                                .addDataBeforeAdd(0, songModel)
                                .setFragmentDataListener(new FragmentDataListener() {
                                    @Override
                                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                                    }
                                })
                                .build());
                    }
                    return;
                }

                if (getActivity() instanceof PlayWaysActivity) {
                    U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), PrepareResFragment.class)
                            .setAddToBackStack(false)
                            .setNotifyHideFragment(SongSelectFragment.class)
                            .setHasAnimation(true)
                            .addDataBeforeAdd(0, songModel)
                            .addDataBeforeAdd(1, mGameType)
                            .setFragmentDataListener(new FragmentDataListener() {
                                @Override
                                public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                                }
                            })
                            .build());
                }
                //测试
//                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), RankingRecordFragment.class)
//                        .setAddToBackStack(true)
//                        .setHasAnimation(true)
//                        .setFragmentDataListener(new FragmentDataListener() {
//                            @Override
//                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
//
//                            }
//                        })
//                        .build());

            }
        });
        mHistoryRecycle.setAdapter(songSelectAdapter);

        presenter = new SongTagDetailsPresenter(this);
        addPresent(presenter);
        presenter.getClickedMusicItmes(0, DEFAULT_SONG_COUNT);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                presenter.getClickedMusicItmes(offset, DEFAULT_SONG_COUNT);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });

        RxView.clicks(mHistoryBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    U.getFragmentUtils().popFragment(this);
                });

    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void loadSongsDetailItems(List<SongModel> list, int offset, boolean hasMore) {
        this.offset = offset;
        this.hasMore = hasMore;
        if (!hasMore) {
            mRefreshLayout.finishLoadMore();
            return;
        }
        if (datas == null) {
            datas = new ArrayList<>();
        }
        if (songSelectAdapter != null && list != null) {
            datas.addAll(list);
            songSelectAdapter.setDataList(list);
            songSelectAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void loadSongsDetailItemsFail() {

    }
}
