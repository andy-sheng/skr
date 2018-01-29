package com.wali.live.watchsdk.income.records;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.CommonUtils;
import com.base.utils.network.NetworkUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.preference.PreferenceKeys;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.income.records.model.ProfitMonthDetail;
import com.wali.live.watchsdk.income.records.model.RecordsItem;
import com.wali.live.watchsdk.income.records.presenter.IProfitRecordsView;
import com.wali.live.watchsdk.income.records.presenter.ProfitRecordsPresenter;
import com.wali.live.watchsdk.income.records.presenter.ProfitRecordsRepository;
import com.wali.live.watchsdk.view.DateWheelDialog;
import com.wali.live.watchsdk.webview.WebViewActivity;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import rx.Observable;

import static java.security.AccessController.getContext;

/**
 * Created by zhaomin on 17-6-27.
 *
 * @mdule 收益记录页面
 */
public class EarningsRecordsFragment extends BaseFragment implements View.OnClickListener, IHeaderInfoCallBack, IProfitRecordsView {

    private static final String TAG = "EarningsRecordsFragment";
    public static final String NUMBER_FORMAT = "0.##"; // 数字保留几个小数
    public static final float NUMBER_FORMAT_TIME = 100.0f; // 显示的数字和服务器下发的比例
    public static final String EXCHANGE_RECORDS_URL = "https://activity.zb.mi.com/live/withdraw/record.html"; //兑换记录跳转url

    BackTitleBar mTitleBar;

    RecyclerView mRecyclerView;

    TextView mTimeTv;

    TextView mSelectTv;

    TextView mIncomeNumTv;

    TextView mCostNumTv;

    ImageView mCloseBtn;

    RelativeLayout mNoticeRl;

    TextView mNoticeTv;

