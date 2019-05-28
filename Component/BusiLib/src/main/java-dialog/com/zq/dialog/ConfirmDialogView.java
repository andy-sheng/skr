package com.zq.dialog;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.component.busilib.R;
import com.dialog.view.StrokeTextView;
import com.facebook.drawee.view.SimpleDraweeView;

public class ConfirmDialogView extends RelativeLayout {
    public final static String TAG = "GrabKickDialogView";

    SimpleDraweeView mAvatarIv;
    TextView mContentTv;
    StrokeTextView mCancleTv;
    StrokeTextView mConfirmTv;
    ImageView mKickMaskIv;

    UserInfoModel mUserInfoModel;
    int type;     // 弹窗类别
    int num;      // 确认金币作金币数目，踢人弹窗时作倒计时用

    HandlerTaskTimer mCounDownTimer;
    Listener mGrabKickViewListener;

    ConfirmDialogView(Context context, UserInfoModel userInfoModel, int type, int num) {
        super(context);
        this.mUserInfoModel = userInfoModel;
        this.type = type;
        this.num = num;
        initView();
        initData();
    }

    public void initView() {
        inflate(getContext(), R.layout.confirm_dialog_view, this);

        mAvatarIv = (SimpleDraweeView) findViewById(R.id.avatar_iv);
        mContentTv = (TextView) findViewById(R.id.content_tv);
        mCancleTv = (StrokeTextView) findViewById(R.id.cancle_tv);
        mConfirmTv = (StrokeTextView) findViewById(R.id.confirm_tv);
        mKickMaskIv = (ImageView) this.findViewById(R.id.kick_mask_iv);
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

        if (type == ConfirmDialog.TYPE_KICK_CONFIRM) {
            // 金币确认
            if (num != 0) {
                SpannableStringBuilder stringBuilder = new SpanUtils()
                        .append("将").setForegroundColor(Color.parseColor("#3B4E79"))
                        .append(" " + mUserInfoModel.getNicknameRemark() + " \n").setForegroundColor(Color.parseColor("#F5A623"))
                        .append("移除房间需要消耗").setForegroundColor(Color.parseColor("#3B4E79"))
                        .append("" + num).setForegroundColor(Color.parseColor("#F5A623"))
                        .append("金币").setForegroundColor(Color.parseColor("#3B4E79"))
                        .create();
                mContentTv.setText(stringBuilder);
            } else {
                SpannableStringBuilder stringBuilder = new SpanUtils()
                        .append("是否发起投票将").setForegroundColor(Color.parseColor("#3B4E79"))
                        .append(" " + mUserInfoModel.getNicknameRemark() + " ").setForegroundColor(Color.parseColor("#F5A623"))
                        .append("踢出房间").setForegroundColor(Color.parseColor("#3B4E79"))
                        .create();
                mContentTv.setText(stringBuilder);
            }
            mCancleTv.setText("取消");
            mConfirmTv.setText("发起投票");
        } else if (type == ConfirmDialog.TYPE_KICK_REQUEST) {
            // 是否同意踢人
            starCounDown(num);
            mCancleTv.setText("不同意");
            mConfirmTv.setText("同意");
        } else if (type == ConfirmDialog.TYPE_INVITE_CONFIRM) {
            mKickMaskIv.setVisibility(GONE);
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(" " + mUserInfoModel.getNicknameRemark() + " ").setForegroundColor(Color.parseColor("#F5A623"))
                    .append("\n")
                    .append("邀请你加入一唱到底").setForegroundColor(U.getColor(R.color.black_trans_50))
                    .create();
            mContentTv.setText(stringBuilder);

            mCancleTv.setText("忽略");
            mConfirmTv.setText("同意");
        }else if(type == ConfirmDialog.TYPE_OWNER_KICK_CONFIRM){
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append("是否将").setForegroundColor(Color.parseColor("#3B4E79"))
                    .append(" " + mUserInfoModel.getNicknameRemark() + " ").setForegroundColor(Color.parseColor("#F5A623"))
                    .append("踢出房间?").setForegroundColor(Color.parseColor("#3B4E79"))
                    .create();
            mContentTv.setText(stringBuilder);
            mCancleTv.setText("取消");
            mConfirmTv.setText("踢人");
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
                                .append("" + mUserInfoModel.getNicknameRemark()+"\n").setForegroundColor(Color.parseColor("#F5A623"))
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
