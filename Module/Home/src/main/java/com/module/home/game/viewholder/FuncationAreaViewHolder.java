package com.module.home.game.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.module.home.R;
import com.module.home.game.adapter.GameAdapter;
import com.module.home.game.model.FuncationModel;

public class FuncationAreaViewHolder extends RecyclerView.ViewHolder {

    ExImageView mTaskIv;
    ExImageView mTaskRedIv;
    ExImageView mRankIv;
    ExImageView mPracticeIv;

    public FuncationAreaViewHolder(View itemView, GameAdapter.GameAdapterListener listener) {
        super(itemView);

        mTaskIv = (ExImageView) itemView.findViewById(R.id.task_iv);
        mTaskRedIv = (ExImageView) itemView.findViewById(R.id.task_red_iv);
        mRankIv = (ExImageView) itemView.findViewById(R.id.rank_iv);
        mPracticeIv = (ExImageView) itemView.findViewById(R.id.practice_iv);

        mTaskIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (listener != null) {
                    listener.clickTask();
                }
            }
        });

        mRankIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (listener != null) {
                    listener.clickRank();
                }
            }
        });

        mPracticeIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (listener != null) {
                    listener.clickPractice();
                }
            }
        });

    }

    public void bindData(FuncationModel model) {
        if (model.isTaskHasRed()) {
            mTaskRedIv.setVisibility(View.VISIBLE);
        } else {
            mTaskRedIv.setVisibility(View.GONE);
        }
    }
}
