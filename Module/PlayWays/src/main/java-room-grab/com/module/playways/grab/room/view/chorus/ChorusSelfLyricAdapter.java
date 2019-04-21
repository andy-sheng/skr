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

public class ChorusSelfLyricAdapter extends DiffAdapter<String, ChorusSelfLyricAdapter.ChorusSelfLyric> {
    
    public final static String TAG = "ChorusSelfLyricAdapter";

    UserInfoModel mLeftUserInfoModel;
    UserInfoModel mRightUserInfoModel;

    public void setUserInfos(UserInfoModel mLeftUserInfoModel, UserInfoModel mRightUserInfoModel) {
        this.mLeftUserInfoModel = mLeftUserInfoModel;
        this.mRightUserInfoModel = mRightUserInfoModel;
    }

    @NonNull
    @Override
    public ChorusSelfLyric onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chorus_self_lyric_item_layout, parent, false);
        ChorusSelfLyric chorusSelfLyric = new ChorusSelfLyric(view);
        return chorusSelfLyric;
    }

    @Override
    public void onBindViewHolder(@NonNull ChorusSelfLyric holder, int position) {
        String string = mDataList.get(position);
        holder.bindData(string, position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class ChorusSelfLyric extends RecyclerView.ViewHolder {

        BaseImageView mAvatarIv;
        ExTextView mLyricLineTv;

        public ChorusSelfLyric(View itemView) {
            super(itemView);

            mAvatarIv = (BaseImageView) itemView.findViewById(R.id.avatar_iv);
            mLyricLineTv = (ExTextView) itemView.findViewById(R.id.lyric_line_tv);
        }

        public void bindData(String text, int position) {
            mLyricLineTv.setText(text);
            if (position % 2 == 0) {
                // left
                if (mLeftUserInfoModel != null) {
                    AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mLeftUserInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColor(Color.WHITE)
                            .build());
                } else {
                    MyLog.w(TAG, "bindData" + " text=" + text + " position=" + position);
                }
            } else {
                // right
                if (mRightUserInfoModel != null) {
                    AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mRightUserInfoModel.getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColor(Color.WHITE)
                            .build());
                } else {
                    MyLog.w(TAG, "bindData" + " text=" + text + " position=" + position);
                }
            }
        }
    }
}




