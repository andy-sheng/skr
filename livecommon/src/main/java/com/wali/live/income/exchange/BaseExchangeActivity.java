package com.wali.live.income.exchange;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.PluralsRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.base.activity.BaseActivity;
import com.base.dialog.DialogUtils;
import com.base.dialog.MyProgressDialogEx;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.jakewharton.rxbinding.view.RxView;
import com.live.module.common.R;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.preference.PreferenceKeys;
import com.wali.live.common.view.ItemDecorationBuidler;
import com.wali.live.event.EventClass;
import com.wali.live.income.Exchange;
import com.wali.live.task.ITaskCallBack;
import com.wali.live.task.TaskCallBackWrapper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

import static com.wali.live.event.EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_BIND_CHANGE;
import static com.wali.live.event.EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_TICKET_CHANGE;


public abstract class BaseExchangeActivity extends BaseActivity implements View.OnClickListener{
    protected static final int PROGRESS_SHOW_TIME_LEAST = 1000;
    protected static final int PROGRESS_SHOW_TIME_MOST = 5000;

    protected static final int CLICK_INTERVAL_SECOND = 2;
    //view
    protected BackTitleBar mTitleBar;
    protected RecyclerView mExchangeRecycleView;
    protected ExchangeRecycleViewAdapter mExchangeRecycleAdapter;
    protected TextView mTicketBalanceTv;
    protected TextView mBalanceTipTv;
    private TextView mExchangeTv;
    private TextView mUnsignedExchangeableMoneyTv;//非签约主播可兑换金额提示
    private TextView mShowTicket;
    private TextView mGameTicket;

    //data
    protected List<Exchange> mExchangeList = new ArrayList<>();
    protected List<Exchange> mExchangeGameList = new ArrayList<>();
    protected int mMaxBtnWidth;//最宽的按钮的宽度
    private LinearLayout mShowBtn;
    private LinearLayout mGameBtn;

    public final static int TYPE_SHOW = 0;
    public final static int TYPE_GAME = 1;
    private TextView mNotice;
    protected int mType = 0;//判断当前时娱乐直播还是游戏直播模块

