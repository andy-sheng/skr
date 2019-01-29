package com.module.playways.rank.song.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.audioroom.AudioRoomActivity;
import com.module.playways.PlayWaysActivity;
import com.module.playways.rank.prepare.fragment.AuditionFragment;
import com.module.playways.rank.prepare.fragment.AuditionPrepareResFragment;
import com.module.playways.rank.prepare.fragment.PrepareResFragment;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.song.adapter.SongCardSwipAdapter;
import com.module.playways.rank.song.flingswipe.SwipeFlingAdapterView;
import com.module.playways.rank.song.model.SongCardModel;
import com.module.playways.rank.song.model.SongModel;
import com.module.playways.rank.song.presenter.SongTagDetailsPresenter;
import com.module.playways.rank.song.view.ISongTagDetailView;
import com.module.rank.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.module.playways.PlayWaysActivity.KEY_GAME_TYPE;

public class SongSelectFragment extends BaseFragment implements ISongTagDetailView, SwipeFlingAdapterView.onFlingListener,
        SwipeFlingAdapterView.OnItemClickListener {

    public final static String TAG = "SongSelectFragment";

    public static final int DEFAULT_FIRST_COUNT = 30; // 第一次从推荐页面拉去歌曲数
    public static final int DEFAULT_COUNT = 6;  // 从服务器拉去歌曲数

    RelativeLayout mMainActContainer;

    SwipeFlingAdapterView mSwipeView;
    SongCardSwipAdapter mSongCardSwipAdapter;

    ExImageView mSelectBack;
    ExImageView mSelectBackIv;
    ExImageView mSelectClickedIv;

    SongTagDetailsPresenter presenter;

    List<SongCardModel> mDeleteList; // 已经滑走的数据

    int offset; //当前偏移量
    int mGameType;
    boolean hasMore = true; // 是否还有更多数据标记位

    @Override
    public int initView() {
        return R.layout.song_select_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);

        mSwipeView = (SwipeFlingAdapterView) mRootView.findViewById(R.id.swipe_view);

        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mSelectBackIv = (ExImageView) mRootView.findViewById(R.id.select_back_iv);
        mSelectClickedIv = (ExImageView) mRootView.findViewById(R.id.select_clicked_iv);
        mSelectBack = (ExImageView) mRootView.findViewById(R.id.select_back);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mGameType = bundle.getInt(KEY_GAME_TYPE);
        }

        RxView.clicks(mSelectBackIv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    backToLastCard();
                });

        RxView.clicks(mSelectClickedIv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    U.getSoundUtils().play(TAG, R.raw.general_button);
                    switchToClicked();
                });

        RxView.clicks(mSelectBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    U.getSoundUtils().play(TAG, R.raw.general_back, 500);
                    getActivity().finish();
                });

        U.getSoundUtils().preLoad(TAG, R.raw.general_button, R.raw.general_back, R.raw.musiclist_nextpage);

        mDeleteList = new ArrayList<>();

        mSongCardSwipAdapter = new SongCardSwipAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                if (!U.getNetworkUtils().hasNetwork()) {
                    U.getToastUtil().showShort("无网络连接，请检查网络后重试");
                    return;
                }

                U.getSoundUtils().play(TAG, R.raw.general_button);
                SongModel songModel = (SongModel) model;
                if (getActivity() instanceof AudioRoomActivity) {
                    U.getToastUtil().showShort("试音房");
                    if (songModel.isAllResExist()) {
                        PrepareData prepareData = new PrepareData();
                        prepareData.setSongModel(songModel);
                        prepareData.setBgMusic(songModel.getRankUserVoice());

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
                            .setAddToBackStack(true)
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

        // 默认推荐
        presenter = new SongTagDetailsPresenter(this);
        addPresent(presenter);
        presenter.getRcomdMusicItems(0, DEFAULT_FIRST_COUNT);

        if (mSwipeView != null) {
            mSwipeView.setIsNeedSwipe(true);
            mSwipeView.setFlingListener(this);
            mSwipeView.setOnItemClickListener(this);


            mSwipeView.setAdapter(mSongCardSwipAdapter);
        }

    }

    // 返回上一张选歌卡片
    private void backToLastCard() {
        if (mDeleteList != null && mDeleteList.size() != 0) {
            U.getSoundUtils().play(TAG, R.raw.general_back);
            mSongCardSwipAdapter.addData(0, mDeleteList.remove(0));
            mSwipeView.swipeBack();
        } else {
            U.getToastUtil().showShort("没有更多返回了");
        }
    }

    // TODO: 2018/12/17  切换到已点界面, 要不要保存当前记录的数据，取决从已点回来的逻辑 
    private void switchToClicked() {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_GAME_TYPE, mGameType);
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), HistorySongFragment.class)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .setBundle(bundle)
                .setEnterAnim(R.anim.slide_in_bottom)
                .setExitAnim(R.anim.slide_out_bottom)
                .setFragmentDataListener(new FragmentDataListener() {
                    @Override
                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                    }
                })
                .build());
    }

    @Override
    public void loadSongsDetailItems(List<SongModel> list, int offset, boolean hasMore) {
        this.offset = offset; //保存当前偏移量
        this.hasMore = hasMore;
        List<SongCardModel> songCardModels = new ArrayList<>();
        if (list != null && list.size() > 0) {
            SongCardModel songCardModel = new SongCardModel();
            for (int i = 0; i < list.size(); i++) {
                if (songCardModel == null) {
                    songCardModel = new SongCardModel();
                }
                songCardModel.getList().add(list.get(i));

                if ((i + 1) % SongCardModel.MAX_COUNT == 0) {
                    songCardModels.add(songCardModel);
                    songCardModel = null;
                }
            }
            if (songCardModel != null) {
                songCardModels.add(songCardModel);
            }
        }

        if (songCardModels != null && songCardModels.size() > 0) {
            mSongCardSwipAdapter.addAll(songCardModels);
        } else {
            if (mDeleteList != null && mDeleteList.size() > 0) {
                mSongCardSwipAdapter.addData(mDeleteList.remove(mDeleteList.size() - 1));
            }
        }

    }

    @Override
    public void loadSongsDetailItemsFail() {

    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
    }

    @Override
    public void notifyToShow() {
        MyLog.d(TAG, "toStaskTop");
        mRootView.setVisibility(View.VISIBLE);
    }

    @Override
    public void notifyToHide() {
        MyLog.d(TAG, "pushIntoStash");
        mRootView.setVisibility(View.GONE);
    }

    @Override
    public void onItemClicked(MotionEvent event, View v, Object dataObject) {
        MyLog.d(TAG, "onItemClicked" + " event=" + event + " v=" + v + " dataObject=" + dataObject);
    }

    @Override
    public void removeFirstObjectInAdapter() {
        mSongCardSwipAdapter.remove(0);
        presenter.getRcomdMusicItems(offset, DEFAULT_COUNT);
        MyLog.d(TAG, "removeFirstObjectInAdapter");
    }

    @Override
    public void onLeftCardExit(Object dataObject) {
        U.getSoundUtils().play(TAG, R.raw.musiclist_nextpage);
        MyLog.d(TAG, "onLeftCardExit" + " dataObject=" + dataObject);
        if (dataObject instanceof SongCardModel) {
            mDeleteList.add(0, (SongCardModel) dataObject);
        }
    }

    @Override
    public void onRightCardExit(Object dataObject) {
        U.getSoundUtils().play(TAG, R.raw.musiclist_nextpage);
        MyLog.d(TAG, "onRightCardExit" + " dataObject=" + dataObject);
        if (dataObject instanceof SongCardModel) {
            mDeleteList.add(0, (SongCardModel) dataObject);
        }
    }

    @Override
    public void onAdapterAboutToEmpty(int itemsInAdapter) {
        MyLog.d(TAG, "onAdapterAboutToEmpty" + " itemsInAdapter=" + itemsInAdapter);
    }

    @Override
    public void onScroll(float progress, float scrollXProgress) {

    }

    @Override
    public void onCardBack(Object dataObject) {
        MyLog.d(TAG, "onCardBack" + " dataObject=" + dataObject);
    }
}
