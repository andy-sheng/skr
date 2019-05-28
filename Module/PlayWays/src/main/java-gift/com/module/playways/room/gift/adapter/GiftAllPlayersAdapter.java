package com.module.playways.room.gift.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.R;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;

public class GiftAllPlayersAdapter extends DiffAdapter<GrabPlayerInfoModel, RecyclerView.ViewHolder> {
    GrabPlayerInfoModel mSelectedGrabPlayerInfoModel;

    OnClickPlayerListener mOnClickPlayerListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_item_view_layout, parent, false);
        ItemHolder viewHolder = new ItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GrabPlayerInfoModel model = mDataList.get(position);

        ItemHolder reportItemHolder = (ItemHolder) holder;
        reportItemHolder.bind(model);
    }

    public void setSelectedGrabPlayerInfoModel(GrabPlayerInfoModel selectedGrabPlayerInfoModel) {
        mSelectedGrabPlayerInfoModel = selectedGrabPlayerInfoModel;
    }

    public void setOnClickPlayerListener(OnClickPlayerListener onClickPlayerListener) {
        mOnClickPlayerListener = onClickPlayerListener;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        BaseImageView mIvPlayerIcon;
        ImageView mIvSelectedIcon;

        GrabPlayerInfoModel mGrabPlayerInfoModel;

        public ItemHolder(View itemView) {
            super(itemView);
            mIvPlayerIcon = (BaseImageView) itemView.findViewById(R.id.iv_player_icon);
            mIvSelectedIcon = (ImageView) itemView.findViewById(R.id.iv_selected_icon);
            mIvPlayerIcon.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    GrabPlayerInfoModel grabPlayerInfoModel = mSelectedGrabPlayerInfoModel;
                    mSelectedGrabPlayerInfoModel = mGrabPlayerInfoModel;
                    update(mSelectedGrabPlayerInfoModel);

                    if (grabPlayerInfoModel != null) {
                        update(grabPlayerInfoModel);
                    }

                    if (mOnClickPlayerListener != null) {
                        mOnClickPlayerListener.onClick(mGrabPlayerInfoModel);
                    }
                }
            });
        }

        public void bind(GrabPlayerInfoModel model) {
            this.mGrabPlayerInfoModel = model;
            AvatarUtils.loadAvatarByUrl(mIvPlayerIcon,
                    AvatarUtils.newParamsBuilder(model.getUserInfo().getAvatar())
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setCircle(true)
                            .build());

            if (mSelectedGrabPlayerInfoModel != null && mSelectedGrabPlayerInfoModel.getUserID() == mGrabPlayerInfoModel.getUserID()) {
                mIvSelectedIcon.setVisibility(View.VISIBLE);
            } else {
                mIvSelectedIcon.setVisibility(View.GONE);
            }
        }
    }

    public interface OnClickPlayerListener {
        void onClick(GrabPlayerInfoModel grabPlayerInfoModel);
    }
}
