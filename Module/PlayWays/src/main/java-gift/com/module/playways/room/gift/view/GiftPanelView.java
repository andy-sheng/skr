package com.module.playways.room.gift.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.ToastUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.module.playways.R;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabPlaySeatUpdateEvent;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.room.gift.GiftServerApi;
import com.module.playways.room.gift.adapter.GiftAllManAdapter;
import com.module.playways.room.gift.event.BuyGiftEvent;
import com.module.playways.room.gift.event.ShowHalfRechargeFragmentEvent;
import com.module.playways.room.gift.event.UpdateCoinAndDiamondEvent;
import com.orhanobut.dialogplus.DialogPlus;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GiftPanelView extends FrameLayout {
    public final static String TAG = "GiftPanelView";
    public static final int SHOW_PANEL = 0;
    public static final int HIDE_PANEL = 1;
    public static final int ANIMATION_DURATION = 300;
    BaseImageView mIvSelectedIcon;
    ExTextView mTvSelectedName;
    ExTextView mTvAllMan;
    ExImageView mIvRecharge;
    ImageView mIvDiamondIcon;
    ExImageView mIvSend;
    ExRelativeLayout mGiftPanelArea;
    RecyclerView mRecyclerView;
    LinearLayout mLlSelectedMan;
    ExTextView mTvDiamond;

    GiftView mGiftView;

    GiftAllManAdapter mGiftAllManAdapter;

    //当前迈上的人
    GrabPlayerInfoModel mCurMicroMan;

    GrabRoomData mGrabRoomData;

    private boolean mHasInit = false;

    GiftServerApi mGiftServerApi;

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
    }

    private void inflate() {
        inflate(getContext(), R.layout.gift_panel_view_layout, this);
        mHasInit = true;
        mGiftPanelArea = (ExRelativeLayout) findViewById(R.id.gift_panel_area);
        mIvSelectedIcon = (BaseImageView) findViewById(R.id.iv_selected_icon);
        mTvSelectedName = (ExTextView) findViewById(R.id.tv_selected_name);
        mTvAllMan = (ExTextView) findViewById(R.id.tv_all_man);
        mIvRecharge = (ExImageView) findViewById(R.id.iv_recharge);
        mIvDiamondIcon = (ImageView) findViewById(R.id.iv_diamond_icon);
        mIvSend = (ExImageView) findViewById(R.id.iv_send);
        mGiftView = (GiftView) findViewById(R.id.gift_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLlSelectedMan = (LinearLayout) findViewById(R.id.ll_selected_man);
        mTvDiamond = (ExTextView) findViewById(R.id.tv_diamond);

        mGiftAllManAdapter = new GiftAllManAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mGiftAllManAdapter);
        EventBus.getDefault().register(this);

        mGiftPanelArea.setOnClickListener(v -> {
        });

        mGiftAllManAdapter.setOnClickPlayerListener(new GiftAllManAdapter.OnClickPlayerListener() {
            @Override
            public void onClick(GrabPlayerInfoModel grabPlayerInfoModel) {
                mRecyclerView.setVisibility(GONE);
                mLlSelectedMan.setVisibility(VISIBLE);
                AvatarUtils.loadAvatarByUrl(mIvSelectedIcon,
                        AvatarUtils.newParamsBuilder(grabPlayerInfoModel.getUserInfo().getAvatar())
                                .setBorderColor(U.getColor(R.color.white))
                                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                                .setCircle(true)
                                .build());
                mTvSelectedName.setText(grabPlayerInfoModel.getUserInfo().getNickname());
                mCurMicroMan = grabPlayerInfoModel;
            }
        });

        setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getVisibility() == VISIBLE) {
                    hide();
                } else {
                    show(null);
                }
            }
        });

        mIvSend.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mGiftView.getSelectedGift() == null) {
                    ToastUtils.showShort("请选择礼物");
                    return;
                }

                hide();
                EventBus.getDefault().post(new BuyGiftEvent(mGiftView.getSelectedGift(), mCurMicroMan.getUserID()));
            }
        });

        mTvAllMan.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                int visibleState = mRecyclerView.getVisibility();
                mRecyclerView.setVisibility(visibleState == VISIBLE ? GONE : VISIBLE);
                mLlSelectedMan.setVisibility(visibleState == VISIBLE ? VISIBLE : GONE);
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

    public void getZSBalance() {
        ApiMethods.subscribe(mGiftServerApi.getZSBalance(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                MyLog.w(TAG, "getZSBalance process" + " obj=" + obj);
                if (obj.getErrno() == 0) {
                    String amount = JSON.parseObject(obj.getData().getString("totalAmountStr"), String.class);
                    mTvDiamond.setText(amount);
                }
            }
        });
    }

    public void setGrabRoomData(GrabRoomData grabRoomData) {
        mGrabRoomData = grabRoomData;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabPlaySeatUpdateEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        mGiftAllManAdapter.setDataList(event.list);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateCoinAndDiamondEvent event) {
        mTvDiamond.setText(String.valueOf(event.getZuanBalance()));
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
    }

    public void show(GrabPlayerInfoModel grabPlayerInfoModel) {
        if (!mHasInit) {
            inflate();
        }
        selectSendGiftMan(grabPlayerInfoModel);
        mGiftAllManAdapter.setDataList(mGrabRoomData.getPlayerInfoList());
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

    private void selectSendGiftMan(GrabPlayerInfoModel grabPlayerInfoModel) {
        if (grabPlayerInfoModel == null) {
            grabPlayerInfoModel = RoomDataUtils.getPlayerInfoById(mGrabRoomData, mGrabRoomData.getOwnerId());
        }

        if (grabPlayerInfoModel != null) {
            mGiftAllManAdapter.setSelectedGrabPlayerInfoModel(grabPlayerInfoModel);
            mGiftAllManAdapter.update(grabPlayerInfoModel);
            mCurMicroMan = grabPlayerInfoModel;

            AvatarUtils.loadAvatarByUrl(mIvSelectedIcon,
                    AvatarUtils.newParamsBuilder(grabPlayerInfoModel.getUserInfo().getAvatar())
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setCircle(true)
                            .build());
            mTvSelectedName.setText(grabPlayerInfoModel.getUserInfo().getNickname());
        }
    }

    public boolean onBackPressed() {
        if (getVisibility() == VISIBLE) {
            hide();
            return true;
        }

        return false;
    }

    public void destroy() {
        if (mGiftView != null) {
            mGiftView.destroy();
        }
        EventBus.getDefault().unregister(this);
    }
}
