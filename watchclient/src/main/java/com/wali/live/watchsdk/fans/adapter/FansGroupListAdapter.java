package com.wali.live.watchsdk.fans.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.holder.CreateFansGroupHolder;
import com.wali.live.watchsdk.fans.holder.MemFansGroupHolder;
import com.wali.live.watchsdk.fans.holder.MyFansGroupHolder;
import com.wali.live.watchsdk.fans.holder.special.HintMemGroupHolder;
import com.wali.live.watchsdk.fans.listener.FansGroupListListener;
import com.wali.live.watchsdk.fans.model.FansGroupListModel;
import com.wali.live.watchsdk.fans.model.item.MemFansGroupModel;
import com.wali.live.watchsdk.fans.model.item.ViewType;
import com.wali.live.watchsdk.fans.model.item.special.HintMemGroupModel;
import com.wali.live.watchsdk.fans.model.type.BaseTypeModel;
import com.wali.live.watchsdk.lit.recycler.adapter.EmptyRecyclerAdapter;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 2017/11/7.
 */
public class FansGroupListAdapter extends EmptyRecyclerAdapter {
    private List<BaseTypeModel> mDataList;
    private FansGroupListListener mListener;

    public FansGroupListAdapter(FansGroupListListener listener) {
        mDataList = new ArrayList();
        mListener = listener;
    }

    public void setDataList(FansGroupListModel model) {
        mDataList.clear();
        if (model.getCreateFansGroupModel() != null) {
            mDataList.add(model.getCreateFansGroupModel());
        }
        if (model.getMyFansGroupModel() != null) {
            mDataList.add(model.getMyFansGroupModel());
        }
        addDataList(model);
    }

    public void addDataList(FansGroupListModel model) {
        List<MemFansGroupModel> memGroupList = model.getMemFansGroupModelList();
        if (memGroupList.size() > 0) {
            mDataList.add(new HintMemGroupModel());
            mDataList.addAll(memGroupList);
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fans_group_create_item, parent, false);
                holder = new CreateFansGroupHolder(view, mListener);
                break;
            case ViewType.TYPE_MY_GROUP:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fans_group_my_item, parent, false);
                holder = new MyFansGroupHolder(view);
                break;
            case ViewType.TYPE_MEM_GROUP:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fans_group_mem_item, parent, false);
                holder = new MemFansGroupHolder(view);
                break;
            case ViewType.TYPE_HINT_MEM_GROUP:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fans_group_mem_hint_item, parent, false);
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
