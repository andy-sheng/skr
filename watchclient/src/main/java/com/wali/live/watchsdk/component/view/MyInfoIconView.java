package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

public class MyInfoIconView {
    private static final String TAG = "MyInfoIconView";

    View mRealView;

    BaseImageView mMyAvatarIv;
    TextView mDescTv;

    public MyInfoIconView(Context context) {
        init(context);
    }

    private void init(Context context) {
        LayoutInflater factory = LayoutInflater.from(context);
        mRealView = factory.inflate(R.layout.my_info_icon_view, null);
        mMyAvatarIv = (BaseImageView)mRealView.findViewById(R.id.my_avatar_iv);
        mDescTv = (TextView)mRealView.findViewById(R.id.desc_tv);
        tryBindAvatar();
    }

    public void tryBindAvatar() {
        if(UserAccountManager.getInstance().hasAccount()){
            AvatarUtils.loadAvatarByUidTs(mMyAvatarIv, MyUserInfoManager.getInstance().getUuid(), MyUserInfoManager.getInstance().getAvatar(),true);
        }else{
            AvatarUtils.loadAvatarByUidTs(mMyAvatarIv,0,0,true);
        }
    }

    public View getRealView() {
        return mRealView;
    }
}
