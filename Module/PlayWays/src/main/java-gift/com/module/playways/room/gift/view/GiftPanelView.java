package com.module.playways.room.gift.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.ToastUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabPlaySeatUpdateEvent;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.room.gift.GiftServerApi;
import com.module.playways.room.gift.adapter.GiftAllPlayersAdapter;
import com.module.playways.room.gift.event.BuyGiftEvent;
import com.module.playways.room.gift.event.CancelGiftCountDownEvent;
import com.module.playways.room.gift.event.ShowHalfRechargeFragmentEvent;
import com.module.playways.room.gift.event.StartGiftCountDownEvent;
import com.module.playways.room.gift.event.UpdateDiamondEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class GiftPanelView extends FrameLayout {
    public final static String TAG = "GiftPanelView";
    public static final int SHOW_PANEL = 0;
    public static final int HIDE_PANEL = 1;
    public static final int ANIMATION_DURATION = 300;
    BaseImageView mIvSelectedIcon;
    ExTextView mTvSelectedName;
    ExTextView mAllPlayersTv;
    ExImageView mIvRecharge;
    ImageView mIvDiamondIcon;
    ExImageView mIvSend;
    ExRelativeLayout mGiftPanelArea;
    RecyclerView mAllPlayersRV;
    RelativeLayout mLlSelectedMan;
    ExTextView mTvDiamond;
    RelativeLayout mRlPlayerSelectArea;
    ExTextView mFollowTv;

    GiftDisplayView mGiftDisplayView;
    Disposable mRelationTask;

    GiftAllPlayersAdapter mGiftAllPlayersAdapter;
    UserInfoServerApi mUserInfoServerApi;
    GiftDisplayView.IGetGiftCountDownListener mIGetGiftCountDownListener;

    //当前迈上的人
    GrabPlayerInfoModel mCurMicroMan;

    GrabRoomData mGrabRoomData;

    private boolean mHasInit = false;

    GiftServerApi mGiftServerApi;

    Drawable mNeedFollowDrawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
            .setStrokeWidth(U.getDisplayUtils().dip2px(2))
            .setStrokeColor(Color.parseColor("#3B4E79"))
            .setCornersRadius(U.getDisplayUtils().dip2px(16))
            .setSolidColor(Color.parseColor("#FFC15B"))
            .build();

    Drawable mHasFollowDrawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
            .setStrokeWidth(U.getDisplayUtils().dip2px(2))
            .setStrokeColor(U.getColor(R.color.white_trans_50))
            .setCornersRadius(U.getDisplayUtils().dip2px(16))
            .setSolidColor(U.getColor(R.color.transparent))
            .build();

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HIDE_PANEL) {
                clearAnimation();
                setVisibility(GONE);
            }
        }
    };

    public GiftPanelView(Context context) {
        super(context);
        init();
    }

    public GiftPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GiftPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mGiftServerApi = ApiManager.getInstance().createService(GiftServerApi.class);
        mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
    }

    private void inflate() {
        inflate(getContext(), R.layout.gift_panel_view_layout, this);
        mHasInit = true;
        mRlPlayerSelectArea = (RelativeLayout) findViewById(R.id.rl_player_select_area);
        mGiftPanelArea = (ExRelativeLayout) findViewById(R.id.gift_panel_area);
        mIvSelectedIcon = (BaseImageView) findViewById(R.id.iv_selected_icon);
        mTvSelectedName = (ExTextView) findViewById(R.id.tv_selected_name);
        mAllPlayersTv = (ExTextView) findViewById(R.id.all_players_tv);
        mIvRecharge = (ExImageView) findViewById(R.id.iv_recharge);
        mIvDiamondIcon = (ImageView) findViewById(R.id.iv_diamond_icon);
        mIvSend = (ExImageView) findViewById(R.id.iv_send);
        mGiftDisplayView = (GiftDisplayView) findViewById(R.id.gift_view);
        mAllPlayersRV = (RecyclerView) findViewById(R.id.all_players_rv);
        mLlSelectedMan = (RelativeLayout) findViewById(R.id.ll_selected_man);
        mTvDiamond = (ExTextView) findViewById(R.id.tv_diamond);
        mFollowTv = (ExTextView) findViewById(R.id.follow_tv);
        mGiftDisplayView.setIGetGiftCountDownListener(mIGetGiftCountDownListener);

        mGiftAllPlayersAdapter = new GiftAllPlayersAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mAllPlayersRV.setLayoutManager(linearLayoutManager);
        mAllPlayersRV.setAdapter(mGiftAllPlayersAdapter);
        EventBus.getDefault().register(this);

        mGiftPanelArea.setOnClickListener(v -> {
        });

        mGiftAllPlayersAdapter.setOnClickPlayerListener(new GiftAllPlayersAdapter.OnClickPlayerListener() {
            @Override
            public void onClick(GrabPlayerInfoModel grabPlayerInfoModel) {
                if (mAllPlayersRV.getVisibility() != VISIBLE) {
                    return;
                }

                mGiftAllPlayersAdapter.setSelectedGrabPlayerInfoModel(grabPlayerInfoModel);
                mGiftAllPlayersAdapter.update(grabPlayerInfoModel);
                mCurMicroMan = grabPlayerInfoModel;

                bindSelectedPlayerData();

                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        collapsePlayerList();
                    }
                }, 100);
            }
        });

        setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getVisibility() == VISIBLE) {
                    hide();
                }
            }
        });

        mFollowTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                UserInfoManager.getInstance().mateRelation(mCurMicroMan.getUserID(), UserInfoManager.RA_BUILD, mCurMicroMan.getUserInfo().isFriend());
            }
        });

        mIvSend.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mGiftDisplayView.getSelectedGift() == null) {
                    ToastUtils.showShort("请选择礼物");
                    return;
                }

                if (mCurMicroMan == null) {
                    ToastUtils.showShort("请选择送礼对象");
                    return;
                }

                hide();
                EventBus.getDefault().post(new BuyGiftEvent(mGiftDisplayView.getSelectedGift(), mCurMicroMan.getUserInfo()));
            }
        });

        mAllPlayersTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mCurMicroMan == null) {
                    ToastUtils.showShort("请选择送礼用户");
                    return;
                }


                int visibleState = mAllPlayersRV.getVisibility();

                if (visibleState == VISIBLE) {
                    collapsePlayerList();
                } else {
                    RecyclerView.LayoutManager layoutManager = mAllPlayersRV.getLayoutManager();
                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                    int lastItemPosition = linearManager.findLastVisibleItemPosition();
                    int firstItemPosition = linearManager.findFirstVisibleItemPosition();

                    mAllPlayersTv.setEnabled(false);
                    mAllPlayersRV.setVisibility(VISIBLE);
                    mLlSelectedMan.setVisibility(GONE);
                    for (int i = firstItemPosition; i <= lastItemPosition; i++) {
                        View view = linearLayoutManager.getChildAt(i);
                        TranslateAnimation translateAnimation = new TranslateAnimation(-i * U.getDisplayUtils().dip2px(46), 0, 0, 0);
                        translateAnimation.setDuration(300);
                        translateAnimation.setInterpolator(new DecelerateInterpolator());
                        view.setAnimation(translateAnimation);
                        view.startAnimation(translateAnimation);
                    }

                    Drawable drawable = U.getDrawable(R.drawable.suoyouren_left);
                    drawable.setBounds(new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
                    mAllPlayersTv.setCompoundDrawables(drawable, null, null, null);
                    mAllPlayersTv.setText("收起");

                    mUiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAllPlayersTv.setEnabled(true);
                        }
                    }, 300);
                }
            }
        });

        mIvRecharge.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                EventBus.getDefault().post(new ShowHalfRechargeFragmentEvent());
            }
        });

        getZSBalance();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        for (GrabPlayerInfoModel grabPlayerInfoModel : getPlayerInfoListExpectSelf()) {
            if (grabPlayerInfoModel.getUserID() == event.useId) {
                grabPlayerInfoModel.getUserInfo().setFollow(event.isFollow);
                grabPlayerInfoModel.getUserInfo().setFriend(event.isFriend);
            }
        }

        bindSelectedPlayerData();
    }

    public void setIGetGiftCountDownListener(GiftDisplayView.IGetGiftCountDownListener IGetGiftCountDownListener) {
        mIGetGiftCountDownListener = IGetGiftCountDownListener;
    }

    public void collapsePlayerList() {
        RecyclerView.LayoutManager layoutManager = mAllPlayersRV.getLayoutManager();
        LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
        int lastItemPosition = linearManager.findLastVisibleItemPosition();
        int firstItemPosition = linearManager.findFirstVisibleItemPosition();
        mAllPlayersTv.setEnabled(false);
        for (int i = firstItemPosition; i <= lastItemPosition; i++) {
            View view = linearManager.getChildAt(i);
            if (view != null) {
                TranslateAnimation translateAnimation = new TranslateAnimation(0, -i * U.getDisplayUtils().dip2px(46), 0, 0);
                translateAnimation.setDuration(300);
                translateAnimation.setInterpolator(new DecelerateInterpolator());
                view.setAnimation(translateAnimation);
                view.startAnimation(translateAnimation);
            } else {
                MyLog.w(TAG, "collapsePlayerList view = null" );
            }
        }

        Drawable drawable = U.getDrawable(R.drawable.suoyouren_right);
        drawable.setBounds(new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
        mAllPlayersTv.setCompoundDrawables(drawable, null, null, null);
        mAllPlayersTv.setText("重选");

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAllPlayersTv.setEnabled(true);
                mAllPlayersRV.setVisibility(INVISIBLE);
                mLlSelectedMan.setVisibility(VISIBLE);
            }
        }, 300);
    }

    public void updateZS() {
        MyLog.d(TAG, "updateZS");
        getZSBalance();
    }

    private void getZSBalance() {
        ApiMethods.subscribe(mGiftServerApi.getZSBalance(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                MyLog.w(TAG, "getZSBalance process" + " obj=" + obj);
                if (obj.getErrno() == 0) {
                    String amount = obj.getData().getString("totalAmountStr");
                    mTvDiamond.setText(amount);
                }
            }
        }, new ApiMethods.RequestControl("getZSBalance", ApiMethods.ControlType.CancelThis));
    }

    public void setGrabRoomData(GrabRoomData grabRoomData) {
        mGrabRoomData = grabRoomData;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabPlaySeatUpdateEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        mGiftAllPlayersAdapter.setDataList(getPlayerInfoListExpectSelf());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateDiamondEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        mTvDiamond.setText(String.format("%.1f", event.getZuanBalance()));
    }

    //外面不希望用这个函数
    @Deprecated
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    public void hide() {
        mUiHandler.removeMessages(HIDE_PANEL);
        clearAnimation();
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1.0f);
        animation.setDuration(ANIMATION_DURATION);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setFillAfter(true);
        startAnimation(animation);

        mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(HIDE_PANEL), ANIMATION_DURATION);
        EventBus.getDefault().post(new CancelGiftCountDownEvent());
    }

    /**
     * @param grabPlayerInfoModel 麦上的人
     */
    public void show(GrabPlayerInfoModel grabPlayerInfoModel) {
        MyLog.d(TAG, "show" + " grabPlayerInfoModel=" + grabPlayerInfoModel);
        if (!mHasInit) {
            inflate();
        }

        if (getVisibility() == VISIBLE) {
            MyLog.d(TAG, "show" + " getVisibility() == VISIBLE");
            return;
        }

        EventBus.getDefault().post(new StartGiftCountDownEvent());

        setSelectArea(grabPlayerInfoModel);
        mUiHandler.removeMessages(HIDE_PANEL);
        clearAnimation();
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0);
        animation.setDuration(ANIMATION_DURATION);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setFillAfter(true);
        startAnimation(animation);
        setVisibility(VISIBLE);
    }

    private void setSelectArea(GrabPlayerInfoModel grabPlayerInfoModel) {
        if (mGrabRoomData.getInSeatPlayerInfoList().size() == 0
                || (mGrabRoomData.getInSeatPlayerInfoList().size() == 1
                && mGrabRoomData.getInSeatPlayerInfoList().get(0).getUserID() == MyUserInfoManager.getInstance().getUid())) {
            //只有自己
            mRlPlayerSelectArea.setVisibility(GONE);
        } else {
            mRlPlayerSelectArea.setVisibility(VISIBLE);

            if (grabPlayerInfoModel != null) {
                if (grabPlayerInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                    //自己在麦上
                    selectSendGiftPlayer(null);
                } else {
                    //别人在麦上
                    selectSendGiftPlayer(grabPlayerInfoModel);
                }
            } else {
                //没人在麦上
                selectSendGiftPlayer(null);
            }

            mGiftAllPlayersAdapter.setDataList(getPlayerInfoListExpectSelf());
        }
    }

    /**
     * 选择送礼的人
     *
     * @param grabPlayerInfoModel
     */
    private void selectSendGiftPlayer(GrabPlayerInfoModel grabPlayerInfoModel) {
        //麦上没有人
        boolean isPlayerInMic = true;
        if (grabPlayerInfoModel == null) {
            isPlayerInMic = false;
            grabPlayerInfoModel = getFirstPlayerInfo();
        }

        if (grabPlayerInfoModel != null) {
            mGiftAllPlayersAdapter.setSelectedGrabPlayerInfoModel(grabPlayerInfoModel);
            mGiftAllPlayersAdapter.update(grabPlayerInfoModel);
            mCurMicroMan = grabPlayerInfoModel;

            bindSelectedPlayerData();
        } else {
            mGiftAllPlayersAdapter.setSelectedGrabPlayerInfoModel(null);
            mGiftAllPlayersAdapter.update(grabPlayerInfoModel);
        }

        if (isPlayerInMic) {
            mAllPlayersRV.setVisibility(INVISIBLE);
            mLlSelectedMan.setVisibility(VISIBLE);
        } else {
            mAllPlayersRV.setVisibility(VISIBLE);
            mLlSelectedMan.setVisibility(GONE);
        }

        if (mAllPlayersRV.getVisibility() == VISIBLE) {
            Drawable drawable = U.getDrawable(R.drawable.suoyouren_left);
            drawable.setBounds(new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
            mAllPlayersTv.setCompoundDrawables(drawable, null, null, null);
            mAllPlayersTv.setText("收起");
        } else {
            Drawable drawable = U.getDrawable(R.drawable.suoyouren_right);
            drawable.setBounds(new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
            mAllPlayersTv.setCompoundDrawables(drawable, null, null, null);
            mAllPlayersTv.setText("重选");
        }
    }

    private void bindSelectedPlayerData() {
        if (mCurMicroMan == null) {
            return;
        }

        if (mRelationTask != null && !mRelationTask.isDisposed()) {
            mRelationTask.dispose();
        }

        mFollowTv.setVisibility(GONE);
        mFollowTv.setEnabled(false);
        if (mCurMicroMan.getUserInfo().isFriend() || mCurMicroMan.getUserInfo().isFollow()) {
            mFollowTv.setVisibility(VISIBLE);
            mFollowTv.setBackground(mHasFollowDrawable);
            mFollowTv.setText("已关注");
            mFollowTv.setTextColor(U.getColor(R.color.white_trans_50));
        } else {
            mRelationTask = ApiMethods.subscribe(mUserInfoServerApi.getRelation(mCurMicroMan.getUserID()), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult obj) {
                    if (obj.getErrno() == 0) {
                        mFollowTv.setVisibility(VISIBLE);

                        boolean isFriend = obj.getData().getBooleanValue("isFriend");
                        boolean isFollow = obj.getData().getBooleanValue("isFollow");
                        mCurMicroMan.getUserInfo().setFriend(isFriend);
                        mCurMicroMan.getUserInfo().setFollow(isFollow);

                        if (!mCurMicroMan.getUserInfo().isFollow() && !mCurMicroMan.getUserInfo().isFriend()) {
                            mFollowTv.setEnabled(true);
                            mFollowTv.setBackground(mNeedFollowDrawable);
                            mFollowTv.setText("+关注");
                            mFollowTv.setTextColor(Color.parseColor("#ff3b4e79"));
                        } else {
                            mFollowTv.setBackground(mHasFollowDrawable);
                            mFollowTv.setText("已关注");
                            mFollowTv.setTextColor(U.getColor(R.color.white_trans_50));
                        }
                    }
                }
            });
        }

        AvatarUtils.loadAvatarByUrl(mIvSelectedIcon,
                AvatarUtils.newParamsBuilder(mCurMicroMan.getUserInfo().getAvatar())
                        .setBorderColor(U.getColor(R.color.white))
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setCircle(true)
                        .build());

        mTvSelectedName.setText(mCurMicroMan.getUserInfo().getNicknameRemark());
    }

    private GrabPlayerInfoModel getFirstPlayerInfo() {
        List<GrabPlayerInfoModel> grabPlayerInfoModelList = mGrabRoomData.getInSeatPlayerInfoList();

        for (GrabPlayerInfoModel grabPlayerInfoModel : grabPlayerInfoModelList) {
            if (grabPlayerInfoModel.getUserID() != MyUserInfoManager.getInstance().getUid()) {
                return grabPlayerInfoModel;
            }
        }

        return null;
    }

    private List<GrabPlayerInfoModel> getPlayerInfoListExpectSelf() {
        List<GrabPlayerInfoModel> grabPlayerInfoModelList = new ArrayList<>(mGrabRoomData.getInSeatPlayerInfoList());

        Iterator<GrabPlayerInfoModel> it = grabPlayerInfoModelList.iterator();
        while (it.hasNext()) {
            GrabPlayerInfoModel grabPlayerInfoModel = it.next();
            if (grabPlayerInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                it.remove();
            }
        }

        return grabPlayerInfoModelList;
    }

    public boolean onBackPressed() {
        if (getVisibility() == VISIBLE) {
            hide();
            return true;
        }

        return false;
    }

    public void destroy() {
        if (mGiftDisplayView != null) {
            mGiftDisplayView.destroy();
        }
        EventBus.getDefault().unregister(this);
        if (mRelationTask != null && !mRelationTask.isDisposed()) {
            mRelationTask.dispose();
        }
    }
}
