package com.wali.live.watchsdk.income;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.URLSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.Constants;
import com.base.utils.network.Network;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.event.EventClass;
import com.wali.live.task.IActionCallBack;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.income.income.UserIncomeActivity;
import com.wali.live.watchsdk.income.view.NoLeakEditText;
import com.wali.live.watchsdk.webview.WebViewActivity;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

/**
 * 绑定PayPal帐号
 *
 * @author wuxiaoshan
 */
public class FillPayPalAccountActivity extends BaseSdkActivity implements View.OnClickListener, IActionCallBack {

    private static final String TAG = FillPayPalAccountActivity.class.getSimpleName();

    public static final int FAST_DOUBLE_CLICK_INTERVAL = 500;

    public static final int REQUEST_CODE_PAYPAL_WITHDRAW = 1002;

    private BackTitleBar mTitleBar;

//    private BaseImageView mAvatarIv;
//
//    private TextView mNickNameTv;

    private TextView mDetailInfoTv;

    private NoLeakEditText mPaypalIdEt;

    private NoLeakEditText mFirstNameEt;

    private NoLeakEditText mLastNameEt;

    private CheckedTextView mCheckedTv;

    private TextView mSubmitTv;

    private Bundle mBundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_pay_pal_account);
        mTitleBar = (BackTitleBar) findViewById(R.id.title_bar);
        mTitleBar.getBackBtn().setText(R.string.certification_bind_title);
        mTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtils.hideKeyboard(FillPayPalAccountActivity.this);
                setResult(RESULT_OK);
                finish();
            }
        });
