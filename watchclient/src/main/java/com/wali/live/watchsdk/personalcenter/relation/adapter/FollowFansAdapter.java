package com.wali.live.watchsdk.personalcenter.relation.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.global.GlobalData;
import com.mi.live.data.data.UserListData;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.personalcenter.relation.contact.IFollowOptListener;
import com.wali.live.watchsdk.personalcenter.relation.holder.FollowFansHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhujianning on 18-6-21.
 */

public class FollowFansAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<UserListData> mDataList = new ArrayList<>();

    private IFollowOptListener mIFollowOptListener;

    public void setFollowOptListener(IFollowOptListener listener) {
        mIFollowOptListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(GlobalData.app()).inflate(R.layout.follow_fan_item, parent, false);
        FollowFansHolder holder = new FollowFansHolder(view);
        holder.setFollowOptListener(mIFollowOptListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder == null) {
            return;
        }

        if(holder instanceof  FollowFansHolder) {
            ((FollowFansHolder) holder).bind(mDataList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void setDataSourse(List<UserListData> list) {
        if (list != null) {
            mDataList = list;
            notifyDataSetChanged();
        }
    }

    public void addDataSourse(List<UserListData> list) {
        if (list != null) {
            List<UserListData> copyOfmDataList = new ArrayList<>(mDataList);
            copyOfmDataList.addAll(list);
            mDataList = copyOfmDataList;
            notifyDataSetChanged();
        }
    }

    public List<UserListData> getDatas() {
        return mDataList;
    }
}
