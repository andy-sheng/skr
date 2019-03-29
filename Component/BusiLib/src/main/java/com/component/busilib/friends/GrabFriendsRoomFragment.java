package com.component.busilib.friends;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import java.util.List;

public class GrabFriendsRoomFragment extends BaseFragment {

    ExImageView mIvBack;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mContentRv;

    int offset = 0;          //偏移量
    int DEFAULT_COUNT = 10;  // 每次拉去列表数目

    FriendRoomVerticalAdapter mFriendRoomVeritAdapter;

    @Override
    public int initView() {
        return R.layout.grab_friends_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mContentRv = (RecyclerView) mRootView.findViewById(R.id.content_rv);

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(GrabFriendsRoomFragment.this);
            }
        });

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadData(offset, DEFAULT_COUNT);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });
        mContentRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        mFriendRoomVeritAdapter = new FriendRoomVerticalAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                if (model != null) {
                    // TODO: 2019/3/29 跳到房间里面
//                    FriendRoomModel model1 = (FriendRoomModel) model;
//                    GrabRoomServerApi roomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
//                    HashMap<String, Object> map = new HashMap<>();
//                    map.put("roomID", model1.getRoomInfo().getRoomID());
//                    RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map));
//                    ApiMethods.subscribe(roomServerApi.joinGrabRoom(body), new ApiObserver<ApiResult>() {
//                        @Override
//                        public void process(ApiResult result) {
//                            if (result.getErrno() == 0) {
//                                JoinGrabRoomRspModel grabCurGameStateModel = JSON.parseObject(result.getData().toString(), JoinGrabRoomRspModel.class);
//                                //先跳转
//                                ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
//                                        .withSerializable("prepare_data", grabCurGameStateModel)
//                                        .navigation();
//                                Activity activity = getActivity();
//                                if (activity != null) {
//                                    activity.finish();
//                                }
//                            } else {
//                                U.getToastUtil().showShort(result.getErrmsg());
//                            }
//                        }
//
//                        @Override
//                        public void onNetworkError(ErrorType errorType) {
//                            super.onNetworkError(errorType);
//                        }
//                    });
                }

            }
        });
        mContentRv.setAdapter(mFriendRoomVeritAdapter);

        loadData(offset, DEFAULT_COUNT);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    private void loadData(int offset, int count) {
        GrabSongApi grabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        ApiMethods.subscribe(grabSongApi.getOnlineFriendsRoom(offset, count), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    List<FriendRoomModel> list = JSON.parseArray(obj.getData().getString("friends"), FriendRoomModel.class);
                    int offset = obj.getData().getIntValue("offset");
                    int totalNum = obj.getData().getIntValue("cnt");
                    refreshView(list, offset);
                }
            }
        }, this);
    }

    private void refreshView(List<FriendRoomModel> list, int offset) {
        this.offset = offset;
        if (list != null) {
            mFriendRoomVeritAdapter.getDataList().addAll(list);
            mFriendRoomVeritAdapter.notifyDataSetChanged();
        }
    }
}
