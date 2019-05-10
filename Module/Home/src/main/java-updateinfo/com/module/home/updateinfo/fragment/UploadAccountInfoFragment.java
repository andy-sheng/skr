package com.module.home.updateinfo.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.core.account.event.AccountEvent;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.MyUserInfoServerApi;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.updateinfo.UploadAccountInfoActivity;
import com.zq.live.proto.Common.ESex;

import java.util.concurrent.TimeUnit;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;


public class UploadAccountInfoFragment extends BaseFragment {

    boolean isUpload = false; //当前是否是完善个人资料

    CommonTitleBar mTitlebar;

    RelativeLayout mMainActContainer;
    NoLeakEditText mNicknameEt;
    ExTextView mNicknameHintTv;

    ExImageView mMale;
    TextView mMaleTv;
    ExImageView mFemale;
    TextView mFemaleTv;

    ImageView mNextIv;

    int sex = 0;// 未知、非法参数
    String mNickName = "";
    String mLastName = "";   //最后一次检查的昵称

    CompositeDisposable mCompositeDisposable;
    PublishSubject<String> mPublishSubject = PublishSubject.create();
    DisposableObserver<ApiResult> mDisposableObserver;

    @Override
    public int initView() {
        return R.layout.upload_account_info_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mNicknameEt = (NoLeakEditText) mRootView.findViewById(R.id.nickname_et);
        mNicknameHintTv = (ExTextView) mRootView.findViewById(R.id.nickname_hint_tv);
        mMale = (ExImageView) mRootView.findViewById(R.id.male);
        mMaleTv = (TextView) mRootView.findViewById(R.id.male_tv);
        mFemale = (ExImageView) mRootView.findViewById(R.id.female);
        mFemaleTv = (TextView) mRootView.findViewById(R.id.female_tv);

        mNextIv = (ImageView) mRootView.findViewById(R.id.next_iv);


        Bundle bundle = getArguments();
        if (bundle != null) {
            isUpload = bundle.getBoolean(UploadAccountInfoActivity.BUNDLE_IS_UPLOAD);
        }

        mNicknameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable.toString();
                int length = U.getStringUtils().getStringLength(str);
                if (length > 14) {
                    setNicknameHintText("昵称不能超过7个汉字或14个英文", true);
                    setCompleteTv(false);
                } else if (length == 0) {
                    setNicknameHintText("昵称不能为空哦～", true);
                    setCompleteTv(false);
                } else {
                    mNicknameHintTv.setVisibility(View.GONE);
                    if (mPublishSubject != null) {
                        mPublishSubject.onNext(str);
                    }
                }
            }
        });

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
//                U.getSoundUtils().play(UploadAccountInfoActivity.TAG, R.raw.normal_back, 500);
                if (getActivity() != null) {
                    getActivity().finish();
                }
                UserAccountManager.getInstance().logoff(true, AccountEvent.LogoffAccountEvent.REASON_SELF_QUIT, false);
                ARouter.getInstance().build(RouterConstants.ACTIVITY_LOGIN)
                        .greenChannel().navigation();
            }
        });

        mNextIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mNickName = mNicknameEt.getText().toString().trim();
                checkNameAndSex(mNickName, sex);
            }
        });

        mMale.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                selectSex(true);
            }
        });

        mFemale.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                selectSex(false);
            }
        });

        if (!TextUtils.isEmpty(MyUserInfoManager.getInstance().getNickName())) {
            mNicknameEt.setText(MyUserInfoManager.getInstance().getNickName());
            mNicknameEt.setSelection(MyUserInfoManager.getInstance().getNickName().length());
            setCompleteTv(true);
        } else {
            setCompleteTv(false);
        }

        if (MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue()) {
            selectSex(true);
        } else if (MyUserInfoManager.getInstance().getSex() == ESex.SX_FEMALE.getValue()) {
            selectSex(false);
        }

        initPublishSubject();
    }

    @Override
    public void onStart() {
        super.onStart();
        mNicknameEt.requestFocus();
        U.getKeyBoardUtils().showSoftInputKeyBoard(getActivity());
    }

    private void checkNameAndSex(String nickName, int sex) {
        if (TextUtils.isEmpty(nickName)) {
            setNicknameHintText("昵称不能为空哦～", true);
            setCompleteTv(false);
            return;
        }

        if (sex == 0) {
            U.getToastUtil().showShort("请选择性别");
            return;
        }

        MyUserInfoServerApi myUserInfoServerApi = ApiManager.getInstance().createService(MyUserInfoServerApi.class);
        ApiMethods.subscribe(myUserInfoServerApi.checkNickName(nickName), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    boolean isValid = result.getData().getBooleanValue("isValid");
                    String unValidReason = result.getData().getString("unValidReason");
                    if (isValid) {
                        // 昵称可用
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
//                        Bundle bundle = new Bundle();
//                        bundle.putBoolean(UploadAccountInfoActivity.BUNDLE_IS_UPLOAD, isUpload);
//                        bundle.putString(UploadAccountInfoActivity.BUNDLE_UPLOAD_NICKNAME, nickName);
//                        bundle.putInt(UploadAccountInfoActivity.BUNDLE_UPLOAD_SEX, sex);
//                        U.getFragmentUtils().addFragment(FragmentUtils
//                                .newAddParamsBuilder(getActivity(), EditInfoAgeFragment2.class)
//                                .setBundle(bundle)
//                                .setAddToBackStack(true)
//                                .setHasAnimation(true)
//                                .build());
                        MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                                .setNickName(nickName).setSex(sex)
                                .build(), true, true, new MyUserInfoManager.ServerCallback() {
                            @Override
                            public void onSucess() {
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                                StatisticsAdapter.recordCountEvent("signup", "success2", null);
                            }

                            @Override
                            public void onFail() {

                            }
                        });
                    } else {
                        // 昵称不可用
                        setNicknameHintText(unValidReason, true);
                    }
                }
            }
        });
    }

    private void setNicknameHintText(String text, boolean isError) {
        if (isError) {
            mNicknameHintTv.setTextColor(Color.parseColor("#EDADC5"));
        } else {
            mNicknameHintTv.setTextColor(Color.WHITE);
        }
        mNicknameHintTv.setVisibility(View.VISIBLE);
        mNicknameHintTv.setText(text);
    }

    private void setCompleteTv(boolean isClick) {
        if (isClick && sex != 0 && !TextUtils.isEmpty(mNicknameEt.getText().toString().trim())) {
            mNextIv.setClickable(true);
            mNextIv.setBackgroundResource(R.drawable.complete_normal_icon);
        } else {
            mNextIv.setClickable(false);
            mNextIv.setBackgroundResource(R.drawable.complete_unclick_icon);
        }
    }

    // 选一个，另一个需要缩放动画
    private void selectSex(boolean isMale) {
        this.sex = isMale ? ESex.SX_MALE.getValue() : ESex.SX_FEMALE.getValue();
        mMale.setBackground(isMale ? getResources().getDrawable(R.drawable.head_man_xuanzhong) : getResources().getDrawable(R.drawable.head_man_weixuanzhong));
        mFemale.setBackground(isMale ? getResources().getDrawable(R.drawable.head_woman_weixuanzhong) : getResources().getDrawable(R.drawable.head_woman_xuanzhong));

        mMale.setClickable(isMale ? false : true);
        mMaleTv.setTextColor(isMale ? U.getColor(R.color.white) : U.getColor(R.color.white_trans_50));
        mFemale.setClickable(isMale ? true : false);
        mFemaleTv.setTextColor(isMale ? U.getColor(R.color.white_trans_50) : U.getColor(R.color.white));

        setCompleteTv(true);
    }

    private void initPublishSubject() {
        mDisposableObserver = new DisposableObserver<ApiResult>() {
            @Override
            public void onNext(ApiResult result) {
                if (result.getErrno() == 0) {
                    boolean isValid = result.getData().getBooleanValue("isValid");
                    String unValidReason = result.getData().getString("unValidReason");
                    mNickName = mNicknameEt.getText().toString().trim();
                    if (!TextUtils.isEmpty(mLastName) && mLastName.equals(mNickName)) {
                        if (isValid) {
                            // 昵称可用
                            setNicknameHintText("昵称可用", false);
                            setCompleteTv(true);
                        } else {
                            // 昵称不可用
                            setNicknameHintText(unValidReason, true);
                            setCompleteTv(false);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        mPublishSubject.debounce(500, TimeUnit.MILLISECONDS).filter(new Predicate<String>() {
            @Override
            public boolean test(String s) throws Exception {
                return s.length() > 0;
            }
        }).switchMap(new Function<String, ObservableSource<ApiResult>>() {
            @Override
            public ObservableSource<ApiResult> apply(String string) throws Exception {
                mLastName = string;
                MyUserInfoServerApi myUserInfoServerApi = ApiManager.getInstance().createService(MyUserInfoServerApi.class);
                return myUserInfoServerApi.checkNickName(string).subscribeOn(Schedulers.io());
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(mDisposableObserver);
        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable.add(mDisposableObserver);
    }


    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        StatisticsAdapter.recordCountEvent("signup", "namesex_expose2", null);
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }

    }
}
