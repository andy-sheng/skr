package com.module.playways.grab.room.view.chorus;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.R;
import com.module.playways.doubleplay.DoubleRoomData;

public class ChorusSelfLyricAdapter extends DiffAdapter<ChorusSelfLyricAdapter.ChorusLineLyricModel, ChorusSelfLyricAdapter.ChorusSelfLyricHolder> {

    public final static String TAG = "ChorusSelfLyricAdapter";
    ChorusSelfSingCardView.DH mLeft;
    ChorusSelfSingCardView.DH mRight;
    boolean mLeftGiveUp = false;
    boolean mRightGiveUp = false;
    boolean mIsForVideo = false;
    DoubleRoomData mDoubleRoomData;

    int colorDisable = Color.parseColor("#beb19d");
    int colorEnable = Color.parseColor("#364E7C");

    public ChorusSelfLyricAdapter(ChorusSelfSingCardView.DH left, ChorusSelfSingCardView.DH right, boolean isForVideo) {
        mLeft = left;
        mRight = right;
        mIsForVideo = isForVideo;
        if (isForVideo) {
            colorEnable = Color.parseColor("#99ffffff");
            colorDisable = Color.parseColor("#33ffffff");
        }
    }

    public ChorusSelfLyricAdapter(ChorusSelfSingCardView.DH left, ChorusSelfSingCardView.DH right, boolean isForVideo, DoubleRoomData doubleRoomData) {
        this(left, right, isForVideo);
        mDoubleRoomData = doubleRoomData;
    }

