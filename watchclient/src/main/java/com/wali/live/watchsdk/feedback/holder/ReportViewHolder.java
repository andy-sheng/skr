package com.wali.live.watchsdk.feedback.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.feedback.listener.OnItemListener;

/**
 * Created by zhujianning on 18-7-5.
 */

public class ReportViewHolder extends RecyclerView.ViewHolder {
    private TextView mReason;
    private View mDivider;
    private int mPos;
    private OnItemListener mOnItemListener;

    public ReportViewHolder(View itemView) {
        super(itemView);
        mReason = (TextView) itemView.findViewById(R.id.text);
        mDivider = itemView.findViewById(R.id.divider);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemListener.onClick(mPos);
            }
        });
    }

    public void bind(String s, int position, boolean b) {
        mPos = position;
        mReason.setText(s);
        mDivider.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void setListener(OnItemListener onItemListener) {
        this.mOnItemListener = onItemListener;
    }
}
