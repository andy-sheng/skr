package com.component.notification;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.ResponseCallBack;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.component.busilib.R;
import com.component.busilib.view.AvatarView;
import com.component.busilib.view.NickNameView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zq.live.proto.Common.ESex;

/**
 * 关注弹窗通知
 */
public class FollowNotifyView extends RelativeLayout {

    public final String TAG = "FollowNotifyView";

    AvatarView mAvatarIv;
    NickNameView mNameView;
    ExTextView mHintTv;
    ExTextView mFollowTv;

    UserInfoModel mUserInfoModel;

    Drawable unFollowDrawable = new DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#FFC15B"))
            .setCornersRadius(U.getDisplayUtils().dip2px(20))
            .build();

    public FollowNotifyView(Context context) {
        super(context);
        init();
    }

    public FollowNotifyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FollowNotifyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.relation_notification_view_layout, this);
        mNameView = findViewById(R.id.name_view);
        mAvatarIv = findViewById(R.id.avatar_iv);
        mHintTv = findViewById(R.id.hint_tv);
        mFollowTv = findViewById(R.id.follow_tv);

        mFollowTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // TODO: 2019/3/21  处理关注请求
                if (mUserInfoModel.isFollow() || mUserInfoModel.isFriend()) {
//                    UserInfoManager.getInstance().mateRelation(mUserInfoModel.getUserId(),
//                            UserInfoManager.RA_UNBUILD, mUserInfoModel.isFriend());
                } else {
                    UserInfoManager.getInstance().mateRelation(mUserInfoModel.getUserId(),
                            UserInfoManager.RA_BUILD, mUserInfoModel.isFriend(), new ResponseCallBack() {
                                @Override
                                public void onServerSucess(Object o) {
                                    mFollowTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, U.getDisplayUtils().dip2px(18f));
                                    mFollowTv.setTextColor(Color.parseColor("#CC7F00"));
                                    mFollowTv.setClickable(false);
                                    mFollowTv.setText("互相关注");
                                    mFollowTv.setBackground(null);
                                }

                                @Override
                                public void onServerFailed() {

                                }
                            });
                }
                if (mListener != null) {
                    mListener.onFollowBtnClick();
                }
            }
        });
    }

    public void bindData(UserInfoModel userInfoModel) {
        this.mUserInfoModel = userInfoModel;

        mAvatarIv.bindData(userInfoModel);
        mNameView.setAllStateText(userInfoModel);
        if (mUserInfoModel.isFriend()) {
            // 好友怎么展示
            mFollowTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, U.getDisplayUtils().dip2px(18f));
            mFollowTv.setTextColor(Color.parseColor("#CC7F00"));
            mFollowTv.setClickable(false);
            mFollowTv.setText("互相关注");
            mFollowTv.setBackground(null);
        } else if (mUserInfoModel.isFollow()) {
            MyLog.w(TAG, "error 他关注我，为什么我能收到我关注他，但是我们不是好友？？？");
        } else {
            // 粉丝
            mFollowTv.setTextColor(Color.parseColor("#AD6C00"));
            mFollowTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, U.getDisplayUtils().dip2px(16f));
            mFollowTv.setText("关注Ta");
            mFollowTv.setClickable(true);
            mFollowTv.setBackground(unFollowDrawable);
        }
    }

    Listener mListener;

    public void setListener(Listener l) {
        mListener = l;
    }

    public interface Listener {
        void onFollowBtnClick();
    }
}
