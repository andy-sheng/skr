package com.wali.live.watchsdk.income;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.span.SpanUtils;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.PayProto;
import com.wali.live.task.IActionCallBack;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.income.auth.WXOAuth;
import com.wali.live.watchsdk.income.income.UserIncomeActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;

import static com.wali.live.watchsdk.income.UserProfit.WeixinPayAccount.NOT_REAL_NAME;


/**
 * Created by qianyuan on 2/27/16.
 */
public class FillWXAccountActivity extends BaseSdkActivity implements View.OnClickListener, IActionCallBack {

    private static final String TAG = FillWXAccountActivity.class.getSimpleName();
    private static final int REQUEST_CODE_WX_CERTIFICATION = 1000;

    private final int ACTION_INCOME_TITLEBAR_BACKBTN = 100;
    private final int ACTION_INCOME_TITLEBAR_RIGHTBTN = 102;
    private final int ACTION_INCOME_WX_INFORMATION_COMMIT = 201;

    private ProgressDialog mProgressDialog;
    private BackTitleBar mTitleBar;
    private TextView mWxBindBtn;
    private TextView mWXWithDrawNoti;

    private boolean mIsNeedRebind = false;
    private boolean mIsInBindStep1;
    private int mVerificationStatus;

    private View mFirstBindView;
    private View mRebindView;

