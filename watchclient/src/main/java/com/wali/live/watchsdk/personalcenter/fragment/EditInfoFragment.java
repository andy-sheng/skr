package com.wali.live.watchsdk.personalcenter.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

import static android.app.Activity.RESULT_OK;

/**
 * Created by zhujianning on 18-6-25.
 * 修改资料页面
 */

public class EditInfoFragment extends BaseFragment implements FragmentDataListener{
    private static final String TAG = "EditInfoFragment";
    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    //ui
    private TextView mBackTv;
    private BaseImageView mAvatorIv;
    private RelativeLayout mAvatorContainer;
    private RelativeLayout mNameContainer;
    private RelativeLayout mGenderContainer;
    private RelativeLayout mSignContainer;
    private TextView mNameTv;
    private TextView mGenderTv;
    private TextView mSignTv;

    //data
    private User mUser;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.frag_edit_info_half, container, false);
    }

    @Override
    protected void bindView() {
        mBackTv = (TextView) mRootView.findViewById(R.id.back_tv);
        mAvatorIv = (BaseImageView) mRootView.findViewById(R.id.avatar_iv);
        mAvatorContainer = (RelativeLayout) mRootView.findViewById(R.id.avator_container);
        mNameContainer = (RelativeLayout) mRootView.findViewById(R.id.name_container);
        mGenderContainer = (RelativeLayout) mRootView.findViewById(R.id.gender_container);
        mSignContainer = (RelativeLayout) mRootView.findViewById(R.id.sign_container);
        mNameTv = (TextView) mRootView.findViewById(R.id.name_tv);
        mGenderTv = (TextView) mRootView.findViewById(R.id.gender_tv);
        mSignTv = (TextView) mRootView.findViewById(R.id.sign_tv);

        mUser = MyUserInfoManager.getInstance().getUser();

        initListener();

        if(mUser != null) {
            bindAvator();
            bindNickName();
            bindGender();
            bindSign();
        } else {
            MyLog.w(TAG, "user Info is null");
        }
    }

    private void initListener() {
        RxView.clicks(mBackTv).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        FragmentNaviUtils.popFragmentFromStack(getActivity());
                    }
                });

        RxView.clicks(mNameContainer).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EditNameHalfFragment.openFragment((BaseSdkActivity) getActivity(), R.id.fl_edit_container, EditInfoFragment.this);
                    }
                });

        RxView.clicks(mGenderContainer).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EditGenderHalfFragment.openFragment((BaseSdkActivity) getActivity(), R.id.fl_edit_container, EditInfoFragment.this);
                    }
                });
        RxView.clicks(mSignContainer).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EditSignHalfFragment.openFragment((BaseSdkActivity) getActivity(), R.id.fl_edit_container, EditInfoFragment.this);
                    }
                });
    }

    private void bindAvator() {
        AvatarUtils.loadAvatarByUidTs(mAvatorIv, mUser.getUid(), mUser.getAvatar(), true);
    }

    private void bindNickName() {
        mNameTv.setText(TextUtils.isEmpty(mUser.getNickname()) ? String.valueOf(mUser.getUid()) : mUser.getNickname());
    }

    private void bindGender() {
        mGenderTv.setText(mUser.getGender() == 1 ? CommonUtils.getString(R.string.gender_man) : CommonUtils.getString(R.string.gender_woman));
    }

    private void bindSign() {
        mSignTv.setText(TextUtils.isEmpty(mUser.getSign()) ? CommonUtils.getString(R.string.default_sign_txt) : mUser.getSign());
    }

    public static void openFragment(BaseSdkActivity activity, int containerId) {
        Bundle bundle = new Bundle();
        FragmentNaviUtils.openFragment(activity, EditInfoFragment.class, bundle, containerId,
                true, R.anim.slide_right_in, R.anim.slide_bottom_out);
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
        MyLog.w(TAG, "requestCode=" + requestCode + ", resultCode=" + resultCode);
//        if (resultCode != RESULT_OK) {
//            return;
//        }
        if (requestCode == EditNameHalfFragment.REQUEST_CODE) {
            bindNickName();
        } else if(requestCode == EditGenderHalfFragment.REQUEST_CODE) {
            bindGender();
        } else if(requestCode == EditSignHalfFragment.REQUEST_CODE) {
            bindSign();
        }
    }
}
