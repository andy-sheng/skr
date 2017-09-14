package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.IFrescoCallBack;
import com.base.log.MyLog;
import com.base.thread.ThreadPool;
import com.base.utils.display.DisplayUtils;
import com.base.utils.network.Network;
import com.base.utils.toast.ToastUtils;
import com.base.utils.ui.ColorFormatter;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.repository.GiftRepository;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.event.UserActionEvent;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveProto;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.scheme.SchemeConstants;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by lan on 17/4/6.
 */
public class WidgetItemView extends LinearLayout {
    private static final String TAG = WidgetItemView.class.getSimpleName();

    public static final int POS_LEFT_TOP = 0;
    public static final int POS_RIGHT_TOP = 1;
    public static final int POS_LEFT_BOTTOM = 2;
    public static final int POS_RIGHT_BOTTOM = 3;

    private int mPosFlag;

    private WidgetView.IPresenter mPresenter;

    private BaseImageView mItemIv;
    private BaseImageView mItemIv2;
    private TextView mItemTv;

    private Subscription mImgSubscription;
    private Subscription mTxtSubscription;
    private Subscription mShowSubscription;

    private int mImgCounter;
    private int mTxtCounter;

    private SupportWidgetView mSupportWv;
    private boolean mIsCanClick = true;

    public WidgetItemView(Context context) {
        super(context);
        init(context);
    }

    public WidgetItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WidgetItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final <T extends View> T $$(@IdRes int resId) {
        return (T) ((ViewStub) findViewById(resId)).inflate();
    }

    protected final <T extends View> T $(View parent, @IdRes int resId) {
        return (T) parent.findViewById(resId);
    }

    private void init(Context context) {
        inflate(context, R.layout.widget_item_view, this);

        mItemIv = $(R.id.item_iv);
        mItemIv2 = $(R.id.item_iv2);
        mItemTv = $(R.id.item_tv);

        mSupportWv = $(R.id.support_widget_view);
    }

    public void setWidgetPos(int posFlag) {
        mPosFlag = posFlag;
        if ((mPosFlag & 0x1) != 0) {
            setGravity(Gravity.RIGHT);
            mItemTv.setGravity(Gravity.RIGHT);
        }
    }

    public void setPresenter(WidgetView.IPresenter presenter) {
        mPresenter = presenter;
    }

    public void hide() {
        if (getVisibility() != View.GONE) {
            clearAnimation();
            setVisibility(GONE);
        }
    }

    public void show() {
        if (getVisibility() != View.VISIBLE) {
            setVisibility(VISIBLE);
            setAlpha(0);
            animate().alpha(1f).setStartDelay(300).setDuration(300).start();
        }
    }

    public void showWidgetItem(LiveCommonProto.NewWidgetItem info, boolean needShow) {
        initWidget(info.getDisplayType(), info, mItemIv, mItemTv, mItemIv2, this, mPosFlag);
        if (needShow) {
            show();
        }
    }

    public void setSupportWidgetView(LiveCommonProto.NewWidgetItem info) {
        if (mSupportWv.hasInitial()) {
            return;
        }
        if (info.hasClickItem()) {
            LiveCommonProto.ClickItem click = info.getClickItem();
            if (click.hasClickImageUrl()) {
                mSupportWv.setPic(click.getClickWaitingImageUrl(), click.getClickImageUrl());
            }
            if (click.hasClickInterval()) {
                mSupportWv.setTotalTime(click.getClickInterval());
            }
            setClick(info, mSupportWv, mItemTv, click.getWarningText(), click.getPushSendSuccText());
            mSupportWv.showWaiting();
        } else {
            mSupportWv.setVisibility(GONE);
            mSupportWv.stopCountdown();
            mSupportWv.stopRippleAnimator();
        }
    }

