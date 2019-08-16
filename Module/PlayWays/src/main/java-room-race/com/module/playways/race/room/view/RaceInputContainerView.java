package com.module.playways.race.room.view;

import android.content.Context;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.component.busilib.recommend.RA;
import com.module.playways.R;
import com.module.playways.race.room.model.RacePlayerInfoModel;
import com.module.playways.room.msg.event.EventHelper;
import com.module.playways.room.room.RankRoomServerApi;
import com.module.playways.room.room.view.InputContainerView;
import com.zq.live.proto.Common.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RaceInputContainerView extends InputContainerView {
    RecyclerView mRecyclerView;
    PlayerAdapter mPlayerAdapter;
    RacePlayerInfoModel mRacePlayerInfoModel;
    ExImageView mBackgroundIv;

    public RaceInputContainerView(Context context) {
        super(context);
    }

    public RaceInputContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        inflate(getContext(), R.layout.race_input_container_view_layout, this);
        initInputView();
    }

    @Override
    protected void initInputView() {
        super.initInputView();
        mRecyclerView = findViewById(R.id.recycler_view);
        mBackgroundIv = (ExImageView) findViewById(R.id.background_iv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mPlayerAdapter = new PlayerAdapter();
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mPlayerAdapter);

        mSendMsgBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mHasPretend = false;
                String content = mEtContent.getText().toString();
                RankRoomServerApi roomServerApi = ApiManager.getInstance().createService(RankRoomServerApi.class);

                HashMap<String, Object> map = new HashMap<>();
                map.put("gameID", mRoomData.getGameId());
                map.put("content", content);
                if (mRacePlayerInfoModel != null && mRacePlayerInfoModel.getUserInfo() != null) {
                    UserInfo userInfo = new UserInfo.Builder()
                            .setAvatar(mRacePlayerInfoModel.getUserInfo().getAvatar())
                            .setUserID(mRacePlayerInfoModel.getUserID())
                            .setNickName(mRacePlayerInfoModel.getUserInfo().getNickname())
                            .build();
                    map.put("receiver", new UserInfo[]{userInfo});
                }

                RacePlayerInfoModel racePlayerInfoModel = mRacePlayerInfoModel;

                RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
                ApiMethods.subscribe(roomServerApi.sendMsg(body), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult result) {
                        if (result.getErrno() == 0) {
                            mEtContent.setText("");
                            String content = result.getData().getString("afterFilterContent");
                            if (!mHasPretend && !TextUtils.isEmpty(content)) {
                                mUiHandler.removeMessages(100);

                                if (racePlayerInfoModel != null && racePlayerInfoModel.getUserInfo() != null) {
                                    UserInfoModel userInfoModel = new UserInfoModel();
                                    userInfoModel.setNickname(racePlayerInfoModel.getUserInfo().getNicknameRemark());
                                    userInfoModel.setAvatar(racePlayerInfoModel.getUserInfo().getAvatar());
                                    userInfoModel.setUserId(racePlayerInfoModel.getUserID());
                                    EventHelper.pretendCommentPush(content, mRoomData.getGameId(), userInfoModel);
                                } else {
                                    EventHelper.pretendCommentPush(content, mRoomData.getGameId());
                                }
                            }
                            if(RA.hasTestList()){
                                HashMap map = new HashMap();
                                map.put("testList", RA.getTestList());
                                StatisticsAdapter.recordCountEvent("ra","sendmsg",map);
                            }
                        }
                    }
                });
                Message msg = mUiHandler.obtainMessage();
                msg.what = 100;
                msg.obj = content;
                mUiHandler.removeMessages(100);
                mUiHandler.sendMessageDelayed(msg, 500);
                U.getKeyBoardUtils().hideSoftInputKeyBoard(U.getActivityUtils().getCurrentActivity());
            }
        });
    }

    @Override
    public void onBoradShow() {
        super.onBoradShow();
        List<RacePlayerInfoModel> racePlayerInfoModelList = getPlayerInfoListExpectSelf();
        if (racePlayerInfoModelList.size() > 0) {
            racePlayerInfoModelList.add(0, new RacePlayerInfoModel());
            mRacePlayerInfoModel = racePlayerInfoModelList.get(0);
        }

        if (racePlayerInfoModelList.size() > 0) {
            mRecyclerView.setVisibility(VISIBLE);
            mBackgroundIv.setVisibility(VISIBLE);
        }

        mPlayerAdapter.setDataList(racePlayerInfoModelList);
        mEtContent.setHint("");
    }

    @Override
    public void onBoradHide() {
        super.onBoradHide();
        mRecyclerView.setVisibility(GONE);
        mBackgroundIv.setVisibility(GONE);
    }

    private List<RacePlayerInfoModel> getPlayerInfoListExpectSelf() {
        List<RacePlayerInfoModel> racePlayerInfoModelList = new ArrayList<>();
        Iterator<RacePlayerInfoModel> it = racePlayerInfoModelList.iterator();
        while (it.hasNext()) {
            RacePlayerInfoModel racePlayerInfoModel = it.next();
            if (racePlayerInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                it.remove();
            }
        }

        return racePlayerInfoModelList;
    }

    public class PlayerAdapter extends DiffAdapter<RacePlayerInfoModel, RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.input_select_player_item_layout, parent, false);
            WalletRecordItemHolder viewHolder = new WalletRecordItemHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            RacePlayerInfoModel model = mDataList.get(position);

            WalletRecordItemHolder reportItemHolder = (WalletRecordItemHolder) holder;
            reportItemHolder.bind(model);
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        private class WalletRecordItemHolder extends RecyclerView.ViewHolder {

            BaseImageView mPlayerIv;
            ExTextView mSelected;

            RacePlayerInfoModel mWalletRecordModel;

            public WalletRecordItemHolder(View itemView) {
                super(itemView);

                mPlayerIv = (BaseImageView) itemView.findViewById(R.id.player_iv);
                mSelected = (ExTextView) itemView.findViewById(R.id.selected_icon_tv);
                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mRacePlayerInfoModel = mWalletRecordModel;

                        if (mWalletRecordModel.getUserInfo() != null) {
                            String nickName = mRacePlayerInfoModel.getUserInfo().getNicknameRemark();
                            if (TextUtils.isEmpty(nickName)) {
                                nickName = "";
                            }

                            mEtContent.setHint("@ " + nickName);
                        } else {
                            mEtContent.setHint("");
                        }

                        notifyDataSetChanged();
                    }
                });
            }

            public void bind(RacePlayerInfoModel model) {
                this.mWalletRecordModel = model;
                String avatar = "";
                if (model.getUserInfo() != null) {
                    avatar = mWalletRecordModel.getUserInfo().getAvatar();
                }

                if (mRacePlayerInfoModel == mWalletRecordModel) {
                    mSelected.setVisibility(VISIBLE);
                } else {
                    mSelected.setVisibility(GONE);
                }

                if (TextUtils.isEmpty(avatar)) {
                    mPlayerIv.setImageDrawable(U.getDrawable(R.drawable.tubiao_all));
                } else {
                    AvatarUtils.loadAvatarByUrl(mPlayerIv,
                            AvatarUtils.newParamsBuilder(model.getUserInfo().getAvatar())
                                    .setCircle(true)
                                    .build());
                }
            }
        }
    }
}