    protected int mShowTip;//判断银钻

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.income_base_exchange_activity);
        setContentView(getLayoutResId());
        mShowBtn = (LinearLayout)findViewById(R.id.bottom_exchange_show_area);
        mGameBtn = (LinearLayout)findViewById(R.id.bottom_exchange_mibi_area);
        mShowBtn.setOnClickListener(this);
        mGameBtn.setOnClickListener(this);
        mShowBtn.setSelected(true);
        mShowTicket = (TextView)findViewById(R.id.excwhangeable_mibi_cnt_tv);
        mNotice = (TextView)findViewById(R.id.notice_top);
        int showTip = PreferenceUtils.getSettingInt(PreferenceKeys.PER_KEY_TICKET_EXCHANGE_NOTICE, 0);
        if (showTip == 1) {
            mNotice.setVisibility(View.VISIBLE);
        }
        mTitleBar = (BackTitleBar)findViewById(R.id.title_bar);
        mTitleBar.setTitle(getTitleBarTitle());
        mTitleBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mGameTicket = (TextView)findViewById(R.id.exchangeable_mibi_cnt_tv);
        mExchangeTv = (TextView)findViewById(R.id.exchange_tv);
        mShowTip = PreferenceUtils.getSettingInt(PreferenceKeys.PER_KEY_TICKET_EXCHANGE_TYPE, 2);
        mExchangeRecycleAdapter = new ExchangeRecycleViewAdapter(mShowTip);
        mExchangeRecycleView = (RecyclerView)findViewById(R.id.recycler_view);
        mExchangeRecycleView.setAdapter(mExchangeRecycleAdapter);
        setRecyclerview(mExchangeRecycleView);
        mTicketBalanceTv = (TextView)findViewById(R.id.ticket_balance);
        mBalanceTipTv = (TextView)findViewById(R.id.balance_tip);
        updateBalanceView();
        getExchangeListFromServer(false);
        RxView.clicks(mExchangeTv).throttleFirst(CLICK_INTERVAL_SECOND, TimeUnit.SECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                final Exchange data = getSelect();
                if (data == null) {
                    return;
                }
                switch (mType) {
                    case TYPE_SHOW:
                        if (getTicketCountBalance() < data.getTicketNum()) {
                            DialogUtils.showNormalDialog(BaseExchangeActivity.this, R.string.exchange_failure_dialog_title, getErrorTipText(), R.string.ok, 0, null, null);
                            return;
                        }
                        break;
                    case TYPE_GAME:
                        if (getGameTicketCountBalance() < data.getTicketNum()) {
                            DialogUtils.showNormalDialog(BaseExchangeActivity.this, R.string.exchange_failure_dialog_title, getErrorTipText(), R.string.ok, 0, null, null);
                            return;
                        }
                        break;
                    default:
                        return;
                }

                DialogUtils.showNormalDialog(BaseExchangeActivity.this, null, getString(getExchangeTipId(), data.getDiamondNum()), R.string.ok, R.string.cancel, new DialogUtils.IDialogCallback() {
                    @Override
                    public void process(DialogInterface dialogInterface, int i) {
//                        mExchangeTv.setEnabled(false);
                        exchange(new SoftReference<ITaskCallBack>(
                                new TaskCallBackWrapper() {
                                    @Override
                                    public void process(Object object) {
//                                        holder.ticketView.setEnabled(true);
                                        ToastUtils.showWithDrawToast(GlobalData.app(), R.string.exchange_success, Toast.LENGTH_SHORT);
                                    }

                                    @Override
                                    public void processWithFailure(int errCode) {
//                                        holder.ticketView.setEnabled(true);
                                        if (errCode != ErrorCode.CODE_SUCCESS) {// 兑换米币时授权失败，也得让Item恢复enable
                                            ToastUtils.showWithDrawToast(GlobalData.app(), R.string.exchange_failure_dialog_title, Toast.LENGTH_SHORT);
                                        }
                                        MyLog.e(TAG, "errCode = " + errCode);
                                    }
                                }
                        ), data);
                    }
                }, null);
            }
        });
    }

    protected void setRecyclerview(RecyclerView view) {
        int leftMargin = DisplayUtils.dip2px(5);
        int topMargin = getResources().getDimensionPixelSize(R.dimen.view_dimen_40);
        view.addItemDecoration(new ItemDecorationBuidler().setLeft(leftMargin).setTop(topMargin).setRight(leftMargin).build());
        view.setItemAnimator(new DefaultItemAnimator());
        view.setLayoutManager(new GridLayoutManager(this, 3));
        view.setHasFixedSize(true);
    }


    private Exchange getSelect() {
        Exchange data = null;
        switch (mType) {
            case TYPE_SHOW:
                data = mExchangeList.get(mExchangeRecycleAdapter.getSelect());
                break;
            case TYPE_GAME:
                data = mExchangeGameList.get(mExchangeRecycleAdapter.getSelect());
                break;
        }
        return data;
    }


    @MainThread
    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.income_base_exchange_activity;
    }

    @MainThread
    @StringRes
    protected abstract int getTitleBarTitle();

    @MainThread
    protected abstract int getTicketCountBalance();

    @MainThread
    protected abstract int getGameTicketCountBalance();

    @MainThread
    protected void updateBalanceView() {
        mTicketBalanceTv.setText(String.valueOf(getTicketCountBalance() + getGameTicketCountBalance()));
        mShowTicket.setText(String.valueOf(getTicketCountBalance()));
        mGameTicket.setText(String.valueOf(getGameTicketCountBalance()));
    }

    /**
     * 通过网络拉取数据，得到结果后在主线程调用
     * <ul>
     * <li>{@link #updateBalanceView()}</li>
     * <li>{@link #updateExchangeList(List)}</li>
     * </ul>
     */
    protected abstract void getExchangeListFromServer(boolean callByEvent);

    @MainThread
    @LayoutRes
    protected abstract int getItemLayout();

    @MainThread
    protected void updateExchangeList(List<Exchange> exchangeList) {
        if (null != exchangeList) {
            mExchangeList.clear();
            mExchangeList.addAll(exchangeList);
            if (mType == TYPE_SHOW) {
                mExchangeRecycleAdapter.setData(mExchangeList, TYPE_SHOW);
            }
        }
    }

    @MainThread
    protected void updateGameExchangeList(List<Exchange> exchangeList) {
        if (null != mExchangeGameList) {
            mExchangeGameList.clear();
            mExchangeGameList.addAll(exchangeList);
            if (mType == TYPE_GAME) {
                mExchangeRecycleAdapter.setData(mExchangeGameList, TYPE_GAME);
            }
        }
    }

    public List<Exchange> getData() {
        return mExchangeList;
    }

    /**
     * 获取兑换目标的名称，比如金钻、米币
     *
     * @return
     */
    @MainThread
    @PluralsRes
    public abstract int getExchangeTargetName();

    @MainThread
    @StringRes
    public abstract int getTicketViewText();

    @MainThread
    @StringRes
    public abstract int getErrorTipText();

    @MainThread
    @StringRes
    public abstract int getExchangeTipId();

    public abstract void exchange(SoftReference<ITaskCallBack> callBack, @NonNull Exchange data);

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final EventClass.WithdrawEvent event) {
        if (event == null
                || !(event.eventType == EVENT_TYPE_ACCOUNT_TICKET_CHANGE || event.eventType == EVENT_TYPE_ACCOUNT_BIND_CHANGE)) {
            return;
        }

        // 刷新数据
        getExchangeListFromServer(true);
    }

    protected MyProgressDialogEx mProgressDialog;

    protected void showProcessDialog(long most, @StringRes int strId) {
        if (!isFinishing()) {
            if (mProgressDialog == null) {
                mProgressDialog = MyProgressDialogEx.createProgressDialog(this);
            }
            mProgressDialog.setMessage(getString(strId));
            mProgressDialog.show(most);
        }
    }

    protected void hideProcessDialog(long least) {
        if (mProgressDialog != null) {
            mProgressDialog.hide(least);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bottom_exchange_show_area) {
            if (mType != TYPE_SHOW) {
                mShowBtn.setSelected(true);
                mGameBtn.setSelected(false);
                mType = TYPE_SHOW;
                mExchangeRecycleAdapter.setData(mExchangeList, TYPE_SHOW);
            }
        } else if (v.getId() == R.id.bottom_exchange_mibi_area) {
            if (mType != TYPE_GAME) {
                mShowBtn.setSelected(false);
                mGameBtn.setSelected(true);
                mType = TYPE_GAME;
                mExchangeRecycleAdapter.setData(mExchangeGameList, TYPE_GAME);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.ItemClickEvent event) {
        if (!event.tag.equals(ExchangeRecycleViewAdapter.class.getSimpleName()))
            return;
        Exchange data = getSelect();
        if (data == null) {
            return;
        }
        switch (mType) {
            case TYPE_SHOW:
                mExchangeTv.setText(getResources().getQuantityString(R.plurals.cost_show_ticket_now, data.getTicketNum(), data.getTicketNum()));
                break;
            case TYPE_GAME:
                mExchangeTv.setText(getResources().getQuantityString(R.plurals.cost_game_ticket_now, data.getTicketNum(), data.getTicketNum()));
                break;
        }
    }

}
