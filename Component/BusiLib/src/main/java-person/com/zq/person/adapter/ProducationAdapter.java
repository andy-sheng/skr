package com.zq.person.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.floatwindow.FloatWindow;
import com.common.view.recyclerview.DiffAdapter;
import com.component.busilib.R;
import com.zq.person.holder.ProducationHolder;
import com.zq.person.model.ProducationModel;

import org.apache.commons.lang3.builder.Diff;

public class ProducationAdapter extends DiffAdapter<ProducationModel, ProducationHolder> {

    Listener mListener;
    boolean mHasDele;

    int mSelectPlayPosition = -1;  //选中播放的id

    public ProducationAdapter(Listener listener, boolean hasDeleted) {
        this.mListener = listener;
        this.mHasDele = hasDeleted;
    }

    @NonNull
    @Override
    public ProducationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.producation_item_view_layout, parent, false);
        ProducationHolder viewHolder = new ProducationHolder(view, mListener, mHasDele);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProducationHolder holder, int position) {
        ProducationModel model = mDataList.get(position);
        if (mSelectPlayPosition == position) {
            holder.bindData(position, model, true);
        } else {
            holder.bindData(position, model, false);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public int getSelectPlayPosition() {
        return mSelectPlayPosition;
    }

    public void setSelectPlayPosition(int selectPlayPosition) {
        mSelectPlayPosition = selectPlayPosition;
        notifyDataSetChanged();
    }


    public interface Listener {

        void onClickDele(int position, ProducationModel model);

        void onClickShare(int position, ProducationModel model);

        void onClickPlay(int position, ProducationModel model);

        void onClickPause(int position, ProducationModel model);
    }
}
