package com.module.rankingmode.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoModel;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.rankingmode.R;
import com.module.rankingmode.room.model.RoomData;
import com.module.rankingmode.room.model.RoomDataUtils;
import com.module.rankingmode.room.model.VoteInfoModel;
import com.module.rankingmode.song.model.SongModel;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class RecordItemView extends RelativeLayout {

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

    public RecordItemView(Context context) {
        super(context);
        init();
    }

    public RecordItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.record_athletics_item_layout, this);
        mTvSongName = (ExTextView)findViewById(R.id.tv_song_name);

        mSdvSingerIcon = (SimpleDraweeView)findViewById(R.id.sdv_singer_icon);
        mIvRanking = (ExImageView) findViewById(R.id.iv_ranking);
        mTvSingerName = (ExTextView) findViewById(R.id.tv_singer_name);
        mIvLightOne = (ExImageView) findViewById(R.id.iv_light_one);
        mIvLightTwo = (ExImageView) findViewById(R.id.iv_light_two);
        mIvLightThree = (ExImageView) findViewById(R.id.iv_light_three);
        mExImageViews[0] = mIvLightOne;
        mExImageViews[1] = mIvLightTwo;
        mExImageViews[2] = mIvLightThree;


        AvatarUtils.loadAvatarByUrl(mSdvSingerIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(0xFF85EAFF)
                        .build());


        mIvLightOneIcon = (SimpleDraweeView)findViewById(R.id.iv_light_one_icon);
        mIvLightOneTwo = (SimpleDraweeView)findViewById(R.id.iv_light_one_two);
        mIvLightOneThree = (SimpleDraweeView)findViewById(R.id.iv_light_one_three);

        mSimpleDraweeViews[0] = mIvLightOneIcon;
        mSimpleDraweeViews[1] = mIvLightOneTwo;
        mSimpleDraweeViews[2] = mIvLightOneThree;

        AvatarUtils.loadAvatarByUrl(mIvLightOneIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(0xFF85EAFF)
                        .build());

        AvatarUtils.loadAvatarByUrl(mIvLightOneTwo,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(0xFF85EAFF)
                        .build());

        AvatarUtils.loadAvatarByUrl(mIvLightOneThree,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(0xFF85EAFF)
                        .build());
    }

    public void setData(RoomData roomData, VoteInfoModel voteInfoModel){
        if(voteInfoModel == null){
            return;
        }

        UserInfoModel playerInfo = roomData.getUserInfo(voteInfoModel.getUserID());
        SongModel songModel = RoomDataUtils.getPlayerInfoUserId(roomData.getPlayerInfoList(), voteInfoModel.getItemID());

        AvatarUtils.loadAvatarByUrl(mSdvSingerIcon,
                AvatarUtils.newParamsBuilder(playerInfo.getAvatar())
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(0xFF85EAFF)
                        .build());

        mTvSingerName.setText(playerInfo.getUserNickname());
        mTvSongName.setText("《" + songModel.getItemName() + "》");

        switch (voteInfoModel.getRank()){
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
        if(true){
            Observable.range(0, voteInfoModel.getVoter().size()).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    mExImageViews[integer].setSelected(true);
                }
            });

            Observable.fromIterable(voteInfoModel.getVoter()).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    if(integer == 1){
                        AvatarUtils.loadAvatarByUrl(mSimpleDraweeViews[integer],
                                AvatarUtils.newParamsBuilder("local")
                                        .setCircle(true)
                                        .setGray(false)
                                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                                        .setBorderColor(0xFF85EAFF)
                                        .build());
                    }else {
                        UserInfoModel playerInfo = roomData.getUserInfo(integer);
                        AvatarUtils.loadAvatarByUrl(mSimpleDraweeViews[integer],
                                AvatarUtils.newParamsBuilder(playerInfo.getAvatar())
                                        .setCircle(true)
                                        .setGray(false)
                                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                                        .setBorderColor(0xFF85EAFF)
                                        .build());
                    }
                }
            });
        }
    }
}
