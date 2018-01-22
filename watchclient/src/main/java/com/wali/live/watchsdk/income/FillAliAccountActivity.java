package com.wali.live.watchsdk.income;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.watchsdk.R;

/**
 * Created by qianyuan on 2/27/16.
 */
public class FillAliAccountActivity extends BaseSdkActivity implements View.OnClickListener {

    private static final String TAG = FillAliAccountActivity.class.getSimpleName();

    private final int ACTION_INCOME_TITLEBAR_BACKBTN = 100;
    private final int ACTION_INCOME_TITLEBAR_TITLEBTN = 101;
    private final int ACTION_INCOME_TITLEBAR_RIGHTBTN = 102;

    private final int ACTION_INCOME_ACCOUNT_INFORMATION_COMMIT = 201;
    private String PAY_TYPE = "pay_type";
    private final int TYPE_ALI = 1;
    private final int TYPE_WX = 2;

    private ProgressDialog mProgressDialog;
    private BackTitleBar mTitleBar;
    private TextView mCommit;
    private EditText mAccountName;
    private EditText mAccountAlipay;
    private EditText mAccountCID;

    private Bundle mBundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MyLog.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_withdraw_fillinfo_ali_activity);
        initView();
        initData(getIntent());
    }

    private void initView() {
        mTitleBar = (BackTitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.account_fill);

        mTitleBar.getBackBtn().setOnClickListener(this);

        mCommit = (TextView) findViewById(R.id.withdraw_information_btn);
        mCommit.setOnClickListener(this);
        mCommit.setTag(ACTION_INCOME_ACCOUNT_INFORMATION_COMMIT);

        mAccountName = (EditText) findViewById(R.id.account_name_value);
        mAccountAlipay = (EditText) findViewById(R.id.account_alipay_value);
        mAccountCID = (EditText) findViewById(R.id.account_cid_value);

        KeyboardUtils.showKeyboard(
                FillAliAccountActivity.this, mAccountName);
    }

    private void initData(Intent intent) {
        mBundle = intent.getExtras();
    }

    @Override
    public void onClick(View v) {
        if (null != v.getTag()) {
            int viewAction = Integer.valueOf(String.valueOf(v.getTag()));
            switch (viewAction) {
                case ACTION_INCOME_TITLEBAR_BACKBTN:
                    KeyboardUtils.hideKeyboard(FillAliAccountActivity.this);
                    finish();
                    break;
                case ACTION_INCOME_TITLEBAR_RIGHTBTN:
                    break;
                case ACTION_INCOME_ACCOUNT_INFORMATION_COMMIT:
                    commitHandlerAsyn();
                    break;
                default:
                    break;
            }
        }
    }

    private void commitHandlerAsyn() {
        boolean mIsLegal = isLegal();
        if (mIsLegal) {
            FillAccountInfoTask fillAccountTask = new FillAccountInfoTask();
            showDialog();
            fillAccountTask.commitWithDrawAccountInfo(new WithdrawCallBack() {
                @Override
                public void commitError(int errCode) {
                    MyLog.d(TAG, "FillAccountActivity commitError ,error code :" + errCode);
                    hideDialog();
                    switch (errCode) {
                        case ErrorCode.CODE_SERVER_RESPONSE_ERROR_CODE:
                            ToastUtils.showLongToast(FillAliAccountActivity.this, R.string.account_withdraw_info_yet);
                            break;
                        case ErrorCode.CODE_SERVER_RESPONSE_ERROR_INFO_NOT_CORRENT:
                            ToastUtils.showLongToast(FillAliAccountActivity.this, R.string.account_withdraw_info_not_conrrect);
                            break;
                        default:
                            ToastUtils.showLongToast(FillAliAccountActivity.this, R.string.account_withdraw_info_error);
                            break;
                    }
                }

                @Override
                public void commitSuccess() {
                    mBundle.putInt(PAY_TYPE, TYPE_ALI);
                    hideDialog();
                    WithDrawActivity.openActivity(FillAliAccountActivity.this, mBundle);
                }

                @Override
                public void process(Object... params) {
                }
            }, mAccountName.getText().toString(), mAccountAlipay.getText().toString(), mAccountCID.getText().toString());
        }
    }

    private boolean isLegal() {
        if (TextUtils.isEmpty(mAccountName.getText())) {
            ToastUtils.showToast(this, "name is null,please fill it in");
            return false;
        } else if (TextUtils.isEmpty(mAccountAlipay.getText())) {
            ToastUtils.showToast(this, "alipay is null,please fill it in");
            return false;
        } else if (mAccountCID.getText().length() != 18 && mAccountCID.getText().length() != 15) {
            ToastUtils.showToast(this, "cid nunber is not correct,please wirte again");
            return false;
        }
        return true;
    }

    private void showDialog() {
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.account_withdraw_info_noti));
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
    public void onBackPressed() {
        MyLog.d(TAG, "onBackPressed test onBackPressed");
        finish();
    }

    @Override
    public void onDestroy() {
        MyLog.d(TAG, "fillAcountActivity onDestroy");
        super.onDestroy();
    }

    public static void openActivity(Activity activity, Bundle bundle) {
        Intent intent = new Intent(activity, FillAliAccountActivity.class);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        activity.startActivity(intent);
    }
}
