package com.module.playways.rank.song.holder;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.utils.SpanUtils;
import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rank.R;

public class SongSearchFooter extends RecyclerView.ViewHolder {

    TextView mSearchBackTv;
    LinearLayout mFeedbackArea;
    int position;

    public SongSearchFooter(View itemView, RecyclerOnItemClickListener recyclerOnItemClickListener) {
        super(itemView);

        mSearchBackTv = (TextView) itemView.findViewById(R.id.search_back_tv);
        mFeedbackArea = (LinearLayout)itemView.findViewById(R.id.feedback_area);

        mFeedbackArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onItemClicked(itemView, position, null);
                }
            }
        });
    }

    public void bind(int position) {
        this.position = position;
    }
}
