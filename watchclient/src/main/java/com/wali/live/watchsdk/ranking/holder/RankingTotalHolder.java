package com.wali.live.watchsdk.ranking.holder;

import android.view.View;
import android.widget.TextView;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;
import com.wali.live.watchsdk.lit.recycler.viewmodel.SimpleTextModel;

public class RankingTotalHolder extends BaseHolder<SimpleTextModel> {
    public TextView voteTotalTv;

    public RankingTotalHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        voteTotalTv = $(R.id.voteTotalTv);
    }

    @Override
    protected void bindView() {

    }
}