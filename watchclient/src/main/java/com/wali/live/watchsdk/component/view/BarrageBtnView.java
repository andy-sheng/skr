package com.wali.live.watchsdk.component.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.AttrRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.display.DisplayUtils;
import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.preference.PreferenceKeys;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.event.EventClass;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.watchsdk.R;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * Created by wangmengjie on 17-7-24.
 *
 * @module 底部输入框
 */
public class BarrageBtnView extends FrameLayout implements
        IComponentView<BarrageBtnView.IPresenter, BarrageBtnView.IView> {
    private static final String TAG = "BarrageBtnView";

    protected IPresenter mPresenter;

    protected TextView mBarrageBtnViewTv;
    protected ImageView mBarrageBtnViewIv;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mPopAnimator != null && mPopAnimator.isRunning()) {
            mPopAnimator.cancel();
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public BarrageBtnView(@NonNull Context context) {
        this(context, null);
    }

    public BarrageBtnView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarrageBtnView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.barrage_btn_view, this);
        LayoutParams layoutParams = new LayoutParams(getResources().getDimensionPixelSize(R.dimen.view_dimen_424),
                getResources().getDimensionPixelSize(R.dimen.view_dimen_110));
        setLayoutParams(layoutParams);

        mBarrageBtnViewTv = $(R.id.barrage_btn_view_txt);
        mBarrageBtnViewIv = $(R.id.barrage_btn_view_img);
        mBarrageBtnViewIv.setEnabled(false);

        mBarrageBtnViewTv.setHint(getResources().getString(R.string.empty_edittext_hint));
        $click(mBarrageBtnViewTv, new OnClickListener() {
            @Override
            public void onClick(View view) {
                MyLog.w(TAG, "open input view!");
                mPresenter.showInputView();
                String msgType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SENDMESSAGE;
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        String.format(msgType, HostChannelManager.getInstance().getChannelId()),
                        TIMES, "1");
            }
        });
    }

    protected View mShiningView;
    protected SimpleDraweeView mBarrageBtnShiningBg;
    protected TextView mPopText;
    protected AnimatorSet mPopAnimator;

    private void playShiningBgAnim() {
        MyLog.d(TAG, "start to play pop anime");

        mShiningView = LayoutInflater.from(getContext()).inflate(R.layout.barrage_send_tip_layout, this, false);
        addView(mShiningView, 1);

        mBarrageBtnShiningBg = (SimpleDraweeView) mShiningView.findViewById(R.id.barrage_btn_bg_shining);
        mPopText = (TextView) mShiningView.findViewById(R.id.pop_text);

        Uri uri = new Uri.Builder().scheme(UriUtil.LOCAL_RESOURCE_SCHEME).path(String.valueOf(R.raw.barrage_shining_bg)).build();
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(mBarrageBtnShiningBg.getController())
                .setImageRequest(request)
                .setAutoPlayAnimations(true)
                .build();
        mBarrageBtnShiningBg.setController(controller);

        ObjectAnimator transAnimator = ObjectAnimator.ofFloat(mPopText, "translationY",
                DisplayUtils.dip2px(6.67f), 0, DisplayUtils.dip2px(6.67f), 0, DisplayUtils.dip2px(6.67f), 0);
        transAnimator.setDuration(5400);
        transAnimator.setRepeatCount(0);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mPopText, "alpha", 1.0f, 0f);
        alphaAnimator.setStartDelay(4500);
        alphaAnimator.setDuration(900);
        alphaAnimator.setRepeatCount(0);

        mPopAnimator = new AnimatorSet();
        mPopAnimator.playTogether(transAnimator, alphaAnimator);
        mPopAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                stopShiningBg();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                stopShiningBg();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mPopAnimator.start();

        rx.Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                PreferenceUtils.setSettingLong(PreferenceKeys.PRE_KEY_LAST_BARRAGE_POP_TIME, System.currentTimeMillis());
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .compose(((RxActivity)getContext()).bindUntilEvent())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Object>() {
                        @Override
                        public void onCompleted() {
                            MyLog.v(TAG, "save show pop time onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            MyLog.e(TAG, e);
                        }

                        @Override
                        public void onNext(Object object) {
                        }
                    });
    }

    private void stopShiningBg() {
        MyLog.d(TAG, "stopShiningBg");
        if (mShiningView == null) {
            return;
        }
        mShiningView.setVisibility(GONE);
        removeView(mShiningView);
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onBarrageShowEvent(EventClass.ShowBarragePopEvent event) {
        MyLog.d(TAG, "got show barrage pop event");
        playShiningBgAnim();
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) BarrageBtnView.this;
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 显示输入框
         */
        void showInputView();
    }

    public interface IView extends IViewProxy {
    }
}
