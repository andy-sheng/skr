package com.wali.live.watchsdk.editinfo.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.user.User;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.editinfo.EditInfoActivity;
import com.wali.live.watchsdk.editinfo.fragment.presenter.EditNamePresenter;
import com.wali.live.watchsdk.editinfo.fragment.presenter.IEditNameView;

/**
 * Created by lan on 2017/8/15.
 */
public class EditNameFragment extends RxFragment implements View.OnClickListener, FragmentListener,
        IEditNameView {
    // Fragment单一的请求数据回调，直接使用REQUEST_CODE
    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    private BackTitleBar mTitleBar;
    private TextView mRightButton;

    private EditText mNameEt;

    private int mNameMaxCount;
    private String mName;

    private EditNamePresenter mPresenter;
    private User mMe;
    private boolean mInfoChanged;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.edit_name_layout, container, false);
    }

    @Override
    protected void bindView() {
        mTitleBar = $(R.id.title_bar);
        mTitleBar.setTitle(getString(R.string.change_nickname_text));
        mTitleBar.getBackBtn().setOnClickListener(this);

        mRightButton = mTitleBar.getRightTextBtn();
        mRightButton.setText(getString(R.string.save));
        mRightButton.setOnClickListener(this);

        mNameEt = $(R.id.name_et);
        mNameEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        mNameEt.setTransformationMethod(new ReplacementTransformationMethod() {
            @Override
            protected char[] getOriginal() {
                return new char[]{'\r', '\n'};
            }

            @Override
            protected char[] getReplacement() {
                return new char[]{' ', ' '};
            }
        });
        mNameMaxCount = getResources().getInteger(R.integer.max_name_char_count);

        initView();
        initPresenter();
    }

    private void initView() {
        mMe = MyUserInfoManager.getInstance().getUser();
        mName = mMe.getNickname();
        if (!TextUtils.isEmpty(mName)) {
            mNameEt.setText(mName);
        }
    }

    private void initPresenter() {
        mPresenter = new EditNamePresenter(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_iv) {
            closeFragment();
        } else if (i == R.id.right_text_btn) {
            clickSaveBtn();
        }
    }

    private void clickSaveBtn() {
        String name = mNameEt.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtils.showToast(R.string.name_is_empty);
            return;
        }
        if (name.equals(mName)) {
            ToastUtils.showToast(R.string.input_same_nickname);
            return;
        }
        if (name.length() > mNameMaxCount) {
            ToastUtils.showToast(R.string.nickname_illegal);
            return;
        }

        //替换回车换行
        name = name.replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", " ");
        //替换连续的空格
        name = name.replaceAll("\\s+", " ");

        mPresenter.uploadName(name);
    }

    @Override
    public void editSuccess(String name) {
        MyLog.d(TAG, "editSuccess name=" + name);
        mInfoChanged = true;
        ToastUtils.showToast(R.string.change_name_success);

        mName = name;
        mMe.setNickname(mName);

        closeFragment();
    }

    @Override
    public void editFailure(int code) {
        MyLog.w(TAG, "editFailure code=" + code);
        if (code == ErrorCode.CODE_CONTAIN_SENSITIVE) {
            ToastUtils.showToast(R.string.change_failed_include_sensitive);
        } else {
            ToastUtils.showToast(R.string.change_name_failed);
        }
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
        MyLog.d(TAG, " onBackPressed ");
        closeFragment();
        return true;
    }

    public static void open(BaseActivity activity, FragmentDataListener listener, Bundle bundle) {
        BaseFragment fragment = FragmentNaviUtils.addFragment(activity, R.id.main_act_container, EditNameFragment.class,
                bundle, true, true, true);
        fragment.initDataResult(REQUEST_CODE, listener);
    }
}
