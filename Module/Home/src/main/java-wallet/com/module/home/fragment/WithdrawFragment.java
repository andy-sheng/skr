package com.module.home.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.rxretrofit.ApiManager;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.WalletServerApi;
import com.module.home.inter.IWithDrawView;
import com.module.home.model.WithDrawInfoModel;
import com.module.home.presenter.WithDrawPresenter;
import com.module.home.view.GetRedPkgCashView;
import com.module.home.view.WithDrawRuleView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.w3c.dom.Text;

import java.util.Map;

public class WithdrawFragment extends BaseFragment implements IWithDrawView {
    public static final int NO_CHANNEL = -1;
    public static final int WX_CHANNEL = 1;
    public static final int ZFB_CHANNEL = 2;
    public static final int HF = 100000;
    LinearLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    TextView mTvWithdrawCash;
    ImageView mIvAttention;
    LinearLayout mLlInput;
    EditText mEditCashNum;
    View mDivider;
    TextView mTvTip;
    LinearLayout mLlChannel;
    ImageView mWxIcon;
    ExTextView mTvWxSelect;
    ExTextView mTvWithdrawBtn;
    TextView mTvHasNotBindTip;

    DialogPlus mRedPkgView;

    WithDrawInfoModel mWithDrawInfoModel;

    WithDrawPresenter mWithDrawPresenter;

    int mSelectedChannel = NO_CHANNEL;

