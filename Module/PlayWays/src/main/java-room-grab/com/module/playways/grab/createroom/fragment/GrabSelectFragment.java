package com.module.playways.grab.createroom.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.permission.SkrAudioPermission;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.constans.GameModeType;
import com.module.RouterConstants;
import com.module.playways.grab.createroom.GrabSongApi;
import com.module.playways.grab.createroom.friends.FriendRoomHorizontalAdapter;
import com.module.playways.grab.createroom.friends.FriendRoomModel;
import com.module.playways.grab.createroom.friends.GrabFriendsRoomFragment;
import com.module.playways.grab.createroom.model.SpecialModel;
import com.module.playways.grab.createroom.view.SpecialSelectView;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.rank.prepare.model.JoinGrabRoomRspModel;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.rank.R;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.common.rxretrofit.ApiManager.APPLICATION_JSON;

/**
 * 一唱到底选择页面
 */
public class GrabSelectFragment extends BaseFragment {

    ExImageView mGameCreate;
    ExImageView mSelectBack;
    RelativeLayout mFriendsArea;
    ExTextView mFriendsTv;
    ExTextView mMoreTv;
    RecyclerView mFriendsRecycle;
    ExTextView mFastBeginTv;
    SpecialSelectView mSpecialView;

    FriendRoomHorizontalAdapter mFriendRoomAdapter;

    int offset = 0;          //偏移量
    int DEFAULT_COUNT = 10;  // 每次拉去列表数目

    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

    @Override
    public int initView() {
        return R.layout.grab_select_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mGameCreate = (ExImageView) mRootView.findViewById(R.id.game_create);
        mSelectBack = (ExImageView) mRootView.findViewById(R.id.select_back);
        mFriendsArea = (RelativeLayout) mRootView.findViewById(R.id.friends_area);
        mFriendsTv = (ExTextView) mRootView.findViewById(R.id.friends_tv);
        mMoreTv = (ExTextView) mRootView.findViewById(R.id.more_tv);
        mFriendsRecycle = (RecyclerView) mRootView.findViewById(R.id.friends_recycle);
        mFastBeginTv = (ExTextView) mRootView.findViewById(R.id.fast_begin_tv);
        mSpecialView = (SpecialSelectView) mRootView.findViewById(R.id.special_view);

        mSelectBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mGameCreate.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), GrabCreateRoomFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        });

        mSpecialView.setSpecialSelectListner(new SpecialSelectView.SpecialSelectListner() {
            @Override
            public void onClickSpecial(SpecialModel model, List<String> music) {
                goMatchFragment(model.getTagID(), music);
            }
        });

        mMoreTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), GrabFriendsRoomFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        });

        mFriendsRecycle.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mFriendRoomAdapter = new FriendRoomHorizontalAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                if (model != null) {
                    mSkrAudioPermission.ensurePermission(new Runnable() {
                        @Override
                        public void run() {
                            FriendRoomModel model1 = (FriendRoomModel) model;
                            GrabRoomServerApi roomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("roomID", model1.getRoomInfo().getRoomID());
                            RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map));
                            ApiMethods.subscribe(roomServerApi.joinGrabRoom(body), new ApiObserver<ApiResult>() {
                                @Override
                                public void process(ApiResult result) {
                                    if (result.getErrno() == 0) {
                                        JoinGrabRoomRspModel grabCurGameStateModel = JSON.parseObject(result.getData().toString(), JoinGrabRoomRspModel.class);
                                        //先跳转
                                        ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
                                                .withSerializable("prepare_data", grabCurGameStateModel)
                                                .navigation();
                                        Activity activity = getActivity();
                                        if (activity != null) {
                                            activity.finish();
                                        }
                                    } else {
                                        U.getToastUtil().showShort(result.getErrmsg());
                                    }
                                }

                                @Override
                                public void onNetworkError(ErrorType errorType) {
                                    super.onNetworkError(errorType);
                                }
                            });
                        }
                    }, true);
                }

            }
        });
        mFriendsRecycle.setAdapter(mFriendRoomAdapter);
        loadData(offset, DEFAULT_COUNT);
    }

    private void loadData(int offset, int count) {
        GrabSongApi grabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        ApiMethods.subscribe(grabSongApi.getOnlineFriendsRoom(offset, count), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    List<FriendRoomModel> list = JSON.parseArray(obj.getData().getString("friends"), FriendRoomModel.class);
                    int offset = obj.getData().getIntValue("offset");
                    int totalNum = obj.getData().getIntValue("totalRoomsNum");
                    refreshView(list, offset, totalNum);
                } else {
                    mFriendsArea.setVisibility(View.GONE);
                }
            }
        }, this);
    }

    private void refreshView(List<FriendRoomModel> list, int offset, int totalNum) {
        this.offset = offset;
        if (totalNum == 0) {
            mFriendsArea.setVisibility(View.GONE);
            return;
        }

        if (list != null) {
            mFriendRoomAdapter.getDataList().addAll(list);
            mFriendRoomAdapter.notifyDataSetChanged();
        }
        mFriendsTv.setText("好友在线（" + totalNum + ")");
    }

    public void goMatchFragment(int specialId, List<String> musicURLs) {
        PrepareData prepareData = new PrepareData();
        prepareData.setGameType(GameModeType.GAME_MODE_GRAB);
        prepareData.setTagId(specialId);

        if (musicURLs != null && musicURLs.size() > 0) {
            prepareData.setBgMusic(musicURLs.get(0));
        }

        ARouter.getInstance()
                .build(RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
                .withSerializable("prepare_data", prepareData)
                .navigation();
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrAudioPermission.onBackFromPermisionManagerMaybe();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
