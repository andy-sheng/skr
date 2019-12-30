package com.module.playways.room.song.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.permission.SkrAudioPermission;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.RouterConstants;
import com.module.playways.R;
import com.module.playways.relay.match.model.JoinRelayRoomRspModel;
import com.module.playways.relay.room.RelayRoomActivity;
import com.module.playways.relay.room.RelayRoomData;
import com.module.playways.room.song.SongSelectServerApi;
import com.module.playways.room.song.adapter.SongCardSwipAdapter;
import com.module.playways.room.song.adapter.SongSelectAdapter;
import com.module.playways.room.song.flingswipe.SwipeFlingAdapterView;
import com.module.playways.room.song.model.SongCardModel;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.room.song.presenter.SongTagDetailsPresenter;
import com.module.playways.room.song.view.ISongTagDetailView;
import com.module.playways.room.song.view.RelaySongInfoDialogView;
import com.module.playways.songmanager.SongManagerActivity;
import com.module.playways.songmanager.event.AddSongEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

// 合唱首页和练歌房通用此页面
public class SongSelectFragment extends BaseFragment implements ISongTagDetailView, SwipeFlingAdapterView.onFlingListener,
        SwipeFlingAdapterView.OnItemClickListener {

    public final String TAG = "SongSelectFragment";

    public int DEFAULT_COUNT = 6;  // 从服务器拉去歌曲数，即每个页面默认个数
    public int DEFAULT_FIRST_COUNT = DEFAULT_COUNT * 3; // 第一次从推荐页面拉去歌曲数

    RelativeLayout mMainActContainer;

    SwipeFlingAdapterView mSwipeView;
    SongCardSwipAdapter mSongCardSwipAdapter;

    ImageView mTopIconIv;
    TextView mTopTextTv;
    ExImageView mSelectBack;
    ExImageView mSelectSearch;
    TextView mInviteTv;

    ExTextView mBottomLeftTv;
    ExTextView mBottomRightTv;

    SongTagDetailsPresenter presenter;

    List<SongCardModel> mDeleteList; // 已经滑走的数据

    int mFrom;

    int offset; //当前偏移量
    boolean hasMore = true; // 是否还有更多数据标记位

    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

    RelaySongInfoDialogView mRelaySongInfoDialogView;

    @Override
    public void setData(int type, @org.jetbrains.annotations.Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mFrom = (Integer) data;
        }
    }

    @Override
    public int initView() {
        return R.layout.song_select_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = getRootView().findViewById(R.id.main_act_container);
        mSwipeView = getRootView().findViewById(R.id.swipe_view);

        mMainActContainer = getRootView().findViewById(R.id.main_act_container);

        mTopIconIv = getRootView().findViewById(R.id.top_icon_iv);
        mTopTextTv = getRootView().findViewById(R.id.top_text_tv);
        mSelectBack = getRootView().findViewById(R.id.select_back);
        mSelectSearch = getRootView().findViewById(R.id.select_search);
        mInviteTv = getRootView().findViewById(R.id.invite_tv);

        mBottomLeftTv = getRootView().findViewById(R.id.bottom_left_tv);
        mBottomRightTv = getRootView().findViewById(R.id.bottom_right_tv);


        int songCardHeight = U.getDisplayUtils().getScreenHeight() - U.getDisplayUtils().dip2px(84) - U.getDisplayUtils().dip2px(60)
                - (U.getDeviceUtils().hasNotch(getActivity()) ? U.getStatusBarUtil().getStatusBarHeight(getActivity()) : 0);
        DEFAULT_COUNT = songCardHeight / U.getDisplayUtils().dip2px(72);
        DEFAULT_FIRST_COUNT = DEFAULT_COUNT * 3;

        if (mFrom == SongManagerActivity.TYPE_FROM_AUDITION) {
            // 练歌房
            mTopTextTv.setVisibility(View.GONE);
            mSelectSearch.setVisibility(View.VISIBLE);
            mInviteTv.setVisibility(View.GONE);
            mBottomLeftTv.setText("上一页");
            mTopIconIv.setBackground(U.getDrawable(R.drawable.audition_top_icon));
        } else {
            // 合唱
            mTopTextTv.setText("选择合唱歌曲");
            mTopTextTv.setVisibility(View.VISIBLE);
            mSelectSearch.setVisibility(View.GONE);
            mInviteTv.setVisibility(View.VISIBLE);
            mBottomLeftTv.setText("搜歌");
            mTopIconIv.setBackground(U.getDrawable(R.drawable.relay_top_icon));
        }

        mBottomLeftTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mFrom == SongManagerActivity.TYPE_FROM_AUDITION) {
                    backToLastCard();
                } else {
                    goSearchFragment();
                }

            }
        });

        mSelectSearch.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // todo 补充已点中合唱列表
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), SearchSongFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .addDataBeforeAdd(0, mFrom)
                        .addDataBeforeAdd(1, false)
                        .build());
            }
        });

        mInviteTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mInviteTv.setClickable(false);
                createRelayRoom();
            }
        });

        mBottomRightTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                switchToClicked();
            }
        });

        mSelectBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mDeleteList = new ArrayList<>();
        int selectModel = SongSelectAdapter.AUDITION_MODE;
        String selectText = "演唱";
        if (mFrom == SongManagerActivity.TYPE_FROM_RELAY_HOME) {
            selectModel = SongSelectAdapter.RELAY_MODE;
            selectText = "合唱";
        }
        mSongCardSwipAdapter = new SongCardSwipAdapter(new SongSelectAdapter.Listener() {
            @Override
            public void onClickSelect(int position, SongModel model) {
                if (!U.getNetworkUtils().hasNetwork()) {
                    U.getToastUtil().showShort("无网络连接，请检查网络后重试");
                    return;
                }
                jump(model);
            }

            @Override
            public void onClickSongName(int position, SongModel model) {
                if (mFrom == SongManagerActivity.TYPE_FROM_RELAY_HOME) {
                    // todo 做个展示歌曲的弹窗
                    mRelaySongInfoDialogView = new RelaySongInfoDialogView(model, getActivity());
                    mRelaySongInfoDialogView.showByDialog(U.getDisplayUtils().getScreenHeight() - 2 * U.getDisplayUtils().dip2px(60));
                }
            }

        }, DEFAULT_COUNT, selectModel, selectText);

        // 默认推荐
        presenter = new SongTagDetailsPresenter(this);
        addPresent(presenter);
        presenter.getRcomdMusicItems(0, DEFAULT_FIRST_COUNT, mFrom);

        if (mSwipeView != null) {
            mSwipeView.setIsNeedSwipe(true);
            mSwipeView.setFlingListener(this);
            mSwipeView.setOnItemClickListener(this);

            mSwipeView.setAdapter(mSongCardSwipAdapter);
        }

    }

    private void goSearchFragment() {
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), SearchSongFragment.class)
                .setAddToBackStack(true)
                .setHasAnimation(false)
                .addDataBeforeAdd(0, mFrom)
                .addDataBeforeAdd(1, false)
                .setFragmentDataListener(new FragmentDataListener() {
                    @Override
                    public void onFragmentResult(int requestCode, int resultCode, @org.jetbrains.annotations.Nullable Bundle bundle, @org.jetbrains.annotations.Nullable Object obj) {
                        if (requestCode == 0 && resultCode == 0 && obj != null) {
                            SongModel model = (SongModel) obj;
                            EventBus.getDefault().post(new AddSongEvent(model, mFrom));
                        }
                    }
                })
                .build());
    }

    void jump(SongModel songModel) {
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
    protected void onFragmentVisible() {
        super.onFragmentVisible();
    }

    // 返回上一张选歌卡片
    private void backToLastCard() {
        if (mDeleteList != null && mDeleteList.size() != 0) {
            U.getSoundUtils().play(TAG, R.raw.rank_flipsonglist);
            mSongCardSwipAdapter.addData(0, mDeleteList.remove(0));
            mSwipeView.swipeBack();
        } else {
            if (mSongCardSwipAdapter.getSongCardHolderArrayList() != null && mSongCardSwipAdapter.getSongCardHolderArrayList().size() > 0 && !hasMore) {
                U.getSoundUtils().play(TAG, R.raw.rank_flipsonglist);
                int size = mSongCardSwipAdapter.getSongCardHolderArrayList().size();
                mSongCardSwipAdapter.addData(0, mSongCardSwipAdapter.getSongCardHolderArrayList().remove(size - 1));
                mSwipeView.swipeBack();
            } else {
                U.getToastUtil().showShort("没有更多返回了");
            }
        }
    }

    // TODO: 2018/12/17  切换到已点界面, 要不要保存当前记录的数据，取决从已点回来的逻辑 
    private void switchToClicked() {
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), HistorySongFragment.class)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .addDataBeforeAdd(0, mFrom)
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

                if ((i + 1) % DEFAULT_COUNT == 0) {
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

    private void createRelayRoom() {
        SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
        HashMap map = new HashMap();
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(songSelectServerApi.createRelayRoom(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    JoinRelayRoomRspModel rsp = JSON.parseObject(result.getData().toJSONString(), JoinRelayRoomRspModel.class);
                    rsp.setEnterType(RelayRoomData.EnterType.INVITE);
                    createSuccess(rsp);
                } else {
                    U.getToastUtil().showShort(result.getErrmsg());
                }
                mInviteTv.setClickable(false);
            }
        }, this);
    }

    public void createSuccess(JoinRelayRoomRspModel joinRelayRoomRspModel) {
        Intent intent = new Intent(getContext(), RelayRoomActivity.class);
        intent.putExtra("JoinRelayRoomRspModel", joinRelayRoomRspModel);
        startActivity(intent);
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
        getRootView().setVisibility(View.VISIBLE);
    }

    @Override
    public void notifyToHide() {
        MyLog.d(TAG, "pushIntoStash");
        getRootView().setVisibility(View.GONE);
    }

    @Override
    public void onItemClicked(MotionEvent event, View v, Object dataObject) {
        MyLog.d(TAG, "onItemClicked" + " event=" + event + " v=" + v + " dataObject=" + dataObject);
    }

    @Override
    public void removeFirstObjectInAdapter() {
        mSongCardSwipAdapter.remove(0);
        presenter.getRcomdMusicItems(offset, DEFAULT_COUNT, mFrom);
        MyLog.d(TAG, "removeFirstObjectInAdapter");
    }

    @Override
    public void onLeftCardExit(Object dataObject) {
        U.getSoundUtils().play(TAG, R.raw.rank_flipsonglist);
        MyLog.d(TAG, "onLeftCardExit" + " dataObject=" + dataObject);
        if (dataObject instanceof SongCardModel) {
            mDeleteList.add(0, (SongCardModel) dataObject);
        }
    }

    @Override
    public void onRightCardExit(Object dataObject) {
        U.getSoundUtils().play(TAG, R.raw.rank_flipsonglist);
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