    @NonNull
    @Override
    public ChorusSelfLyricHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chorus_self_lyric_item_layout, parent, false);
        ChorusSelfLyricHolder chorusSelfLyric = null;
        if (viewType == ChorusLineLyricModel.GRAB_TYPE) {
            chorusSelfLyric = new ChorusSelfLyricHolder(view);
        } else if (viewType == ChorusLineLyricModel.DOUBLE_TYPE) {
            chorusSelfLyric = new DoubleChorusSelfLyricHolder(view);
        }

        return chorusSelfLyric;
    }

    @Override
    public void onBindViewHolder(@NonNull ChorusSelfLyricHolder holder, int position) {
        ChorusLineLyricModel chorusLineLyricModel = mDataList.get(position);
        holder.bindData(chorusLineLyricModel, position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChorusLineLyricModel chorusLineLyricModel = mDataList.get(position);
        return chorusLineLyricModel.getType();
    }

    /**
     * 计算几个标记位
     */
    public void computeFlag() {
        mLeftGiveUp = false;
        mRightGiveUp = false;
        if (mLeft.mChorusRoundInfoModel != null) {
            if (mLeft.mChorusRoundInfoModel.isHasExit() || mLeft.mChorusRoundInfoModel.isHasGiveUp()) {
                // 左边的人不唱了
                mLeftGiveUp = true;
            }
        }

        if (mRight.mChorusRoundInfoModel != null) {
            if (mRight.mChorusRoundInfoModel.isHasExit() || mRight.mChorusRoundInfoModel.isHasGiveUp()) {
                // 右边的人不唱了
                mRightGiveUp = true;
            }
        }

    }

    class ChorusSelfLyricHolder extends RecyclerView.ViewHolder {

        View mBlankView;
        BaseImageView mAvatarIv;
        ExTextView mLyricLineTv;
        ChorusLineLyricModel mChorusLineLyricModel;

        public ChorusSelfLyricHolder(View itemView) {
            super(itemView);
            mAvatarIv = (BaseImageView) itemView.findViewById(R.id.avatar_iv);
            mLyricLineTv = (ExTextView) itemView.findViewById(R.id.lyric_line_tv);
            mBlankView = itemView.findViewById(R.id.blank_view);
        }

        public void bindData(ChorusLineLyricModel chorusLineLyricModel, int position) {
            mChorusLineLyricModel = chorusLineLyricModel;
            mLyricLineTv.setText(chorusLineLyricModel.getLyrics());

            if (!mLeftGiveUp && !mRightGiveUp) {
                mBlankView.setVisibility(View.VISIBLE);
                if (mChorusLineLyricModel.getUserInfoModel() != null &&
                        mChorusLineLyricModel.getUserInfoModel().getUserId() == MyUserInfoManager.getInstance().getUid()) {
                    // 左边是自己
                    mLyricLineTv.setTextColor(colorEnable);
                } else {
                    mLyricLineTv.setTextColor(colorDisable);
                }
                if (mChorusLineLyricModel.getUserInfoModel() != null) {
                    mAvatarIv.setVisibility(View.VISIBLE);
                    AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mChorusLineLyricModel.getUserInfoModel().getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColor(Color.WHITE)
                            .build());
                } else {
                    mAvatarIv.setVisibility(View.GONE);
                }

            } else if (mLeftGiveUp) {
                mBlankView.setVisibility(View.GONE);
                // 左边的人不唱了
                if (mLeft.mUserInfoModel != null && mLeft.mUserInfoModel.getUserId() == MyUserInfoManager.getInstance().getUid()) {
                    // 左边是自己 ,自己放弃了,设置成灰色
                    mLyricLineTv.setTextColor(colorDisable);
                } else {
                    mLyricLineTv.setTextColor(colorEnable);
                }
                if (position == 0) {
                    // 只有第一排有头像，头像设置成右边的人
                    if (mRight.mUserInfoModel != null) {
                        mAvatarIv.setVisibility(View.VISIBLE);
                        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mRight.mUserInfoModel.getAvatar())
                                .setCircle(true)
                                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                                .setBorderColor(Color.WHITE)
                                .build());
                    } else {
                        mAvatarIv.setVisibility(View.GONE);
                    }
                } else {
                    mAvatarIv.setVisibility(View.GONE);
                }
            } else if (mRightGiveUp) {
                mBlankView.setVisibility(View.GONE);
                if (mRight.mUserInfoModel != null && mRight.mUserInfoModel.getUserId() == MyUserInfoManager.getInstance().getUid()) {
                    // 右边是自己，自己放弃了,设置成灰色
                    mLyricLineTv.setTextColor(colorDisable);
                } else {
                    mLyricLineTv.setTextColor(colorEnable);
                }
                if (position == 0) {
                    // 只有第一排有头像，头像设置成右边的人
                    if (mLeft.mUserInfoModel != null) {
                        mAvatarIv.setVisibility(View.VISIBLE);
                        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mLeft.mUserInfoModel.getAvatar())
                                .setCircle(true)
                                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                                .setBorderColor(Color.WHITE)
                                .build());
                    } else {
                        mAvatarIv.setVisibility(View.GONE);
                    }
                } else {
                    mAvatarIv.setVisibility(View.GONE);
                }
            }

        }
    }

    class DoubleChorusSelfLyricHolder extends ChorusSelfLyricHolder {
        public DoubleChorusSelfLyricHolder(View itemView) {
            super(itemView);
            mAvatarIv = (BaseImageView) itemView.findViewById(R.id.avatar_iv);
            mLyricLineTv = (ExTextView) itemView.findViewById(R.id.lyric_line_tv);
            mBlankView = itemView.findViewById(R.id.blank_view);
        }

        @Override
        public void bindData(ChorusLineLyricModel chorusLineLyricModel, int position) {
            mChorusLineLyricModel = chorusLineLyricModel;
            mLyricLineTv.setText(chorusLineLyricModel.getLyrics());

            mBlankView.setVisibility(View.VISIBLE);
            if (mChorusLineLyricModel.getUserInfoModel() != null &&
                    mChorusLineLyricModel.getUserInfoModel().getUserId() == MyUserInfoManager.getInstance().getUid()) {
                // 左边是自己
                mLyricLineTv.setTextColor(colorEnable);
            } else {
                mLyricLineTv.setTextColor(colorDisable);
            }
            if (mChorusLineLyricModel.getUserInfoModel() != null) {
                mAvatarIv.setVisibility(View.VISIBLE);
                AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mDoubleRoomData.getAvatarById(mChorusLineLyricModel.getUserInfoModel().getUserId()))
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(Color.WHITE)
                        .build());
            } else {
                mAvatarIv.setVisibility(View.GONE);
            }
        }
    }

    public static class ChorusLineLyricModel {
        public final static int GRAB_TYPE = 0;
        public final static int DOUBLE_TYPE = 1;
        public UserInfoModel UserInfoModel;
        public String lyrics;
        int type;

        public ChorusLineLyricModel(UserInfoModel userInfoModel, String lyrics, int type) {
            UserInfoModel = userInfoModel;
            this.lyrics = lyrics;
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public String getLyrics() {
            return lyrics;
        }

        public com.common.core.userinfo.model.UserInfoModel getUserInfoModel() {
            return UserInfoModel;
        }
    }

}




