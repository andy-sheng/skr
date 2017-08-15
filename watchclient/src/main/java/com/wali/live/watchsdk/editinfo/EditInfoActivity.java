package com.wali.live.watchsdk.editinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.base.view.BackTitleBar;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

/**
 * Created by lan on 2017/8/14.
 */
public class EditInfoActivity extends BaseSdkActivity implements View.OnClickListener {
    public final static String EXTRA_OUT_INFO_CHANGED = "info_changed";

    private BackTitleBar mTitleBar;

    private View mAvatarContainer;      //头像区域
    private SimpleDraweeView mAvatarDv;     //显示头像

    private View mNameContainer;        //名字区域
    private TextView mUserNameTv;       //显示姓名

    private View mGenderContainer;      //性别区域
    private TextView mUserGenderTv;     //显示性别

    private View mSloganContainer;      //签名区域
    private TextView mUserSloganTv;     //显示签名

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
        if (!TextUtils.isEmpty(mMe.getNickname())) {
            mUserNameTv.setText(mMe.getNickname());
        }

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
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_iv) {
            clickBackBtn();
        }
    }

    private void clickBackBtn() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_OUT_INFO_CHANGED, mInfoChanged);
        setResult(RESULT_OK, intent);
        finish();
    }

    public static void open(Activity activity) {
        Intent intent = new Intent(activity, EditInfoActivity.class);
        activity.startActivity(intent);
    }
}