    @Override
    public int initView() {
        return R.layout.withdraw_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mMainActContainer = (LinearLayout) mRootView.findViewById(R.id.main_act_container);
        mTvWithdrawCash = (TextView) mRootView.findViewById(R.id.tv_withdraw_cash);
        mIvAttention = (ImageView) mRootView.findViewById(R.id.iv_attention);
        mLlInput = (LinearLayout) mRootView.findViewById(R.id.ll_input);
        mEditCashNum = (EditText) mRootView.findViewById(R.id.edit_cash_num);
        mDivider = (View) mRootView.findViewById(R.id.divider);
        mTvTip = (TextView) mRootView.findViewById(R.id.tv_tip);
        mLlChannel = (LinearLayout) mRootView.findViewById(R.id.ll_channel);
        mWxIcon = (ImageView) mRootView.findViewById(R.id.wx_icon);
        mTvWxSelect = (ExTextView) mRootView.findViewById(R.id.tv_wx_select);
        mTvWithdrawBtn = (ExTextView) mRootView.findViewById(R.id.tv_withdraw_btn);
        mTvHasNotBindTip = (TextView) mRootView.findViewById(R.id.tv_has_not_bind_tip);

        mWithDrawPresenter = new WithDrawPresenter(this);
        addPresent(mWithDrawPresenter);

        mTvWxSelect.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mTvWxSelect.isSelected()) {
                    updateChannleState(NO_CHANNEL);
                } else {
                    if (mWithDrawInfoModel.getByChannel(WX_CHANNEL).isIsBind()) {
                        updateChannleState(WX_CHANNEL);
                    } else {
                        authWX();
                    }
                }
            }
        });

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mTvWithdrawBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (!mWithDrawInfoModel.isIsRealAuth()) {
                    U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), SmsAuthFragment.class)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .setFragmentDataListener(new FragmentDataListener() {
                                @Override
                                public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
                                    if (requestCode == 0 && resultCode == 0) {
                                        mWithDrawInfoModel.setIsRealAuth(true);
                                        U.getToastUtil().showShort("验证成功");
                                    }
                                }
                            })
                            .build());
                } else {
                    mWithDrawPresenter.withDraw(stringToHaoFen(mEditCashNum.getText().toString()), mSelectedChannel);
                }
            }
        });

        mIvAttention.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                showRuleView();
            }
        });

        mEditCashNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String editString = s.toString();

                if (checkInputNum(editString)) {
                    int cash = stringToHaoFen(editString);
                    if (TextUtils.isEmpty(mEditCashNum.getText().toString())) {
                        mTvTip.setText(String.format("可提现余额%s元", mWithDrawInfoModel.getAvailable()));
                        mTvTip.setTextColor(Color.parseColor("#B7BED5"));
                    } else if (cash > mWithDrawInfoModel.getAvailableInt()) {
                        mTvTip.setText("账户余额不足～");
                        mTvTip.setTextColor(Color.parseColor("#EF5E85"));
                    } else {
                        mTvTip.setText(String.format("可提现余额%s元", mWithDrawInfoModel.getAvailable()));
                        mTvTip.setTextColor(Color.parseColor("#B7BED5"));
                    }
                    checkWithdrawBtnEable();
                }
            }
        });

        mWithDrawPresenter.getWithDrawInfo();
        mTvWithdrawBtn.setEnabled(false);
        mTvWxSelect.setSelected(false);
    }

    private void showRuleView(){
        if (mRedPkgView != null) {
            mRedPkgView.dismiss();
        }

        WithDrawRuleView withDrawRuleView = new WithDrawRuleView(getContext());
        withDrawRuleView.bindData(mWithDrawInfoModel.getRule());

        mRedPkgView = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(withDrawRuleView))
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_40)
                .setExpanded(false)
                .setCancelable(true)
                .create();

        mRedPkgView.show();
    }

    /**
     * 检查输入的数字是否合法
     * @param editString
     * @return
     */
    private boolean checkInputNum(String editString) {
        //不可以以 . 开始
        if (editString.startsWith(".")) {
            mEditCashNum.setText("");
            return false;
        }

        if (!TextUtils.isEmpty(editString)) {
            //01 02这样的情况
            if (editString.startsWith("0") && !editString.equals("0") && !editString.startsWith("0.")) {
                mEditCashNum.setText("0");
                mEditCashNum.setSelection("0".length());
                return false;
            }

            if (editString.contains(".") && !editString.endsWith(".")) {
                //小数点后面只能有两位
                String floatNum = editString.split("\\.")[1];
                String intNum = editString.split("\\.")[0];
                if (floatNum.length() > 2) {
                    floatNum = floatNum.substring(0, 2);
                    String text = intNum + "." + floatNum;
                    mEditCashNum.setText(text);
                    mEditCashNum.setSelection(text.length());
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRedPkgView != null) {
            mRedPkgView.dismiss();
        }
    }

    private int stringToHaoFen(String floatString) {
        if (TextUtils.isEmpty(floatString)) {
            return 0;
        }

        return (int) (Float.parseFloat(floatString) * HF);
    }

    private void authWX() {
        UMShareAPI.get(U.app()).getPlatformInfo(getActivity(), SHARE_MEDIA.WEIXIN, new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {

            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> data) {
                String accessToken = data.get("access_token");
                String openid = data.get("openid");
                mWithDrawPresenter.bindWxChannel(openid, accessToken);
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                U.getToastUtil().showShort("授权取消");
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                U.getToastUtil().showShort("授权失败，" + throwable.getMessage());
            }
        });
    }

    @Override
    public void showWithDrawInfo(WithDrawInfoModel withDrawInfoModel) {
        mWithDrawInfoModel = withDrawInfoModel;
        mTvTip.setText(String.format("可提现余额%s元", withDrawInfoModel.getAvailable()));
        mIvAttention.setVisibility(View.VISIBLE);
        for (WithDrawInfoModel.CfgBean cfgBean :
                withDrawInfoModel.getCfg()) {
            if (cfgBean.getChannel() == WX_CHANNEL) {
                if (cfgBean.isIsBind()) {
                    updateChannleState(WX_CHANNEL);
                    mTvHasNotBindTip.setVisibility(View.GONE);
                } else {
                    updateChannleState(NO_CHANNEL);
                    mTvHasNotBindTip.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void bindWxResult(boolean success) {
        if (success) {
            updateChannleState(WX_CHANNEL);
            mTvHasNotBindTip.setVisibility(View.GONE);
            checkWithdrawBtnEable();
        } else {
            updateChannleState(NO_CHANNEL);
            mTvHasNotBindTip.setVisibility(View.VISIBLE);
            checkWithdrawBtnEable();
        }
    }

    private void updateChannleState(int channel) {
        if (channel == NO_CHANNEL) {
            mTvWxSelect.setSelected(false);
            mTvWithdrawBtn.setEnabled(false);
            mSelectedChannel = NO_CHANNEL;
        } else {
            mTvWxSelect.setSelected(true);
            if (TextUtils.isEmpty(mEditCashNum.getText().toString())) {
                mTvWithdrawBtn.setEnabled(false);
            } else if (stringToHaoFen(mEditCashNum.getText().toString()) <= mWithDrawInfoModel.getAvailableInt()) {
                mTvWithdrawBtn.setEnabled(true);
            }

            mSelectedChannel = channel;
        }
    }

    private void checkWithdrawBtnEable() {
        if (mSelectedChannel != NO_CHANNEL) {
            String text = mEditCashNum.getText().toString();
            int cash = stringToHaoFen(text);
            if (cash == 0) {
                mTvTip.setText(String.format("可提现余额%s元", mWithDrawInfoModel.getAvailable()));
                mTvTip.setTextColor(Color.parseColor("#B7BED5"));
                mTvWithdrawBtn.setEnabled(false);
            } else if (cash > mWithDrawInfoModel.getAvailableInt()) {
                mTvTip.setText("账户余额不足～");
                mTvTip.setTextColor(Color.parseColor("#EF5E85"));
                mTvWithdrawBtn.setEnabled(false);
            } else {
                mTvTip.setText(String.format("可提现余额%s元", mWithDrawInfoModel.getAvailable()));
                mTvTip.setTextColor(Color.parseColor("#B7BED5"));
                mTvWithdrawBtn.setEnabled(true);
            }
        }
    }

    @Override
    public void withDraw(boolean success) {
        if(success){
            U.getToastUtil().showShort("提现成功");
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
