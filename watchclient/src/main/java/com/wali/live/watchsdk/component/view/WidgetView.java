package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.thread.ThreadPool;
import com.base.utils.display.DisplayUtils;
import com.base.utils.rx.RxRetryAssist;
import com.base.utils.ui.ColorFormatter;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.event.GiftEventClass;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.event.UserActionEvent;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

import org.greenrobot.eventbus.EventBus;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by chenyong on 2017/03/24.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 运营位组件
 */
public class WidgetView extends RelativeLayout
        implements IComponentView<WidgetView.IPresenter, WidgetView.IView> {
    private static final String TAG = "WidgetView";

    private static final int POS_LEFT_TOP = 0;
    private static final int POS_RIGHT_TOP = 1;
    private static final int POS_LEFT_BOTTOM = 2;
    private static final int POS_RIGHT_BOTTOM = 3;

    private static final int PADDING = DisplayUtils.dip2px(10f);

    @Nullable
    protected IPresenter mPresenter;

    private Map<Integer, Integer> mWidgetIds = new HashMap();

    private LinearLayout mLeftTopWv;
    private LinearLayout mRightTopWv;
    private LinearLayout mLeftBottomWv;
    private LinearLayout mRightBottomWv;

    private BaseImageView mLeftTopIv;
    private BaseImageView mLeftTopIv2;
    private BaseImageView mRightTopIv;
    private BaseImageView mRightTopIv2;
    private BaseImageView mLeftBottomIv;
    private BaseImageView mLeftBottomIv2;
    private BaseImageView mRightBottomIv;
    private BaseImageView mRightBottomIv2;

    private TextView mLeftTopTv;
    private TextView mRightTopTv;
    private TextView mLeftBottomTv;
    private TextView mRightBottomTv;

    private Subscription mImgSubscription;
    private Subscription mTxtSubscription;
    private Subscription mShowSubscription;

    private int mImgCounter;
    private int mTxtCounter;

    public WidgetView(Context context) {
        this(context, null, 0);
    }

    public WidgetView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WidgetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.widget_view, this);

        setPadding(PADDING, 0, PADDING, 0);

        mLeftTopWv = $(R.id.left_top_wv);
        mRightTopWv = $(R.id.right_top_wv);
        mLeftBottomWv = $(R.id.left_bottom_wv);
        mRightBottomWv = $(R.id.right_bottom_wv);

        mLeftTopIv = $(mLeftTopWv, R.id.left_top_iv);
        mLeftTopTv = $(mLeftTopWv, R.id.left_top_tv);
        mLeftTopIv2 = $(mLeftTopWv, R.id.left_top_iv2);

        mRightTopIv = $(mRightTopWv, R.id.right_top_iv);
        mRightTopTv = $(mRightTopWv, R.id.right_top_tv);
        mRightTopIv2 = $(mRightTopWv, R.id.right_top_iv2);

        mLeftBottomIv = $(mLeftBottomWv, R.id.left_bottom_iv);
        mLeftBottomTv = $(mLeftBottomWv, R.id.left_bottom_tv);
        mLeftBottomIv2 = $(mLeftBottomWv, R.id.left_bottom_iv2);

        mRightBottomIv = $(mRightBottomWv, R.id.right_bottom_iv);
        mRightBottomTv = $(mRightBottomWv, R.id.right_bottom_tv);
        mRightBottomIv2 = $(mRightBottomWv, R.id.right_bottom_iv2);
    }

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final <T extends View> T $(View parent, @IdRes int resId) {
        return (T) parent.findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    private void showWidgetView(@NonNull List<LiveCommonProto.NewWidgetItem> list) {
        for (int i = 0; i < list.size(); i++) {
            LiveCommonProto.NewWidgetItem info = list.get(i);
            switch (info.getPosition()) {
                case 0://左上角
                    mWidgetIds.put(info.getWidgetID(), POS_LEFT_TOP);
                    setLeftTopInfo(info);
                    break;
                case 1://右上角
                    mWidgetIds.put(info.getWidgetID(), POS_RIGHT_TOP);
                    setRightTopInfo(info);
                    break;
                case 2://左下角
                    mWidgetIds.put(info.getWidgetID(), POS_LEFT_BOTTOM);
                    setLeftBottomInfo(info);
                    break;
                case 3://右下角
                    mWidgetIds.put(info.getWidgetID(), POS_RIGHT_BOTTOM);
                    setRightBottomInfo(info);
                    break;
            }
        }
    }

    /**
     * 设置左上角运营位数据
     */
    private void setLeftTopInfo(LiveCommonProto.NewWidgetItem info) {
        initWidget(info.getDisplayType(), info, mLeftTopIv, mLeftTopTv, mLeftTopIv2, mLeftTopWv, POS_LEFT_TOP);
        mLeftTopWv.setVisibility(VISIBLE);
    }

    /**
     * 设置右上角运营位数据
     */
    private void setRightTopInfo(LiveCommonProto.NewWidgetItem info) {
        initWidget(info.getDisplayType(), info, mRightTopIv, mRightTopTv, mRightTopIv2, mRightTopWv, POS_RIGHT_TOP);
        mRightTopWv.setVisibility(VISIBLE);
    }

    /**
     * 设置左下角运营位数据
     */
    private void setLeftBottomInfo(LiveCommonProto.NewWidgetItem info) {
        initWidget(info.getDisplayType(), info, mLeftBottomIv, mLeftBottomTv, mLeftBottomIv2, mLeftBottomWv, POS_LEFT_BOTTOM);
        mLeftBottomWv.setVisibility(VISIBLE);
    }

    /**
     * 设置右下角运营位数据
     */
    private void setRightBottomInfo(LiveCommonProto.NewWidgetItem info) {
        initWidget(info.getDisplayType(), info, mRightBottomIv, mRightBottomTv, mRightBottomIv2, mRightBottomWv, POS_RIGHT_BOTTOM);
        mRightBottomWv.setVisibility(VISIBLE);
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

            if (unit.hasLinkUrl()) {
                if (unit.hasIcon() || unit.hasText()) {
                    RxView.clicks(unit.hasIcon() ? iv : tv)
                            .throttleFirst(500, TimeUnit.MILLISECONDS)
                            .subscribe(new Action1<Void>() {
                                @Override
                                public void call(Void aVoid) {
                                    UserActionEvent.post(UserActionEvent.EVENT_TYPE_CLICK_ATTACHMENT, unit.getLinkUrl(), unit.getUrlNeedParam(), unit.getOpenType(), mPresenter.getUid());
                                    sendClick(info.getWidgetID(), "iconClick");
                                }
                            });
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
     * 加载网络图片
     */
    public void loadImgFromNet(final String icon, final BaseImageView imageView, final int posFlag) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String str = loadImageFromNetwork(icon);
                if (str != null) {
                    String[] params = str.split(",");
                    if (Integer.parseInt(params[0]) > 0 && Integer.parseInt(params[1]) > 0) {
                        subscriber.onNext(str);
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(new Throwable("not get params"));
                    }
                } else {
                    subscriber.onError(new Throwable("not get params"));
                }
            }
        }).retryWhen(
                new RxRetryAssist(10, 2, false))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {
                        if (s != null) {
                            imageView.setVisibility(VISIBLE);
                            String[] params = s.split(",");

                            int screenWidth = DisplayUtils.getScreenHeight() > DisplayUtils.getScreenWidth() ? DisplayUtils.getScreenWidth() : DisplayUtils.getScreenHeight();
                            int screenHeight = DisplayUtils.getScreenHeight() > DisplayUtils.getScreenWidth() ? DisplayUtils.getScreenHeight() : DisplayUtils.getScreenWidth();

                            int width = Integer.parseInt(params[0]) * screenWidth / 1080;
                            int height = Integer.parseInt(params[1]) * screenHeight / 1920;

                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
                            if ((posFlag & 0x1) == 1) {
                                lp.gravity = Gravity.RIGHT;
                            }
                            imageView.setLayoutParams(lp);
                            AvatarUtils.loadCoverByUrl(imageView, icon, false, 0, width, height);
                        }
                    }
                });
    }

    /**
     * 运营位显示图片时先去网络获取图片的宽高信息
     */
    private String loadImageFromNetwork(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            InputStream in = con.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);

            int height = options.outHeight;
            int width = options.outWidth;
            String s = width + "," + height;
            in.close();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void hideWidgetView() {
        mLeftTopWv.setVisibility(GONE);
        mRightTopWv.setVisibility(GONE);
        mLeftBottomWv.setVisibility(GONE);
        mRightBottomWv.setVisibility(GONE);
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

    /**
     * 运营位打点
     */
    private void sendClick(int widgetID, String type) {
        String key = String.format(StatisticsKey.KEY_WIDGET_CLICK, String.valueOf(widgetID), type, mPresenter.getUid());
        if (TextUtils.isEmpty(key)) {
            return;
        }
        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Nullable
            @Override
            public <T extends View> T getRealView() {
                return (T) WidgetView.this;
            }

            @Override
            public void hideWidgetView() {
                WidgetView.this.hideWidgetView();
            }

            @Override
            public void showWidgetView(@NonNull List<LiveCommonProto.NewWidgetItem> list) {
                WidgetView.this.showWidgetView(list);
            }

            @Override
            public void destroyView() {
                WidgetView.this.destroyView();
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        long getUid();
    }

    public interface IView extends IViewProxy {
        void hideWidgetView();

        void showWidgetView(@NonNull List<LiveCommonProto.NewWidgetItem> list);

        void destroyView();
    }
}
