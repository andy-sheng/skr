package com.module.home.updateinfo.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.core.account.event.AccountEvent;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.MyUserInfoServerApi;
import com.common.core.permission.SkrAudioPermission;
import com.common.core.permission.SkrSdcardPermission;
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
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.updateinfo.UploadAccountInfoActivity;
import com.module.playways.IPlaywaysModeService;
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


    RelativeLayout mMainActContainer;

    CommonTitleBar mTitlebar;
    SimpleDraweeView mAvatarIv;
    NoLeakEditText mNicknameEt;
    ExTextView mNicknameHintTv;

    ImageView mEditTipsIv;

    RadioGroup mSexButtonArea;
    RadioButton mSecre;
    RadioButton mMale;
    RadioButton mFemale;

    ImageView mNextIv;

    int mSex = 0;// 未知、非法参数
    String mNickName = "";
    String mLastName = "";   //最后一次检查的昵称

    CompositeDisposable mCompositeDisposable;
    PublishSubject<String> mPublishSubject = PublishSubject.create();
    DisposableObserver<ApiResult> mDisposableObserver;

    SkrSdcardPermission mSkrSdcardPermission = new SkrSdcardPermission(){
        @Override
        public void onRequestPermissionFailure1(Activity activity, boolean goSettingIfRefuse) {
            // 点击拒绝但不是不再询问 不弹去设置的弹窗
        }
    };
    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

    @Override
    public int initView() {
        return R.layout.upload_account_info_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mAvatarIv = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_iv);
        mNicknameEt = (NoLeakEditText) mRootView.findViewById(R.id.nickname_et);
        mEditTipsIv = (ImageView) mRootView.findViewById(R.id.edit_tips_iv);
        mNicknameHintTv = (ExTextView) mRootView.findViewById(R.id.nickname_hint_tv);

        mSexButtonArea = (RadioGroup) mRootView.findViewById(R.id.sex_button_area);
        mSecre = (RadioButton) mRootView.findViewById(R.id.secre);
        mMale = (RadioButton) mRootView.findViewById(R.id.male);
        mFemale = (RadioButton) mRootView.findViewById(R.id.female);
        mNextIv = (ImageView) mRootView.findViewById(R.id.next_iv);

        Bundle bundle = getArguments();
        if (bundle != null) {
            isUpload = bundle.getBoolean(UploadAccountInfoActivity.BUNDLE_IS_UPLOAD);
        }

        mNicknameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mEditTipsIv.setVisibility(View.GONE);
                } else {
                    mEditTipsIv.setVisibility(View.VISIBLE);
                }
            }
        });

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

        mNextIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mNickName = mNicknameEt.getText().toString().trim();
                if (TextUtils.isEmpty(mNickName)) {
                    setNicknameHintText("昵称不能为空哦～", true);
                    setCompleteTv(false);
                } else {
                    mSkrSdcardPermission.ensurePermission(getActivity(), new Runnable() {
                        @Override
                        public void run() {
                            mSkrAudioPermission.ensurePermission(getActivity(), new Runnable() {
                                @Override
                                public void run() {
                                    verifyName(mNickName);
                                }
                            }, true);
                        }
                    }, true);
                }
            }
        });

        mSexButtonArea.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.male) {
                    selectSex(ESex.SX_MALE.getValue());
                } else if (checkedId == R.id.female) {
                    selectSex(ESex.SX_FEMALE.getValue());
                } else if (checkedId == R.id.secre) {
                    selectSex(0);
                }
                changeFocus();
            }
        });

        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .build());

        if (!TextUtils.isEmpty(MyUserInfoManager.getInstance().getNickName())) {
            mEditTipsIv.setVisibility(View.VISIBLE);
            mNicknameEt.setText(MyUserInfoManager.getInstance().getNickName());
            mNicknameEt.setSelection(MyUserInfoManager.getInstance().getNickName().length());
            setCompleteTv(true);
        } else {
            setCompleteTv(false);
        }

        selectSex(MyUserInfoManager.getInstance().getSex());

        initPublishSubject();
    }

    private void changeFocus() {
        mNickName = mNicknameEt.getText().toString().trim();
        if (TextUtils.isEmpty(mNickName)) {
            if (!TextUtils.isEmpty(MyUserInfoManager.getInstance().getNickName())) {
                mNicknameEt.setText(MyUserInfoManager.getInstance().getNickName());
                mNicknameEt.clearFocus();
            } else {
                mNicknameEt.requestFocus();
            }
        } else {
            mNicknameEt.clearFocus();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (TextUtils.isEmpty(MyUserInfoManager.getInstance().getNickName())) {
            mNicknameEt.requestFocus();
            U.getKeyBoardUtils().showSoftInputKeyBoard(getActivity());
        } else {
            mNicknameEt.clearFocus();
        }
    }


    private void verifyName(String nickName) {
        if (nickName.equals(MyUserInfoManager.getInstance().getNickName()) && (mSex == MyUserInfoManager.getInstance().getSex())) {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
            goNewMatch();
            return;
        }

        MyUserInfoServerApi myUserInfoServerApi = ApiManager.getInstance().createService(MyUserInfoServerApi.class);
        ApiMethods.subscribe(myUserInfoServerApi.verifyName(nickName), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    boolean isValid = result.getData().getBooleanValue("isValid");
                    String unValidReason = result.getData().getString("unValidReason");
                    if (isValid) {
                        // 昵称可用
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                        MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                                .setNickName(nickName).setSex(mSex)
                                .build(), true, true, new MyUserInfoManager.ServerCallback() {
                            @Override
                            public void onSucess() {
                                // 进入新手引导房间匹配
                                goNewMatch();
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

    private void goNewMatch() {
        IPlaywaysModeService playwaysModeService = (IPlaywaysModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
        if (playwaysModeService != null) {
            playwaysModeService.tryGoNewGrabMatch();
        }

        // TODO: 2019/5/16 因为fastLogin的标记为用在是否要完善资料上了
        MyUserInfoManager.getInstance().setFirstLogin(false);

        if (getActivity() != null) {
            getActivity().finish();
        }
        StatisticsAdapter.recordCountEvent("signup", "success2", null);
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
        if (isClick && !TextUtils.isEmpty(mNicknameEt.getText().toString().trim())) {
            mNextIv.setClickable(true);
            mNextIv.setBackgroundResource(R.drawable.complete_normal_icon);
        } else {
            mNextIv.setClickable(false);
            mNextIv.setBackgroundResource(R.drawable.complete_unclick_icon);
        }
    }

    private void selectSex(int sex) {
        this.mSex = sex;
        if (sex == ESex.SX_MALE.getValue()) {
            mMale.setChecked(true);
            mFemale.setChecked(false);
            mSecre.setChecked(false);
        } else if (sex == ESex.SX_FEMALE.getValue()) {
            mMale.setChecked(false);
            mFemale.setChecked(true);
            mSecre.setChecked(false);
        } else {
            mMale.setChecked(false);
            mFemale.setChecked(false);
            mSecre.setChecked(true);
        }
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
                return myUserInfoServerApi.verifyName(string).subscribeOn(Schedulers.io());
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
        if (!mSkrSdcardPermission.onBackFromPermisionManagerMaybe(getActivity())) {
        }
        if (!mSkrAudioPermission.onBackFromPermisionManagerMaybe(getActivity())) {
        }
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
