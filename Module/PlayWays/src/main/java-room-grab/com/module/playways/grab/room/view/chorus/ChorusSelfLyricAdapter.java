package com.module.playways.grab.room.view.chorus;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.R;

public class ChorusSelfLyricAdapter extends DiffAdapter<String, ChorusSelfLyricAdapter.ChorusSelfLyricHolder> {

    public final static String TAG = "ChorusSelfLyricAdapter";

    ChorusSelfSingCardView.DH mLeft;
    ChorusSelfSingCardView.DH mRight;

    boolean mLeftGiveUp = false;
    boolean mRightGiveUp = false;

    public ChorusSelfLyricAdapter(ChorusSelfSingCardView.DH left, ChorusSelfSingCardView.DH right) {
        mLeft = left;
        mRight = right;
    }

    @NonNull
    @Override
    public ChorusSelfLyricHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chorus_self_lyric_item_layout, parent, false);
        ChorusSelfLyricHolder chorusSelfLyric = new ChorusSelfLyricHolder(view);
        return chorusSelfLyric;
    }

    @Override
    public void onBindViewHolder(@NonNull ChorusSelfLyricHolder holder, int position) {
        String string = mDataList.get(position);
        holder.bindData(string, position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
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

    int colorDisable = Color.parseColor("#beb19d");
    int colorEnable = Color.parseColor("#364E7C");

    class ChorusSelfLyricHolder extends RecyclerView.ViewHolder {

        View mBlankView;
        BaseImageView mAvatarIv;
        ExTextView mLyricLineTv;

        public ChorusSelfLyricHolder(View itemView) {
            super(itemView);
            mAvatarIv = (BaseImageView) itemView.findViewById(R.id.avatar_iv);
            mLyricLineTv = (ExTextView) itemView.findViewById(R.id.lyric_line_tv);
            mBlankView =  itemView.findViewById(R.id.blank_view);
        }

        public void bindData(String text, int position) {
            mLyricLineTv.setText(text);

            if (!mLeftGiveUp && !mRightGiveUp) {
                mBlankView.setVisibility(View.VISIBLE);
                if (position % 2 == 0) {
                    if (mLeft.mUserInfoModel.getUserId() == MyUserInfoManager.getInstance().getUid()) {
                        // 左边是自己
                        mLyricLineTv.setTextColor(colorEnable);
                    } else {
                        mLyricLineTv.setTextColor(colorDisable);
                    }
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
                    if (mRight.mUserInfoModel.getUserId() == MyUserInfoManager.getInstance().getUid()) {
                        // 右边是自己
                        mLyricLineTv.setTextColor(colorEnable);
                    } else {
                        mLyricLineTv.setTextColor(colorDisable);
                    }
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
                }
            } else if (mLeftGiveUp) {
                mBlankView.setVisibility(View.GONE);
                // 左边的人不唱了
                if (mLeft.mUserInfoModel.getUserId() == MyUserInfoManager.getInstance().getUid()) {
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
                if (mRight.mUserInfoModel.getUserId() == MyUserInfoManager.getInstance().getUid()) {
                    // 右边是自己，自己放弃了,设置成灰色
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
            }

        }
    }
}




