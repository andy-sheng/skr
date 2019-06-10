package com.zq.person.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.view.recyclerview.DiffAdapter;
import com.component.busilib.R;
import com.zq.person.holder.EmptyProducationHolder;
import com.zq.person.holder.ProducationHolder;
import com.zq.person.model.ProducationModel;

/**
 * 用recycleview做一个空页面
 */
public class ProducationAdapter extends DiffAdapter<ProducationModel, RecyclerView.ViewHolder> {

    private static final int TYPE_NORMAL = 1;
    private static final int TYPE_EMPTY = 2;

    Listener mListener;
    boolean mIsSelf;

    int mProducationworksID = -1;  //选中播放的id

    LinearLayoutManager mLinearLayoutManager;

    public ProducationAdapter(Listener listener, boolean isSelf, LinearLayoutManager linearLayoutManager) {
        this.mIsSelf = isSelf;
        this.mListener = listener;
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.producation_item_view_layout, parent, false);
            ProducationHolder viewHolder = new ProducationHolder(view, mListener, mIsSelf);
            return viewHolder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.produacation_empty_view_layout, parent, false);
            EmptyProducationHolder viewHolder = new EmptyProducationHolder(view);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (mDataList != null && mDataList.size() > 0) {
            ProducationModel model = mDataList.get(position);
            ProducationHolder viewHolder = (ProducationHolder) holder;
            if (mProducationworksID == model.getWorksID()) {
                viewHolder.bindData(position, model, true);
            } else {
                viewHolder.bindData(position, model, false);
            }
        } else {
            EmptyProducationHolder viewHolder = (EmptyProducationHolder) holder;
            viewHolder.bindData(mIsSelf);
        }
    }

    @Override
    public int getItemCount() {
        if (mDataList != null && mDataList.size() > 0) {
            return mDataList.size();
        } else {
            return 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataList != null && mDataList.size() > 0) {
            return TYPE_NORMAL;
        } else {
            return TYPE_EMPTY;
        }
    }

    public int getPlayingWorksIdPosition() {
        return mProducationworksID;
    }

    public void setPlayPosition(int workId) {
        mProducationworksID = workId;
        notifyDataSetChanged();
    }

    public interface Listener {

        void onClickDele(int position, ProducationModel model);

        void onClickShare(int position, ProducationModel model);

        void onClickPlayBtn(View view, boolean play, int position, ProducationModel model);
    }
}