//        mAvatarIv=(BaseImageView)findViewById(R.id.bind_account_avatar);
//        mNickNameTv=(TextView)findViewById(R.id.bind_account_name);
        mDetailInfoTv = (TextView) findViewById(R.id.withdraw_warn_info);
        mDetailInfoTv.setOnClickListener(this);
        mPaypalIdEt = (NoLeakEditText) findViewById(R.id.bind_paypal_id);
        mPaypalIdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateBindBtnStatus();
            }
        });
        mFirstNameEt = (NoLeakEditText) findViewById(R.id.bind_first_name);
        mFirstNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateBindBtnStatus();
            }
        });
        mLastNameEt = (NoLeakEditText) findViewById(R.id.bind_last_name);
        mLastNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateBindBtnStatus();
            }
        });
        mCheckedTv = (CheckedTextView) findViewById(R.id.agreement_switch);
        mCheckedTv.setChecked(true);
        mCheckedTv.setOnClickListener(this);
        setAgreementUrl();
        mSubmitTv = (TextView) findViewById(R.id.bind_submit);
        mSubmitTv.setOnClickListener(this);

        mBundle = getIntent().getExtras();
        if (mBundle == null) {
            mBundle = new Bundle();
        }
        MyLog.w(TAG, "receive bundle:" + mBundle.toString());
    }

    @Override
    public void processAction(String action, int errCode, Object... objects) {
        if (MiLinkCommand.COMMAND_COMMIT_PAY_INFO.equals(action)) {
            MyLog.w(TAG, "bind PayPal result,errCode:" + errCode);
            hideProgress();
            if (errCode == ErrorCode.CODE_SUCCESS) {
                if (objects.length >= 1) {
                    UserProfit.PaypalPay paypalPay = (UserProfit.PaypalPay) objects[0];
                    if (paypalPay != null) {
                        mBundle.putInt(UserProfit.BUNDLE_PAY_TYPE, UserProfit.TYPE_PAYPAL);
                        mBundle.putString(UserIncomeActivity.BUNDLE_BIND_ACCOUNT, paypalPay.getAccount());
                        mBundle.putString(UserIncomeActivity.BUNDLE_BIND_FRIST_NAME, paypalPay.getFirstName());
                        mBundle.putString(UserIncomeActivity.BUNDLE_BIND_LAST_NAME, paypalPay.getLastName());
                        mBundle.putInt(UserIncomeActivity.BUNDLE_VERIFICATION_STATE, paypalPay.getVertification());
                    }
                    GetConfigManager.WithdrawConfig withdrawConfig = GetConfigManager.getInstance().getWithdrawConfig();
                    if (withdrawConfig.isH5WithdrawEnable()) {
                        MyLog.w(TAG, "open H5 withdraw");
                        Intent intent = new Intent(this, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.EXTRA_URL, withdrawConfig.mPayPalH5WithdrawUrl);
                        startActivity(intent);
                    } else if (withdrawConfig.isNativeWithdrawEnable()) {
                        MyLog.w(TAG, "open WithDrawActivity");
                        WithDrawActivity.openActivity(FillPayPalAccountActivity.this, REQUEST_CODE_PAYPAL_WITHDRAW, mBundle);
                    } else {
                        MyLog.e(TAG, "either H5 nor native withdraw view applied");
                    }
                    finish();
                }
            } else if (errCode == ErrorCode.CODE_PAYPAL_HAS_BEEN_BINDED) {
                openBindFailDialog(R.string.withdraw_bind_fail, R.string.paypal_has_been_binded);
            } else if (errCode == ErrorCode.CODE_ACCOUNT_HAS_BEEN_BINDED_PAYPAL) {
                openHasBeenBindedDialog(R.string.withdraw_bind_success, R.string.account_has_been_binded_paypal);
                EventBus.getDefault().post(new EventClass.WithdrawEvent(EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_BIND_CHANGE));
            } else {
                openBindFailDialog(R.string.withdraw_bind_fail, R.string.paypal_bind_fail);
            }
        }
    }

    @Override
    public void onClick(View v) {
        //网络判断
        if (!Network.hasNetwork((GlobalData.app()))) {
            ToastUtils.showToast(R.string.network_unavailable);
            return;
        }

        if (CommonUtils.isFastDoubleClick(FAST_DOUBLE_CLICK_INTERVAL)) {
            return;
        }
        int i = v.getId();
        if (i == R.id.withdraw_warn_info) {
            openDetailInfoDialog();

        } else if (i == R.id.agreement_switch) {
            mCheckedTv.setChecked(!mCheckedTv.isChecked());
            updateBindBtnStatus();

        } else if (i == R.id.bind_submit) {
            String paypalId = mPaypalIdEt.getText().toString();
            String firstName = mFirstNameEt.getText().toString();
            String lastName = mLastNameEt.getText().toString();
            if (TextUtils.isEmpty(paypalId)) {
                ToastUtils.showToast(R.string.paypal_id_is_empty);
                return;
            }
            if (TextUtils.isEmpty(firstName)) {
                ToastUtils.showToast(R.string.first_name_is_empty);
                return;
            }
            if (TextUtils.isEmpty(lastName)) {
                ToastUtils.showToast(R.string.last_name_is_empty);
                return;
            }
            showProgress(R.string.account_withdraw_info_noti);
            MyLog.w(TAG, "bind PayPal account,id:" + paypalId);
            FillAccountInfoTask fillAccountTask = new FillAccountInfoTask();
            WeakReference<IActionCallBack> callBack = new WeakReference<IActionCallBack>(this);
            fillAccountTask.bindPayPalAccount(callBack, paypalId, firstName, lastName);

        } else {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PAYPAL_WITHDRAW:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
        }
    }

    private void openDetailInfoDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append(getResources().getString(R.string.paypal_withdraw_detail_info_1))
                .append("\n")
                .append(getResources().getString(R.string.paypal_withdraw_detail_info_2))
                .append("\n")
                .append(getResources().getString(R.string.paypal_withdraw_detail_info_3));
        String message = sb.toString();

        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(this);
        builder.setTitle(R.string.withdraw_detail_info);
        builder.setPositiveButton(R.string.ok, null);
        MyAlertDialog dialog = builder.create();

        dialog.setMessage(message, Gravity.LEFT);
        dialog.show();
    }

    private void openBindFailDialog(int titleId, int messageId) {

        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(this);
        builder.setTitle(titleId);
        builder.setPositiveButton(R.string.ok, null);
        MyAlertDialog dialog = builder.create();

        dialog.setMessage(getResources().getString(messageId), Gravity.LEFT);
        dialog.show();
    }

    private void openHasBeenBindedDialog(int titleId, int messageId) {

        MyAlertDialog dialog = null;

        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(this);
        builder.setTitle(titleId);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_OK);
                finish();
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.setMessage(getResources().getString(messageId), Gravity.LEFT);
        dialog.show();
    }

    private void updateBindBtnStatus() {
        if (isLegal()) {
            mSubmitTv.setEnabled(true);
        } else {
            mSubmitTv.setEnabled(false);
        }
    }

    private boolean isLegal() {
        if (TextUtils.isEmpty(mPaypalIdEt.getText().toString())) {
            return false;
        } else if (TextUtils.isEmpty(mFirstNameEt.getText().toString())) {
            return false;
        } else if (TextUtils.isEmpty(mLastNameEt.getText().toString())) {
            return false;
        } else if (!mCheckedTv.isChecked()) {
            return false;
        }
        return true;
    }

    private void setAgreementUrl() {
        String clickTipsText = GlobalData.app().getString(R.string.withdraw_agreement);
        String agreementTxt = GlobalData.app().getString(R.string.agreement_txt);
        SpannableString spannableString = new SpannableString(clickTipsText);
        int start = clickTipsText.indexOf(agreementTxt);
        if (start >= 0) {
            spannableString.setSpan(new URLSpan(Constants.AGREEMENT_URL), start, start + agreementTxt.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new CharacterStyle() {
                @Override
                public void updateDrawState(TextPaint tp) {
                    tp.setUnderlineText(false);
                    tp.setColor(getResources().getColor(R.color.all_blue_bg_color));
                }
            }, start, start + agreementTxt.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
//        mCheckedTv.setHighlightColor(getResources().getColor(R.color.color_acacac));
        mCheckedTv.setText(spannableString);
        mCheckedTv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static void openActivity(Activity activity, Bundle bundle) {
        Intent intent = new Intent(activity, FillPayPalAccountActivity.class);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        activity.startActivity(intent);
    }
}
