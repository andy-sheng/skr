package com.wali.live.watchsdk.fans.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.global.GlobalData;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.holder.GroupNotify.ApplyJoinNotifyHolder;
import com.wali.live.watchsdk.fans.holder.GroupNotify.BaseNotifyHolder;
import com.wali.live.watchsdk.fans.holder.GroupNotify.HandleJoinNotifyHolder;
import com.wali.live.watchsdk.fans.holder.GroupNotify.RemoveNotifyHolder;
import com.wali.live.watchsdk.fans.holder.GroupNotify.UpdateNotifyHolder;
import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;
import com.wali.live.watchsdk.fans.push.type.GroupNotifyType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyh on 2017/11/23.
 */

public class GroupNotifyAdapter extends RecyclerView.Adapter<BaseNotifyHolder> {

    private List<GroupNotifyBaseModel> mGroupNotifyBaseModels = new ArrayList<>();
    private BaseNotifyHolder.OnItemClickListener mListener;
    private final static int TYPE_EMPTY = 10;

    public void setListener(BaseNotifyHolder.OnItemClickListener listener) {
        mListener = listener;
    }

    public void setGroupNotifyBaseModels(List<GroupNotifyBaseModel> groupNotifyBaseModels) {
        if (groupNotifyBaseModels != null) {
            mGroupNotifyBaseModels.clear();
            mGroupNotifyBaseModels.addAll(groupNotifyBaseModels);
            notifyDataSetChanged();
        }
    }

    @Override
    public BaseNotifyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_group_notify_item, parent, false);
        BaseNotifyHolder holder = null;
        switch (viewType) {
            case TYPE_EMPTY:
                view = LayoutInflater.from(GlobalData.app()).inflate(R.layout.empty_view, parent, false);
                holder = new BaseNotifyHolder(view);
                break;
            case GroupNotifyType.APPLY_JOIN_GROUP_NOTIFY:
                holder = new ApplyJoinNotifyHolder(view);
                holder.setListener(mListener);
                break;
            case GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY:
            case GroupNotifyType.REJECT_JOIN_GROUP_NOTIFY:
                holder = new HandleJoinNotifyHolder(view);
                break;
            case GroupNotifyType.BE_GROUP_MEM_NOTIFY:
            case GroupNotifyType.BE_GROUP_MANAGER_NOTIFY:
            case GroupNotifyType.CANCEL_GROUP_MANAGER_NOTIFY:
                holder = new UpdateNotifyHolder(view);
                break;
            case GroupNotifyType.REMOVE_GROUP_MEM_NOTIFY:
                holder = new RemoveNotifyHolder(view);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(BaseNotifyHolder holder, int position) {
        if (getItemViewType(position) != TYPE_EMPTY) {
            holder.bindHolder(mGroupNotifyBaseModels.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (mGroupNotifyBaseModels.isEmpty()) {
            return 1;
        }
        return mGroupNotifyBaseModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mGroupNotifyBaseModels.isEmpty()) {
            return TYPE_EMPTY;
        }
        return mGroupNotifyBaseModels.get(position).getNotificationType();
    }
}
