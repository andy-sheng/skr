package com.wali.live.watchsdk.personalcenter.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.event.KeyboardEvent;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.user.User;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.editinfo.EditInfoActivity;
import com.wali.live.watchsdk.editinfo.fragment.presenter.EditNamePresenter;
import com.wali.live.watchsdk.editinfo.fragment.presenter.IEditNameView;
import com.wali.live.watchsdk.income.view.NoLeakEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by zhujianning on 18-6-25.
 * 半屏修改名字页面
 */

public class EditNameHalfFragment extends RxFragment implements IEditNameView {
    private static final String TAG = "EditNameHalfFragment";
    public static final int REQUEST_CODE = GlobalData.getRequestCode();
    private final static int NICKNAME_CHANGE_CD = 60 * 60 * 24;

    //ui
    private TextView mBackTv;
    private TextView mConfirmTv;
    private NoLeakEditText mInputEt;
    private View mTopView;

    //data
    private User mUser;
    private int mTrans = 0;
    private int mNameMaxCount;
    private boolean mInfoChanged;

    //presenter
    private EditNamePresenter mPresenter;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.frag_change_name_half, container, false);
    }

    @Override
    protected void bindView() {
        mBackTv = (TextView) mRootView.findViewById(R.id.back_tv);
        mConfirmTv = (TextView) mRootView.findViewById(R.id.confirm_tv);
        mInputEt = (NoLeakEditText) mRootView.findViewById(R.id.input_et);
        mTopView = mRootView.findViewById(R.id.place_holder_view);

        initListener();
        initPresenter();

        mNameMaxCount = getResources().getInteger(R.integer.max_name_char_count);
        mUser = MyUserInfoManager.getInstance().getUser();
        if(mUser != null) {
            bindInput();
        }
    }

    private void bindInput() {
        if(!TextUtils.isEmpty(mUser.getNickname())) {
            mInputEt.setText(mUser.getNickname());
            mInputEt.setSelection(mUser.getNickname().length());
        }
    }

    private void initPresenter() {
        mPresenter = new EditNamePresenter(this);
    }

    private void initListener() {
        //禁止输入回车
        mInputEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });

        mInputEt.setTransformationMethod(new ReplacementTransformationMethod() {
            @Override
            protected char[] getOriginal() {
                char[] aa = {'\r', '\n'};
                return aa;
            }

            @Override
            protected char[] getReplacement() {
                char[] cc = {' ', ' '};
                return cc;
            }
        });

        RxView.clicks(mBackTv).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        FragmentNaviUtils.popFragmentFromStack(getActivity());
                    }
                });
        RxView.clicks(mConfirmTv).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        onClickConfirm();
                    }
                });
    }

    private void onClickConfirm() {
        String name = mInputEt.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtils.showToast(R.string.name_is_empty);
            return;
        }
        if (name.equals(mUser.getNickname())) {
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

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false, priority = 1)
    public void onEventMainThread(KeyboardEvent event) {
        MyLog.d(TAG, "KeyboardEvent");
        int start = DisplayUtils.dip2px(0f);
        int height = KeyboardUtils.getKeyboardHeight(getActivity());
        int y = KeyboardUtils.getScreenHeight(getActivity()) - mInputEt.getBottom() - mTopView.getHeight();
        mTrans = height - y;
        if(mTrans < 0) {
            mTrans = 0;
        }
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE:
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTopView.getLayoutParams();
                params.height = mTopView.getHeight() - mTrans;
                mTopView.setLayoutParams(params);
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                RelativeLayout.LayoutParams paramsl = (RelativeLayout.LayoutParams) mTopView.getLayoutParams();
                paramsl.height = start;
                mTopView.setLayoutParams(paramsl);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mPresenter.destory();
    }

    @Override
    public void editSuccess(String name) {
        MyLog.d(TAG, "editSuccess name=" + name);
        mInfoChanged = true;
        ToastUtils.showToast(R.string.change_name_success);
        MyUserInfoManager.getInstance().setNickname(name);

        closeFragment();
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
    public void editFailure(int code) {
        MyLog.w(TAG, "editFailure code=" + code);
        if (code == ErrorCode.CODE_CONTAIN_SENSITIVE) {
            ToastUtils.showToast(R.string.change_failed_include_sensitive);
        } else {
            ToastUtils.showToast(R.string.change_name_failed);
        }
    }

    public static void openFragment(BaseSdkActivity activity, int containerId, FragmentDataListener listener) {
        Bundle bundle = new Bundle();
        BaseFragment fragment = FragmentNaviUtils.addFragment(activity, containerId, EditNameHalfFragment.class,
                bundle, true, true, true);
        fragment.initDataResult(REQUEST_CODE, listener);
    }
}
