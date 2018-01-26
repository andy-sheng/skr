package com.wali.live.watchsdk.editinfo.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.FragmentListener;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.user.User;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.editinfo.EditInfoActivity;
import com.wali.live.watchsdk.editinfo.fragment.presenter.EditGenderPresenter;
import com.wali.live.watchsdk.editinfo.fragment.presenter.IEditGenderView;

import static com.wali.live.watchsdk.R.string.gender;

/**
 * Created by lan on 2017/8/15.
 */
public class EditGenderFragment extends RxFragment implements View.OnClickListener, FragmentListener,
        IEditGenderView {
    // Fragment单一的请求数据回调，直接使用REQUEST_CODE
    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    private BackTitleBar mTitleBar;
    private TextView mRightButton;

    private ImageView mManBtn;
    private ImageView mWomanBtn;

    private int mGender;
    private int mSelectedGender;

    private EditGenderPresenter mPresenter;
    private User mMe;
    private boolean mInfoChanged;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.edit_gender_layout, container, false);
    }

    @Override
    protected void bindView() {
        mTitleBar = $(R.id.title_bar);
        mTitleBar.setTitle(getString(gender));
        mTitleBar.getBackBtn().setOnClickListener(this);

        mRightButton = mTitleBar.getRightTextBtn();
        mRightButton.setText(getString(R.string.save));
        mRightButton.setOnClickListener(this);

        mManBtn = $(R.id.check1);
        mWomanBtn = $(R.id.check2);

        $(R.id.setting1).setOnClickListener(this);
        $(R.id.setting2).setOnClickListener(this);

        initView();
        initPresenter();
    }

    private void initView() {
        mMe = MyUserInfoManager.getInstance().getUser();
        mGender = mMe.getGender();
        updateView(mGender);
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

    private void initPresenter() {
        mPresenter = new EditGenderPresenter(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_iv) {
            closeFragment();
        } else if (i == R.id.right_text_btn) {
            clickSaveBtn();
        } else if (i == R.id.setting1) {
            updateView(User.GENDER_MAN);
        } else if (i == R.id.setting2) {
            updateView(User.GENDER_WOMAN);
        }
    }

    private void clickSaveBtn() {
        if (mSelectedGender != mGender) {
            mPresenter.uploadGender(mSelectedGender);
        } else {
            closeFragment();
        }
    }

    @Override
    public void editSuccess(int gender) {
        MyLog.d(TAG, "editSuccess gender=" + gender);
        mInfoChanged = true;
        ToastUtils.showToast(R.string.change_gender_success);

        mGender = gender;
        mMe.setGender(mGender);
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

    @Override
    public boolean onBackPressed() {
        MyLog.d(TAG, "onBackPressed");
        closeFragment();
        return true;
    }

    public static void open(BaseActivity activity, FragmentDataListener listener, Bundle bundle) {
        BaseFragment fragment = FragmentNaviUtils.addFragment(activity, R.id.main_act_container, EditGenderFragment.class,
                bundle, true, true, true);
        fragment.initDataResult(REQUEST_CODE, listener);
    }
}
