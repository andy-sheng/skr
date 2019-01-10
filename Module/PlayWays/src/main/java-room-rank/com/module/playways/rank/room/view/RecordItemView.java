package com.module.playways.rank.room.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.rank.prepare.GameModeType;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.rank.room.model.RoomData;
import com.module.playways.rank.room.model.RoomDataUtils;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class RecordItemView extends RelativeLayout {
    public final static String TAG = "RecordItemView";
    SimpleDraweeView mSdvSingerIcon;
    ExImageView mIvRanking;
    ExTextView mTvSingerName;
    ExImageView mIvLightOne;
    ExImageView mIvLightTwo;
    ExImageView mIvLightThree;

    SimpleDraweeView mIvLightOneIcon;
    SimpleDraweeView mIvLightOneTwo;
    SimpleDraweeView mIvLightOneThree;

    ExImageView[] mExImageViews = new ExImageView[3];
    SimpleDraweeView[] mSimpleDraweeViews = new SimpleDraweeView[3];

    ExTextView mTvSongName;

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

        UserInfoModel playerInfo = roomData.getUserInfo(voteInfoModel.getUserID());
        SongModel songModel = RoomDataUtils.getPlayerSongInfoUserId(roomData.getPlayerInfoList(), voteInfoModel.getUserID());

        AvatarUtils.loadAvatarByUrl(mSdvSingerIcon,
                AvatarUtils.newParamsBuilder(playerInfo.getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(strokeColor)
                        .build());

        mTvSingerName.setText(playerInfo.getNickname());
        mTvSongName.setText("《" + songModel.getItemName() + "》");

        switch (voteInfoModel.getRank()) {
            case 1:
                mIvRanking.setBackground(getResources().getDrawable(R.drawable.ic_medal1_normal));
                break;
            case 2:
                mIvRanking.setBackground(getResources().getDrawable(R.drawable.ic_medal2_normal));
                break;
            case 3:
                mIvRanking.setBackground(getResources().getDrawable(R.drawable.ic_medal3_normal));
                break;
        }

        //这里需要判读是娱乐还是竞技

        Observable.range(0, voteInfoModel.getVoter().size()).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                mExImageViews[integer].setSelected(true);
            }
        }, throwable -> MyLog.e(throwable));

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
                        int borderColor = getUserIndex(voterId);
                        UserInfoModel playerInfo = roomData.getUserInfo(voterId);
                        AvatarUtils.loadAvatarByUrl(mSimpleDraweeViews[integer],
                                AvatarUtils.newParamsBuilder(playerInfo.getAvatar())
                                        .setCircle(true)
                                        .setGray(false)
                                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                                        .setBorderColor(borderColor == 0 ? 0xFFFF79A9 : 0xFF85EAFF)
                                        .build());
                    }
                }
            }, throwable -> MyLog.e(throwable));
        }
    }

    private int getUserIndex(long voterId){
        for (int i = 0; i < mRecordData.mVoteInfoModels.size(); i++){
            VoteInfoModel voteInfoModel = mRecordData.mVoteInfoModels.get(i);
            if(voteInfoModel.getUserID() == voterId){
                return i;
            }
        }

        return 0;
    }
}