    private EarningsRecordsAdapter mAdapter;
    private StickyItemDecoration mItemDecoration;
    private List<RecordsItem> mItemList;
    private DateWheelDialog.OnDateSetListener mDateSetListener;
    private ProfitRecordsPresenter mPresenter;
    private int mYear;
    private int mMonth;
    private int day;
    private DateWheelDialog mDatePickerDialog;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_earnings_records, container, false);
    }

    @Override
    protected void bindView() {
        mNoticeTv = (TextView) mRootView.findViewById(R.id.notice_tv);
        mNoticeRl = (RelativeLayout) mRootView.findViewById(R.id.notice_rl);
        mCloseBtn = (ImageView) mRootView.findViewById(R.id.close);
        mCostNumTv = (TextView) mRootView.findViewById(R.id.cost_num_tv);
        mIncomeNumTv = (TextView) mRootView.findViewById(R.id.income_num_tv);
        mSelectTv = (TextView) mRootView.findViewById(R.id.select_tv);
        mTimeTv = (TextView) mRootView.findViewById(R.id.time_tv);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mTitleBar = (BackTitleBar) mRootView.findViewById(R.id.back_title_bar);
        mTitleBar.setTitle(R.string.cash_records_tip);
        mTitleBar.getBackBtn().setOnClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new EarningsRecordsAdapter();
        mAdapter.setOnItemClickListener(new EarningsRecordsAdapter.OnItemClickListener() {
            @Override
            public void onWeChatNumClick() {
                //WithdrawRecordsActivity.openActivity(getActivity());
                WebViewActivity.open(getActivity(), EXCHANGE_RECORDS_URL);
            }

            @Override
            public void onClickTryAgain() {
                if (!NetworkUtils.hasNetwork(getContext())) {
                    MyLog.d(TAG, "load  NO Network");
                    mRootView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.setStatus(EarningsRecordsAdapter.STATUS_FAILED);
                        }
                    }, 1000);
                    // 断网时制造一个点击加载的效果
                } else {
                    mPresenter.fetchRecords(UserAccountManager.getInstance().getUuidAsLong(), mYear, mMonth);
                }
                mAdapter.setStatus(EarningsRecordsAdapter.STATUS_LOADING);
                setTotalNum(0, 0);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mItemDecoration = new StickyItemDecoration(getActivity(), this);
        mRecyclerView.addItemDecoration(mItemDecoration);
        Calendar calendar = Calendar.getInstance();
        mMonth = calendar.get(Calendar.MONTH) + 1;
        mYear = calendar.get(Calendar.YEAR);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        setTime();
        mSelectTv.setOnClickListener(this);
        mCloseBtn.setOnClickListener(this);
        mDateSetListener = new DateWheelDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int year, int monthOfYear, int dayOfMonth) {
                MyLog.d(TAG, "onDateSet " + year + " month: " + monthOfYear);
                mYear = year;
                mMonth = monthOfYear;
                setTime();
                load();
            }
        };
        mPresenter = new ProfitRecordsPresenter(this, new ProfitRecordsRepository());
        load();
        boolean showNotice = PreferenceUtils.getSettingBoolean(getContext(), PreferenceKeys.PRE_KEY_RECORDS_SHOW_NOTICE, false);
        if (!showNotice) {
            mNoticeRl.setVisibility(View.VISIBLE);
            mNoticeRl.setClickable(true);
            mNoticeTv.setSelected(true);
        }
    }

    private void load() {
        MyLog.d(TAG, "load  Month: " + mMonth + " year: " + mYear);
        mAdapter.clear();
        if (!NetworkUtils.hasNetwork(getContext())) {
            MyLog.d(TAG, "load  NO Network");
            mAdapter.setStatus(EarningsRecordsAdapter.STATUS_FAILED);
            setTotalNum(0, 0);
            return;
        }
        mPresenter.fetchRecords(UserAccountManager.getInstance().getUuidAsLong(), mYear, mMonth);
        mAdapter.setStatus(EarningsRecordsAdapter.STATUS_LOADING);
        setTotalNum(0, 0);
    }

    private void setTime() {
        mTimeTv.setText(String.format(getContext().getResources().getString(R.string.earn_records_title_time), mYear, mMonth));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_iv) {
            finish();
        } else if (v.getId() == R.id.select_tv) {
            if (CommonUtils.isFastDoubleClick()) {
                return;
            }
            showDatePickDialog();
        } else if (v.getId() == R.id.close) {
            mNoticeRl.setVisibility(View.GONE);
            PreferenceUtils.setSettingBoolean(getContext(), PreferenceKeys.PRE_KEY_RECORDS_SHOW_NOTICE, true);
        }
    }

    private void showDatePickDialog() {
        Calendar calendar = Calendar.getInstance();
        long max = calendar.getTimeInMillis();
        calendar.set(2010, 1, 1);
        long min = calendar.getTimeInMillis();
        mDatePickerDialog = new DateWheelDialog(getActivity(), mDateSetListener);
        mDatePickerDialog.setCanceledOnTouchOutside(false);
        mDatePickerDialog.setInitDate(mYear, mMonth, day);
        mDatePickerDialog.show();
        mDatePickerDialog.setPositiveButtonTextColor(getResources().getColor(R.color.color_ff2966));
        mDatePickerDialog.getDayWheelView().setVisibility(View.GONE);
        mDatePickerDialog.hideLabel();
        mDatePickerDialog.setTitleFormat(getContext().getResources().getString(R.string.earn_records_picker_date_format));
        mDatePickerDialog.setMaxDate(max);
        mDatePickerDialog.setMinDate(min);
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    private void setTotalNum(long income, long cost) {
        DecimalFormat format = new DecimalFormat(NUMBER_FORMAT);
        mIncomeNumTv.setText(String.valueOf(format.format(income / EarningsRecordsFragment.NUMBER_FORMAT_TIME)));
        mCostNumTv.setText(String.valueOf(format.format(Math.abs(cost) / EarningsRecordsFragment.NUMBER_FORMAT_TIME)));
    }

    public static void openFragment(BaseActivity activity) {
        FragmentNaviUtils.addFragment(activity, R.id.main_act_container, EarningsRecordsFragment.class, null, true, true, true);
    }

    @Override
    public RecordsItem getInfo(int position) {
        return (mItemList != null && !mItemList.isEmpty()) ? mItemList.get(position) : null;
    }

    @Override
    public boolean isTodayAndHasIncome(int position) {
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.YEAR) == mYear && calendar.get(Calendar.MONTH) == mMonth - 1) {
            if (mItemList != null && !mItemList.isEmpty() && (mItemList.get(position).getDay() == calendar.get(Calendar.DAY_OF_MONTH))) {
                // 是今天
                return true;
            }
        }
        return false;
    }

    @Override
    public void onProfitRecordsGetSuccess(ProfitMonthDetail detail) {
        if (detail == null) {
            MyLog.w(TAG, "onProfitRecordsGetSuccess  ProfitMonthDetail null ");
            return;
        }
        List<RecordsItem> recordsItemList = detail.parseToRecordsList();
        MyLog.w(TAG, "onProfitRecordsGetSuccess  SIZE : " + recordsItemList.size());
        mItemList = recordsItemList;
        if (!recordsItemList.isEmpty()) {
            mAdapter.setData(recordsItemList);
        } else {
            mAdapter.setStatus(EarningsRecordsAdapter.STATUS_NOTHING);
        }
        setTotalNum(detail.getIncomeProfit(), detail.getCostProfit());
    }

    @Override
    public void onProfitRecordGetFailed() {
        MyLog.w(TAG, "onProfitRecordGetFailed ");
        mAdapter.setStatus(EarningsRecordsAdapter.STATUS_NOTHING);
    }

    @Override
    public <T> Observable.Transformer<T, T> bindUntilEvent() {
        return ((BaseActivity) getActivity()).bindUntilEvent();
    }
}
