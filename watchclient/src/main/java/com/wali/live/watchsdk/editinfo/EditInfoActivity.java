package com.wali.live.watchsdk.editinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.FragmentDataListener;
import com.base.log.MyLog;
import com.base.view.BackTitleBar;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.editinfo.fragment.EditNameFragment;

/**
 * Created by lan on 2017/8/14.
 */
public class EditInfoActivity extends BaseSdkActivity implements View.OnClickListener, FragmentDataListener {
    public final static String EXTRA_OUT_INFO_CHANGED = "info_changed";

    private BackTitleBar mTitleBar;

    //头像区域
    private View mAvatarContainer;
    private SimpleDraweeView mAvatarDv;

    //名字区域
    private View mNameContainer;
    private TextView mUserNameTv;

    //性别区域
    private View mGenderContainer;
    private TextView mUserGenderTv;

    //签名区域
    private View mSloganContainer;
    private TextView mUserSloganTv;

    private User mMe;

    private boolean mInfoChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        initData();
        initView();
    }

    private void initData() {
        mMe = MyUserInfoManager.getInstance().getUser();
        MyLog.d(TAG, "my info=" + mMe);
    }

    private void initView() {
        mTitleBar = $(R.id.title_bar);
        mTitleBar.setTitle(getString(R.string.change_info));
        mTitleBar.getBackBtn().setOnClickListener(this);

        mAvatarContainer = $(R.id.avatar_container);
        mAvatarContainer.setOnClickListener(this);
        mAvatarDv = (SimpleDraweeView) findViewById(R.id.avatar_dv);
        AvatarUtils.loadAvatarByUidTs(mAvatarDv, mMe.getUid(), mMe.getAvatar(), true);

        mNameContainer = $(R.id.name_container);
        mNameContainer.setOnClickListener(this);
        mUserNameTv = $(R.id.user_name_tv);


        mGenderContainer = $(R.id.gender_container);
        mGenderContainer.setOnClickListener(this);
        mUserGenderTv = $(R.id.user_gender_tv);
        int gender = mMe.getGender();
        switch (gender) {
            case 1:
                mUserGenderTv.setText(R.string.gender_man);
                break;
            case 2:
                mUserGenderTv.setText(R.string.gender_woman);
                break;
            default:
                MyLog.v(TAG, "unkown gender=" + gender);
                break;
        }

        mSloganContainer = $(R.id.signature_container);
        mSloganContainer.setOnClickListener(this);
        mUserSloganTv = $(R.id.user_signature_tv);
        if (!TextUtils.isEmpty(mMe.getSign())) {
            mUserSloganTv.setText(mMe.getSign());
        }

        // 第一次初始化更新UI
        updateUI();
    }

    private void updateUI() {
        updateNameContainer();
    }

    private void updateNameContainer() {
        if (!TextUtils.isEmpty(mMe.getNickname())) {
            mUserNameTv.setText(mMe.getNickname());
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_iv) {
            clickBackBtn();
        } else if (i == R.id.name_container) {
            clickNameContainer();
        } else if (i == R.id.gender_container) {
            clickGenderContainer();   //点击性别区域
        }
    }

    private void clickBackBtn() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_OUT_INFO_CHANGED, mInfoChanged);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void clickNameContainer() {
        EditNameFragment.open(this, this, null);
    }

    private void clickGenderContainer() {

    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
        MyLog.w(TAG, "requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == EditNameFragment.REQUEST_CODE) {
            updateNameContainer();
        }
    }

    public static void open(Activity activity) {
        Intent intent = new Intent(activity, EditInfoActivity.class);
        activity.startActivity(intent);
    }
}
