package com.zq.dialog;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;

public class GrabKickDialogView extends RelativeLayout {
    public final static String TAG = "GrabKickDialogView";

    SimpleDraweeView mAvatarIv;
    TextView mContentTv;
    ExTextView mCancleTv;
    ExTextView mConfirmTv;

    UserInfoModel mUserInfoModel;
    int type;     // 弹窗类别
    int num;      // 确认金币作金币数目，踢人弹窗时作倒计时用

    HandlerTaskTimer mCounDownTimer;
    Listener mGrabKickViewListener;

    GrabKickDialogView(Context context, UserInfoModel userInfoModel, int type, int num) {
        super(context);
        this.mUserInfoModel = userInfoModel;
        this.type = type;
        this.num = num;
        initView();
        initData();
    }

    public void initView() {
        inflate(getContext(), R.layout.grab_kick_dialog_view, this);

        mAvatarIv = (SimpleDraweeView) findViewById(R.id.avatar_iv);
        mContentTv = (TextView) findViewById(R.id.content_tv);
        mCancleTv = (ExTextView) findViewById(R.id.cancle_tv);
        mConfirmTv = (ExTextView) findViewById(R.id.confirm_tv);

    }

    private void initData() {
        if (mUserInfoModel == null) {
            MyLog.d(TAG, "未找到该用户相关信息");
            return;
        }
        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(mUserInfoModel.getAvatar())
                        .setCircle(true)
                        .build());

        if (type == GrabKickDialog.KICK_TYPE_CONFIRM) {
            // 金币确认
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append("将").setForegroundColor(U.getColor(R.color.black_trans_50))
                    .append(" " + mUserInfoModel.getNickname() + " ").setForegroundColor(Color.parseColor("#F5A623"))
                    .append("移除房间需要消耗").setForegroundColor(U.getColor(R.color.black_trans_50))
                    .append("" + num).setForegroundColor(Color.parseColor("#F5A623"))
                    .append("金币").setForegroundColor(U.getColor(R.color.black_trans_50))
                    .create();
            mContentTv.setText(stringBuilder);
            mCancleTv.setText("取消");
            mConfirmTv.setText("确认");
        } else if (type == GrabKickDialog.KICK_TYPE_REQUEST) {
            // 是否同意踢人
            starCounDown(num);
            mCancleTv.setText("不同意");
            mConfirmTv.setText("同意");
        }
    }

    public void setListener(Listener mGrabKickViewListener) {
        this.mGrabKickViewListener = mGrabKickViewListener;
    }

    public void starCounDown(final int counDown) {
        disposeCounDownTask();
        mCounDownTimer = HandlerTaskTimer.newBuilder()
                .take(counDown)
                .interval(1000)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        SpannableStringBuilder stringBuilder = new SpanUtils()
                                .append("是否同意将").setForegroundColor(U.getColor(R.color.black_trans_50))
                                .append("" + mUserInfoModel.getNickname()).setForegroundColor(Color.parseColor("#F5A623"))
                                .append("移出房间").setForegroundColor(U.getColor(R.color.black_trans_50))
                                .append((counDown - integer) + "s").setForegroundColor(Color.parseColor("#F5A623"))
                                .create();
                        mContentTv.setText(stringBuilder);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (mGrabKickViewListener != null) {
                            mGrabKickViewListener.onTimeOut();
                        }

                    }
                });
    }

    private void disposeCounDownTask() {
        if (mCounDownTimer != null) {
            mCounDownTimer.dispose();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disposeCounDownTask();
        mGrabKickViewListener = null;
    }

    public interface Listener {
        void onTimeOut();
    }
}
