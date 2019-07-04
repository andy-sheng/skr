package com.zq.relation.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.zq.relation.view.RelationHolderView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RelationAdapter extends RecyclerView.Adapter {

    List<UserInfoModel> mUserInfos;

    int mMode = 0;
    RecyclerOnItemClickListener mRecyclerOnItemClickListener;

    public static Drawable mUnFollowDrawable;  // 未关注
    public static Drawable mFollowDrawable;  // 已关注
    public static Drawable mFriendDrawable;  // 好友

    public RelationAdapter(int mode, RecyclerOnItemClickListener mRecyclerOnItemClickListener) {
        this.mMode = mode;
        this.mRecyclerOnItemClickListener = mRecyclerOnItemClickListener;
        mUserInfos = new ArrayList<>();

        mUnFollowDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#FFC15B"))
                .setStrokeColor(Color.parseColor("#AD6C00"))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1f))
                .setCornersRadius(U.getDisplayUtils().dip2px(16))
                .build();

        mFollowDrawable = new DrawableCreator.Builder()
                .setStrokeColor(Color.parseColor("#3B4E79"))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1f))
                .setCornersRadius(U.getDisplayUtils().dip2px(16))
                .build();

        mFriendDrawable = new DrawableCreator.Builder()
                .setStrokeColor(Color.parseColor("#AD6C00"))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1f))
                .setCornersRadius(U.getDisplayUtils().dip2px(16))
                .build();
    }

    public void addData(List<UserInfoModel> list) {
        mUserInfos.addAll(list);
        notifyDataSetChanged();
    }

    public void setData(List<UserInfoModel> list) {
        mUserInfos.clear();
        if(list!=null) {
            mUserInfos.addAll(list);
        }
        notifyDataSetChanged();
    }

    public List<UserInfoModel> getData() {
        return mUserInfos;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.relation_view_holder_item, parent, false);
        RelationHolderView viewHolder = new RelationHolderView(view, mMode, mRecyclerOnItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RelationHolderView) {
            RelationHolderView songInfoHolder = (RelationHolderView) holder;
            UserInfoModel songModel = mUserInfos.get(position);
            songInfoHolder.bind(position, songModel);
        }
    }

    @Override
    public int getItemCount() {
        return mUserInfos.size();
    }
}
