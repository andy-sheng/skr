package com.module.playways.grab.room.songmanager.tags;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.grab.createroom.model.SpecialModel;
import com.module.rank.R;

public class GrabTagsAdapter extends DiffAdapter<SpecialModel, RecyclerView.ViewHolder> {
    OnTagClickListener mOnTagClickListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grab_song_tag_item_layout, parent, false);
        ItemHolder viewHolder = new ItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SpecialModel model = mDataList.get(position);

        ItemHolder reportItemHolder = (ItemHolder) holder;
        reportItemHolder.bind(model);
    }

    public void setOnTagClickListener(OnTagClickListener onTagClickListener) {
        mOnTagClickListener = onTagClickListener;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ExTextView mTvSelectedTag;
        SpecialModel mSpecialModel;

        public ItemHolder(View itemView) {
            super(itemView);
            mTvSelectedTag = (ExTextView) itemView.findViewById(R.id.tv_selected_tag);
            mTvSelectedTag.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    if (mOnTagClickListener != null) {
                        mOnTagClickListener.onClick(mSpecialModel);
                    }
                }
            });
        }

        public void bind(SpecialModel model) {
            this.mSpecialModel = model;

            int color = Color.parseColor("#68ABD3");
            if (!TextUtils.isEmpty(model.getBgColor())) {
                color = Color.parseColor(model.getBgColor());
            }

            Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                    .setStrokeColor(Color.parseColor("#202239"))
                    .setStrokeWidth(U.getDisplayUtils().dip2px(2))
                    .setSolidColor(color)
                    .setCornersRadius(U.getDisplayUtils().dip2px(8))
                    .build();
            mTvSelectedTag.setBackground(drawable);
            mTvSelectedTag.setText(model.getTagName());
        }
    }

    public interface OnTagClickListener {
        void onClick(SpecialModel specialModel);
    }
}