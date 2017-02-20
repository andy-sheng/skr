package com.wali.live.common.pay.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.dialog.MyProgressDialogEx;
import com.base.log.MyLog;
import com.base.utils.date.DateTimeUtils;
import com.base.utils.rx.RefuseRetryExeption;
import com.base.utils.rx.RxRetryAssist;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.live.module.common.R;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.milink.sdk.aidl.PacketData;
import com.trello.rxlifecycle.ActivityEvent;
import com.base.fragment.MyRxFragment;
import com.wali.live.proto.HttpDnsProto;
import com.wali.live.proto.PayProto;
import com.base.fragment.utils.FragmentNaviUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * @moudle 充值
 * Created by caoxiangyu on 16-11-4.
 */
public class RechargeRecordFragment extends MyRxFragment {

    private List<RechargeRecord> mData = new ArrayList<>();
    private MyAdapter mAdapter;
    private ListView mRecordListView;
    private LayoutInflater mInflater;
    private BackTitleBar mBar;
    private View mEmpty;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        this.mInflater = inflater;
        return inflater.inflate(R.layout.recharge_record_fragment, container, false);
    }

    @Override
    protected void bindView() {
        mBar = $(R.id.title_bar);
        mBar.setTitle(getString(R.string.record_recharge_title));
        mBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mRecordListView = $(R.id.record_list);
        mAdapter = new MyAdapter();
        showProcessDialog(6000);
        getData();
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    private void getData() {
        Observable.create(new Observable.OnSubscribe<PayProto.RechargeRecordResponse>() {
            @Override
            public void call(Subscriber<? super PayProto.RechargeRecordResponse> subscriber) {
                PayProto.RechargeRecordRequest req = PayProto.RechargeRecordRequest.newBuilder()
                        .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                        .setPlatform(PayProto.Platform.ANDROID)
                        .build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_PAY_PRECHARGELIST);
                data.setData(req.toByteArray());
                MyLog.v(TAG, "RechargeRecordRequest request:" + req.toString());
                PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
                PayProto.RechargeRecordResponse recordRsp = null;
                try {
                    recordRsp = PayProto.RechargeRecordResponse.parseFrom(response.getData());
                    MyLog.v(TAG, "RechargeRecordRequest response:" + recordRsp);
                } catch (Exception e) {
                    MyLog.e(TAG, "RechargeRecordRequest response exception:", e);
                    subscriber.onError(e);
                }
                subscriber.onNext(recordRsp);
                subscriber.onCompleted();
            }
        }).flatMap(new Func1<PayProto.RechargeRecordResponse, Observable<PayProto.RechargeRecordResponse>>() {
            @Override
            public Observable<PayProto.RechargeRecordResponse> call(PayProto.RechargeRecordResponse recordRsp) {
                if (recordRsp == null) {
                    MyLog.e(TAG, "RechargeRecordRequest response:" + "recordRsp is null");
                    return Observable.error(new RefuseRetryExeption(getString(R.string.net_error_return)));
                } else if (recordRsp.getRetCode() != HttpDnsProto.ErrorCode.SUCCESS_VALUE) {
                    MyLog.e(TAG, "RechargeRecordRequest response retCode:" + recordRsp.getRetCode());
                    return Observable.error(new RefuseRetryExeption(getString(R.string.net_error_return)));
                }
                return Observable.just(recordRsp);
            }
        })
                .retryWhen(new RxRetryAssist(1, 5, true))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((RxActivity) getActivity()).<PayProto.RechargeRecordResponse>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Subscriber<PayProto.RechargeRecordResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProcessDialog(1000);
                        ToastUtils.showToast(getActivity(), getString(R.string.live_network_error));
                        finish();
                    }

                    @Override
                    public void onNext(PayProto.RechargeRecordResponse rsp) {
                        hideProcessDialog(1000);
                        List<PayProto.RechargeRecord> recordList = rsp.getRechargeRecordsList();
                        mData.clear();
                        for (PayProto.RechargeRecord record : recordList) {
                            mData.add(new RechargeRecord(record));
                        }
                        if (!mData.isEmpty()) {
                            if (mData.size() >= 100) {
                                mRecordListView.addFooterView(mInflater.inflate(R.layout.list_recharge_bottom_item, null));
                            }
                            mRecordListView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();
                        }else {
                            mEmpty = mInflater.inflate(R.layout.empty_diamond_layout, (ViewGroup) mRecordListView.getParent());
                        }
                    }
                });
    }

    private MyProgressDialogEx mProgressDialog;

    public void showProcessDialog(long most) {
        if (getActivity() != null && !isDetached()) {
            if (mProgressDialog == null) {
                //创建ProgressDialog对象
                mProgressDialog = MyProgressDialogEx.createProgressDialog(getActivity());
            }
            mProgressDialog.show(most);
        }

    }

    public void hideProcessDialog(long least) {
        if (mProgressDialog != null) {
            mProgressDialog.hide(least);
        }
    }

    @Override
    public boolean onBackPressed() {
        finish();
        return super.onBackPressed();
    }

    private final class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public Object getItem(int arg0) {
            return mData.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.recharge_record_item_layout, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.goldTv.setText(getString(R.string.gold_iv_tint, mData.get(position).gemCnt));
            holder.timeTv.setText(mData.get(position).timeStamp);
            if (mData.get(position).status == RechargeRecord.RECHARGE_STATUS_UNDONE) {
                holder.statusTv.setText(R.string.wait_to_recharge_tv);
                holder.statusTv.setTextColor(getResources().getColor(R.color.color_e5aa1e));
            } else {
                holder.statusTv.setText(R.string.recharge_done_tv);
                holder.statusTv.setTextColor(getResources().getColor(R.color.color_black_trans_50));
            }
            return convertView;
        }

    }

    public static class ViewHolder {
        public TextView goldTv;
        public TextView timeTv;
        public TextView statusTv;

        ViewHolder(View convertView) {
            goldTv = (TextView) convertView.findViewById(R.id.gold_balance);
            timeTv = (TextView) convertView.findViewById(R.id.time_tv);
            statusTv = (TextView) convertView.findViewById(R.id.recharge_status);
        }
    }

    public static class RechargeRecord {
        public static int RECHARGE_STATUS_UNDONE = 1;
        public static int RECHARGE_STATUS_DONE = 2;

        public String itemId;
        public int gemCnt;//获得多少钻
        public int amount;//充了多少钱，单位分。注意，paypal渠道的单位是美分
        public String timeStamp;//时间,单位ms
        public int status;//1-等待支付，2-成功充值
        public String orderId;//充值订单号
        public PayProto.PayType payType;//充值渠道

        RechargeRecord(PayProto.RechargeRecord record) {
            parseFrom(record);
        }

        private void parseFrom(PayProto.RechargeRecord record) {
            itemId = record.getItemId();
            gemCnt = record.getGemCnt();
            amount = record.getAmount();
            timeStamp = DateTimeUtils.formatTimeStringForDate(record.getTimestamp() * 1000, DateTimeUtils.DATETIME_FORMAT_SECOND);
            status = record.getStatus();
            orderId = record.getOrderId();
            payType = record.getPayType();
        }
    }
}
