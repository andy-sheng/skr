package com.module.playways.grab.room.top;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.playways.R;
import com.module.playways.grab.room.event.GrabWantInviteEvent;
import com.zq.person.event.ShowPersonCardEvent;
import com.module.playways.room.prepare.model.PlayerInfoModel;
import com.zq.live.proto.Room.EWantSingType;

import org.greenrobot.eventbus.EventBus;

public class GrabTopItemView extends RelativeLayout {
    public final String TAG = "GrabTopItemView";
    public static final int MODE_GRAB = 1;
    public static final int MODE_SING = 2;

    public CircleAnimationView mCircleAnimationView;
    public BaseImageView mAvatarIv;
    public ExImageView mFlagIv;
    public PlayerInfoModel mPlayerInfoModel;
    public AnimationDrawable mFlickerAnim;
    public ExTextView mTvIconBg;
    public ExImageView mOwnerIconIv;
    public SpeakingTipsAnimationView mSpeakingTipsAnimationView;
    public boolean mShowEmptySeat = false;

    int mMode = MODE_GRAB;
    private boolean mCanShowInviteWhenEmpty = false; // 能否显示邀请按钮

    private boolean mLast = false; // 是否是最后一个

    public GrabTopItemView(Context context) {
        super(context);
        init();
    }

