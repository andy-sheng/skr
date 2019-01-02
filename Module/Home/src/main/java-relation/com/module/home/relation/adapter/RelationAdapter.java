package com.module.home.relation.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoModel;
import com.module.home.R;
import com.module.home.relation.view.RelationHolderView;

import java.util.ArrayList;
import java.util.List;

public class RelationAdapter extends RecyclerView.Adapter {

    List<UserInfoModel> mUserInfos = new ArrayList<>();

    private int mode;

    public RelationAdapter(int mode) {
        this.mode = mode;
        UserInfoModel userInfoModel = new UserInfoModel();
        userInfoModel.setAvatar(MyUserInfoManager.getInstance().getAvatar());
        userInfoModel.setUserId((int) MyUserInfoManager.getInstance().getUid());
        userInfoModel.setUserNickname(MyUserInfoManager.getInstance().getNickName());
        for (int i = 0; i < 10; i++) {
            mUserInfos.add(userInfoModel);
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.relation_view_holder_item, parent, false);
        RelationHolderView viewHolder = new RelationHolderView(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RelationHolderView) {
            RelationHolderView songInfoHolder = (RelationHolderView) holder;
            UserInfoModel songModel = mUserInfos.get(position);
            songInfoHolder.bind(mode, songModel);
        }
    }

    @Override
    public int getItemCount() {
        return mUserInfos.size();
    }
}
