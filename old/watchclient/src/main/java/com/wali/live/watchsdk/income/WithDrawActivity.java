package com.wali.live.watchsdk.income;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.base.activity.BaseSdkActivity;
import com.base.dialog.DialogUtils;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.span.SpanUtils;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.event.EventClass;
import com.wali.live.proto.PayProto;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.income.income.UserIncomeActivity;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by qianyuan on 2/27/16.
 */
public class WithDrawActivity extends BaseSdkActivity implements View.OnClickListener {
    private static final String TAG = WithDrawActivity.class.getSimpleName();
    private static final Pattern WITHDRAW_INPUT_PATTERN = Pattern.compile("\\d+(\\.\\d{0,2})?");

    private final int ACTION_INCOME_TITLEBAR_BACKBTN = 100;
    private final int ACTION_INCOME_TITLEBAR_RIGHTBTN = 102;

    private final int ACTION_INCOME_ACCOUNT_COMMIT = 201;
    private final int MAX_INPUT_NUM = 8;

    private int mCurrentPayType = 0;
    private PayProto.WithdrawType type;

    private BackTitleBar mTitleBar;
    private TextView mCommit;
    private TextView mInputMoneyNoti;
    private ProgressDialog mProgressDialog;
    private TextView mInputMoneyMoreWarn;
    private EditText mInputMoneyET;
    private BaseImageView mBindAvatarIv;
    private TextView mBindNameTv;
    private TextView mBindStatusTv;
    private TextView mBindAccountTipsTv;
    /**
     * 可提现金额，单位：元
     */
    private double mMoneyCount = 0;
    /**
     * 可提现金额，单位：分
     */
    private double mMoneyCountImage = 0;
    /**
     * 要提现的金额，单位：分
     */
    private int money = 0;
    private int minLimit = 0;
    private int maxLimit = 0;

    private int mVerificationStatus;
    private String mBindAvatarUrl;
    private String mBindName;

    private int mExchangeMinCash;
    private int mExchangeMaxCash;

