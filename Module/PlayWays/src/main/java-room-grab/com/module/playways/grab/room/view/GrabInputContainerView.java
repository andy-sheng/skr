package com.module.playways.grab.room.view;

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
import com.common.core.account.UserAccountManager;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.component.busilib.constans.GameModeType;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.room.msg.event.EventHelper;
import com.module.playways.room.room.RankRoomServerApi;
import com.module.playways.room.room.view.InputContainerView;
import com.module.playways.voice.activity.VoiceRoomActivity;
import com.zq.live.proto.Common.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class GrabInputContainerView extends InputContainerView {
    RecyclerView mRecyclerView;
    PlayerAdapter mPlayerAdapter;
    GrabPlayerInfoModel mGrabPlayerInfoModel;
    ExImageView mBackgroundIv;

    public GrabInputContainerView(Context context) {
        super(context);
    }

    public GrabInputContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        inflate(getContext(), R.layout.grab_input_container_view_layout, this);
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
                if (mRoomData.getGameType() == GameModeType.GAME_MODE_CLASSIC_RANK) {
                    if (getContext() instanceof VoiceRoomActivity) {
                        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK), "chatroom_chat", null);
                    } else {
                        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK), "game_chat", null);
                    }
                } else if (mRoomData.getGameType() == GameModeType.GAME_MODE_GRAB) {
                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB), "game_chat", null);
                }
                String content = mEtContent.getText().toString();
                RankRoomServerApi roomServerApi = ApiManager.getInstance().createService(RankRoomServerApi.class);

                HashMap<String, Object> map = new HashMap<>();
                map.put("gameID", mRoomData.getGameId());
                map.put("content", content);
                if (mGrabPlayerInfoModel != null && mGrabPlayerInfoModel.getUserInfo() != null) {
                    UserInfo userInfo = new UserInfo.Builder()
                            .setAvatar(mGrabPlayerInfoModel.getUserInfo().getAvatar())
                            .setUserID(mGrabPlayerInfoModel.getUserID())
                            .setNickName(mGrabPlayerInfoModel.getUserInfo().getNickname())
                            .build();
                    map.put("receiver", new UserInfo[]{userInfo});
                }

                GrabPlayerInfoModel grabPlayerInfoModel = mGrabPlayerInfoModel;

                RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
                ApiMethods.subscribe(roomServerApi.sendMsg(body), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult result) {
                        if (result.getErrno() == 0) {
                            mEtContent.setText("");
                            String content = result.getData().getString("afterFilterContent");
                            if (!mHasPretend && !TextUtils.isEmpty(content)) {
                                mUiHandler.removeMessages(100);

                                if (grabPlayerInfoModel != null && grabPlayerInfoModel.getUserInfo() != null) {
                                    UserInfoModel userInfoModel = new UserInfoModel();
                                    userInfoModel.setNickname(grabPlayerInfoModel.getUserInfo().getNickname());
                                    userInfoModel.setAvatar(grabPlayerInfoModel.getUserInfo().getAvatar());
                                    userInfoModel.setUserId(grabPlayerInfoModel.getUserID());
                                    EventHelper.pretendCommentPush(content, mRoomData.getGameId(), userInfoModel);
                                } else {
                                    EventHelper.pretendCommentPush(content, mRoomData.getGameId());
                                }
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
        List<GrabPlayerInfoModel> grabPlayerInfoModelList = getPlayerInfoListExpectSelf();
        if (grabPlayerInfoModelList.size() > 0) {
            grabPlayerInfoModelList.add(0, new GrabPlayerInfoModel());
            mGrabPlayerInfoModel = grabPlayerInfoModelList.get(0);
        }

        if (grabPlayerInfoModelList.size() > 0) {
            mRecyclerView.setVisibility(VISIBLE);
            mBackgroundIv.setVisibility(VISIBLE);
        }

        mPlayerAdapter.setDataList(grabPlayerInfoModelList);
        mEtContent.setHint("");
    }

    @Override
    public void onBoradHide() {
        super.onBoradHide();
        mRecyclerView.setVisibility(GONE);
        mBackgroundIv.setVisibility(GONE);
    }

    private List<GrabPlayerInfoModel> getPlayerInfoListExpectSelf() {
        List<GrabPlayerInfoModel> grabPlayerInfoModelList = new ArrayList<>(((GrabRoomData) mRoomData).getInSeatPlayerInfoList());
        Iterator<GrabPlayerInfoModel> it = grabPlayerInfoModelList.iterator();
        while (it.hasNext()) {
            GrabPlayerInfoModel grabPlayerInfoModel = it.next();
            if (grabPlayerInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                it.remove();
            }
        }

        return grabPlayerInfoModelList;
    }

    public class PlayerAdapter extends DiffAdapter<GrabPlayerInfoModel, RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.input_select_player_item_layout, parent, false);
            WalletRecordItemHolder viewHolder = new WalletRecordItemHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            GrabPlayerInfoModel model = mDataList.get(position);

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

            GrabPlayerInfoModel mWalletRecordModel;

            public WalletRecordItemHolder(View itemView) {
                super(itemView);

                mPlayerIv = (BaseImageView) itemView.findViewById(R.id.player_iv);
                mSelected = (ExTextView) itemView.findViewById(R.id.selected_icon_tv);
                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mGrabPlayerInfoModel = mWalletRecordModel;

                        if (mWalletRecordModel.getUserInfo() != null) {
                            String nickName = mGrabPlayerInfoModel.getUserInfo().getNickname();
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

            public void bind(GrabPlayerInfoModel model) {
                this.mWalletRecordModel = model;
                String avatar = "";
                if (model.getUserInfo() != null) {
                    avatar = mWalletRecordModel.getUserInfo().getAvatar();
                }

                if (mGrabPlayerInfoModel == mWalletRecordModel) {
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
