package com.wali.live.watchsdk.income;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.view.BackTitleBar;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.task.IActionCallBack;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.income.view.PullToRefreshRecycleView;
import com.wali.live.watchsdk.view.EmptyView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by liuyanyan on 16/2/29.
 *
 * @Module 提现页面
 */

public class WithdrawRecordsActivity extends BaseSdkActivity implements IActionCallBack {
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm";

    private BackTitleBar mTitleBar;
    private PullToRefreshRecycleView mPullToRefreshRecycleView;
    private RecyclerView mRecordRecycleView;
    private RecordRecycleViewAdapter mRecordRecycleViewAdapter;

    private LinkedHashMap<String, WithdrawRecordData> mDataMap = new LinkedHashMap<>();
    private List<WithdrawRecordData> mRecordData = new ArrayList<>();
    private String mLastItemId;
    private boolean mNeedClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.withdraw_record_activity);

        mTitleBar = (BackTitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.cash_records);
        mTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mPullToRefreshRecycleView = (PullToRefreshRecycleView) findViewById(R.id.withdraw_recycle_view);
        mPullToRefreshRecycleView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        mPullToRefreshRecycleView.setScrollingWhileRefreshingEnabled(true);
        mPullToRefreshRecycleView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<RecyclerView>() {
            @Override
            public void onRefresh(PullToRefreshBase<RecyclerView> refreshView) {
                getDataFromServer();
            }
        });
        mRecordRecycleView = mPullToRefreshRecycleView.getRefreshableView();
        mRecordRecycleViewAdapter = new RecordRecycleViewAdapter();
        mRecordRecycleView.setAdapter(mRecordRecycleViewAdapter);
        mRecordRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecordRecycleView.setHasFixedSize(true);
        mRecordRecycleView.setLayoutManager(new LinearLayoutManager(this));

        getDataFromServer();
    }

    static final int EMPTY_TYPE = 1;
    static final int NORMAL_TYPE = 0;

    class RecordRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == EMPTY_TYPE) {
                EmptyView view = (EmptyView) LayoutInflater.from(GlobalData.app()).inflate(R.layout.empty_view, parent, false);
                ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin = -190;
                return new EmptyViewHolder(view);
            } else {
                View view = LayoutInflater.from(GlobalData.app()).inflate(R.layout.withdraw_record_item, parent, false);
                return new RecordViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder vholder, int position) {
            if (vholder instanceof RecordViewHolder) {
                RecordViewHolder holder = (RecordViewHolder) vholder;
                WithdrawRecordData record = getRecordData(position);
                if (record != null) {
                    if (record.getWithdrawType() == UserProfit.TYPE_PAYPAL) {
                        holder.withdrawAmountView.setText(getString(R.string.account_withdraw_record, (float) record.getOrtherCurrencyAmount() / 100, getString(R.string.usd_unit)));
                    } else {
                        holder.withdrawAmountView.setText(getString(R.string.account_withdraw_record, (float) record.getAmount() / 100, getString(R.string.rmb_unit)));
                    }
                    holder.withdrawTimeView.setText(formatTime(record.getRecordTime()));
                    holder.withdrawStatusView.setText(record.getRecordStatusString());
                    if (TextUtils.isEmpty(record.getStatusMsg())) {
                        holder.withdrawStatusMsgView.setVisibility(View.GONE);
                    } else {
                        holder.withdrawStatusMsgView.setVisibility(View.VISIBLE);
                        holder.withdrawStatusMsgView.setText(record.getStatusMsg());
                    }
                }
            } else if (vholder instanceof EmptyViewHolder) {
                ((EmptyViewHolder) vholder).emptyView.setVisibility(View.VISIBLE);
                ((EmptyViewHolder) vholder).emptyView.setEmptyDrawable(R.drawable.withdraw_empty_icon);
                ((EmptyViewHolder) vholder).emptyView.setEmptyTips(R.string.no_withdraw_record);
            }
        }

        @Override
        public int getItemCount() {
            return mRecordData.size() == 0 ? 1 : mRecordData.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (mRecordData.size() == 0) {
                return EMPTY_TYPE;
            } else {
                return NORMAL_TYPE;
            }
        }

        public WithdrawRecordData getRecordData(int position) {
            if (position < 0 || position >= getItemCount()) {
                return null;
            }
            return mRecordData.get(position);
        }
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {

        TextView withdrawAmountView;
        TextView withdrawTimeView;
        TextView withdrawStatusView;
        TextView withdrawStatusMsgView;

        public RecordViewHolder(View itemView) {
            super(itemView);
            withdrawAmountView = (TextView) itemView.findViewById(R.id.amount_tv);
            withdrawTimeView = (TextView) itemView.findViewById(R.id.time_tv);
            withdrawStatusView = (TextView) itemView.findViewById(R.id.status_tv);
            withdrawStatusMsgView = (TextView) itemView.findViewById(R.id.status_msg_tv);
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyView emptyView;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            this.emptyView = (EmptyView) itemView;
        }
    }

    private void getDataFromServer() {
        if (TextUtils.isEmpty(mLastItemId)) {
            mNeedClear = true;
        }
//        WithdrawTask.getWithdrawRecords(new WeakReference<com.wali.live.task.IActionCallBack>(this), mLastItemId, Constants.DEFAULT_LIMIT);
        WithdrawTask.getWithdrawRecords(new WeakReference<IActionCallBack>(this), mLastItemId, Constants.DEFAULT_LIMIT);
    }

    @Override
    public void processAction(String action, int errCode, Object... objects) {
        if (!isFinishing()) {
            List<WithdrawRecordData> exchangeList = null;
            try {
                if (errCode == ErrorCode.CODE_SUCCESS) {
                    if (null != objects && objects.length > 0) {
                        exchangeList = (List<WithdrawRecordData>) objects[0];
                        if (null != exchangeList && exchangeList.size() > 0) {
                            mLastItemId = exchangeList.get(exchangeList.size() - 1).getItemID();
                        }
                        MyLog.v(TAG, "data from server size = " + (exchangeList == null ? 0 : exchangeList.size()));
                    }
                }
            } catch (ClassCastException ex) {
                MyLog.d(TAG, ex.toString());
            }
            refreshView(exchangeList);
        }
    }

    private void refreshView(List<WithdrawRecordData> list) {
        if (mNeedClear) {
            mNeedClear = false;
            mDataMap.clear();
        }
        if (null != list && !list.isEmpty()) {
            for (WithdrawRecordData data : list) {
                if (!mDataMap.containsKey(data.getItemID())) {
                    mDataMap.put(data.getItemID(), data);
                }
            }
        }
        mRecordData = new ArrayList<>(mDataMap.values());
        mRecordRecycleViewAdapter.notifyDataSetChanged();
        mPullToRefreshRecycleView.onRefreshComplete();
    }

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, WithdrawRecordsActivity.class);
        activity.startActivity(intent);
    }

    private String formatTime(long time) {
        if (time > 0) {
            SimpleDateFormat sf = new SimpleDateFormat(TIME_FORMAT);
            Date date = new Date(time);
            return sf.format(date);
        }
        return "";
    }
}
