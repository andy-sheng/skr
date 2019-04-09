package com.zq.notification;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zq.live.proto.Common.ESex;

/**
 * 关注弹窗通知
 */
public class FollowNotifyView extends RelativeLayout {

    public final static String TAG = "FollowNotifyView";

    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ExTextView mHintTv;
    ExImageView mFollowTv;

    UserInfoModel mUserInfoModel;

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
        mAvatarIv = (SimpleDraweeView) findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) findViewById(R.id.name_tv);
        mHintTv = (ExTextView) findViewById(R.id.hint_tv);
        mFollowTv = (ExImageView) findViewById(R.id.follow_tv);

        mFollowTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // TODO: 2019/3/21  处理关注请求
                if (mUserInfoModel.isFollow() || mUserInfoModel.isFriend()) {
//                    UserInfoManager.getInstance().mateRelation(mUserInfoModel.getUserId(),
//                            UserInfoManager.RA_UNBUILD, mUserInfoModel.isFriend());
                } else {
                    UserInfoManager.getInstance().mateRelation(mUserInfoModel.getUserId(),
                            UserInfoManager.RA_BUILD, mUserInfoModel.isFriend(), new UserInfoManager.ResponseCallBack() {
                                @Override
                                public void onServerSucess(Object o) {
                                    mFollowTv.setBackgroundResource(R.drawable.person_card_friend);
                                    mFollowTv.setClickable(false);
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

        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(mUserInfoModel.getAvatar())
                        .setCircle(true)
                        .build());
        mNameTv.setText(mUserInfoModel.getNickname());

        if (mUserInfoModel.isFriend()) {
            // 好友怎么展示
            mFollowTv.setBackgroundResource(R.drawable.person_card_friend);
            mFollowTv.setClickable(false);
        } else if (mUserInfoModel.isFollow()) {
            MyLog.w(TAG, "error 他关注我，为什么我能收到我关注他，但是我们不是好友？？？");
        } else {
            // 粉丝
            mFollowTv.setClickable(true);
            mFollowTv.setBackgroundResource(R.drawable.person_card_follow);
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
