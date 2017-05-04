package com.wali.live.sdk.litedemo.barrage;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mi.liveassistant.barrage.data.Message;
import com.wali.live.sdk.litedemo.R;
import com.wali.live.sdk.litedemo.topinfo.viewer.TopViewerAdapter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lan on 17/5/3.
 */
public class BarrageAdapter extends RecyclerView.Adapter<BarrageAdapter.BarrageHolder> {
    private static final String TAG = TopViewerAdapter.class.getSimpleName();

    private List<Message> mMessageList = new LinkedList<>();

    public BarrageAdapter() {
    }

    public void setMessageList(List<Message> dataList) {
        mMessageList.clear();
        mMessageList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void addMessageList(Collection<Message> dataList) {
        int pos = mMessageList.size();
        mMessageList.addAll(dataList);
        notifyItemInserted(pos);
    }

    @Override
    public int getItemCount() {
        return mMessageList == null ? 0 : mMessageList.size();
    }

    @Override
    public BarrageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_barrage_item, parent, false);
        return new BarrageHolder(view);
    }

    @Override
    public void onBindViewHolder(BarrageHolder holder, int position) {
        holder.bindView(mMessageList.get(position));
    }


    public static class BarrageHolder extends RecyclerView.ViewHolder {
        private TextView mBarrageTv;

        public BarrageHolder(View itemView) {
            super(itemView);
            mBarrageTv = (TextView) itemView.findViewById(R.id.barrage_tv);
        }

        protected void bindView(Message message) {
            mBarrageTv.setText(message.getSenderName() + ":" + message.getBody());
        }
    }
}
