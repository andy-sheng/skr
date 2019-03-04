package com.module.playways.grab.room.top;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;

import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.grab.room.event.ShowPersonCardEvent;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;

import com.common.view.ex.ExTextView;

import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import io.reactivex.functions.Consumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GrabTopItemView extends RelativeLayout {
    public static final int MODE_GRAB = 1;
    public static final int MODE_SING = 2;

    public CircleAnimationView mCircleAnimationView;
    public BaseImageView mAvatarIv;
    public ExImageView mFlagIv;
    public PlayerInfoModel mPlayerInfoModel;
    AnimationDrawable mFlickerAnim;

    public ExTextView mLeaveIv;

    int mMode = MODE_GRAB;

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
        mLeaveIv = (ExTextView) findViewById(R.id.leave_iv);

        RxView.clicks(mAvatarIv)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        if (mPlayerInfoModel != null && mPlayerInfoModel.getUserInfo() != null) {
                            EventBus.getDefault().post(new ShowPersonCardEvent(mPlayerInfoModel.getUserInfo().getUserId()));
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

    public void updateOnLineState(PlayerInfoModel userInfoModel){
        if (userInfoModel == null) {
            return;
        }
        mPlayerInfoModel = userInfoModel;
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mPlayerInfoModel.getUserInfo().getAvatar())
                .setCircle(true)
                .setGray(mPlayerInfoModel.isOnline() ? false : true) // 先加上，方便调试时看出哪个用户离开了
                .setBorderColorBySex(mPlayerInfoModel.getUserInfo().getSex() == 1)
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .build()
        );
    }

    public void bindData(PlayerInfoModel userInfoModel) {
        if (userInfoModel == null) {
            return;
        }
        mPlayerInfoModel = userInfoModel;
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mPlayerInfoModel.getUserInfo().getAvatar())
                .setCircle(true)
                .setGray(mPlayerInfoModel.isOnline() ? false : true) // 先加上，方便调试时看出哪个用户离开了
                .setBorderColorBySex(mPlayerInfoModel.getUserInfo().getSex() == 1)
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .build()
        );

//        if (mPlayerInfoModel.isOnline()) {
//            mLeaveIv.setVisibility(GONE);
//            mFlagIv.setVisibility(VISIBLE);
//        } else {
//            mLeaveIv.setVisibility(VISIBLE);
//            mFlagIv.setVisibility(GONE);
//        }
        mLeaveIv.setVisibility(GONE);
        mFlagIv.setVisibility(VISIBLE);
    }

    //占位的View
    public void setToPlaceHolder() {
        mAvatarIv.setImageDrawable(U.getDrawable(R.drawable.guanzhong_kongwei));
    }

    //开始闪烁，有人爆灯的时候
    public void startEvasive() {
        stopEvasive();
        mFlickerAnim = new AnimationDrawable();
        mFlickerAnim.setOneShot(true);
        Drawable drawable = null;
        drawable = U.getDrawable(R.drawable.liangdeng_shan);
        mFlickerAnim.addFrame(drawable, 200);
        drawable = U.getDrawable(R.drawable.liangdeng);
        mFlickerAnim.addFrame(drawable, 300);
        drawable = U.getDrawable(R.drawable.liangdeng_shan);
        mFlickerAnim.addFrame(drawable, 200);
        drawable = U.getDrawable(R.drawable.liangdeng);
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
        setGrap(false);
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

    public void setGrap(boolean grap) {
//        if (!mPlayerInfoModel.isOnline()) {
//            mLeaveIv.setVisibility(VISIBLE);
//            mFlagIv.setVisibility(GONE);
//        } else {
//            if (grap) {
//                mFlagIv.setVisibility(VISIBLE);
//                LayoutParams lp = (LayoutParams) mFlagIv.getLayoutParams();
//                lp.topMargin = -U.getDisplayUtils().dip2px(10);
//                mFlagIv.setLayoutParams(lp);
//                mFlagIv.setImageResource(R.drawable.xiangchang_flag);
//            } else {
//                mFlagIv.setVisibility(GONE);
//            }
//        }

        if (grap) {
            mFlagIv.setVisibility(VISIBLE);
            LayoutParams lp = (LayoutParams) mFlagIv.getLayoutParams();
            lp.topMargin = -U.getDisplayUtils().dip2px(10);
            mFlagIv.setLayoutParams(lp);
            mFlagIv.setImageResource(R.drawable.xiangchang_flag);
        } else {
            mFlagIv.setVisibility(GONE);
        }
    }

    public void setLight(boolean on) {
//        if (!mPlayerInfoModel.isOnline()) {
//            mLeaveIv.setVisibility(VISIBLE);
//            mFlagIv.setVisibility(GONE);
//        } else {
//            mFlagIv.setVisibility(VISIBLE);
//            LayoutParams lp = (LayoutParams) mFlagIv.getLayoutParams();
//            lp.topMargin = -U.getDisplayUtils().dip2px(20);
//            mFlagIv.setLayoutParams(lp);
//            if (on) {
//                mFlagIv.setImageResource(R.drawable.liangdeng);
//            } else {
//                mFlagIv.setImageResource(R.drawable.miedeng);
//            }
//        }

        mFlagIv.setVisibility(VISIBLE);
        LayoutParams lp = (LayoutParams) mFlagIv.getLayoutParams();
        lp.topMargin = -U.getDisplayUtils().dip2px(20);
        mFlagIv.setLayoutParams(lp);
        if (on) {
            mFlagIv.setImageResource(R.drawable.liangdeng);
        } else {
            mFlagIv.setImageResource(R.drawable.miedeng);
        }
    }

    public void setGetSingChance() {
        mCircleAnimationView.setVisibility(VISIBLE);
    }

    public PlayerInfoModel getPlayerInfoModel() {
        return mPlayerInfoModel;
    }
}
