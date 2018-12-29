package com.module.playways.rank.room.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.rank.room.model.RoomData;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.rank.R;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

public class RecordTitleView extends RelativeLayout {
    RoomData mRoomData;

    RecordData mRecordData;

    SimpleDraweeView mSdvOwnIcon;
    ExTextView mTvOwnerName;
    ExImageView mIvOwnRecord;
    ExTextView mTvOwnRecord;

    ExTextView mTvLightCount;
    ExImageView mIvLightCount;
    ExTextView mTvSongName;

    public RecordTitleView(Context context) {
        this(context, null);
    }

    public RecordTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(RecordData recordData, RoomData roomData) {
        mRoomData = roomData;
        mRecordData = recordData;
        //娱乐模式
        if (true) {
            inflate(getContext(), R.layout.record_title_happy, this);
            mTvLightCount = (ExTextView) findViewById(R.id.tv_light_count);
            mIvLightCount = (ExImageView) findViewById(R.id.iv_light_count);
            mSdvOwnIcon = (SimpleDraweeView) findViewById(R.id.sdv_own_icon);
            mTvOwnerName = (ExTextView) findViewById(R.id.tv_owner_name);
            mTvOwnRecord = (ExTextView) findViewById(R.id.tv_own_record);

            AvatarUtils.loadAvatarByUrl(mSdvOwnIcon,
                    AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                            .setCircle(true)
                            .setGray(false)
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(Color.WHITE)
                            .build());

            mTvOwnerName.setText(MyUserInfoManager.getInstance().getNickName());
            mTvLightCount.setText("亮灯X" + recordData.getSelfVoteInfoModel().getVoter().size());
        } else {
            inflate(getContext(), R.layout.record_title_athletics, this);
        }

        mTvSongName = (ExTextView) findViewById(R.id.tv_song_name);
        mIvOwnRecord = (ExImageView) findViewById(R.id.iv_own_record);


        mTvSongName.setText("《" + mRoomData.getSongModel().getItemName() + "》");

        Observable.fromIterable(mRecordData.mVoteInfoModels)
                .filter(new Predicate<VoteInfoModel>() {
                    @Override
                    public boolean test(VoteInfoModel voteInfoModel) throws Exception {
                        return voteInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid();
                    }
                })
                .subscribe(new Consumer<VoteInfoModel>() {
                    @Override
                    public void accept(VoteInfoModel voteInfoModel) throws Exception {
                        Drawable drawable = null;
                        String str = "";
                        switch (voteInfoModel.getRank()) {
                            case 0:
                                drawable = getResources().getDrawable(R.drawable.ic_medal1_normal);
                                str = "冠军";
                                break;
                            case 2:
                                drawable = getResources().getDrawable(R.drawable.ic_medal2_normal);
                                str = "亚军";
                                break;
                            case 3:
                                drawable = getResources().getDrawable(R.drawable.ic_medal3_normal);
                                str = "季军";
                                break;
                        }
                        mIvOwnRecord.setBackground(drawable);

                        if (mTvOwnRecord != null) {
                            mTvOwnRecord.setText(str);
                        }
                    }
                });
    }
}
