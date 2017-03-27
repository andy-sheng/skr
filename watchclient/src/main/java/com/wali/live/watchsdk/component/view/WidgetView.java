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
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
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
    private static final String TAG = "OperatingView";

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

    public WidgetView(Context context) {
        this(context, null, 0);
    }

    public WidgetView(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public WidgetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.widget_view, this);

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
                    mWidgetIds.put(info.getWidgetID(), 0);
                    setLeftTopInfo(info);
                    break;
                case 1://右上角
                    mWidgetIds.put(info.getWidgetID(), 1);
                    setRightTopInfo(info);
                    break;
                case 2://左下角
                    mWidgetIds.put(info.getWidgetID(), 2);
                    setLeftBottomInfo(info);
                    break;
                case 3://右下角
                    mWidgetIds.put(info.getWidgetID(), 3);
                    setRightBottomInfo(info);
                    break;
            }
        }
    }

    /**
     * 设置左上角运营位数据
     */
    private void setLeftTopInfo(LiveCommonProto.NewWidgetItem info) {
        initWidget(info.getDisplayType(), info, mLeftTopIv, mLeftTopTv, mLeftTopIv2, mLeftTopWv, "left");
        mLeftTopWv.setVisibility(VISIBLE);
    }

    /**
     * 设置右上角运营位数据
     */
    private void setRightTopInfo(LiveCommonProto.NewWidgetItem info) {
        initWidget(info.getDisplayType(), info, mRightTopIv, mRightTopTv, mRightTopIv2, mRightTopWv, "right");
        mRightTopWv.setVisibility(VISIBLE);
    }

    /**
     * 设置左下角运营位数据
     */
    private void setLeftBottomInfo(LiveCommonProto.NewWidgetItem info) {
        initWidget(info.getDisplayType(), info, mLeftBottomIv, mLeftBottomTv, mLeftBottomIv2, mLeftBottomWv, "leftBottom");
        mLeftBottomWv.setVisibility(VISIBLE);
    }

    /**
     * 设置右下角运营位数据
     */
    private void setRightBottomInfo(LiveCommonProto.NewWidgetItem info) {
        initWidget(info.getDisplayType(), info, mRightBottomIv, mRightBottomTv, mRightBottomIv2, mRightBottomWv, "rightBottom");
        mRightBottomWv.setVisibility(VISIBLE);
    }

    /**
     * 初始化运营位信息
     *
     * @param type       运营位类型 0：常驻 1：一次性展示 2：轮播展示
     * @param info       运营位信息
     * @param imgTop     图片控件
     * @param txtCounter 计数控件
     * @param imgDown    点赞图片控件
     * @param llytClick  根布局点击
     */
    private void initWidget(int type, LiveCommonProto.NewWidgetItem info, BaseImageView imgTop, final TextView txtCounter, BaseImageView imgDown, LinearLayout llytClick, String widgetDirection) {
        MyLog.i(TAG, "initWidget position = " + info.getPosition() + ",widget id:" + info.getWidgetID());

        imgTop.setVisibility(GONE);
        txtCounter.setVisibility(GONE);
        imgDown.setVisibility(GONE);

        imgTop.setImageBitmap(null);
        imgTop.setBackground(null);

        imgDown.setImageBitmap(null);
        imgDown.setBackground(null);

        List<LiveCommonProto.NewWidgetUnit> data = info.getWidgetUintList();

        // 第一张图片
        if (data != null && data.size() > 0) {
            LiveCommonProto.NewWidgetUnit unit = data.get(0);
            if (unit.hasIcon()) {
                imgTop.setVisibility(VISIBLE);
                loadImgFromNet(unit.getIcon(), imgTop, widgetDirection);
            }

            if (unit.hasLinkUrl()) {
                // TODO
//                RxView.clicks(imgTop)
//                        .throttleFirst(500, TimeUnit.MILLISECONDS)
//                        .subscribe(aVoid -> {
//                            if (unit.getUrlNeedParam()) {
//                                EventBus.getDefault().post(new EventClass.UserActionEvent(EventClass.UserActionEvent.EVENT_TYPE_CLICK_ATTACHMENT, unit.getLinkUrl(), true, unit.getOpenType(), currentZuid));
//                            } else {
//                                EventBus.getDefault().post(new EventClass.UserActionEvent(EventClass.UserActionEvent.EVENT_TYPE_CLICK_ATTACHMENT, unit.getLinkUrl(), false, unit.getOpenType(), currentZuid));
//                            }
//                            sendClick(info.getWidgetID(), "iconClick");
//                        });
            }

            // 文案
            if (unit.hasText()) {
                txtCounter.setVisibility(VISIBLE);
                txtCounter.setText(unit.getText());
                if (unit.hasTextColor()) {
                    txtCounter.setTextColor(ColorFormatter.toHexColor(unit.getTextColor().getRgb()));
                }

                if (!unit.hasIcon() && unit.hasLinkUrl()) {
                    // TODO
//                    RxView.clicks(txtCounter)
//                            .throttleFirst(500, TimeUnit.MILLISECONDS)
//                            .subscribe(aVoid -> {
//                                if (unit.getUrlNeedParam()) {
//                                    EventBus.getDefault().post(new EventClass.UserActionEvent(EventClass.UserActionEvent.EVENT_TYPE_CLICK_ATTACHMENT, unit.getLinkUrl(), true, unit.getOpenType(), currentZuid));
//                                } else {
//                                    EventBus.getDefault().post(new EventClass.UserActionEvent(EventClass.UserActionEvent.EVENT_TYPE_CLICK_ATTACHMENT, unit.getLinkUrl(), false, unit.getOpenType(), currentZuid));
//                                }
//                                sendClick(info.getWidgetID(), "iconClick");
//                            });
                }
            }
        }

        // 计数文字
        LiveCommonProto.CounterItem item = info.getCounterItem();
        if (item != null && item.hasCounterText()) {
            txtCounter.setVisibility(VISIBLE);

            if (item.hasIsBold() && item.getIsBold()) {
                TextPaint tp = txtCounter.getPaint();
                tp.setFakeBoldText(true);
            }
            txtCounter.setText(item.getCounterText());

            if (item.hasTextColor()) {
                txtCounter.setTextColor(ColorFormatter.toHexColor(item.getTextColor().getRgb()));
            } else {
                txtCounter.setTextColor(getContext().getResources().getColor(R.color.color_ffd171));
            }

            if (item.hasTextEdgeColor()) {
                txtCounter.setShadowLayer(2f, 2.5f, 2.5f, ColorFormatter.toHexColor(item.getTextColor().getRgb()));
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
                                if (txtCounter != null) {
                                    txtCounter.setBackground(new BitmapDrawable(bitmap));
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

        //支持图片
        LiveCommonProto.ClickItem click = info.getClickItem();
        if (click != null) {
            if (click.getClickType() == 1) {
                if (click.hasClickImageUrl()) {
                    loadImgFromNet(click.getClickImageUrl(), imgDown, widgetDirection);
                }
                // TODO 选中礼物
//                RxView.clicks(imgDown)
//                        .throttleFirst(500, TimeUnit.MILLISECONDS)
//                        .subscribe(aVoid -> {
//                            EventBus.getDefault().post(new EventClass.GiftEvent(EventClass.GiftEvent.EVENT_TYPE_CLICK_SELECT_GIFT, click.getGiftId(), null));
//                            sendClick(info.getWidgetID(), "buttonClick");
//                        });
            }
        }
    }

    /**
     * 加载网络图片
     *
     * @param icon
     * @param imageView
     */
    public void loadImgFromNet(final String icon, final BaseImageView imageView, final String type) {
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
                            switch (type) {
                                case "right":
                                case "rightBottom":
                                    lp.gravity = Gravity.RIGHT;
                                    break;
                            }
                            imageView.setLayoutParams(lp);
                            AvatarUtils.loadCoverByUrl(imageView, icon, false, 0, width, height);
                        }
                    }
                });
    }

    /**
     * 运营位显示图片时先去网络获取图片的宽高信息
     *
     * @param url 图片地址
     * @return 宽，高
     */
    private String loadImageFromNetwork(String url) {
        try {
            URL m_url = new URL(url);
            HttpURLConnection con = (HttpURLConnection) m_url.openConnection();
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
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy {
        void hideWidgetView();

        void showWidgetView(@NonNull List<LiveCommonProto.NewWidgetItem> list);
    }
}
