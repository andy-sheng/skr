package com.wali.live.watchsdk.fans.adapter;

import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.holder.EmptyHolder;
import com.wali.live.watchsdk.fans.model.task.GroupJobModel;
import com.wali.live.watchsdk.fans.model.task.LimitGroupJobModel;

import java.util.ArrayList;

/**
 * Created by zyh on 2017/11/13.
 *
 * @module 粉丝任务页面
 */
public class FansTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = "FansTaskAdapter";

    private static final int ITEM_TYPE_EMPTY = -1;
    private static final int ITEM_TYPE_DAILY_TASK = 1;
    private static final int ITEM_TYPE_LIMIT_TASK = 2;
    private static final int ITEM_TYPE_HEAD_DAILY_TASK = 3;
    private static final int ITEM_TYPE_HEAD_LIMIT_TASK = 4;

    private ArrayList<GroupJobModel> mGroupJobModels = new ArrayList<>();
    private ArrayList<LimitGroupJobModel> mLimitGroupJobModels = new ArrayList<>();
    private int mVipType;

    public void setList(ArrayList<GroupJobModel> groupJobModels,
                        ArrayList<LimitGroupJobModel> limitGroupJobModels,
                        int vipType) {
        if (groupJobModels != null) {
            mGroupJobModels.clear();
            mGroupJobModels.addAll(groupJobModels);
        }
        if (limitGroupJobModels != null) {
            mLimitGroupJobModels.clear();
            mLimitGroupJobModels.addAll(limitGroupJobModels);
        }
        mVipType = vipType;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case ITEM_TYPE_EMPTY:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_view, parent, false);
                return new EmptyHolder(view);
            case ITEM_TYPE_DAILY_TASK:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vfans_task_daily_item, parent, false);
                return new DailyTaskHolder(view);
            case ITEM_TYPE_HEAD_DAILY_TASK:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vfans_task_daily_head_item, parent, false);
                return new DailyTaskHeaderHolder(view);
            case ITEM_TYPE_LIMIT_TASK:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vfans_task_daily_item, parent, false);
                return new LimitTaskHolder(view);
            case ITEM_TYPE_HEAD_LIMIT_TASK:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vfans_task_limit_head_item, parent, false);
                return new BaseHeaderHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case ITEM_TYPE_EMPTY:
                ((EmptyHolder) holder).getEmptyTv().setText(R.string.vip_prililege_more_text);
                break;
            case ITEM_TYPE_DAILY_TASK:
                ((DailyTaskHolder) holder).onBind(mGroupJobModels.get(position - 1));
                break;
            case ITEM_TYPE_LIMIT_TASK:
                ((LimitTaskHolder) holder).onBind(mLimitGroupJobModels.get(position - (1 + mGroupJobModels.size() + 1)));
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return (!mGroupJobModels.isEmpty()) ? ITEM_TYPE_HEAD_DAILY_TASK :
                    (!mLimitGroupJobModels.isEmpty() ? ITEM_TYPE_HEAD_LIMIT_TASK : ITEM_TYPE_EMPTY);
        } else if (!mGroupJobModels.isEmpty() && position < mGroupJobModels.size() + 1) {
            return ITEM_TYPE_DAILY_TASK;
        } else if (!mGroupJobModels.isEmpty() && position == mGroupJobModels.size() + 1) {
            return ITEM_TYPE_HEAD_LIMIT_TASK;
        } else {
            return ITEM_TYPE_LIMIT_TASK;
        }
    }

    @Override
    public int getItemCount() {
        if (mGroupJobModels.isEmpty() && mLimitGroupJobModels.isEmpty()) {
            return 1;
        } else if (mLimitGroupJobModels.isEmpty()) {
            return mGroupJobModels.size() + 1;
        } else if (mGroupJobModels.isEmpty()) {
            return mLimitGroupJobModels.size() + 1;
        } else {
            return mGroupJobModels.size() + 1 + mLimitGroupJobModels.size() + 1;
        }
    }

    private class LimitTaskHolder extends DailyTaskHolder {
        private TextView mTaskInfoTv;
        private TextView mTaskTipTv;

        public LimitTaskHolder(View itemView) {
            super(itemView);
            mTaskInfoTv = $(itemView, R.id.task_info_tv);
            mTaskTipTv = $(itemView, R.id.task_tip_tv);
        }

        @Override
        public void onBind(GroupJobModel model) {
            super.onBind(model);
            LimitGroupJobModel limitGroupJobModel = (LimitGroupJobModel) model;
            if (!TextUtils.isEmpty(limitGroupJobModel.getJobInfo())) {
                mTaskInfoTv.setText(limitGroupJobModel.getJobInfo());
                mTaskInfoTv.setVisibility(View.VISIBLE);
            } else {
                mTaskInfoTv.setVisibility(View.GONE);
            }
            mTaskTipTv.setText(limitGroupJobModel.getJobLimitTip());
            mTaskTipTv.setVisibility(View.VISIBLE);
        }
    }

    private class DailyTaskHolder extends RecyclerView.ViewHolder {
        protected ImageView mDotIv;
        protected TextView mTaskTextTv;
        protected TextView mExpValueTv;
        protected TextView mExpReceivedTv;

        public DailyTaskHolder(View itemView) {
            super(itemView);
            mDotIv = $(itemView, R.id.notice_gift_dot_iv);
            mTaskTextTv = $(itemView, R.id.task_tv);
            mExpValueTv = $(itemView, R.id.gift_exp_value_tv);
            mExpReceivedTv = $(itemView, R.id.gift_exp_receive_btn);
        }

        public void onBind(GroupJobModel model) {
            mTaskTextTv.setText(model.getJobName());
            mExpValueTv.setText("+" + model.getExpSum());
            switch (model.getJobStatus()) {
                case VFansCommonProto.GroupJobStatus.AVAILABLE_COMPLETE_VALUE:
                    mExpReceivedTv.setBackgroundResource(R.drawable.shape_vfans_task_item_receive_bg);
                    mExpReceivedTv.setText(R.string.vfans_daily_tasks_receive);
                    mExpReceivedTv.setTextColor(itemView.getContext().getResources().getColor(R.color.white));
                    mExpReceivedTv.setEnabled(true);
                    mDotIv.setBackgroundResource(R.drawable.little_red_dot);
                    break;
                case VFansCommonProto.GroupJobStatus.COMPLETED_VALUE:
                    mExpReceivedTv.setBackground(null);
                    Drawable drawable = itemView.getContext().getResources().getDrawable(R.drawable.live_pet_group_already_received);
                    drawable.setBounds(0, 0, 34, 34);
                    mExpReceivedTv.setCompoundDrawables(drawable, null, null, null);
                    mExpReceivedTv.setCompoundDrawablePadding(12);
                    mExpReceivedTv.setText(R.string.vfans_daily_tasks_received);
                    mExpReceivedTv.setTextColor(itemView.getContext().getResources().getColor(R.color.color_black_trans_20));
                    mDotIv.setBackgroundResource(R.drawable.little_red_dot1);
                    mExpReceivedTv.setEnabled(false);
                    mExpValueTv.setVisibility(View.GONE);
                    mTaskTextTv.setTextColor(itemView.getContext().getResources().getColor(R.color.color_black_trans_30));
                    break;
                case VFansCommonProto.GroupJobStatus.INITIALIZE_VALUE:
                    mExpReceivedTv.setBackgroundResource(R.drawable.shape_vfans_task_item_received_bg);
                    mExpReceivedTv.setText(R.string.vfans_daily_tasks_receive);
                    mExpReceivedTv.setTextColor(itemView.getContext().getResources().getColor(R.color.color_black_trans_20));
                    mDotIv.setBackgroundResource(R.drawable.little_red_dot1);
                    mExpReceivedTv.setEnabled(false);
                    break;
            }

        }
    }

    private class DailyTaskHeaderHolder extends BaseHeaderHolder {
        private TextView mTipTv;

        public DailyTaskHeaderHolder(View itemView) {
            super(itemView);
            mTipTv = $(itemView, R.id.tip_tv);
            switch (mVipType) {
                case 0://未付费
                    mTipTv.setText(R.string.vfans_daily_tasks_title_notice);
                    break;
                case 1://一个月
                    mTipTv.setText(R.string.one_month_pay_fans);
                    break;
                case 2://三个月
                    mTipTv.setText(R.string.three_month_pay_fans);
                    break;
                case 3://半年
                    mTipTv.setText(R.string.six_month_pay_fans);
                    break;
                case 4://年费
                    mTipTv.setText(R.string.year_pay_fans);
                    break;
            }
        }
    }

    private class BaseHeaderHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTv;

        public BaseHeaderHolder(View itemView) {
            super(itemView);
            mTitleTv = $(itemView, R.id.title_tv);
        }
    }

    private final <V extends View> V $(View parent, @IdRes int resId) {
        if (parent == null) {
            return null;
        }
        return (V) parent.findViewById(resId);
    }
}