    private Bundle mBundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MyLog.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_withdraw_fillinfo_wx_activity);
        initData();
        initView();
    }

    private void initData() {
        mBundle = getIntent().getExtras();
        if (mBundle == null) {
            mBundle = new Bundle();
        }
        mVerificationStatus = mBundle.getInt(UserIncomeActivity.BUNDLE_VERIFICATION_STATE, NOT_REAL_NAME);
        mIsNeedRebind = UserProfit.WeixinPayAccount.inNeedReBind(mVerificationStatus);// 这里已经肯定不是3了
    }

    private void initView() {
        mTitleBar = (BackTitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.account_withdraw);

        mTitleBar.getBackBtn().setTag(ACTION_INCOME_TITLEBAR_BACKBTN);
        mTitleBar.getBackBtn().setOnClickListener(this);
        mWxBindBtn = (TextView) findViewById(R.id.withdraw_bind_btn);
        mWxBindBtn.setOnClickListener(this);
        mWxBindBtn.setTag(ACTION_INCOME_WX_INFORMATION_COMMIT);

        mFirstBindView = findViewById(R.id.first_bind_view);
        mRebindView = findViewById(R.id.rebind_view);

        if (mIsNeedRebind) {
            initRebindView();
        } else {
            initFirstBindView();
        }
    }

    private void initFirstBindView() {
        MyLog.d(TAG, "initFirstBindView");
        mRebindView.setVisibility(View.GONE);
        mFirstBindView.setVisibility(View.VISIBLE);
        mWxBindBtn.setText(R.string.account_withdraw_bind_btn);

        mWXWithDrawNoti = (TextView) findViewById(R.id.wx_withdraw_noti_first);
        mWXWithDrawNoti.setText(SpanUtils.addColorSpan("1", getString(R.string.account_fill_wx_info_first), R.color.cash_color, R.color.color_black_trans_90));

        TextView tipTv = (TextView) findViewById(R.id.tip);
        String highLightText = getString(R.string.account_fill_wx_info_noti_highlight);
        tipTv.setText(SpanUtils.addColorSpan(highLightText, getString(R.string.account_fill_wx_info_noti, highLightText), R.color.color_zhibo_btn_bg_normal, R.color.color_black_trans_50));

        mIsInBindStep1 = true;
    }

    private void initRebindView() {
        mFirstBindView.setVisibility(View.GONE);
        mRebindView.setVisibility(View.VISIBLE);

        mWxBindBtn.setText(R.string.withdraw_rebind);

        BaseImageView bindAvatarIv = (BaseImageView) findViewById(R.id.bind_account_avatar);
        String bindAvatarUrl = mBundle.getString(UserIncomeActivity.BUNDLE_BIND_AVATAR, "");
        AvatarUtils.loadAvatarByUrl(bindAvatarIv, bindAvatarUrl, false);

        String bindName = mBundle.getString(UserIncomeActivity.BUNDLE_BIND_ACCOUNT, "");
        TextView bindNameTv = (TextView) findViewById(R.id.bind_account_name);
        bindNameTv.setText(bindName);

        TextView bindStatusTv = (TextView) findViewById(R.id.bind_status_tv);
        setCertificationText(bindStatusTv, mVerificationStatus);

        mIsInBindStep1 = false;
        MyLog.w(TAG, "initRebindView bindName = " + bindName + " bindAvatar = " + bindAvatarUrl + " status = " + mVerificationStatus);
    }

    @Override
    public void onClick(View v) {
        if (null != v.getTag()) {
            int viewAction = Integer.valueOf(String.valueOf(v.getTag()));
            switch (viewAction) {
                case ACTION_INCOME_TITLEBAR_BACKBTN:
                    finish();
                    break;
                case ACTION_INCOME_TITLEBAR_RIGHTBTN:
                    break;
                case ACTION_INCOME_WX_INFORMATION_COMMIT: {
                    if (mIsNeedRebind && !mIsInBindStep1) {
                        initFirstBindView();
                    } else {
                        commitHandlerAsyn();
                    }
                }
                break;
                default:
                    break;
            }
        }
    }

    private void commitHandlerAsyn() {
        // TODO: 18-1-20 有注释
        WXOAuth wxOauth = new WXOAuth();
        wxOauth.oAuthByWeiXin(this, WXOAuth.WEIXIN_REQ_LOGIN_STATE);
    }

    /**
     * EventBus 回调
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(final EventClass.OauthResultEvent event) {
        MyLog.w(TAG, "EventClass.OauthResultEvent");
        // TODO: 18-1-22 有注释
//        if (event == null || event.getEventFrom() != LoginPresenter.REQUEST_LOGIN) { //在前台的时候处理
//            return;
//        }

        if (event == null) { //在前台的时候处理
            return;
        }
        switch (event.getEventType()) {
            case EventClass.OauthResultEvent.EVENT_TYPE_CODE:
                String code = event.getCode();
                if (!TextUtils.isEmpty(code)) {
                    MyLog.d(TAG, "FillWXAccoutActivity receive a event,code = " + code);
                    commitWxCodeAsyn(code, PayProto.WithdrawType.WEIXIN_WITHDRAW);
                } else {
                    MyLog.w(TAG, "code is null");
                }
                break;
            default:
                break;
        }
    }

    private void commitWxCodeAsyn(String oauthCode, PayProto.WithdrawType type) {
        FillAccountInfoTask fillAccountTask = new FillAccountInfoTask();
        showDialog();
        mBundle.putString(UserProfit.BUNDLE_OATH_CODE, oauthCode);
        fillAccountTask.commitWithWxCode(new WeakReference<IActionCallBack>(this), oauthCode, type);
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
    protected void onResume() {
        super.onResume();
        hideProgress();
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
        Intent intent = new Intent(activity, FillWXAccountActivity.class);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        activity.startActivity(intent);
    }

    private void setCertificationText(TextView statusTv, int status) {
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

    @Override
    public void processAction(String action, int errCode, Object... objects) {
        switch (errCode) {
            case ErrorCode.CODE_SUCCESS:
                if (objects.length >= 4) {
                    String openId = (String) objects[0];
                    int type = (int) objects[1];
                    UserProfit.WeixinPayAccount wxAccount = (UserProfit.WeixinPayAccount) objects[2];
                    UserProfit.AliPayAccount aliAccount = (UserProfit.AliPayAccount) objects[3];
                    if (wxAccount != null) {
                        mBundle.putString(UserIncomeActivity.BUNDLE_BIND_ACCOUNT, wxAccount.name);
                        mBundle.putString(UserIncomeActivity.BUNDLE_BIND_AVATAR, wxAccount.headImgUrl);
                        mBundle.putInt(UserIncomeActivity.BUNDLE_VERIFICATION_STATE, wxAccount.verification);
                    }

                    MyLog.w(TAG, "open WxCertificationActivity fill real name info");
                    WxCertificationActivity.openActivity(FillWXAccountActivity.this, REQUEST_CODE_WX_CERTIFICATION, mBundle);
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
            default:
                ToastUtils.showLongToast(GlobalData.app(), R.string.account_withdraw_info_error);
                break;
        }
        hideDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_WX_CERTIFICATION:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
        }
    }
}
