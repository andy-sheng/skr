package com.wali.live.watchsdk.watchtop.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.rx.RxRetryAssist;
import com.base.utils.toast.ToastUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.relation.RelationApi;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.base.BaseEvent;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.RelationProto;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by chengsimin on 16/3/31.
 */
public class WatchTopInfoSingleView extends WatchTopInfoBaseView {
    public static final String TAG = "WatchTopInfoSingleView";


    TextView mFollowBtnTv;

    ImageView mFollowBtnBackground;

    ImageView mLinkAnchorIcon;

    private ImageView mLinkGuestIcon;

    View mLinkGuestArea;

    View mNameAndViewerNumAreaView;


    private int mOriginFollowBtnWidth;

    private User mLinkUser;

    private PopupWindow mPopupWindow;

    private Date mPopupWindowShowTime;

    private List<LiveCommonProto.NewWidgetItem> currentAttachmentList = new ArrayList<LiveCommonProto.NewWidgetItem>();
    private long currentZuid;

    private String currentRoomid;

    public WatchTopInfoSingleView(Context context) {
        super(context);
        init(context, false);
    }


    public WatchTopInfoSingleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, false);
    }

    public WatchTopInfoSingleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, false);
    }

    @Override
    protected int getLayout(boolean mIsLandScape) {
//        if (mIsLandscape) {
//            return R.layout.watch_top_info_single_view_landscape;
//        } else {
        return R.layout.watchsdk_top_info_single_view;
//        }
    }


    @Override
    protected void initParticular() {
        mAvatarLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        if (!mIsAnchor) {
            RxView.clicks(mFollowBtnTv)
                    .throttleFirst(500, TimeUnit.MILLISECONDS)
                    .subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            tryFollowOwner();
                        }
                    });
        } else {
            hideFollowBtn(false);
        }

        mLinkAnchorIcon.setImageDrawable(GetConfigManager.getInstance().getAnchorBadge());
        mLinkGuestIcon = (ImageView) mLinkGuestArea.findViewById(R.id.link_guest_icon);
        mLinkGuestIcon.setImageDrawable(GetConfigManager.getInstance().getGuestBadge());

        RxView.clicks(mLinkAnchorIcon)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EventBus.getDefault().post(new BaseEvent.UserActionEvent(BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, mMyRoomBaseDataModel.getUid(), null));
                    }
                });

        RxView.clicks(mLinkGuestArea.findViewById(R.id.guest_iv))
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (mLinkUser != null) {
                            EventBus.getDefault().post(new BaseEvent.UserActionEvent(BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, mLinkUser.getUid(), null));
                        } else {
                            MyLog.w(TAG, "mLinkUser == null");
                        }
                    }
                });

    }

    Subscription mFollowSubscription;

    private void tryFollowOwner() {
        if (!AccountAuthManager.triggerActionNeedAccount(getContext())) {
            return;
        }
        if (mFollowSubscription != null && !mFollowSubscription.isUnsubscribed()) {
            return;
        }
        mFollowSubscription = RelationApi.follow(UserAccountManager.getInstance().getUuidAsLong(), mMyRoomBaseDataModel.getUid(), mMyRoomBaseDataModel.getRoomId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RelationProto.FollowResponse>() {
                    @Override
                    public void call(RelationProto.FollowResponse followResponse) {
                        if (followResponse.getCode() == RelationApi.ERROR_CODE_BLACK) {
                            ToastUtils.showToast(getResources().getString(R.string.setting_black_follow_hint));
                        } else if (followResponse.getCode() == 0) {
                            ToastUtils.showToast(getResources().getString(R.string.follow_success));
                        }
                        hideFollowBtn(true);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        ToastUtils.showToast(getResources().getString(R.string.follow_failed));
                    }
                });
    }

    @Override
    protected void initView() {
        mFollowBtnTv = (TextView) findViewById(R.id.follow_btn);
        mFollowBtnBackground = (ImageView) findViewById(R.id.follow_btn_background);
        mLinkAnchorIcon = (ImageView) findViewById(R.id.link_anchor_icon);

        mLinkGuestArea = findViewById(R.id.link_guest_area);
        mNameAndViewerNumAreaView = findViewById(R.id.name_and_viewer_num_area);
    }

    private boolean mFollowBtnAnimeStart = false;
    private ObjectAnimator mFollowAnimator;
    private boolean mNeedShowFollowBtn = true;

    public void showFollowBtn() {
        mNeedShowFollowBtn = true;
//        if (mFollowBtnTv != null) {
//            MyLog.v(TAG + " showFollowBtn");
//            mFollowBtnTv.setVisibility(View.VISIBLE);
//            mFollowBtnTv.setAlpha(1f);
//        }

        if (mFollowBtnTv != null && mLinkUser == null) {
            MyLog.v(TAG + " showFollowBtn");
            mFollowBtnTv.setVisibility(VISIBLE);
            mFollowBtnTv.setAlpha(1f);
        }
    }

    /**
     * 隐藏顶部的关注按钮
     */
    public void hideFollowBtn(boolean needAnimation) {
        mNeedShowFollowBtn = false;
        if (!needAnimation) {
//            mFollowBtnTv.setVisibility(View.GONE);
            mFollowBtnTv.setVisibility(GONE);
            return;
        }
        if (!mFollowBtnAnimeStart && mFollowBtnTv != null && mFollowBtnTv.getVisibility() == VISIBLE) {
            MyLog.v(TAG + " goneFollowBtn");
            startFollowAnimation();
        }

        mFollowBtnTv.animate().alpha(0).setDuration(400).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFollowBtnAnimeStart = true;
                mFollowBtnTv.setLayerType(LAYER_TYPE_HARDWARE, null);
                mOriginFollowBtnWidth = mFollowBtnTv.getLayoutParams().width;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mFollowBtnAnimeStart && null != mFollowAnimator) {
                    mFollowAnimator.start();
                }
            }
        });
    }

    private void startFollowAnimation() {
        if (mFollowAnimator == null) {
            mFollowAnimator = ObjectAnimator.ofInt(mFollowBtnTv, "width", 0);
            mFollowAnimator.setDuration(400);
            mFollowAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    hideFollowBtn(false);
                    mFollowBtnTv.getLayoutParams().width = mOriginFollowBtnWidth;
                    mFollowBtnTv.setLayerType(LAYER_TYPE_NONE, null);
                    mFollowBtnAnimeStart = false;
                }
            });
            mFollowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ViewGroup.LayoutParams params = mFollowBtnTv.getLayoutParams();
                    params.width = (int) animation.getAnimatedValue("width");
                }
            });
        }

        mFollowBtnTv.animate().alpha(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFollowBtnAnimeStart = true;
                mFollowBtnTv.setLayerType(LAYER_TYPE_HARDWARE, null);
                mOriginFollowBtnWidth = mFollowBtnTv.getLayoutParams().width;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mFollowBtnAnimeStart) {
                    mFollowAnimator.start();
                }
            }
        });
    }

    public void clearAnimator() {
        if (mFollowBtnAnimeStart) {
            mFollowBtnTv.clearAnimation();
            if (mFollowAnimator != null) {
                mFollowAnimator.cancel();
            }
            mFollowBtnTv.setLayerType(LAYER_TYPE_NONE, null);
            mFollowBtnAnimeStart = false;
        }
    }

    public void initViewUseData() {
        updateAnchorNickName();
        updateOwnerView();
        updateTicketView();
        updateViewerCountView();
        updateViewers();
        setRoomAttachment(currentAttachmentList, currentZuid, currentRoomid);
        if (mIsAnchor) {
//            updateManagers();
        }
        if (!mMyRoomBaseDataModel.isFocused() && mMyRoomBaseDataModel.getUid() != UserAccountManager.getInstance().getUuidAsLong()) {
            showFollowBtn();
        } else {
            hideFollowBtn(false);
        }
        if (!mMyRoomBaseDataModel.isFocused() && mMyRoomBaseDataModel.getUid() != UserAccountManager.getInstance().getUuidAsLong()) {
            showFollowBtn();
        } else {
            hideFollowBtn(false);
        }
    }


    @Override
    public void onUserInfoComplete() {
        updateTicketView();
        updateOwnerView();
        updateAnchorNickName();
        mLastUpdateTime = 0;
        updateViewers();
        if (!mMyRoomBaseDataModel.isFocused() && mMyRoomBaseDataModel.getUid() != UserAccountManager.getInstance().getUuidAsLong()) {
            showFollowBtn();
        } else {
            hideFollowBtn(false);
        }
    }

    protected void updateManagers() {
//        if (null != mAvatarManagerAdapter) {
//            mAvatarManagerAdapter.setViewerList(mMyRoomBaseDataModel.getManagerList(), true);
//        }
    }

    @Override
    public void updateAnchorNickName() {
        super.updateAnchorNickName();
        CharSequence text = TextUtils.ellipsize(mShowerNameTv.getText(), mShowerNameTv.getPaint(),
                mShowerNameTv.getMaxWidth(), TextUtils.TruncateAt.END);
        mShowerNameTv.setText(text);
    }

    @Override
    public void resetData() {
        MyLog.d(TAG, "resetData");
        mLinkUser = null;
        stopAnimation(mShowAnimation);
        stopAnimation(mHideAnimation);
        adjustOriginalAlpha(1.0f);
        adjustOriginalVisibility(VISIBLE);
        adjustLinkingVisibility(GONE);
        cleanData();
        super.resetData();
    }

    private ValueAnimator mShowAnimation;
    private ValueAnimator mHideAnimation;
    private int mOriginalWidth = 0;
    private int mLinkingWidth = 0;

    private void adjustLinkingAlpha(float alpha) {
        mLinkAnchorIcon.setAlpha(alpha);
        mLinkGuestArea.setAlpha(alpha);
    }

    private void adjustLinkingVisibility(int visibility) {
        mLinkAnchorIcon.setVisibility(visibility);
        mLinkGuestArea.setVisibility(visibility);
    }

    private void adjustOriginalAlpha(float alpha) {
        if (mNeedShowFollowBtn) {
            mFollowBtnTv.setAlpha(alpha);
        }
        mOwnerBadgeIv.setAlpha(alpha);
    }

    private void adjustOriginalVisibility(int visibility) {
        if (mNeedShowFollowBtn) {
            mFollowBtnTv.setVisibility(visibility);
        }
        mOwnerBadgeIv.setVisibility(visibility);
    }

    private boolean stopAnimation(ValueAnimator animation) {
        MyLog.d(TAG, "stopAnimation");
        if (animation != null && animation.isStarted()) {
            animation.cancel();
            return true;
        } else {
            return false;
        }
    }

    private boolean startAnimation(ValueAnimator animation) {
        MyLog.d(TAG, "startAnimation");
        if (animation != null && !animation.isStarted()) {
            animation.start();
            return true;
        } else {
            return false;
        }
    }

    private int getRightBoundary(View view) {
        return view.getLeft() + view.getWidth() + ((RelativeLayout.LayoutParams) view.getLayoutParams()).rightMargin;
    }

    public void onLinkMicStarted(User user) {
        MyLog.d(TAG, "onLinkMicStarted width=" + mOwnerContainer.getWidth());
        if (user == null) {
            return;
        }
        if (mShowAnimation == null) {
            mShowAnimation = ValueAnimator.ofFloat(0f, 1f);
            mShowAnimation.setDuration(1000);
            mShowAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    MyLog.d(TAG, "onLinkMicStarted onAnimationStart");
                    super.onAnimationStart(animation);
                    mOwnerContainer.getLayoutParams().width = mOwnerContainer.getWidth();
                    adjustLinkingVisibility(VISIBLE);
                    adjustLinkingAlpha(0f);
                    mOwnerContainer.requestLayout();
                    mOriginalWidth = mLinkingWidth = 0;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    MyLog.d(TAG, "onLinkMicStarted onAnimationEnd");
                    super.onAnimationEnd(animation);
                    mOwnerContainer.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    adjustOriginalVisibility(GONE);
                    mOwnerContainer.requestLayout();
                }
            });
            mShowAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float ratio = (float) animation.getAnimatedValue();
                    if (mOriginalWidth > 0 && mLinkingWidth > 0) {
                        mOwnerContainer.getLayoutParams().width = (int) (mOriginalWidth + ratio * (mLinkingWidth - mOriginalWidth));
                        mOwnerContainer.requestLayout();
                    } else {
                        mOriginalWidth = mOwnerContainer.getWidth();
                        mLinkingWidth = getRightBoundary(mNameAndViewerNumAreaView);
                        mLinkingWidth += DisplayUtils.dip2px(34.66f + 2f); // 连麦嘉宾头像宽度 + 各1dp的左右边距
                        MyLog.d(TAG, "onLinkMicStarted mOriginalWidth=" + mOriginalWidth + ", mLinkingWidth=" + mLinkingWidth);
                    }
                    if (ratio <= 0.5f) {
                        adjustOriginalAlpha(1 - 2 * ratio);
                    }
                    if (ratio >= 0.5f) {
                        adjustLinkingAlpha(2 * ratio - 1);
                    }
                }
            });
        }
        stopAnimation(mHideAnimation);
        startAnimation(mShowAnimation);
        mLinkUser = user;
        AvatarUtils.loadAvatarByUidTs((BaseImageView) mLinkGuestArea.findViewById(R.id.guest_iv),
                mLinkUser.getUid(), mLinkUser.getAvatar(), true);
    }

    public void onLinkMicStopped() {
        MyLog.d(TAG, "onLinkMicStopped width=" + mOwnerContainer.getWidth());
        if (mLinkUser == null) {
            return;
        }
        mLinkUser = null; // 清空连麦人信息
        if (mHideAnimation == null) {
            mHideAnimation = ValueAnimator.ofFloat(0f, 1f);
            mHideAnimation.setDuration(1000);
            mHideAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    MyLog.d(TAG, "onLinkMicStopped onAnimationStart");
                    super.onAnimationStart(animation);
                    mOwnerContainer.getLayoutParams().width = mOwnerContainer.getWidth();
                    adjustOriginalVisibility(VISIBLE);
                    adjustOriginalAlpha(0.01f);
                    mOwnerContainer.requestLayout();
                    mOriginalWidth = mLinkingWidth = 0;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    MyLog.d(TAG, "onLinkMicStopped onAnimationEnd");
                    super.onAnimationEnd(animation);
                    mOwnerContainer.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    adjustLinkingVisibility(GONE);
                    mOwnerContainer.requestLayout();
                }
            });
            mHideAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float ratio = (float) animation.getAnimatedValue();
                    if (mOriginalWidth > 0 && mLinkingWidth > 0) {
                        mOwnerContainer.getLayoutParams().width = (int) (mLinkingWidth + ratio * (mOriginalWidth - mLinkingWidth));
                        mOwnerContainer.requestLayout();
                    } else {
                        mLinkingWidth = mOwnerContainer.getWidth();
                        mOriginalWidth = getRightBoundary(mNameAndViewerNumAreaView);
                        if (mNeedShowFollowBtn) {
                            mOriginalWidth += DisplayUtils.dip2px(30.33f + 3.33f); // 关注按钮的宽度 + 3.33dp的右边距
                        }
                        MyLog.d(TAG, "onLinkMicStopped mOriginalWidth=" + mOriginalWidth + ", mLinkingWidth=" + mLinkingWidth);
                    }
                    if (ratio <= 0.5f) {
                        adjustLinkingAlpha(1 - 2 * ratio);
                    }
                    if (ratio >= 0.5f) {
                        adjustOriginalAlpha(2 * ratio - 1);
                    }
                }
            });
        }
        stopAnimation(mShowAnimation);
        startAnimation(mHideAnimation);
    }

    /**
     * 关注与取消关注
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FollowOrUnfollowEvent event) {
        if (null != event && mMyRoomBaseDataModel != null && mMyRoomBaseDataModel.getUser() != null && mMyRoomBaseDataModel.getUser().getUid() == event.uuid) {

            User user = mMyRoomBaseDataModel.getUser();
            if (user != null && user.getUid() == event.uuid) {
                if (event.eventType == FollowOrUnfollowEvent.EVENT_TYPE_FOLLOW) {
                    user.setIsFocused(true);
                    hideFollowBtn(true);
                } else if (event.eventType == FollowOrUnfollowEvent.EVENT_TYPE_UNFOLLOW) {
                    user.setIsFocused(false);
                    showFollowBtn();
                } else {
                    MyLog.e(TAG, "type error");
                }
            }
        }
    }


    /**
     * 连麦结束时，显示底部运营位数据
     */
    public void updateAttachment() {
        if (currentAttachmentList.size() > 0) {
            setRoomAttachment(currentAttachmentList, currentZuid, currentRoomid);
        }
    }

    public void cleanData() {
        currentAttachmentList.clear();
        currentZuid = 0;
        currentRoomid = "";
    }

    /**
     * 设置运营位数据
     *
     * @param list
     */
    public void setRoomAttachment(List<LiveCommonProto.NewWidgetItem> list, long zuid, String roomId) {
        List<LiveCommonProto.NewWidgetItem> newWidgetItems = new ArrayList<>();
        newWidgetItems.addAll(list);
        if (currentAttachmentList.size() > 0) {
            currentAttachmentList.clear();
        }
        currentAttachmentList.addAll(newWidgetItems);
        currentZuid = zuid;
        currentRoomid = roomId;
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                LiveCommonProto.NewWidgetItem info = list.get(i);
            }
        } else {
        }
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

    private Map<Integer, Integer> widgetIDs = new HashMap<>();


    private final static char[] HEX = "0123456789abcdef".toCharArray();

    public static String toFullHexString(int num) {
        char[] chs = new char[6];
        for (int i = 0; i < chs.length; i++) {
            chs[chs.length - 1 - i] = HEX[(num >> (i * 4)) & 0xf];
        }
        return new String(chs);
    }

    private void setClick(final LiveCommonProto.NewWidgetItem info, final BaseImageView imgDown, final TextView txtCounter, final LiveCommonProto.ClickItem click) {
        Observable.timer(click.getClickInterval(), TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .compose(((RxActivity) getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        RxView.clicks(imgDown)
                                .throttleFirst(500, TimeUnit.MILLISECONDS)
                                .subscribe(new Action1<Void>() {
                                    @Override
                                    public void call(Void aVoid) {
                                        GiftRepository.clickCounter(info.getWidgetID(), currentZuid, currentRoomid)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Observer<String>() {
                                                    @Override
                                                    public void onCompleted() {

                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                    }

                                                    @Override
                                                    public void onNext(String rsp) {
                                                        if (txtCounter != null) {
                                                            txtCounter.setText(rsp);
                                                        }
                                                    }
                                                });

                                        sendClick(info.getWidgetID(), "buttonClick");

                                        RxView.clicks(imgDown)
                                                .throttleFirst(500, TimeUnit.MILLISECONDS)
                                                .subscribe(new Action1<Void>() {
                                                    @Override
                                                    public void call(Void aVoid) {
                                                        //xx时间后才可以点击
                                                        ToastUtils.showToast(com.base.global.GlobalData.app().getApplicationContext(), "每" + click.getClickInterval() + "秒可以点击一次");
                                                    }
                                                });

                                        setClick(info, imgDown, txtCounter, click);
                                    }
                                });
                    }
                });

        RxView.clicks(imgDown)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        ToastUtils.showToast(com.base.global.GlobalData.app().getApplicationContext(), "每" + click.getClickInterval() + "秒可以点击一次");
                    }
                });

    }


    /**
     * 加载网络图片
     *
     * @param icon
     * @param img
     */
    private void loadImgFromNet(final String icon, final BaseImageView img, final String type) {
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
        }).retryWhen(new RxRetryAssist(10, 2, false)).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Subscriber<String>() {
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
                    img.setVisibility(VISIBLE);
                    String[] params = s.split(",");

                    int screenWidth = DisplayUtils.getScreenHeight() > DisplayUtils.getScreenWidth() ? DisplayUtils.getScreenWidth() : DisplayUtils.getScreenHeight();

                    int screenHeight = DisplayUtils.getScreenHeight() > DisplayUtils.getScreenWidth() ? DisplayUtils.getScreenHeight() : DisplayUtils.getScreenWidth();

                    float widthX = (float) (screenWidth * 1.0 / 1080);
//
                    float heighX = (float) (1.0 * screenHeight / 1920);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (Integer.parseInt(params[0]) * widthX), (int) (Integer.parseInt(params[1]) * heighX));
                    switch (type) {
                        case "right":
                            lp.gravity = Gravity.RIGHT;
                            break;
                    }
                    img.setLayoutParams(lp);
                    AvatarUtils.loadCoverByUrl(img, icon, false, 0, (int) (Integer.parseInt(params[0]) * widthX), (int) (Integer.parseInt(params[1]) * heighX));
                }
            }
        });
    }

    /**
     * 显示关注引导浮层
     * <p>
     * 显示条件：1)热门主播（当前房间观众数>=200）
     * 2)观众未关注主播
     * 3)观众停留在直播间1分钟
     * 消失条件：点击弹窗区之外的屏幕，或者，3秒后消失
     */
    public void showFollowGuidePopupWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing())
            return;
        View contentView = null;
        contentView = LayoutInflater.from(com.base.global.GlobalData.app().getApplicationContext()).inflate(R.layout.popup_window_follow_guid, null);
        RxView.clicks(contentView)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .map(new Func1<Void, Boolean>() {
                    @Override
                    public Boolean call(Void aVoid) {
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((RxActivity) getContext()).<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean result) {
                    }
                });
        mPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(false);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.showAsDropDown(mFollowBtnTv, -8, 0);

        mPopupWindowShowTime = new Date();

        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setDuration(300);

        mFollowBtnBackground.setAnimation(scaleAnimation);
        mFollowBtnBackground.setVisibility(VISIBLE);

        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mFollowBtnBackground.clearAnimation();
                mFollowBtnBackground.setVisibility(GONE);
            }
        });
        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, String.format(StatisticsKey.KEY_FOLLOW_FLOATING_WINDOW, mMyRoomBaseDataModel.getRoomId() == null ? "" : mMyRoomBaseDataModel.getRoomId()), 1);
    }

    /**
     * 销毁关注引导浮层
     */
    public void dismissFollowGuidePopupWindow() {
        if (mPopupWindow == null)
            return;
        if (mPopupWindow.isShowing()) {
            synchronized (this) {
                if (mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                }
            }
        }
    }

    /**
     * 获取关注引导浮层的显示状态
     *
     * @return
     */
    public boolean getFollowGuidePopupWindowStatus() {
        if (mPopupWindow == null)
            return false;
        else
            return mPopupWindow.isShowing();
    }

    /**
     * 重绘关注引导浮层
     */
    private void redrawFollowGuidePopupWindow() {
        if (mPopupWindow == null)
            return;
        if (mPopupWindow.isShowing()) {
            synchronized (this) {
                if (mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                    showFollowGuidePopupWindow();
                }
            }
        }
    }

    /**
     * 运营位打点
     *
     * @param widgetID
     * @param type
     */
    private void sendClick(int widgetID, String type) {
        String key = String.format(StatisticsKey.KEY_WIDGET_CLICK, String.valueOf(widgetID), type, String.valueOf(currentZuid));
        if (TextUtils.isEmpty(key)) {
            return;
        }
        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
    }
}
