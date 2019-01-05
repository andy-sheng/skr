package com.zq.relation.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.zq.relation.view.RelationHolderView;

import java.util.ArrayList;
import java.util.List;

public class RelationAdapter extends RecyclerView.Adapter {

    List<UserInfoModel> mUserInfos = new ArrayList<>();

    private int mode;
    RecyclerOnItemClickListener mRecyclerOnItemClickListener;

    public RelationAdapter(int mode, RecyclerOnItemClickListener mRecyclerOnItemClickListener) {
        this.mode = mode;
        this.mRecyclerOnItemClickListener = mRecyclerOnItemClickListener;
    }

    public void addData(List<UserInfoModel> list) {
        mUserInfos.addAll(list);
    }

    public List<UserInfoModel> getData() {
        return mUserInfos;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.relation_view_holder_item, parent, false);
        RelationHolderView viewHolder = new RelationHolderView(view, mRecyclerOnItemClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RelationHolderView) {
            RelationHolderView songInfoHolder = (RelationHolderView) holder;
            UserInfoModel songModel = mUserInfos.get(position);
            songInfoHolder.bind(mode, position, songModel);
        }
    }

    @Override
    public int getItemCount() {
        return mUserInfos.size();
    }
}
