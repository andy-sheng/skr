package com.module.playways.grab.room.view.chorus;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.module.rank.R;

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


    class ChorusSelfLyricHolder extends RecyclerView.ViewHolder {

        BaseImageView mAvatarIv;
        ExTextView mLyricLineTv;

        public ChorusSelfLyricHolder(View itemView) {
            super(itemView);

            mAvatarIv = (BaseImageView) itemView.findViewById(R.id.avatar_iv);
            mLyricLineTv = (ExTextView) itemView.findViewById(R.id.lyric_line_tv);
        }

        public void bindData(String text, int position) {

            mLyricLineTv.setText(text);
            if (position % 2 == 0) {
                // left
                if (mLeft.mUserInfoModel != null && !mLeftGiveUp) {
                    mLyricLineTv.setTextColor(Color.parseColor("#364E7C"));
                    mAvatarIv.setVisibility(View.VISIBLE);
                    AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mLeft.mUserInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColor(Color.WHITE)
                            .build());
                } else {
                    mAvatarIv.setVisibility(View.GONE);
                    mLyricLineTv.setTextColor(Color.parseColor("#beb19d"));
                    MyLog.w(TAG, "bindData" + " text=" + text + " position=" + position);
                }
            } else {
                // right
                if (mRight.mUserInfoModel != null && mRightGiveUp) {
                    mLyricLineTv.setTextColor(Color.parseColor("#364E7C"));
                    mAvatarIv.setVisibility(View.VISIBLE);
                    AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mRight.mUserInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColor(Color.WHITE)
                            .build());
                } else {
                    mAvatarIv.setVisibility(View.GONE);
                    mLyricLineTv.setTextColor(Color.parseColor("#beb19d"));
                    MyLog.w(TAG, "bindData" + " text=" + text + " position=" + position);
                }
            }
        }
    }
}




