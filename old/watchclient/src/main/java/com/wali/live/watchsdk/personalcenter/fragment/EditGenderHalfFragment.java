package com.wali.live.watchsdk.personalcenter.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.FragmentEvent;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.editinfo.EditInfoActivity;
import com.wali.live.watchsdk.editinfo.fragment.presenter.EditGenderPresenter;
import com.wali.live.watchsdk.editinfo.fragment.presenter.IEditGenderView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by zhujianning on 18-6-25.
 */

public class EditGenderHalfFragment extends RxFragment implements IEditGenderView {

    private static final String TAG = "EditGenderHalfFragment";
    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    //ui
    private TextView mBackIv;
    private LinearLayout mManContainer;
    private ImageView mManBtn;
    private LinearLayout mWomanContainer;
    private ImageView mWomanBtn;
    private View mTopView;

    //presenter
    private EditGenderPresenter mPresenter;

    //data
    private User mUser;
    private int mGender;
    private int mSelectedGender;
    private TextView mConfirmTv;
    private boolean mInfoChanged;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.frag_edit_gender_half, container, false);
    }

    @Override
    protected void bindView() {
        mBackIv = (TextView) mRootView.findViewById(R.id.back_tv);
        mManContainer = (LinearLayout) mRootView.findViewById(R.id.setting1);
        mManBtn = (ImageView) mRootView.findViewById(R.id.check1);
        mWomanContainer = (LinearLayout) mRootView.findViewById(R.id.setting2);
        mWomanBtn = (ImageView) mRootView.findViewById(R.id.check2);
        mConfirmTv = (TextView) mRootView.findViewById(R.id.confirm_tv);
        mTopView = mRootView.findViewById(R.id.place_holder_view);

        mUser = MyUserInfoManager.getInstance().getUser();
        mGender = mUser.getGender();
        updateView(mGender);

        initListener();
        initPresenter();
    }

    private void initListener() {
        RxView.clicks(mManContainer).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        updateView(User.GENDER_MAN);
                    }
                });

        RxView.clicks(mWomanContainer).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        updateView(User.GENDER_WOMAN);
                    }
                });

        RxView.clicks(mBackIv).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        closeFragment();
                    }
                });

        RxView.clicks(mConfirmTv).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        clickConfirmBtn();
                    }
                });

        RxView.clicks(mTopView).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        FragmentNaviUtils.popAllFragmentFromStack(getActivity());
                    }
                });
    }

    private void initPresenter() {
        mPresenter = new EditGenderPresenter(this);
    }

    private void updateView(int selectedGender) {
        if (mSelectedGender != selectedGender) {
            mSelectedGender = selectedGender;
            if (mSelectedGender == User.GENDER_MAN) {
                mManBtn.setVisibility(View.VISIBLE);
                mWomanBtn.setVisibility(View.GONE);
            } else {
                mManBtn.setVisibility(View.GONE);
                mWomanBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    private void clickConfirmBtn() {
        if (mSelectedGender != mGender) {
            mPresenter.uploadGender(mSelectedGender);
        } else {
            closeFragment();
        }
    }

    @Override
    public <T> Observable.Transformer<T, T> bindUntilEvent() {
        return bindUntilEvent(FragmentEvent.DESTROY_VIEW);
    }

    @Override
    public void editSuccess(int gender) {
        MyLog.d(TAG, "editSuccess gender=" + gender);
        mInfoChanged = true;
        ToastUtils.showToast(R.string.change_gender_success);

        mGender = gender;
        mUser.setGender(mGender);
        updateView(mGender);

        closeFragment();
    }

    @Override
    public void editFailure(int code) {
        MyLog.w(TAG, "editFailure code=" + code);
        ToastUtils.showToast(R.string.change_gender_failed);
    }

    private void closeFragment() {
        MyLog.w(TAG, "closeFragment infoChanged=" + mInfoChanged);
        if (mInfoChanged && mDataListener != null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(EditInfoActivity.EXTRA_OUT_INFO_CHANGED, mInfoChanged);
            mDataListener.onFragmentResult(mRequestCode, Activity.RESULT_OK, bundle);
        }
        finish();
    }

    private void finish() {
        MyLog.w(TAG, "finish");
        FragmentNaviUtils.popFragmentFromStack(getActivity());
    }

    public static void openFragment(BaseSdkActivity activity, int containerId, FragmentDataListener listener) {
        BaseFragment fragment = FragmentNaviUtils.addFragment(activity, containerId, EditGenderHalfFragment.class,
                new Bundle(), true, true, true);
        fragment.initDataResult(REQUEST_CODE, listener);
    }
}
