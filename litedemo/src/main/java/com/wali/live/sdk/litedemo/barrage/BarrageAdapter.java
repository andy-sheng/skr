package com.wali.live.sdk.litedemo.barrage;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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

    public static final int DEFAULT_SUB_NAME_LENGTH = 4;

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
        private SpannableStringBuilder mBarrageSpan;
        private Message mMessage;

        public BarrageHolder(View itemView) {
            super(itemView);
            mBarrageTv = (TextView) itemView.findViewById(R.id.barrage_tv);
            mBarrageSpan = new SpannableStringBuilder();
        }

        protected void bindView(Message message) {
            mMessage = message;
            reset();
            setName();
            setComment();
            setAll();
        }

        private void reset() {
            mBarrageSpan.clear();
            mBarrageSpan.clearSpans();
        }

        private void setAll() {
            mBarrageTv.setShadowLayer(2f, 2.5f, 2.5f, Color.BLACK);
            mBarrageTv.setPadding(0, 0, 16, 3);
            mBarrageTv.setBackground(null);
            mBarrageTv.setText(mBarrageSpan);
        }

        private void setName() {
            String name = mMessage.getSenderName();
            if (TextUtils.isEmpty(name)) {
                return;
            }
            mBarrageSpan.append(name);
            mBarrageSpan.append(": ");
            mBarrageSpan.setSpan(new ForegroundColorSpan(0xffffd267),
                    0, mBarrageSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        public void setComment() {
            int len = mBarrageSpan.length();
            String body = mMessage.getBody();
            if (TextUtils.isEmpty(body)) {
                return;
            }
            mBarrageSpan.append(body);
            mBarrageSpan.setSpan(new ForegroundColorSpan(Color.WHITE),
                    len, mBarrageSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
    }
}
