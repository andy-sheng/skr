package com.wali.live.watchsdk.income;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.EditText;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.config.GetConfigManager;
import com.wali.live.event.EventClass;
import com.wali.live.proto.PayProto;
import com.wali.live.task.IActionCallBack;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.income.income.UserIncomeActivity;
import com.wali.live.watchsdk.webview.WebViewActivity;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

import static com.wali.live.event.EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_BIND_CHANGE;


/**
 * Created by liuyanyan on 16/4/8.
 */
public class WxCertificationActivity extends BaseSdkActivity implements View.OnClickListener, IActionCallBack {
    private static final int REQUEST_CODE_WX_WITHDRAW = 1001;

    private static final int ACTION_TITLEBAR_BACKBTN = 0;
    private static final int ACTION_BIND_BTN = 1;
    private static final int ACTION_AGREEMENT_CHECK_BTN = 2;
    private static final int ACTION_DETAIL_INFO_BTN = 3;

    private Bundle mBundle;

    private ProgressDialog mProgressDialog;
    private TextView mDetailInfoTv;
    private TextView mCertificationBindBtn;
    private EditText mAccountNameEt;
    private EditText mAccountCidEt;
    private CheckedTextView mCheckedTv;
    private TextView mWxAccountNameTv;

    private String mBindAccount;
    private TextWatcher mNameWatcher;
    private TextWatcher mCidWatcher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = getIntent().getExtras();
        if (mBundle == null) {
            mBundle = new Bundle();
        }
        mBindAccount = mBundle.getString(UserIncomeActivity.BUNDLE_BIND_ACCOUNT, "");

