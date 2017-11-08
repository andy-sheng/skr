package com.wali.live.watchsdk.fans.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.holder.CreateFansGroupHolder;
import com.wali.live.watchsdk.fans.holder.MemFansGroupHolder;
import com.wali.live.watchsdk.fans.holder.special.HintMemGroupHolder;
import com.wali.live.watchsdk.fans.model.FansGroupListModel;
import com.wali.live.watchsdk.fans.model.item.BaseTypeModel;
import com.wali.live.watchsdk.fans.model.item.MemFansGroupModel;
import com.wali.live.watchsdk.fans.model.item.ViewType;
import com.wali.live.watchsdk.fans.model.item.special.HintMemGroupModel;
import com.wali.live.watchsdk.lit.recycler.adapter.EmptyRecyclerAdapter;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 2017/11/7.
 */
public class FansGroupAdapter extends EmptyRecyclerAdapter {
    public List<BaseTypeModel> mDataList;

    public FansGroupAdapter() {
        mDataList = new ArrayList();
    }

    public void setDataList(FansGroupListModel model) {
        mDataList.clear();
        if (model.getCreateFansGroupModel() != null) {
            mDataList.add(model.getCreateFansGroupModel());
        }
//        if (model.getMyFansGroupModel() != null) {
//            mDataList.add(model.getMyFansGroupModel());
//        }
        addDataList(model);
    }

    public void addDataList(FansGroupListModel model) {
        List<MemFansGroupModel> memGroupList = model.getMemFansGroupModelList();
        if (memGroupList.size() > 0) {
            mDataList.add(new HintMemGroupModel());
            mDataList.addAll(model.getMemFansGroupModelList());
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
            case ViewType.TYPE_CREATE_GROUP:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fans_create_group_item, parent, false);
                holder = new CreateFansGroupHolder(view);
                break;
            case ViewType.TYPE_MY_GROUP:
                break;
            case ViewType.TYPE_MEM_GROUP:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fans_mem_group_item, parent, false);
                holder = new MemFansGroupHolder(view);
                break;
            case ViewType.TYPE_HINT_MEM_GROUP:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fans_hint_mem_group_item, parent, false);
                holder = new HintMemGroupHolder(view);
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
