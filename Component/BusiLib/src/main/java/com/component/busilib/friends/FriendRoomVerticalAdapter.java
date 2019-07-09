package com.component.busilib.friends;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.utils.U;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;

import java.util.ArrayList;
import java.util.List;

public class FriendRoomVerticalAdapter extends RecyclerView.Adapter {

    RecyclerOnItemClickListener<RecommendModel> mOnItemClickListener;
    List<RecommendModel> mDataList = new ArrayList<>();

    int ITEM_TYPE_HEAD = 1;
    int ITEM_TYPE_CONTENT = 2;

    public static Drawable mDrawable1;
    public static Drawable mDrawable2;
    public static Drawable mDrawable3;
    public static Drawable mDrawable4;

    public FriendRoomVerticalAdapter(RecyclerOnItemClickListener<RecommendModel> mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;

        mDrawable1 = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#D0EFFF"))
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();

        mDrawable2 = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#FFF6DC"))
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();

        mDrawable3 = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#F4D6D6"))
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();

        mDrawable4 = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#D6F4D8"))
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_HEAD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_room_verit_head_layout, parent, false);
            FriendRoomHeadViewHolder itemHolder = new FriendRoomHeadViewHolder(view, mOnItemClickListener);
            return itemHolder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_room_verit_item_layout, parent, false);
            FriendRoomVerticalViewHolder itemHolder = new FriendRoomVerticalViewHolder(view);
            itemHolder.setOnItemClickListener(mOnItemClickListener);
            return itemHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {

        } else {
            RecommendModel friendRoomModel = mDataList.get(position - 1);
            ((FriendRoomVerticalViewHolder) holder).bindData(friendRoomModel, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE_HEAD;
        } else {
            return ITEM_TYPE_CONTENT;
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size() + 1;
    }

    public List<RecommendModel> getDataList() {
        return mDataList;
    }

    public void setDataList(List<RecommendModel> dataList) {
        mDataList = dataList;
    }
}