        setContentView(R.layout.wx_certification_activity);
        initViews();
        setResult(RESULT_CANCELED);
    }

    private void initViews() {
        BackTitleBar titleBar = (BackTitleBar) findViewById(R.id.title_bar);
        titleBar.setTitle(R.string.certification_bind_title);

        titleBar.getBackBtn().setOnClickListener(this);
        titleBar.getBackBtn().setTag(ACTION_TITLEBAR_BACKBTN);

        mDetailInfoTv = (TextView) findViewById(R.id.withdraw_warn_info);
        mDetailInfoTv.setTag(ACTION_DETAIL_INFO_BTN);
        mDetailInfoTv.setOnClickListener(this);

        mCertificationBindBtn = (TextView) findViewById(R.id.bind_btn);
        mCertificationBindBtn.setTag(ACTION_BIND_BTN);
        mCertificationBindBtn.setOnClickListener(this);
        mCertificationBindBtn.setEnabled(false);

        mAccountNameEt = (EditText) findViewById(R.id.name_et);
        mAccountCidEt = (EditText) findViewById(R.id.id_card_et);
        mNameWatcher = new TextWatcher() {
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
        };
        mAccountNameEt.addTextChangedListener(mNameWatcher);
        mCidWatcher = new TextWatcher() {
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
        };
        mAccountCidEt.addTextChangedListener(mCidWatcher);

        mCheckedTv = (CheckedTextView) findViewById(R.id.agreement_switch);
        mCheckedTv.setTag(ACTION_AGREEMENT_CHECK_BTN);
        mCheckedTv.setChecked(true);
        mCheckedTv.setOnClickListener(this);
        setAgreementUrl();

        mWxAccountNameTv = (TextView) findViewById(R.id.wx_name_tv);
        mWxAccountNameTv.setText(mBindAccount);
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
                    tp.setColor(getResources().getColor(R.color.color_acacac));
                }
            }, start, start + agreementTxt.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mCheckedTv.setText(spannableString);
        mCheckedTv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static void openActivity(Activity activity, int requestCode, Bundle bundle) {
        Intent intent = new Intent(activity, WxCertificationActivity.class);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onClick(View v) {
        int viewAction = 0;
        try {
            if (v.getTag() != null) {
                viewAction = Integer.valueOf(String.valueOf(v.getTag()));
            }
        } catch (NumberFormatException e) {
            MyLog.e(TAG, e);
            return;
        }
        switch (viewAction) {
            case ACTION_TITLEBAR_BACKBTN:
                finish();
                break;
            case ACTION_BIND_BTN:
                String oathCode = mBundle.getString(UserProfit.BUNDLE_OATH_CODE, "");
                int withDrawType = mBundle.getInt(UserProfit.BUNDLE_PAY_TYPE);
                if (withDrawType == UserProfit.TYPE_WX) {
                    sendBindWxInfoToServer(oathCode, PayProto.WithdrawType.WEIXIN_WITHDRAW);
                }
                break;
            case ACTION_AGREEMENT_CHECK_BTN:
                mCheckedTv.setChecked(!mCheckedTv.isChecked());
                updateBindBtnStatus();
                break;
            case ACTION_DETAIL_INFO_BTN:
//                WithDrawInfoFragment.openFragment(WxCertificationActivity.this, WithDrawInfoFragment.TYPE_DETAIL_INFO);
                openDetailInfoDialog();
                break;
            default:
                break;
        }
    }

    private void openDetailInfoDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append(getResources().getString(R.string.withdraw_detail_info_1))
                .append("\n")
                .append(getResources().getString(R.string.withdraw_detail_info_2))
                .append("\n")
                .append(getResources().getString(R.string.withdraw_detail_info_3));
        String message = sb.toString();

        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(this);
        builder.setTitle(R.string.withdraw_detail_info);
        builder.setPositiveButton(R.string.ok, null);
        MyAlertDialog dialog = builder.create();

        dialog.setMessage(message, Gravity.LEFT);
        dialog.show();
    }

    private void openBindFailDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append(getResources().getString(R.string.withdraw_fail_message))
                .append("\n\n")
                .append(getResources().getString(R.string.withdraw_fail_info1))
                .append("\n")
                .append(getResources().getString(R.string.withdraw_fail_info2));
        String message = sb.toString();

        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(this);
        builder.setTitle(R.string.withdraw_fail_title);
        builder.setPositiveButton(R.string.ok, null);
        MyAlertDialog dialog = builder.create();

        dialog.setMessage(message, Gravity.LEFT);
        dialog.show();
        EventBus.getDefault().post(new EventClass.WithdrawEvent(EVENT_TYPE_ACCOUNT_BIND_CHANGE));
    }

    private void updateBindBtnStatus() {
        if (isLegal()) {
            mCertificationBindBtn.setEnabled(true);
        } else {
            mCertificationBindBtn.setEnabled(false);
        }
    }

    private void sendBindWxInfoToServer(String oauthCode, PayProto.WithdrawType type) {
        if (isLegal()) {
            FillAccountInfoTask fillAccountTask = new FillAccountInfoTask();
            showDialog();
            String realName = mAccountNameEt.getText().toString();
            String cardId = mAccountCidEt.getText().toString();
            MyLog.d(TAG, "realName = " + realName + " cardId = " + cardId + " oathCode = " + oauthCode + " bindAccount = " + mBindAccount);
            fillAccountTask.commitBindAndWithdrawInfo(new WeakReference<IActionCallBack>(this), oauthCode, type, realName, mBindAccount, cardId);
        }
    }

    private void showDialog() {
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.account_withdraw_info_noti));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void hideDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void processAction(String action, int errCode, Object... objects) {
        switch (errCode) {
            case ErrorCode.CODE_SUCCESS:
                if (objects.length >= 4) {
                    UserProfit.WeixinPayAccount wxAccount = (UserProfit.WeixinPayAccount) objects[2];
                    UserProfit.AliPayAccount aliAccount = (UserProfit.AliPayAccount) objects[3];
                    if (wxAccount != null) {
                        mBundle.putInt(UserProfit.BUNDLE_PAY_TYPE, UserProfit.TYPE_WX);
                        mBundle.putString(UserIncomeActivity.BUNDLE_BIND_ACCOUNT, wxAccount.name);
                        mBundle.putString(UserIncomeActivity.BUNDLE_BIND_AVATAR, wxAccount.headImgUrl);
                        mBundle.putInt(UserIncomeActivity.BUNDLE_VERIFICATION_STATE, wxAccount.verification);
                        if (UserProfit.WeixinPayAccount.ALREADY_REAL_NAME != wxAccount.verification) {
                            MyLog.e(TAG, "after post person info, WeChat verification:" + wxAccount.verification);
                        }
                    }
                    //todo:支付宝提现
                    // 去提现
                    GetConfigManager.WithdrawConfig withdrawConfig = GetConfigManager.getInstance().getWithdrawConfig();
                    if (withdrawConfig.isH5WithdrawEnable()) {
                        MyLog.w(TAG, "open H5 withdraw");
                        Intent intent = new Intent(WxCertificationActivity.this, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.EXTRA_URL, withdrawConfig.mWeChatH5WithdrawUrl);
                        startActivity(intent);
                    } else if (withdrawConfig.isNativeWithdrawEnable()) {
                        MyLog.d(TAG, "open WithDrawActivity withdraw money");
                        WithDrawActivity.openActivity(WxCertificationActivity.this, REQUEST_CODE_WX_WITHDRAW, mBundle);
                    } else {
                        MyLog.e(TAG, "either H5 nor native withdraw view applied");
                    }
                }
                break;
            case ErrorCode.CODE_SERVER_RESPONSE_ERROR_CODE:
                ToastUtils.showLongToast(GlobalData.app(), R.string.account_withdraw_info_yet);
                break;
            case ErrorCode.CODE_SERVER_RESPONSE_ERROR_INFO_NOT_CORRENT:
                ToastUtils.showLongToast(GlobalData.app(), R.string.account_withdraw_info_not_conrrect);
                break;
            case ErrorCode.CODE_SERVER_RESPONSE_ERROR_CODE_REBIND_ERROR:
                ToastUtils.showLongToast(GlobalData.app(), R.string.rebind_tip);
                break;
            case ErrorCode.WX_CARDID_HAS_BEEN_BINDED:
                ToastUtils.showLongToast(GlobalData.app(), R.string.id_card_rebind_tip);
                break;
            default:
                openBindFailDialog();
//                ToastUtils.showLongToast(GlobalData.app(), R.string.account_withdraw_info_error);
                break;
        }
        hideDialog();
    }

    private boolean isLegal() {
//        String cidStr = IDCard.IDCardValidate(mAccountCidEt.getText().toString());
        if (TextUtils.isEmpty(mAccountNameEt.getText().toString())) {
//            ToastUtils.showToast(GlobalData.app(), "name is null,please fill it in");
            return false;
        } else if (TextUtils.isEmpty(mAccountCidEt.getText().toString())) {
//            ToastUtils.showToast(GlobalData.app(), "cid nunber is not correct,please wirte again");
            return false;
        } else if (!mCheckedTv.isChecked()) {
//            ToastUtils.showToast(GlobalData.app(), "please agree with the policy");
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_WX_WITHDRAW:
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK);
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAccountNameEt.removeTextChangedListener(mNameWatcher);
        mAccountCidEt.removeTextChangedListener(mCidWatcher);
    }
}
