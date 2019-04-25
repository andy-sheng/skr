package com.module.playways.grab.room.view.pk;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.component.busilib.view.BitmapTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.zq.live.proto.Room.EQRoundOverReason;

import java.util.List;


/**
 * PK 结果卡片
 */
public class PKRoundOverCardView extends RelativeLayout {

    LinearLayout mPkArea;
    ImageView mLeftAvatarBg;
    SimpleDraweeView mLeftAvatarIv;
    ExTextView mLeftName;
    ImageView mLeftOverReasonIv;

    ImageView mRightAvatarBg;
    SimpleDraweeView mRightAvatarIv;
    ExTextView mRightName;
    ImageView mRightOverReasonIv;

    LinearLayout mSroreArea;
    TextView mLeftTipsTv;
    BitmapTextView mLeftScoreBtv;
    BitmapTextView mRightScoreBtv;

    Handler mUiHandler = new Handler();

    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

    UserInfoModel mLeftUserInfoModel;
    UserInfoModel mRightUserInfoModel;
    int mLeftOverReason;
    int mRightOverReason;
    String mLeftScore;
    String mRightScore;
    private GrabRoomData mRoomData;
    SVGAListener mSVGAListener;

    public PKRoundOverCardView(Context context) {
        super(context);
        init();
    }

    public PKRoundOverCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PKRoundOverCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_pk_round_over_card_layout, this);

        mPkArea = (LinearLayout) findViewById(R.id.pk_area);
        mLeftAvatarBg = (ImageView) findViewById(R.id.left_avatar_bg);
        mLeftAvatarIv = (SimpleDraweeView) findViewById(R.id.left_avatar_iv);
        mLeftName = (ExTextView) findViewById(R.id.left_name);
        mLeftOverReasonIv = (ImageView) findViewById(R.id.left_over_reason_iv);
        mRightAvatarBg = (ImageView) findViewById(R.id.right_avatar_bg);
        mRightAvatarIv = (SimpleDraweeView) findViewById(R.id.right_avatar_iv);
        mRightName = (ExTextView) findViewById(R.id.right_name);
        mRightOverReasonIv = (ImageView) findViewById(R.id.right_over_reason_iv);
        mSroreArea = (LinearLayout) findViewById(R.id.srore_area);
        mLeftTipsTv = (TextView) findViewById(R.id.left_tips_tv);
        mLeftScoreBtv = (BitmapTextView) findViewById(R.id.left_score_btv);
        mRightScoreBtv = (BitmapTextView) findViewById(R.id.right_score_btv);
    }

    public void bindData(GrabRoundInfoModel roundInfoModel, SVGAListener svgaListener) {
        mSVGAListener = svgaListener;
        mLeftUserInfoModel = null;
        mRightUserInfoModel = null;
        mLeftScore = "0.0";
        mRightScore = "0.0";
        List<SPkRoundInfoModel> list = roundInfoModel.getsPkRoundInfoModels();
        if (list != null && list.size() >= 2) {
            mLeftUserInfoModel = mRoomData.getUserInfo(list.get(0).getUserID());
            this.mLeftScore = String.format("%.1f", list.get(0).getScore());
            mRightUserInfoModel = mRoomData.getUserInfo(list.get(1).getUserID());
            this.mRightScore = String.format("%.1f", list.get(1).getScore());
        }

        if (mLeftUserInfoModel != null) {
            AvatarUtils.loadAvatarByUrl(mLeftAvatarIv,
                    AvatarUtils.newParamsBuilder(mLeftUserInfoModel.getAvatar())
                            .setCircle(true)
                            .build());
            mLeftName.setText(mLeftUserInfoModel.getNickname());
            mLeftScoreBtv.setText(mLeftScore);
            showOverReason(mLeftOverReason, mLeftOverReasonIv);
        }

        if (mRightUserInfoModel != null) {
            AvatarUtils.loadAvatarByUrl(mRightAvatarIv,
                    AvatarUtils.newParamsBuilder(mRightUserInfoModel.getAvatar())
                            .setCircle(true)
                            .build());
            mRightName.setText(mRightUserInfoModel.getNickname());
            mRightScoreBtv.setText(mRightScore);
            showOverReason(mRightOverReason, mRightOverReasonIv);
        }

        playCardEnterAnimation();
    }

    private void showOverReason(int overReason, ImageView overReasonIv) {
        if (overReasonIv == null) {
            return;
        }
        if (overReason == EQRoundOverReason.ROR_SELF_GIVE_UP.getValue()) {
            overReasonIv.setVisibility(VISIBLE);
            overReasonIv.setBackgroundResource(R.drawable.grab_pk_buchangle);
        } else if (overReason == EQRoundOverReason.ROR_MULTI_NO_PASS.getValue()) {
            overReasonIv.setVisibility(VISIBLE);
            overReasonIv.setBackgroundResource(R.drawable.grab_pk_miedeng);
        } else if (overReason == EQRoundOverReason.ROR_IN_ROUND_PLAYER_EXIT.getValue()) {
            overReasonIv.setVisibility(VISIBLE);
            overReasonIv.setBackgroundResource(R.drawable.grab_pk_diaoxianle);
        } else {
            overReasonIv.setVisibility(GONE);
        }
    }

    /**
     * 入场动画
     */
    private void playCardEnterAnimation() {
        setVisibility(VISIBLE);
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = new TranslateAnimation(-U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F, 0.0F);
            mEnterTranslateAnimation.setDuration(200);
        }
        mEnterTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.startAnimation(mEnterTranslateAnimation);

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        }, 3000);
    }

    /**
     * 离场动画，整个pk结束才执行
     */
    public void hide() {
        mUiHandler.removeCallbacksAndMessages(null);
        if (this != null && this.getVisibility() == VISIBLE) {
            if (mLeaveTranslateAnimation == null) {
                mLeaveTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
                mLeaveTranslateAnimation.setDuration(200);
            }
            mLeaveTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    clearAnimation();
                    setVisibility(GONE);
                    if (mSVGAListener != null) {
                        mSVGAListener.onFinished();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.startAnimation(mLeaveTranslateAnimation);
        } else {
            clearAnimation();
            setVisibility(GONE);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            destory();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destory();
    }

    private void destory() {
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation.setAnimationListener(null);
            mEnterTranslateAnimation.cancel();
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation.setAnimationListener(null);
            mLeaveTranslateAnimation.cancel();
        }
        mUiHandler.removeCallbacksAndMessages(null);
        clearAnimation();
    }

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
    }

    public GrabRoomData getRoomData() {
        return mRoomData;
    }
}
