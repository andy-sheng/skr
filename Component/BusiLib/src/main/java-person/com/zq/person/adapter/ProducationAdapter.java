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

    int mPlayPosition = -1;  //选中播放的id

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
            if (mPlayPosition == position) {
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

    public int getPlayPosition() {
        return mPlayPosition;
    }

    public void setPlayPosition(int selectPlayPosition) {
        if (mPlayPosition != selectPlayPosition) {
            ProducationHolder holder1 = getHolderByPosition(mPlayPosition);
            if (holder1 != null) {
                holder1.setPlayBtn(false);
            }
            mPlayPosition = selectPlayPosition;
            ProducationHolder holder2 = getHolderByPosition(mPlayPosition);
            if (holder2 != null) {
                holder2.setPlayBtn(true);
            }
        }
    }

    public void updatePlaycnt(ProducationModel model, int position) {
        ProducationHolder holder = getHolderByPosition(position);
        if (holder != null) {
            holder.setPlaycnt(model.getPlayCnt());
        }
    }

    ProducationHolder getHolderByPosition(int playPosition) {
        if (playPosition >= 0) {
            View view = mLinearLayoutManager.findViewByPosition(mPlayPosition);
            if (view != null) {
                return (ProducationHolder) view.getTag();
            }
        }
        return null;
    }

    public interface Listener {

        void onClickDele(int position, ProducationModel model);

        void onClickShare(int position, ProducationModel model);

        void onClickPlayBtn(View view, boolean play, int position, ProducationModel model);
    }
}
