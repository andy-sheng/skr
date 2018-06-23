package com.wali.live.watchsdk.personalcenter.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.mi.live.data.account.MyUserInfoManager;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

public class MyInfoSummaryView implements IViewProxy{
    public final static String TAG = "MyInfoSummaryView";

    private View mRealView;

    private BaseImageView mAvatarIv;
    private TextView mNameTv;

    public MyInfoSummaryView() {
    }

    private void init(Context context) {
        mRealView = LayoutInflater.from(context).inflate(R.layout.my_info_personal_summary_layout, null);
        mAvatarIv = (BaseImageView) mRealView.findViewById(R.id.avatar_iv);
        mNameTv = (TextView) mRealView.findViewById(R.id.name_tv);

        bindData();
    }

    private void bindData() {
        AvatarUtils.loadAvatarByUidTs(mAvatarIv, MyUserInfoManager.getInstance().getUuid(), MyUserInfoManager.getInstance().getAvatar(), true);
        mNameTv.setText(TAG);
    }

    @Override
    public View getRealView(Context context) {
        if(mRealView==null){
            init(context);
        }
        return mRealView;
    }

}
