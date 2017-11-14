package com.wali.live.watchsdk.fans.task.holder;

import android.view.View;
import android.widget.TextView;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.task.model.GroupJobHeaderModel;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

/**
 * Created by lan on 2017/11/14.
 */
public class GroupTaskHeaderHolder extends BaseHolder<GroupJobHeaderModel> {
    private TextView mTipTv;

    public GroupTaskHeaderHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        mTipTv = $(R.id.tip_tv);
    }

    @Override
    protected void bindView() {
        switch (mViewModel.getViewType()) {
            case 0://未付费
                mTipTv.setText(mTipTv.getContext().getString(R.string.vfans_daily_tasks_title_notice));
                break;
            case 1://一个月
                mTipTv.setText(mTipTv.getContext().getString(R.string.one_month_pay_fans));
                break;
            case 2://三个月
                mTipTv.setText(mTipTv.getContext().getString(R.string.three_month_pay_fans));
                break;
            case 3://半年
                mTipTv.setText(mTipTv.getContext().getString(R.string.six_month_pay_fans));
                break;
            case 4://年费
                mTipTv.setText(mTipTv.getContext().getString(R.string.year_pay_fans));
                break;
        }
    }

}
