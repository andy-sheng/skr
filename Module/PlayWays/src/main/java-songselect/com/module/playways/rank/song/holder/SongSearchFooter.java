package com.module.playways.rank.song.holder;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import com.common.utils.SpanUtils;
import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rank.R;

public class SongSearchFooter extends RecyclerView.ViewHolder {

    TextView mSearchBackTv;
    int position;

    public SongSearchFooter(View itemView, RecyclerOnItemClickListener recyclerOnItemClickListener) {
        super(itemView);

        mSearchBackTv = (TextView) itemView.findViewById(R.id.search_back_tv);
        mSearchBackTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onItemClicked(itemView, position, null);
                }
            }
        });
        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append("搜不到歌\n试试").setForegroundColor(Color.parseColor("#9EA4AC"))
                .append("搜歌反馈").setForegroundColor(Color.parseColor("#2d62ac"))
                .append("吧！").setForegroundColor(Color.parseColor("#9EA4AC"))
                .create();
        mSearchBackTv.setText(stringBuilder);
    }

    public void bind(int position) {
        this.position = position;
    }
}
