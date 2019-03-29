package com.module.home.game;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.component.busilib.callback.ErrorCallback;
import com.component.busilib.friends.FriendRoomModel;
import com.component.busilib.friends.GrabSongApi;
import com.component.busilib.friends.SpecialModel;
import com.module.home.MainPageSlideApi;
import com.module.home.R;
import com.module.home.game.adapter.GameAdapter;
import com.module.home.game.model.BannerModel;
import com.module.home.game.model.QuickJoinRoomModel;
import com.module.home.game.model.RecommendRoomModel;
import com.module.home.model.SlideShowModel;
import com.module.home.widget.UserInfoTileView2;

import java.util.List;

public class Game2Fragment extends BaseFragment {

    UserInfoTileView2 mUserInfoTitle;
    RecyclerView mRecyclerView;

    GameAdapter mGameAdapter;

    MainPageSlideApi mMainPageSlideApi;
    UserInfoServerApi mUserInfoServerApi;
    GrabSongApi mGrabSongApi;

    boolean mIsKConfig = false;

    long mLastUpdateOperaArea = 0;
    long mLastUpdateRoomInfo = 0;
    long mLastupdateQuickInfo = 0;

    @Override
    public int initView() {
        return R.layout.game2_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mUserInfoTitle = (UserInfoTileView2) mRootView.findViewById(R.id.user_info_title);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);

        mGameAdapter = new GameAdapter(getContext());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mGameAdapter);

        initOperationArea(true);
        initFriendRoom(true);
        initQuickRoom(true);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }


    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        initOperationArea(false);
        initFriendRoom(false);
        initQuickRoom(false);
    }

    private void initOperationArea(boolean isFlag) {
        long now = System.currentTimeMillis();
        if (!isFlag) {
            // 距离上次拉去已经超过30秒了
            if ((now - mLastUpdateOperaArea) < 30 * 1000) {
                return;
            }
        }

        String slideshow = U.getPreferenceUtils().getSettingString("slideshow", "");
        if (!TextUtils.isEmpty(slideshow)) {
            try {
                List<SlideShowModel> slideShowModelList = JSON.parseArray(slideshow, SlideShowModel.class);
                setBannerImage(slideShowModelList);
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }

        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi.class);
        ApiMethods.subscribe(mMainPageSlideApi.getSlideList(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mLastUpdateOperaArea = System.currentTimeMillis();
                    List<SlideShowModel> slideShowModelList = JSON.parseArray(result.getData().getString("slideshow"), SlideShowModel.class);
                    U.getPreferenceUtils().setSettingString("slideshow", result.getData().getString("slideshow"));
                    setBannerImage(slideShowModelList);
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("网络异常");
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                U.getToastUtil().showShort("网络超时");
            }
        });
    }

    private void initFriendRoom(boolean isFlag) {
        long now = System.currentTimeMillis();
        if (!isFlag) {
            // 距离上次拉去已经超过30秒了
            if ((now - mLastUpdateRoomInfo) < 30 * 1000) {
                return;
            }
        }

        if (mGrabSongApi == null) {
            mGrabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        }
        ApiMethods.subscribe(mGrabSongApi.getOnlineFriendsRoom(0, 10), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    mLastUpdateRoomInfo = System.currentTimeMillis();
                    List<FriendRoomModel> list = JSON.parseArray(obj.getData().getString("friends"), FriendRoomModel.class);
                    int offset = obj.getData().getIntValue("offset");
                    int totalNum = obj.getData().getIntValue("totalRoomsNum");
                    setFriendRoom(list, offset, totalNum);
                }
            }
        }, this);

    }

    private void initQuickRoom(boolean isFlag) {
        long now = System.currentTimeMillis();
        if (!isFlag) {
            // 半个小时更新一次吧
            if ((now - mLastupdateQuickInfo) < 30 * 60 * 1000) {
                return;
            }
        }

        if (mGrabSongApi == null) {
            mGrabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        }
        ApiMethods.subscribe(mGrabSongApi.getSepcialList(0, 10), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    List<SpecialModel> list = JSON.parseArray(obj.getData().getString("tags"), SpecialModel.class);
                    int offset = obj.getData().getIntValue("offset");
                    setQuickRoom(list, offset);
                }
            }
        }, this);
    }

    private void setQuickRoom(List<SpecialModel> list, int offset) {
        if (list == null || list.size() == 0) {
            MyLog.w(TAG, "initQuickRoom 为null");
            return;
        }

        QuickJoinRoomModel quickJoinRoomModel = new QuickJoinRoomModel(list, offset);
        if (mGameAdapter.getPositionObject(2) != null && mGameAdapter.getPositionObject(2) instanceof RecommendRoomModel) {
            mGameAdapter.getDataList().remove(mGameAdapter.getPositionObject(2));
        }
        mGameAdapter.getDataList().add(quickJoinRoomModel);
        mGameAdapter.notifyDataSetChanged();

    }

    private void setFriendRoom(List<FriendRoomModel> list, int offset, int totalNum) {
        if (list == null || list.size() == 0) {
            MyLog.w(TAG, "initFriendRoom 为null");
            return;
        }

        // TODO: 2019/3/28 测试一下
        for (int i = 0; i < 5; i++) {
            list.addAll(list);
        }

        RecommendRoomModel recommendRoomModel = new RecommendRoomModel(list, offset, totalNum);
        if (mGameAdapter.getPositionObject(1) != null && mGameAdapter.getPositionObject(1) instanceof RecommendRoomModel) {
            mGameAdapter.getDataList().remove(mGameAdapter.getPositionObject(1));
        }
        mGameAdapter.getDataList().add(1, recommendRoomModel);
        mGameAdapter.notifyDataSetChanged();
    }


    private void setBannerImage(List<SlideShowModel> slideShowModelList) {
        if (slideShowModelList == null || slideShowModelList.size() == 0) {
            MyLog.w(TAG, "initOperationArea 为null");
            return;
        }
        BannerModel bannerModel = new BannerModel(slideShowModelList);
        if (mGameAdapter.getPositionObject(0) != null && mGameAdapter.getPositionObject(0) instanceof BannerModel) {
            mGameAdapter.getDataList().remove(mGameAdapter.getPositionObject(0));
        }
        mGameAdapter.getDataList().add(0, bannerModel);
        mGameAdapter.notifyDataSetChanged();
    }

    private void initGameKConfig() {
        if (mIsKConfig) {
            return;
        }
        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi.class);
        ApiMethods.subscribe(mMainPageSlideApi.getKConfig(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mIsKConfig = true;

                } else {

                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("网络异常");
            }
        });
    }

}