    public GrabTopItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabTopItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_top_view_holder_layout, this);
        mCircleAnimationView = (CircleAnimationView) this.findViewById(R.id.circle_animation_view);
        mAvatarIv = (BaseImageView) this.findViewById(R.id.avatar_iv);
        mFlagIv = (ExImageView) this.findViewById(R.id.flag_iv);
        mOwnerIconIv = findViewById(R.id.owner_icon_iv);
        mTvIconBg = (ExTextView) findViewById(R.id.tv_icon_bg);
        mSpeakingTipsAnimationView = findViewById(R.id.speaker_animation_iv);

        mAvatarIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mShowEmptySeat && mCanShowInviteWhenEmpty) {
                    EventBus.getDefault().post(new GrabWantInviteEvent());
                } else {
                    if (mPlayerInfoModel != null && mPlayerInfoModel.getUserInfo() != null) {
                        if (!mShowEmptySeat) {
                            EventBus.getDefault().post(new ShowPersonCardEvent(mPlayerInfoModel.getUserInfo().getUserId()));
                        }
                    }
                }
            }
        });
    }

    public void tryAddParent(LinearLayout grabTopRv) {
        if (this.getParent() == null) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            lp.weight = 1;
            grabTopRv.addView(this, lp);
        }
    }

    public void updateOnLineState(PlayerInfoModel userInfoModel) {
        if (userInfoModel == null) {
            return;
        }
        boolean hasUpdate = false;
        if (mPlayerInfoModel == null) {
            mPlayerInfoModel = userInfoModel;
            hasUpdate = true;
        } else {
            if (userInfoModel.getUserInfo().getSex() == mPlayerInfoModel.getUserInfo().getSex()
                    && userInfoModel.isOnline() == mPlayerInfoModel.isOnline()
                    && userInfoModel.getUserInfo().getAvatar().equals(mPlayerInfoModel.getUserInfo().getAvatar())) {
                hasUpdate = false;
            } else {
                mPlayerInfoModel = userInfoModel;
                hasUpdate = true;
            }
        }
        if (hasUpdate) {
            AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mPlayerInfoModel.getUserInfo().getAvatar())
                    .setCircle(true)
                    .setGray(mPlayerInfoModel.isOnline() ? false : true) // 先加上，方便调试时看出哪个用户离开了
                    .setBorderColor(U.getColor(R.color.white))
                    .setBorderWidth(U.getDisplayUtils().dip2px(2))
                    .build()
            );
            mShowEmptySeat = false;
        }
    }

    public void bindData(PlayerInfoModel userInfoModel, boolean isOwner) {
        if (userInfoModel == null) {
            return;
        }
        mPlayerInfoModel = userInfoModel;
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mPlayerInfoModel.getUserInfo().getAvatar())
                .setCircle(true)
                .setGray(mPlayerInfoModel.isOnline() ? false : true) // 先加上，方便调试时看出哪个用户离开了
                .setBorderColor(U.getColor(R.color.white))
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .build()
        );
        mShowEmptySeat = false;

        mFlagIv.setVisibility(GONE);
        mCircleAnimationView.setVisibility(GONE);
        if (isOwner) {
            mOwnerIconIv.setVisibility(VISIBLE);
        } else {
            mOwnerIconIv.setVisibility(GONE);
        }

        Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                .setSolidColor(Color.parseColor("#6EBDFF"))
                .setCornersRadius(U.getDisplayUtils().dip2px(20))
                .setStrokeWidth(U.getDisplayUtils().dip2px(2))
                .setStrokeColor(Color.parseColor("#3B4E79"))
                .build();

        mTvIconBg.setBackground(drawable);
    }

    //占位的View
    public void setToPlaceHolder() {
        if (mCanShowInviteWhenEmpty) {
            mAvatarIv.setImageDrawable(U.getDrawable(R.drawable.yichangdaodi_yaoqing));
            if (mLast) {
                EventBus.getDefault().post(new InviteBtnVisibleEvent(true));
            }
        } else {
            mAvatarIv.setImageDrawable(U.getDrawable(R.drawable.yichangdaodi_guanzhong));
            if (mLast) {
                EventBus.getDefault().post(new InviteBtnVisibleEvent(false));
            }
        }
        mOwnerIconIv.setVisibility(GONE);
        mShowEmptySeat = true;

        Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                .setSolidColor(Color.parseColor("#5C67C1"))
                .setCornersRadius(U.getDisplayUtils().dip2px(20))
                .setStrokeWidth(U.getDisplayUtils().dip2px(2))
                .setStrokeColor(Color.parseColor("#3B4E79"))
                .build();

        mTvIconBg.setBackground(drawable);
    }

    //开始闪烁，有人爆灯的时候
    public void startEvasive() {
        stopEvasive();
        mFlickerAnim = new AnimationDrawable();
        mFlickerAnim.setOneShot(false);
        Drawable drawable = null;
        drawable = U.getDrawable(R.drawable.ycdd_baodeng_guangyun);
        mFlickerAnim.addFrame(drawable, 200);
        drawable = U.getDrawable(R.drawable.ycdd_liangdeng);
        mFlickerAnim.addFrame(drawable, 300);
        drawable = U.getDrawable(R.drawable.ycdd_baodeng_guangyun);
        mFlickerAnim.addFrame(drawable, 200);
        drawable = U.getDrawable(R.drawable.ycdd_liangdeng);
        mFlickerAnim.addFrame(drawable, 300);
        mFlagIv.setImageDrawable(mFlickerAnim);
        mFlickerAnim.start();
    }

    public void stopEvasive() {
        if (mFlickerAnim != null) {
            mFlickerAnim.stop();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopEvasive();
    }

    public void reset() {
        hideGrabIcon();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        setLayoutParams(lp);
        this.setTranslationY(0);
        this.setAlpha(1);
        this.setScaleX(1);
        this.setScaleY(1);

        mFlagIv.setTranslationY(0);
        mFlagIv.setAlpha(0xff);
        mFlagIv.setScaleX(1);
        mFlagIv.setScaleY(1);

        mAvatarIv.setTranslationY(0);
        mAvatarIv.setAlpha(0xff);
        mAvatarIv.setScaleX(1);
        mAvatarIv.setScaleY(1);
        mCircleAnimationView.setVisibility(GONE);
        stopEvasive();
    }

    public void setGrap(int wantSingType) {
//        MyLog.d(TAG, "setGrap" + " grap=" + grap);
        mFlagIv.setVisibility(VISIBLE);
        LayoutParams lp = (LayoutParams) mFlagIv.getLayoutParams();
        lp.topMargin = -U.getDisplayUtils().dip2px(8);
        mFlagIv.setLayoutParams(lp);
        if (wantSingType == EWantSingType.EWST_ACCOMPANY_OVER_TIME.getValue()
                || wantSingType == EWantSingType.EWST_COMMON_OVER_TIME.getValue()) {
            mFlagIv.setImageResource(R.drawable.ycdd_biaoqian_tiaozhan);
        } else if (wantSingType == EWantSingType.EWST_CHORUS.getValue()) {
            mFlagIv.setImageResource(R.drawable.ycdd_biaoqian_hechang);
        } else if (wantSingType == EWantSingType.EWST_SPK.getValue()) {
            mFlagIv.setImageResource(R.drawable.ycdd_biaoqian_pk);
        } else {
            mFlagIv.setImageResource(R.drawable.ycdd_biaoqian_qiangchang);
        }

    }

    public void hideGrabIcon() {
        mFlagIv.setVisibility(GONE);
    }

    public void setLight(boolean on) {
        MyLog.d(TAG, "setLight" + " on=" + on);
        mFlagIv.setVisibility(VISIBLE);
        LayoutParams lp = (LayoutParams) mFlagIv.getLayoutParams();
        lp.topMargin = -U.getDisplayUtils().dip2px(22);
        mFlagIv.setLayoutParams(lp);
        if (on) {
            mFlagIv.setImageResource(R.drawable.ycdd_liangdeng);
        } else {
            mFlagIv.setImageResource(R.drawable.ycdd_xideng);
        }
    }

    public void showSpeakingAnimation() {
        mSpeakingTipsAnimationView.show(1000);
    }

    public void hideSpeakingAnimation() {
        mSpeakingTipsAnimationView.hide();
    }

    public void setGetSingChance() {
        mCircleAnimationView.setVisibility(VISIBLE);
    }

    public PlayerInfoModel getPlayerInfoModel() {
        return mPlayerInfoModel;
    }

    public void setCanShowInviteWhenEmpty(boolean canShowInviteWhenEmpty) {
        mCanShowInviteWhenEmpty = canShowInviteWhenEmpty;
    }

    public void setLast(boolean last) {
        mLast = last;
    }

    public static class InviteBtnVisibleEvent {
        boolean visiable;

        public InviteBtnVisibleEvent(boolean visiable) {
            this.visiable = visiable;
        }
    }
}
