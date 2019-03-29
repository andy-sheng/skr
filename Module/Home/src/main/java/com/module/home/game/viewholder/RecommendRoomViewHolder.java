package com.module.home.game.viewholder;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.friends.FriendRoomHorizontalAdapter;
import com.component.busilib.friends.FriendRoomModel;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.game.model.RecommendRoomModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.common.rxretrofit.ApiManager.APPLICATION_JSON;

public class RecommendRoomViewHolder extends RecyclerView.ViewHolder {

    ExTextView mFriendsTv;
    ExTextView mMoreTv;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mFriendsRecycle;

    FriendRoomHorizontalAdapter mFriendRoomAdapter;

    public RecommendRoomViewHolder(View itemView, Context context) {
        super(itemView);

        mFriendsTv = (ExTextView) itemView.findViewById(R.id.friends_tv);
        mMoreTv = (ExTextView) itemView.findViewById(R.id.more_tv);
        mRefreshLayout = (SmartRefreshLayout) itemView.findViewById(R.id.refreshLayout);
        mFriendsRecycle = (RecyclerView) itemView.findViewById(R.id.friends_recycle);

        mFriendsRecycle.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        mFriendRoomAdapter = new FriendRoomHorizontalAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                // TODO: 2019/3/28 跳房间
//                if (model != null) {
//                    mSkrAudioPermission.ensurePermission(new Runnable() {
//                        @Override
//                        public void run() {
//                            FriendRoomModel model1 = (FriendRoomModel) model;
//                            GrabRoomServerApi roomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
//                            HashMap<String, Object> map = new HashMap<>();
//                            map.put("roomID", model1.getRoomInfo().getRoomID());
//                            RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSON), JSON.toJSONString(map));
//                            ApiMethods.subscribe(roomServerApi.joinGrabRoom(body), new ApiObserver<ApiResult>() {
//                                @Override
//                                public void process(ApiResult result) {
//                                    if (result.getErrno() == 0) {
//                                        JoinGrabRoomRspModel grabCurGameStateModel = JSON.parseObject(result.getData().toString(), JoinGrabRoomRspModel.class);
//                                        //先跳转
//                                        ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
//                                                .withSerializable("prepare_data", grabCurGameStateModel)
//                                                .navigation();
//                                        Activity activity = getActivity();
//                                        if (activity != null) {
//                                            activity.finish();
//                                        }
//                                    } else {
//                                        U.getToastUtil().showShort(result.getErrmsg());
//                                    }
//                                }
//
//                                @Override
//                                public void onNetworkError(ErrorType errorType) {
//                                    super.onNetworkError(errorType);
//                                }
//                            });
//                        }
//                    }, true);
//                }
            }
        });

        mMoreTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // TODO: 2019/3/28 跳好友界面
            }
        });

        mFriendsRecycle.setAdapter(mFriendRoomAdapter);

    }

    public void bindData(RecommendRoomModel recommendRoomModel) {
        mFriendRoomAdapter.setDataList(recommendRoomModel.getRoomModels());
    }
}
