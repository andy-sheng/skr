package com.component.busilib.friends;


import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfoManager;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.ex.ExConstraintLayout;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.component.busilib.view.VoiceChartView;
import com.component.level.utils.LevelConfigUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;

public class FriendRoomVerticalViewHolder extends RecyclerView.ViewHolder {

    public final String TAG = "FriendRoomVerticalViewHolder";

    FriendRoomVerticalAdapter.FriendRoomClickListener mOnItemClickListener;

    RecommendModel mFriendRoomModel;
    int position;

    ConstraintLayout mBackground;
    SimpleDraweeView mRecommendTagSdv;
    SimpleDraweeView mMediaTagSdv;
    SimpleDraweeView mAvatarIv;
    ImageView mLevelBg;
    TextView mLevelDesc;
    ExTextView mNameTv;
    ExTextView mRoomPlayerNumTv;
    ExTextView mRoomInfoTv;

    ExConstraintLayout mVoiceArea;
    ImageView mPlayBg;
    ImageView mPlayIv;
    TextView mVoiceName;
    VoiceChartView mVoiceChartView;

    public FriendRoomVerticalViewHolder(View itemView) {
        super(itemView);

        mBackground = itemView.findViewById(R.id.background);
        mRecommendTagSdv = itemView.findViewById(R.id.recommend_tag_sdv);
        mMediaTagSdv = itemView.findViewById(R.id.media_tag_sdv);
        mAvatarIv = itemView.findViewById(R.id.avatar_iv);
        mLevelBg = itemView.findViewById(R.id.level_bg);
        mLevelDesc = itemView.findViewById(R.id.level_desc);
        mNameTv = itemView.findViewById(R.id.name_tv);
        mRoomPlayerNumTv = itemView.findViewById(R.id.room_player_num_tv);
        mRoomInfoTv = itemView.findViewById(R.id.room_info_tv);

        mVoiceArea = itemView.findViewById(R.id.voice_area);
        mPlayBg = itemView.findViewById(R.id.play_bg);
        mPlayIv = itemView.findViewById(R.id.play_iv);
        mVoiceName = itemView.findViewById(R.id.voice_name);
        mVoiceChartView = itemView.findViewById(R.id.voice_chart_view);

        itemView.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onClickFriendRoom(position, mFriendRoomModel);
                }
            }
        });

        mVoiceArea.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onClickFriendVoice(position, mFriendRoomModel);
                }
            }
        });
    }

    public void setOnItemClickListener(FriendRoomVerticalAdapter.FriendRoomClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void bindData(RecommendModel friendRoomModel, int position) {
        this.mFriendRoomModel = friendRoomModel;
        this.position = position;

        int colorIndex = position % 4;
        if (colorIndex == 1) {
            mBackground.setBackground(FriendRoomVerticalAdapter.bgDrawable2);
            mPlayBg.setBackground(FriendRoomVerticalAdapter.playDrawable2);
        } else if (colorIndex == 2) {
            mBackground.setBackground(FriendRoomVerticalAdapter.bgDrawable3);
            mPlayBg.setBackground(FriendRoomVerticalAdapter.playDrawable3);
        } else if (colorIndex == 3) {
            mBackground.setBackground(FriendRoomVerticalAdapter.bgDrawable4);
            mPlayBg.setBackground(FriendRoomVerticalAdapter.playDrawable4);
        } else {
            mBackground.setBackground(FriendRoomVerticalAdapter.bgDrawable1);
            mPlayBg.setBackground(FriendRoomVerticalAdapter.playDrawable1);
        }

        if (friendRoomModel.getUserInfo() != null) {
            AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(friendRoomModel.getUserInfo().getAvatar())
                    .setCircle(true)
                    .build());
            if (friendRoomModel.getUserInfo().getRanking() != null && LevelConfigUtils.getAvatarLevelBg(friendRoomModel.getUserInfo().getRanking().getMainRanking()) != 0) {
                mLevelBg.setVisibility(View.VISIBLE);
                mLevelDesc.setVisibility(View.VISIBLE);
                mLevelBg.setBackground(U.getDrawable(LevelConfigUtils.getAvatarLevelBg(friendRoomModel.getUserInfo().getRanking().getMainRanking())));
                mLevelDesc.setText(friendRoomModel.getUserInfo().getRanking().getRankingDesc());
            } else {
                mLevelBg.setVisibility(View.GONE);
                mLevelDesc.setVisibility(View.GONE);
            }
        }

        mVoiceChartView.stop();
        if (friendRoomModel.getVoiceInfo() != null) {
            mVoiceArea.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(friendRoomModel.getVoiceInfo().getSongName())) {
                mVoiceName.setText(friendRoomModel.getVoiceInfo().getSongName());
            } else {
                if (friendRoomModel.getVoiceInfo().getDuration() > (friendRoomModel.getVoiceInfo().getDuration() / 1000) * 1000) {
                    mVoiceName.setText((friendRoomModel.getVoiceInfo().getDuration() / 1000 + 1) + "s");
                } else {
                    mVoiceName.setText(friendRoomModel.getVoiceInfo().getDuration() / 1000 + "s");
                }
            }
        } else {
            mVoiceArea.setVisibility(View.GONE);
        }

        if (mFriendRoomModel != null && mFriendRoomModel.getUserInfo() != null && mFriendRoomModel.getRoomInfo() != null) {
            if (!TextUtils.isEmpty(mFriendRoomModel.getRoomInfo().getRoomTagURL())) {
                mRecommendTagSdv.setVisibility(View.VISIBLE);
                FrescoWorker.loadImage(mRecommendTagSdv, ImageFactory.newPathImage(mFriendRoomModel.getRoomInfo().getRoomTagURL())
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build());
            } else {
                mRecommendTagSdv.setVisibility(View.GONE);
            }

            String nickName = UserInfoManager.getInstance().getRemarkName(mFriendRoomModel.getUserInfo().getUserId(), mFriendRoomModel.getUserInfo().getNickname());
            if (!TextUtils.isEmpty(nickName)) {
                mNameTv.setVisibility(View.VISIBLE);
                mNameTv.setText(nickName);
            } else {
                mNameTv.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(mFriendRoomModel.getRoomInfo().getMediaTagURL())) {
                mMediaTagSdv.setVisibility(View.VISIBLE);
                FrescoWorker.loadImage(mMediaTagSdv, ImageFactory.newPathImage(mFriendRoomModel.getRoomInfo().getMediaTagURL())
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build());
            } else {
                mMediaTagSdv.setVisibility(View.GONE);
            }

            mRoomPlayerNumTv.setText(mFriendRoomModel.getRoomInfo().getInPlayersNum() + "/" + mFriendRoomModel.getRoomInfo().getTotalPlayersNum());

            if (!TextUtils.isEmpty(mFriendRoomModel.getRoomInfo().getRoomName())) {
                mRoomInfoTv.setVisibility(View.VISIBLE);
                mRoomInfoTv.setText(mFriendRoomModel.getRoomInfo().getRoomName());
            } else {
                if (mFriendRoomModel.getTagInfo() != null) {
                    // 只显示专场名称
                    SpannableStringBuilder stringBuilder = new SpanUtils()
                            .append(mFriendRoomModel.getTagInfo().getTagName())
                            .create();
                    mRoomInfoTv.setVisibility(View.VISIBLE);
                    mRoomInfoTv.setText(stringBuilder);
                } else {
                    mRoomInfoTv.setVisibility(View.GONE);
                    MyLog.w(TAG, "服务器数据有问题" + " friendRoomModel=" + friendRoomModel + " position=" + position);
                }
            }
        } else {
            MyLog.w(TAG, "bindData" + " friendRoomModel=" + friendRoomModel + " position=" + position);
        }
    }

    public void startPlay() {
        mPlayIv.setSelected(true);
        mVoiceName.setVisibility(View.INVISIBLE);
        mVoiceChartView.setVisibility(View.VISIBLE);
        mVoiceChartView.start();
    }

    public void stopPlay() {
        mPlayIv.setSelected(false);
        mVoiceName.setVisibility(View.VISIBLE);
        mVoiceChartView.setVisibility(View.GONE);
        mVoiceChartView.stop();
    }
}
