package com.wali.live.watchsdk.fans.task.adapter;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.type.BaseTypeModel;
import com.wali.live.watchsdk.fans.task.holder.GroupTaskHeaderHolder;
import com.wali.live.watchsdk.fans.task.holder.GroupTaskHolder;
import com.wali.live.watchsdk.fans.task.holder.LimitTaskHeaderHolder;
import com.wali.live.watchsdk.fans.task.holder.LimitTaskHolder;
import com.wali.live.watchsdk.fans.task.model.GroupJobHeaderModel;
import com.wali.live.watchsdk.fans.task.model.GroupJobListModel;
import com.wali.live.watchsdk.fans.task.model.GroupJobModel;
import com.wali.live.watchsdk.fans.task.model.LimitJobHeaderModel;
import com.wali.live.watchsdk.fans.task.model.LimitJobModel;
import com.wali.live.watchsdk.fans.task.model.TaskViewType;
import com.wali.live.watchsdk.lit.recycler.adapter.EmptyRecyclerAdapter;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 2017/11/13.
 */
public class FansTaskAdapter extends EmptyRecyclerAdapter {
    private List<BaseTypeModel> mDataList;

    public FansTaskAdapter() {
        mDataList = new ArrayList();
    }

    public void setDataList(GroupJobListModel model, @Nullable FansGroupDetailModel detailModel) {
        mDataList.clear();
        List<GroupJobModel> groupJobList = model.getGroupJobList();
        if (groupJobList.size() > 0) {
            mDataList.add(new GroupJobHeaderModel(detailModel));
            mDataList.addAll(groupJobList);
        }
        List<LimitJobModel> limitJobList = model.getLimitGroupJobList();
        if (groupJobList.size() > 0) {
            mDataList.add(new LimitJobHeaderModel());
            mDataList.addAll(limitJobList);
        }
        notifyDataSetChanged();
    }

    @Override
    protected int getDataCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    protected BaseHolder onCreateHolder(ViewGroup parent, int viewType) {
        BaseHolder holder = null;
        View view;
        switch (viewType) {
            case TaskViewType.TYPE_GROUP_TASK:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fans_task_group_item, parent, false);
                holder = new GroupTaskHolder(view);
                break;
            case TaskViewType.TYPE_LIMIT_TASK:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fans_task_group_item, parent, false);
                holder = new LimitTaskHolder(view);
                break;
            case TaskViewType.TYPE_GROUP_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fans_task_group_header_item, parent, false);
                holder = new GroupTaskHeaderHolder(view);
                break;
            case TaskViewType.TYPE_LIMIT_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fans_task_limit_header_item, parent, false);
                holder = new LimitTaskHeaderHolder(view);
                break;
        }
        return holder;
    }

    @Override
    protected void onBindHolder(BaseHolder holder, int position) {
        holder.bindModel(mDataList.get(position), position);
    }

    @Override
    protected int getItemType(int position) {
        return mDataList.get(position).getViewType();
    }
}
