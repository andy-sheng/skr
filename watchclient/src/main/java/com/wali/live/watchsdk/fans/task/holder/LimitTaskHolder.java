package com.wali.live.watchsdk.fans.task.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.task.listener.FansGroupTaskListener;
import com.wali.live.watchsdk.fans.task.model.LimitJobModel;

/**
 * Created by lan on 2017/11/14.
 */
public class LimitTaskHolder extends GroupTaskHolder<LimitJobModel> {
    private TextView mTaskInfoTv;
    private TextView mTaskTipTv;

    public LimitTaskHolder(View itemView, FansGroupTaskListener listener) {
        super(itemView, listener);
    }

    @Override
    protected void initView() {
        mTaskInfoTv = $(R.id.task_info_tv);
        mTaskTipTv = $(R.id.task_tip_tv);
    }

    @Override
    protected void bindView() {
        super.bindView();
        if (!TextUtils.isEmpty(mViewModel.getJobInfo())) {
            mTaskInfoTv.setText(mViewModel.getJobInfo());
            mTaskInfoTv.setVisibility(View.VISIBLE);
        } else {
            mTaskInfoTv.setVisibility(View.GONE);
        }

        mTaskTipTv.setText(mViewModel.getJobLimitTip());
        mTaskTipTv.setVisibility(View.VISIBLE);
    }
}