    private void setClick(final LiveCommonProto.NewWidgetItem info, final View clickView, final TextView counterTv, final String waringTxt, final String succText) {
        RxView.clicks(clickView)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (!mSupportWv.isCountingDown() && mIsCanClick) {
                            if (!Network.hasNetwork(getContext())) {
                                ToastUtils.showToast(R.string.no_net);
                                return;
                            }

                            mIsCanClick = false;
                            GiftRepository.clickCounter(info.getWidgetID(), mPresenter.getUid(), mPresenter.getRoomId())
                                    .throttleFirst(3000, TimeUnit.MILLISECONDS)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<LiveProto.WidgetClickRsp>() {
                                        @Override
                                        public void call(LiveProto.WidgetClickRsp rsp) {
                                            mIsCanClick = true;
                                            if (rsp.getRetCode() == 0) {
                                                if (counterTv != null) {
                                                    counterTv.setText(rsp.getCounterText());
                                                }

                                                mSupportWv.stopRippleAnimator();
                                                if (!TextUtils.isEmpty(succText)) {
                                                    ToastUtils.showToast(succText);
                                                }
                                                sendClick(info.getWidgetID(), "buttonClick");

                                                mSupportWv.showWaiting();
                                                if (info.getClickItem().hasGiftId()) {
                                                    UserActionEvent.post(UserActionEvent.EVENT_TYPE_CLICK_SUPPORT_WIDGET, rsp.getGiftId(), null);
                                                }

                                                setClick(info, clickView, counterTv, waringTxt, succText);
                                            } else if (rsp.getRetCode() == 21702) {
                                                ToastUtils.showToast(R.string.vote_finish_toast);
                                            } else if (rsp.getRetCode() == 21703) {
                                                ToastUtils.showToast(R.string.vote_max_toast);
                                            } else {
                                                ToastUtils.showToast(R.string.no_net);
                                            }
                                        }
                                    }, new Action1<Throwable>() {
                                        @Override
                                        public void call(Throwable throwable) {
                                            MyLog.e(TAG, throwable);
                                            mIsCanClick = true;
                                        }
                                    });
                        } else {
                            //xx时间后才可以点击
                            if (TextUtils.isEmpty(waringTxt)) {
                                ToastUtils.showToast(R.string.can_not_vote_toast);
                            } else {
                                ToastUtils.showToast(waringTxt);
                            }
                        }
                    }
                });
    }

    public void updateDisplayItemTv(int widgetID, String counter, boolean needShow) {
        if (needShow) {
            mItemTv.setText(counter);
        }
    }

    /**
     * 初始化运营位信息
     *
     * @param type    运营位类型 0：常驻 1：一次性展示 2：轮播展示
     * @param info    运营位信息
     * @param iv      图片控件
     * @param tv      计数控件
     * @param iv2     点赞图片控件
     * @param layout  根布局点击
     * @param posFlag 位置标识
     */
    private void initWidget(int type, final LiveCommonProto.NewWidgetItem info,
                            final BaseImageView iv, final TextView tv, BaseImageView iv2, final LinearLayout layout,
                            final int posFlag) {
        MyLog.i(TAG, "initWidget position = " + info.getPosition() + ",widget id:" + info.getWidgetID());

        iv.setVisibility(GONE);
        tv.setVisibility(GONE);
        iv2.setVisibility(GONE);

        iv.setImageBitmap(null);
        iv.setBackground(null);

        iv2.setImageBitmap(null);
        iv2.setBackground(null);

        final List<LiveCommonProto.NewWidgetUnit> data = info.getWidgetUintList();

        // 第一张图片
        if (data != null && data.size() > 0) {
            final LiveCommonProto.NewWidgetUnit unit = data.get(0);

            if (unit.hasLinkUrl()) {
                // 如果有
                Uri uri = Uri.parse(unit.getLinkUrl());
                String shopType = uri.getQueryParameter(SchemeConstants.PARAM_SHOP_TYPE);
                String shopShowType = uri.getQueryParameter(SchemeConstants.PARAM_SHOP_SHOW_TYPE);

                MyLog.d(TAG, "shop type=" + shopType + ", shop show type=" + shopShowType);
                if (shopType != null && shopShowType != null) {
                    MyLog.d(TAG, "initWidget block position=" + posFlag);
                    return;
                }

                if (unit.hasIcon()) {
                    setClickEvent(iv, unit, info);
                }
                if (unit.hasText()) {
                    setClickEvent(tv, unit, info);
                }
            }

            if (unit.hasIcon()) {
                iv.setVisibility(VISIBLE);
                loadImgFromNet(unit.getIcon(), iv, posFlag);
            }

            if (unit.hasText()) {
                tv.setVisibility(VISIBLE);
                tv.setText(unit.getText());
                if (unit.hasTextColor()) {
                    tv.setTextColor(ColorFormatter.toHexColor(unit.getTextColor().getRgb()));
                }
            }
        }

        if (type == 2) {
            // 轮循
            if (data != null && data.size() > 1) {
                // 图片
                mImgSubscription = Observable.interval(info.getDisplayTime(), info.getDisplayTime(), TimeUnit.SECONDS).compose(((RxActivity) getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                if (mImgCounter >= 0 && mImgCounter < data.size() - 1) {
                                    mImgCounter++;
                                } else {
                                    mImgCounter = 0;
                                }

                                // 活动链接
                                if (data.get(mImgCounter).hasLinkUrl()) {
                                    RxView.clicks(iv)
                                            .throttleFirst(500, TimeUnit.MILLISECONDS)
                                            .subscribe(new Action1<Void>() {
                                                @Override
                                                public void call(Void aVoid) {
                                                    UserActionEvent.post(UserActionEvent.EVENT_TYPE_CLICK_ATTACHMENT,
                                                            data.get(mImgCounter).getLinkUrl(), data.get(mImgCounter).getUrlNeedParam(), data.get(mImgCounter).getOpenType(),
                                                            mPresenter.getUid());
                                                    sendClick(info.getWidgetID(), "iconClick");
                                                }
                                            });
                                }

                                if (data.get(mImgCounter).hasIcon()) {
                                    loadImgFromNet(data.get(mImgCounter).getIcon(), iv, posFlag);
                                }
                            }
                        });

                // 文案
                mTxtSubscription = Observable
                        .interval(info.getDisplayTime(), info.getDisplayTime(), TimeUnit.SECONDS)
                        .compose(((RxActivity) getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                if (mTxtCounter >= 0 && mTxtCounter < data.size() - 1) {
                                    mTxtCounter++;
                                } else {
                                    mTxtCounter = 0;
                                }
                                if (data.get(mTxtCounter).hasText()) {
                                    tv.setText(data.get(mTxtCounter).getText());
                                    if (data.get(mTxtCounter).hasTextColor()) {
                                        tv.setTextColor(ColorFormatter.toHexColor(data.get(mTxtCounter).getTextColor().getRgb()));
                                    }
                                }
                            }
                        });
            } else {
                mShowSubscription = Observable
                        .interval(info.getDisplayTime(), TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                        .compose(((RxActivity) getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                if (layout.getVisibility() == VISIBLE) {
                                    layout.setVisibility(INVISIBLE);
                                } else {
                                    layout.setVisibility(VISIBLE);
                                }
                            }
                        });
            }
        } else if (type == 1) {
            // 一次展示
            Observable.timer(info.getDisplayTime(), TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                    .compose(((RxActivity) getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {
                            layout.setVisibility(INVISIBLE);
                        }
                    });
        }

        // 计数文字
        LiveCommonProto.CounterItem item = info.getCounterItem();
        if (item != null && item.hasCounterText()) {
            tv.setVisibility(VISIBLE);

            if (item.hasIsBold() && item.getIsBold()) {
                TextPaint tp = tv.getPaint();
                tp.setFakeBoldText(true);
            }
            tv.setText(item.getCounterText());

            if (item.hasTextColor()) {
                tv.setTextColor(ColorFormatter.toHexColor(item.getTextColor().getRgb()));
            } else {
                tv.setTextColor(getContext().getResources().getColor(R.color.color_ffd171));
            }

            if (item.hasTextEdgeColor()) {
                tv.setShadowLayer(2f, 2.5f, 2.5f, ColorFormatter.toHexColor(item.getTextColor().getRgb()));
            }

            try {
                ImageRequest imageRequest = ImageRequestBuilder
                        .newBuilderWithSource(Uri.parse(item.getImageUrl()))
                        .setProgressiveRenderingEnabled(true)
                        .build();
                ImagePipeline imagePipeline = Fresco.getImagePipeline();
                DataSource dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);
                dataSource.subscribe(new BaseBitmapDataSubscriber() {
                    @Override
                    public void onNewResultImpl(@Nullable final Bitmap bitmap) {
                        // bitmap即为下载所得图片
                        ThreadPool.runOnUi(new Runnable() {
                            @Override
                            public void run() {
                                if (tv != null) {
                                    tv.setBackground(new BitmapDrawable(bitmap));
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailureImpl(DataSource dataSource) {
                    }
                }, CallerThreadExecutor.getInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 支持图片
        final LiveCommonProto.ClickItem click = info.getClickItem();
        if (click != null) {
            if (click.getClickType() == 1) {
                if (click.hasClickImageUrl()) {
                    loadImgFromNet(click.getClickImageUrl(), iv2, posFlag);
                }
                RxView.clicks(iv2)
                        .throttleFirst(500, TimeUnit.MILLISECONDS)
                        .subscribe(new Action1<Void>() {
                            @Override
                            public void call(Void aVoid) {
                                EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(
                                        GiftEventClass.GiftMallEvent.EVENT_TYPE_CLICK_SELECT_GIFT, click.getGiftId(), null));
                                sendClick(info.getWidgetID(), "buttonClick");
                            }
                        });
            }
        }
    }

    /**
     * 设置图片点击事件
     */
    private void setClickEvent(View clickView, final LiveCommonProto.NewWidgetUnit unit, final LiveCommonProto.NewWidgetItem info) {
        RxView.clicks(clickView)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        UserActionEvent.post(UserActionEvent.EVENT_TYPE_CLICK_ATTACHMENT,
                                unit.getLinkUrl(), unit.getUrlNeedParam(), unit.getOpenType(), mPresenter.getUid());
                        sendClick(info.getWidgetID(), "iconClick");
                    }
                });
    }

    /**
     * 加载网络图片
     */
    public void loadImgFromNet(final String icon, final BaseImageView imageView, final int posFlag) {
        MyLog.w(TAG, "loadImgFromNet url = " + icon);
        AvatarUtils.loadCoverByUrl(imageView, icon, false, 0, 240, 200, new WidgetFrescoCallBack(posFlag, imageView));
    }

    /**
     * 运营位打点
     */
    private void sendClick(int widgetID, String type) {
        String key = String.format(StatisticsKey.KEY_WIDGET_CLICK, String.valueOf(widgetID), type, mPresenter.getRoomId());
        if (TextUtils.isEmpty(key)) {
            return;
        }
        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
    }

    public void destroyView() {
        if (mImgSubscription != null && !mImgSubscription.isUnsubscribed()) {
            mImgSubscription.unsubscribe();
        }
        if (mTxtSubscription != null && !mTxtSubscription.isUnsubscribed()) {
            mTxtSubscription.unsubscribe();
        }
        if (mShowSubscription != null && !mShowSubscription.isUnsubscribed()) {
            mShowSubscription.unsubscribe();
        }
    }

    private static class WidgetFrescoCallBack implements IFrescoCallBack {
        private int mPosFlag;
        private SoftReference<BaseImageView> mIvRef;

        protected WidgetFrescoCallBack(int posFlag, BaseImageView iv) {
            mPosFlag = posFlag;
            mIvRef = new SoftReference<>(iv);
        }

        @Override
        public void process(Object object) {
        }

        @Override
        public void processWithInfo(ImageInfo info) {
            if (null != info) {
                int infoWidth = info.getWidth();
                int infoHeight = info.getHeight();
                MyLog.w(TAG, "processWithInfo width = " + info.getWidth() + " , height = " + info.getHeight());

                if (infoWidth > 0 && infoHeight > 0 && mIvRef.get() != null) {
                    MyLog.w(TAG, "processWithInfo call internal");

                    int screenWidth = DisplayUtils.getScreenHeight() > DisplayUtils.getScreenWidth() ? DisplayUtils.getScreenWidth() : DisplayUtils.getScreenHeight();
                    int screenHeight = DisplayUtils.getScreenHeight() > DisplayUtils.getScreenWidth() ? DisplayUtils.getScreenHeight() : DisplayUtils.getScreenWidth();

                    int width = infoWidth * screenWidth / 1080;
                    int height = infoHeight * screenHeight / 1920;

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
                    if ((mPosFlag & 0x1) == 1) {
                        lp.gravity = Gravity.RIGHT;
                    }
                    mIvRef.get().setLayoutParams(lp);
                }
            } else {
                MyLog.e(TAG, "loadImgFromNet fail ,info is null");
            }
        }

        @Override
        public void processWithFailure() {
            MyLog.w(TAG, "processWithFailure");
        }
    }
}