    private TextWatcher mWatcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MyLog.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        initData();
        setContentView(R.layout.user_withdraw_activity);
        initViews();
        setWarningText();
        setResult(RESULT_OK);
    }

    @Override
    public void onClick(View v) {
        if (null != v.getTag()) {
            int viewAction = Integer.valueOf(String.valueOf(v.getTag()));
            switch (viewAction) {
                case ACTION_INCOME_TITLEBAR_BACKBTN:
                    KeyboardUtils.hideKeyboard(WithDrawActivity.this);
                    finish();
                    break;
                case ACTION_INCOME_TITLEBAR_RIGHTBTN:
                    break;
                case ACTION_INCOME_ACCOUNT_COMMIT:
                    commitPay();
                    break;
                default:
                    break;
            }
        }
    }

    private void commitPay() {
        CheckResult checkResult = checkCommitMoney();
        if (!checkResult.getIsSuccess()) {
            ToastUtils.showWithDrawToast(GlobalData.app(), checkResult.getResId(), Toast.LENGTH_SHORT);
            return;
        }
        if (mCurrentPayType == 0) {
            ToastUtils.showWithDrawToast(GlobalData.app(), R.string.account_withdraw_error_pay_type, Toast.LENGTH_SHORT);
            return;
        } else {
            switch (mCurrentPayType) {
                case UserProfit.TYPE_ALI:
                    type = PayProto.WithdrawType.ALIPAY_WITHDRAW;
                    break;
                case UserProfit.TYPE_WX:
                    type = PayProto.WithdrawType.WEIXIN_WITHDRAW;
                    break;
                case UserProfit.TYPE_PAYPAL:
                    type = PayProto.WithdrawType.PAYPAL_WITHDRAW;
                    break;
                default:
                    type = PayProto.WithdrawType.WEIXIN_WITHDRAW;
                    break;
            }
        }
        PayToTask mPayToTaget = new PayToTask();
        showDialog();
        MyLog.d(TAG, "money = " + money);
        mPayToTaget.commitPayToTaget(new WeakReference<WithdrawCallBack>(new WithdrawCallBack() {
            @Override
            public void commitError(int errCode) {
                hideDialog();
                MyLog.d(TAG, "withdraw commit,error code = " + errCode);
                switch (errCode) {
                    case ErrorCode.CODE_WITHDRAW_ERROR_NULL:
                        MyLog.d(TAG, "commit error,server is not response");
                        ToastUtils.showToast(WithDrawActivity.this, getString(R.string.account_withdraw_pay_service_is_not_reponse));
                        break;
                    case ErrorCode.CODE_SERVER_RESPONSE_ERROR_NOT_RECOMMIT:
                        ToastUtils.showToast(WithDrawActivity.this, getString(R.string.account_withdraw_pay_yet_hanlder_not_commit_again));
                        break;
                    case ErrorCode.CODE_SERVER_RESPONSE_ERROR_USER_HAS_NOT_ENOUGH:
                        ToastUtils.showToast(WithDrawActivity.this, getString(R.string.account_withdraw_pay_user_account_not_enough_money));
                        break;
                    case ErrorCode.CODE_SERVER_RESPONSE_ERROR_USER_FOBITTON_FOR_COMMUTE:
                        ToastUtils.showToast(WithDrawActivity.this, getString(R.string.account_withdraw_pay_user_account_commut_try_later_again));
                        break;
                    case ErrorCode.CODE_SERVER_RESPONSE_ERROR_USER_NOT_ENOUGH:
                        ToastUtils.showToast(WithDrawActivity.this, getString(R.string.account_withdraw_pay_user_account_not_enough));
                        break;
                    case ErrorCode.CODE_SERVER_RESPONSE_ERROR_USER_ITEM_HANDLER:
                        ToastUtils.showToast(WithDrawActivity.this, getString(R.string.account_withdraw_pay_user_account_item_handler));
                        break;
                    case ErrorCode.CODE_SERVER_RESPONSE_ERROR_MORE_THAN_MAX_MONEY_DAY:
                        ToastUtils.showToast(WithDrawActivity.this, getString(R.string.account_withdraw_pay_user_account_more_than_max_day));
                        break;
                    case ErrorCode.CODE_SERVER_RESPONSE_ERROR_WITHDRAW_ERROR:
                        ToastUtils.showToast(WithDrawActivity.this, getString(R.string.account_withdraw_pay_user_account_withdraw_error));
                        break;
                    case ErrorCode.CODE_SERVER_RESPONSE_ERROR_MORE_THAN_MAX_ONE:
                        ToastUtils.showToast(WithDrawActivity.this, getString(R.string.account_withdraw_pay_user_account_more_than_max_money_once));
                        break;
                    case ErrorCode.CODE_SERVER_RESPONSE_ERROR_MORE_THAN_MAX_COUNT:
                        ToastUtils.showToast(WithDrawActivity.this, getString(R.string.account_withdraw_pay_user_account_more_than_max_count_day));
                        break;
                    case ErrorCode.CODE_SERVER_RESPONSE_ERROR_MONEY_NOT_ENOUGH:
                        ToastUtils.showToast(WithDrawActivity.this, getString(R.string.account_withdraw_pay_user_account_less_than_limit_money_day));
                        break;
                    default:
                        MyLog.d(TAG, "commit error,error code :" + errCode);
                        ToastUtils.showToast(WithDrawActivity.this, getString(R.string.account_withdraw_unknown_error, errCode));
                        break;
                }
            }

            @Override
            public void process(Object... params) {
                hideDialog();
                PayProto.WithdrawResponse mWithDrawResponse = (PayProto.WithdrawResponse) params[0];
                String notiMessage = "";
                EventBus.getDefault().post(new EventClass.WithdrawEvent(EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_TICKET_CHANGE));
                //非大陆地区用PayPal提现
                mMoneyCountImage -= money;
                mMoneyCount = mMoneyCountImage / 100;
                MyLog.i(TAG, "mMoneyCount=" + mMoneyCount);
                if (mCurrentPayType == UserProfit.TYPE_PAYPAL) {
                    mInputMoneyMoreWarn.setText(getString(R.string.today_available_money, String.format("%.2f", mMoneyCount), getString(R.string.usd_unit)));
                } else {
                    mInputMoneyMoreWarn.setText(getString(R.string.today_available_money, String.format("%.2f", mMoneyCount), getString(R.string.rmb_unit)));
                }
                switch (type) {
                    case ALIPAY_WITHDRAW:
                        DialogUtils.showNormalDialog(WithDrawActivity.this, 0, R.string.account_withdraw_ali_pay_success, R.string.account_withdraw_pay_success_return, 0, new DialogUtils.IDialogCallback() {
                            @Override
                            public void process(DialogInterface dialogInterface, int i) {
                                KeyboardUtils.hideKeyboard(WithDrawActivity.this);
                                UserIncomeActivity.openActivity(WithDrawActivity.this);
//                                EventBus.getDefault().post(new EventClass.WithdrawEvent(EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_TICKET_CHANGE));
                            }
                        }, null);
                        break;
                    case WEIXIN_WITHDRAW:
                        DialogUtils.showNormalDialog(WithDrawActivity.this, 0, R.string.account_withdraw_wx_pay_success, R.string.account_withdraw_pay_success_return, 0, new DialogUtils.IDialogCallback() {
                            @Override
                            public void process(DialogInterface dialogInterface, int i) {
                                KeyboardUtils.hideKeyboard(WithDrawActivity.this);
                                UserIncomeActivity.openActivity(WithDrawActivity.this);
//                                EventBus.getDefault().post(new EventClass.WithdrawEvent(EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_TICKET_CHANGE));
                            }
                        }, null);
                        break;
                    case PAYPAL_WITHDRAW:
                        DialogUtils.showNormalDialog(WithDrawActivity.this, 0, R.string.account_withdraw_paypal_pay_success, R.string.account_withdraw_pay_success_return, 0, new DialogUtils.IDialogCallback() {
                            @Override
                            public void process(DialogInterface dialogInterface, int i) {
                                KeyboardUtils.hideKeyboard(WithDrawActivity.this);
                                UserIncomeActivity.openActivity(WithDrawActivity.this);
//                                EventBus.getDefault().post(new EventClass.WithdrawEvent(EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_TICKET_CHANGE));
                            }
                        }, null);
                        break;
                }
            }

            @Override
            public void commitSuccess() {

            }
        }), UserAccountManager.getInstance().getUuidAsLong(), System.currentTimeMillis(), money, type);
    }

    private CheckResult checkCommitMoney() {
        CheckResult checkResult = new CheckResult();
        if (money == 0) {
            checkResult.setIsSuccess(false);
            checkResult.setResId(R.string.account_withdraw_input_error_0);
            return checkResult;
        }
        if (money < minLimit) {
            checkResult.setIsSuccess(false);
            checkResult.setResId(R.string.account_withdraw_input_error_min_limit);
            return checkResult;
        }
        if (money > maxLimit) {
            checkResult.setIsSuccess(false);
            checkResult.setResId(R.string.account_withdraw_input_error_max_limit);
            return checkResult;
        }
        if (money > mMoneyCountImage) {
            checkResult.setIsSuccess(false);
            checkResult.setResId(R.string.account_withdraw_input_error_more__personal_account);
            return checkResult;
        }

        checkResult.setIsSuccess(true);
        return checkResult;
    }

    private void initData() {
        Bundle dataBundle = getIntent().getExtras();
        if (null == dataBundle) {
            MyLog.w(TAG, "intent trans data is null,please check it and again");
            return;
        }
        mMoneyCount = dataBundle.getDouble(UserIncomeActivity.BUNDLE_TADAY_EXCHANGE_MONEY, 0);
        mMoneyCountImage = mMoneyCount * 100;
        mCurrentPayType = dataBundle.getInt(UserProfit.BUNDLE_PAY_TYPE);

        if (mCurrentPayType == UserProfit.TYPE_PAYPAL) {
            mVerificationStatus = dataBundle.getInt(UserIncomeActivity.BUNDLE_VERIFICATION_STATE, UserProfit.PaypalPay.REAL_NAME_NOT_VERIFIED);
        } else {
            mVerificationStatus = dataBundle.getInt(UserIncomeActivity.BUNDLE_VERIFICATION_STATE, UserProfit.WeixinPayAccount.REAL_NAME_NOT_VERIFIED);
        }
        mBindName = dataBundle.getString(UserIncomeActivity.BUNDLE_BIND_ACCOUNT, "");
        mBindAvatarUrl = dataBundle.getString(UserIncomeActivity.BUNDLE_BIND_AVATAR, "");
        mExchangeMinCash = dataBundle.getInt(UserProfit.KEY_EXCHANGE_MIN_CASH, 0);
        mExchangeMaxCash = dataBundle.getInt(UserProfit.KEY_EXCHANGE_MAX_CASH, 0);

        MyLog.w(TAG, "payType=" + mCurrentPayType + " bindName = " + mBindName + " bindAvatar = " + mBindAvatarUrl + " bindStatus = " + mVerificationStatus + " moneyCount=" + mMoneyCount);
    }

    private void setWarningText() {
        List<Pair<String, Integer>> textChangeColor = new ArrayList<Pair<String, Integer>>();
        Pair text_5 = new Pair("5", R.color.cash_color);
        String source = "";
        textChangeColor.add(text_5);
        switch (mCurrentPayType) {
            case UserProfit.TYPE_ALI: {
                minLimit = mExchangeMinCash <= 0 ? 100 * 100 : mExchangeMinCash;
                maxLimit = mExchangeMaxCash <= 0 ? 10000 * 100 : mExchangeMaxCash;
                Pair text_100 = new Pair(String.valueOf(minLimit / 100), R.color.cash_color);
                Pair text_10000 = new Pair(String.valueOf(maxLimit / 100), R.color.cash_color);
                textChangeColor.add(text_100);
                textChangeColor.add(text_10000);
                source = getString(R.string.account_withdraw_ali_noti);
                break;
            }
            case UserProfit.TYPE_WX: {
                minLimit = mExchangeMinCash <= 0 ? 1 * 100 : mExchangeMinCash;
                maxLimit = mExchangeMaxCash <= 0 ? 700 * 100 : mExchangeMaxCash;
                Pair text_1 = new Pair(String.valueOf(minLimit / 100), R.color.cash_color);
                Pair text_700 = new Pair(String.valueOf(maxLimit / 100), R.color.cash_color);
                textChangeColor.add(text_1);
                textChangeColor.add(text_700);
                source = getString(R.string.account_withdraw_wx_noti);
                break;
            }
            case UserProfit.TYPE_PAYPAL: {
                minLimit = mExchangeMinCash <= 0 ? 1 * 100 : mExchangeMinCash;
                maxLimit = mExchangeMaxCash <= 0 ? 100 * 100 : mExchangeMaxCash;
                Pair text_1 = new Pair(String.valueOf(minLimit / 100), R.color.cash_color);
                Pair text_100 = new Pair(String.valueOf(maxLimit / 100), R.color.cash_color);
                textChangeColor.add(text_1);
                textChangeColor.add(text_100);
                source = getString(R.string.account_withdraw_paypal_noti);
                break;
            }
            default:
                break;
        }
        mInputMoneyNoti.setText(SpanUtils.addColorSpan(textChangeColor, source, R.color.color_black_trans_50));

    }

    private void initViews() {

        mTitleBar = (BackTitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.account_withdraw);

        mTitleBar.getBackBtn().setOnClickListener(this);
        mTitleBar.getBackBtn().setTag(ACTION_INCOME_TITLEBAR_BACKBTN);

        mCommit = (TextView) findViewById(R.id.withdraw_commit);
        mCommit.setOnClickListener(this);
        mCommit.setTag(ACTION_INCOME_ACCOUNT_COMMIT);

        mInputMoneyMoreWarn = (TextView) findViewById(R.id.input_money_more_warn);

        mInputMoneyET = (EditText) findViewById(R.id.account_input_money);
        //非大陆地区用PayPal提现
        if (mCurrentPayType == UserProfit.TYPE_PAYPAL) {
            mInputMoneyET.setHint(getString(R.string.account_withdraw_input_hint_noti, getString(R.string.usd_unit)));
        } else {
            mInputMoneyET.setHint(getString(R.string.account_withdraw_input_hint_noti, getString(R.string.rmb_unit)));
        }
        mWatcher =
                new TextWatcher() {
                    String beforeStr;

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        beforeStr = charSequence.toString();
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        MyLog.i(TAG, "mMoneyCount=" + mMoneyCount);
                        //非大陆地区用PayPal提现
                        if (mCurrentPayType == UserProfit.TYPE_PAYPAL) {
                            mInputMoneyMoreWarn.setText(getString(R.string.today_available_money, String.format("%.2f", mMoneyCount), getString(R.string.usd_unit)));
                        } else {
                            mInputMoneyMoreWarn.setText(getString(R.string.today_available_money, String.format("%.2f", mMoneyCount), getString(R.string.rmb_unit)));
                        }
                        mInputMoneyMoreWarn.setTextColor(getResources().getColor(R.color.color_black_trans_50));
                        String text = editable.toString();
                        if (!TextUtils.isEmpty(text) && (!WITHDRAW_INPUT_PATTERN.matcher(text).matches() || text.length() > MAX_INPUT_NUM)) {
                            if (text.length() > MAX_INPUT_NUM) {
                                ToastUtils.showToast(GlobalData.app(), R.string.withdraw_length_invalid);
                            } else if (".".equals(text)) {
                                ToastUtils.showToast(GlobalData.app(), R.string.please_input_digits);
                            } else {
                                ToastUtils.showToast(GlobalData.app(), R.string.withdraw_input_invalid);
                            }
                            mInputMoneyET.setText(beforeStr);
                            mInputMoneyET.setSelection(mInputMoneyET.length());
                            return;
                        }
                        try {
                            Double moneyDouble = new Double(Double.parseDouble(text) * 100);
                            MyLog.w(TAG, "moneyDouble" + moneyDouble + ",mMoneyCountImage = " + mMoneyCountImage);
                            if (moneyDouble > mMoneyCountImage) {
                                mCommit.setEnabled(false);
//                        mCommit.setBackgroundResource(R.drawable.withdraw_withdraw_button_pressed);
                                mInputMoneyMoreWarn.setText(getString(R.string.account_withdraw_input_error_more__personal_account));
                                mInputMoneyMoreWarn.setTextColor(getResources().getColor(R.color.cash_color));
                                return;
                            }
                            money = moneyDouble.intValue();
                            MyLog.w(TAG, "money intFloat" + money);
                        } catch (NumberFormatException numEx) {
                            MyLog.w(TAG, numEx.toString());
                        }
                        CheckResult checkResult = checkCommitMoney();
                        if (text.length() == 0) {
                            mCommit.setEnabled(false);
//                    mCommit.setBackgroundResource(R.drawable.withdraw_withdraw_button_pressed);
                        } else if (null != checkResult && !checkResult.getIsSuccess()) {
                            mInputMoneyMoreWarn.setText(getString(checkResult.getResId()));
                            mInputMoneyMoreWarn.setTextColor(getResources().getColor(R.color.cash_color));
                            mCommit.setEnabled(false);
//                    mCommit.setBackgroundResource(R.drawable.withdraw_withdraw_button_pressed);
                        } else {
                            mCommit.setEnabled(true);
//                    mCommit.setBackgroundResource(R.drawable.withdraw_withdraw_button_normal);
                        }
                    }
                };
        mInputMoneyET.addTextChangedListener(mWatcher);
        mInputMoneyNoti = (TextView) findViewById(R.id.faq_btn);
        MyLog.i(TAG, "mMoneyCount=" + mMoneyCount);
        //非大陆地区用PayPal提现
        if (mCurrentPayType == UserProfit.TYPE_PAYPAL) {
            mInputMoneyMoreWarn.setText(getString(R.string.today_available_money, String.format("%.2f", mMoneyCount), getString(R.string.usd_unit)));
        } else {
            mInputMoneyMoreWarn.setText(getString(R.string.today_available_money, String.format("%.2f", mMoneyCount), getString(R.string.rmb_unit)));
        }
        mInputMoneyMoreWarn.setTextColor(getResources().getColor(R.color.color_black_trans_50));

        mBindAvatarIv = (BaseImageView) findViewById(R.id.bind_account_avatar);
        mBindNameTv = (TextView) findViewById(R.id.bind_account_name);
        mBindStatusTv = (TextView) findViewById(R.id.bind_status_tv);

        mBindNameTv.setText(mBindName);
        mBindAccountTipsTv = (TextView) findViewById(R.id.bind_account_tips);
        if (mCurrentPayType == UserProfit.TYPE_PAYPAL) {
            mBindAvatarIv.setVisibility(View.GONE);
            mBindAccountTipsTv.setText(R.string.paypal_withdraw_has_bind_tip);
        } else {
            AvatarUtils.loadAvatarByUrl(mBindAvatarIv, mBindAvatarUrl, false);
            mBindAccountTipsTv.setText(R.string.wx_withdraw_has_bind_tip);
        }
        setCertificationText(mBindStatusTv, mVerificationStatus, mCurrentPayType);
    }

    class CheckResult {
        public boolean isSuccess = false;
        public String notiMessage;
        public int resId;

        public void setIsSuccess(boolean success) {
            this.isSuccess = success;
        }

        public boolean getIsSuccess() {
            return isSuccess;
        }

        public void setNotiMessage(String message) {
            this.notiMessage = message;
        }

        public String getNotiMessage() {
            return notiMessage;
        }

        public void setResId(int id) {
            this.resId = id;
        }

        public int getResId() {
            return resId;
        }
    }

    private void showDialog() {
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.account_withdraw_pay_noti));
        mProgressDialog.setCancelable(false);
        /*mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(final DialogInterface arg0) {
                cancel(true);
            }
        });*/
        mProgressDialog.show();
    }

    private void hideDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        MyLog.w(TAG, "onDestroy");
        mProgressDialog = null;
        mInputMoneyET.removeTextChangedListener(mWatcher);
        super.onDestroy();
    }

    private void setCertificationText(TextView statusTv, int status, int payType) {
        if (payType == UserProfit.TYPE_PAYPAL) {
            if (status == UserProfit.PaypalPay.REAL_NAME_NOT_VERIFIED) {
                statusTv.setBackgroundResource(R.drawable.image_weiyanzheng);
                statusTv.setText(R.string.real_name_status_not_verify);
            } else if (status == UserProfit.PaypalPay.ALREADY_REAL_NAME) {
                statusTv.setBackgroundResource(R.drawable.img_paypal_yanzheng);
                statusTv.setText(R.string.real_name_status_success);
            }
        } else {
            if (status == UserProfit.WeixinPayAccount.REAL_NAME_NOT_VERIFIED) {
                statusTv.setBackgroundResource(R.drawable.image_weiyanzheng);
                statusTv.setText(R.string.real_name_status_not_verify);
            } else if (status == UserProfit.WeixinPayAccount.REAL_NAME_FAIL) {
                statusTv.setBackgroundResource(R.drawable.image_yanzhengshibai);
                statusTv.setText(R.string.real_name_status_fail);
            } else if (status == UserProfit.WeixinPayAccount.ALREADY_REAL_NAME) {
                statusTv.setBackgroundResource(R.drawable.image_yiyanzheng);
                statusTv.setText(R.string.real_name_status_success);
            }
        }
    }

    public static void openActivity(Activity activity, Bundle bundle) {
        Intent intent = new Intent(activity, WithDrawActivity.class);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        activity.startActivity(intent);
    }

    public static void openActivity(Activity activity, int requestCode, Bundle bundle) {
        Intent intent = new Intent(activity, WithDrawActivity.class);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        activity.startActivityForResult(intent, requestCode);
    }
}
