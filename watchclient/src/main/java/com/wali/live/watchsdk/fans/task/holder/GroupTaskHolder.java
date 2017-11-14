package com.wali.live.watchsdk.fans.task.holder;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.task.model.GroupJobModel;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

public class GroupTaskHolder<VM extends GroupJobModel> extends BaseHolder<VM> {
    private ImageView mDotIv;
    private TextView mTaskTextTv;
    private TextView mExpValueTv;
    private TextView mExpReceivedBtn;

    public GroupTaskHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        mDotIv = $(R.id.notice_gift_dot_iv);
        mTaskTextTv = $(R.id.task_tv);
        mExpValueTv = $(R.id.gift_exp_value_tv);
        mExpReceivedBtn = $(R.id.gift_exp_receive_btn);

        initListener();
    }

    private void initListener() {
        mExpReceivedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    protected void bindView() {
        mTaskTextTv.setText(mViewModel.getJobName());
        mExpValueTv.setText("+" + mViewModel.getExpSum());

        if (mViewModel.getJobStatus() == VFansCommonProto.GroupJobStatus.AVAILABLE_COMPLETE_VALUE) {
            mExpReceivedBtn.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.shape_vfans_task_item_receive_bg));
            mExpReceivedBtn.setText(itemView.getContext().getString(R.string.vfans_daily_tasks_receive));
            mExpReceivedBtn.setTextColor(itemView.getContext().getResources().getColor(R.color.white));
            mExpReceivedBtn.setEnabled(true);

            mDotIv.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.little_red_dot));
        } else if (mViewModel.getJobStatus() == VFansCommonProto.GroupJobStatus.COMPLETED_VALUE) {
            mExpReceivedBtn.setBackground(null);

            Drawable drawable = itemView.getContext().getResources().getDrawable(R.drawable.live_pet_group_already_received);
            drawable.setBounds(0, 0, 34, 34);
            mExpReceivedBtn.setCompoundDrawables(drawable, null, null, null);
            mExpReceivedBtn.setCompoundDrawablePadding(12);

            mExpReceivedBtn.setText(itemView.getContext().getString(R.string.vfans_daily_tasks_received));
            mExpReceivedBtn.setTextColor(itemView.getContext().getResources().getColor(R.color.color_black_trans_20));
            mExpReceivedBtn.setEnabled(false);

            mDotIv.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.little_red_dot1));

            mTaskTextTv.setTextColor(itemView.getContext().getResources().getColor(R.color.color_black_trans_30));
            mExpValueTv.setVisibility(View.GONE);
        } else if (mViewModel.getJobStatus() == VFansCommonProto.GroupJobStatus.INITIALIZE_VALUE) {
            mExpReceivedBtn.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.shape_vfans_task_item_received_bg));
            mExpReceivedBtn.setText(itemView.getContext().getString(R.string.vfans_daily_tasks_receive));
            mExpReceivedBtn.setTextColor(itemView.getContext().getResources().getColor(R.color.color_black_trans_20));
            mExpReceivedBtn.setEnabled(false);

            mDotIv.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.little_red_dot1));
        }
    }
}