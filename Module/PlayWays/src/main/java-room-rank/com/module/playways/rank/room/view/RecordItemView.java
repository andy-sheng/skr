package com.module.playways.rank.room.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.rank.room.fragment.RankRecordFragment;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.RoomData;
import com.module.playways.RoomDataUtils;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class RecordItemView extends RelativeLayout {
    public final static String TAG = "RecordItemView";

    SimpleDraweeView mSdvSingerIcon;
    ExImageView mIvRanking;
    ExTextView mTvHintFlag;

    ExTextView mTvSingerName;
    ExTextView mTvSongName;

    ExImageView mIvLightOne;
    ExImageView mIvLightTwo;
    ExImageView mIvLightThree;

    SimpleDraweeView mIvLightOneIcon;
    SimpleDraweeView mIvLightOneTwo;
    SimpleDraweeView mIvLightOneThree;

    ExImageView[] mExImageViews = new ExImageView[3];
    SimpleDraweeView[] mSimpleDraweeViews = new SimpleDraweeView[3];

    RoomData mRoomData;

    RecordData mRecordData;

    public RecordItemView(Context context) {
        super(context);
    }

    public RecordItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void init() {
        if (mRoomData.getGameType() == GameModeType.GAME_MODE_FUNNY) {
            inflate(getContext(), R.layout.record_item_layout, this);
        } else {
            inflate(getContext(), R.layout.record_athletics_item_layout, this);

            mIvLightOneIcon = (SimpleDraweeView) findViewById(R.id.iv_light_one_icon);
            mIvLightOneTwo = (SimpleDraweeView) findViewById(R.id.iv_light_one_two);
            mIvLightOneThree = (SimpleDraweeView) findViewById(R.id.iv_light_one_three);

            mSimpleDraweeViews[0] = mIvLightOneIcon;
            mSimpleDraweeViews[1] = mIvLightOneTwo;
            mSimpleDraweeViews[2] = mIvLightOneThree;
        }

        mTvSongName = (ExTextView) findViewById(R.id.tv_song_name);

        mSdvSingerIcon = (SimpleDraweeView) findViewById(R.id.sdv_singer_icon);
        mIvRanking = (ExImageView) findViewById(R.id.iv_ranking);
        mTvHintFlag = (ExTextView) findViewById(R.id.tv_hint_flag);

        mTvSingerName = (ExTextView) findViewById(R.id.tv_singer_name);
        mIvLightOne = (ExImageView) findViewById(R.id.iv_light_one);
        mIvLightTwo = (ExImageView) findViewById(R.id.iv_light_two);
        mIvLightThree = (ExImageView) findViewById(R.id.iv_light_three);
        mExImageViews[0] = mIvLightOne;
        mExImageViews[1] = mIvLightTwo;
        mExImageViews[2] = mIvLightThree;
    }

    public void setData(RoomData roomData, RecordData recordData, int index, int strokeColor) {
        if (recordData == null || recordData.mVoteInfoModels == null || recordData.mVoteInfoModels.get(index) == null) {
            MyLog.e(TAG, "setData data 为 null");
            return;
        }

        VoteInfoModel voteInfoModel = recordData.mVoteInfoModels.get(index);

        mRoomData = roomData;

        mRecordData = recordData;

        init();

        if (recordData.hasEscape()) {
            // 有人逃跑，显示有人逃跑
            if (voteInfoModel.isIsEscape()) {
                mTvHintFlag.setVisibility(VISIBLE);
                mTvHintFlag.setText("逃跑");
            }
        } else if (!recordData.hasVote(voteInfoModel.getUserID())) {
            mTvHintFlag.setVisibility(VISIBLE);
            mTvHintFlag.setText("未投票");
        }

        UserInfoModel playerInfo = roomData.getUserInfo(voteInfoModel.getUserID());
        SongModel songModel = RoomDataUtils.getPlayerSongInfoUserId(roomData.getPlayerInfoList(), voteInfoModel.getUserID());

        AvatarUtils.loadAvatarByUrl(mSdvSingerIcon,
                AvatarUtils.newParamsBuilder(playerInfo.getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColorBySex(playerInfo.getIsMale())
                        .build());

        mTvSingerName.setText(playerInfo.getNickname());
        mTvSongName.setText("《" + songModel.getItemName() + "》");

        switch (voteInfoModel.getRank()) {
            case 1:
                mIvRanking.setBackground(getResources().getDrawable(R.drawable.diyiming_huizhang));
                break;
            case 2:
                mIvRanking.setBackground(getResources().getDrawable(R.drawable.dierming_huizhang));
                break;
            case 3:
                mIvRanking.setBackground(getResources().getDrawable(R.drawable.disanming_huizhang));
                break;
        }

        //这里需要判读是娱乐还是竞技
        if (voteInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
            if (voteInfoModel.getVoter() != null && voteInfoModel.getVoter().size() > 0) {
                U.getSoundUtils().play(RankRecordFragment.TAG, R.raw.result_win);
            } else {
                U.getSoundUtils().play(RankRecordFragment.TAG, R.raw.result_lose);
            }
        }

        if (voteInfoModel.getVoter() != null) {
            Observable.range(0, voteInfoModel.getVoter().size()).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    mExImageViews[integer].setSelected(true);
                }
            }, throwable -> MyLog.e(throwable));
        }

        if (mRoomData.getGameType() == GameModeType.GAME_MODE_CLASSIC_RANK) {
            Observable.range(0, voteInfoModel.getVoter().size()).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    int voterId = voteInfoModel.getVoter().get(integer);
                    if (voterId == 1) {
                        AvatarUtils.loadAvatarByUrl(mSimpleDraweeViews[integer],
                                AvatarUtils.newParamsBuilder(mRoomData.getSysAvatar())
                                        .setCircle(true)
                                        .setGray(false)
                                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                                        .setBorderColor(Color.WHITE)
                                        .build());
                    } else {
                        UserInfoModel playerInfo = roomData.getUserInfo(voterId);
                        AvatarUtils.loadAvatarByUrl(mSimpleDraweeViews[integer],
                                AvatarUtils.newParamsBuilder(playerInfo.getAvatar())
                                        .setCircle(true)
                                        .setGray(false)
                                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                                        .setBorderColorBySex(playerInfo.getIsMale())
                                        .build());
                    }
                }
            }, throwable -> MyLog.e(throwable));
        }
    }

    private int getUserIndex(long voterId) {
        for (int i = 0; i < mRecordData.mVoteInfoModels.size(); i++) {
            VoteInfoModel voteInfoModel = mRecordData.mVoteInfoModels.get(i);
            if (voteInfoModel.getUserID() == voterId) {
                return i;
            }
        }

        return 0;
    }
}
