package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.view.MyInfoIconView;
import com.wali.live.watchsdk.fastsend.view.GiftFastSendView;

/**
 * Created by vera on 2018/8/7.
 * 竖屏时底部的编辑框、送礼按钮等
 */

public class WatchGameBottomEditView extends RelativeLayout implements
        IComponentView<WatchGameBottomEditView.IPresenter, WatchGameBottomEditView.IView>, View.OnClickListener {

    private static final String TAG = "WatchGameBottomEditView";

    @Nullable
    protected IPresenter mPresenter;

    private MyInfoIconView mMyInfoIconView;
    private PortraitGameBarregeBtnView mPortraitGameBarregeBtnView;
    private GiftFastSendView mGiftFastSendView;
    private ImageView mGiftIv;

    public WatchGameBottomEditView(Context context) {
        this(context, null);
    }

    public WatchGameBottomEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WatchGameBottomEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View root = inflate(context, R.layout.watch_game_bottom_view, this);
        mMyInfoIconView = (MyInfoIconView) root.findViewById(R.id.my_info_icon_container);
        mMyInfoIconView.changeBg(GlobalData.app().getResources().getDrawable(R.drawable.game_my_info_btn_bg));
        mPortraitGameBarregeBtnView = (PortraitGameBarregeBtnView) root.findViewById(R.id.barrage_btn_container);
        mGiftFastSendView = (GiftFastSendView) root.findViewById(R.id.gift_fast_sent_container);
        mGiftIv = (ImageView) root.findViewById(R.id.gift_iv);

        mMyInfoIconView.setOnClickListener(this);
        mGiftFastSendView.setOnClickListener(this);
        mGiftIv.setOnClickListener(this);
        mPortraitGameBarregeBtnView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mPresenter == null) {
            MyLog.w(TAG, "ipresenter is null");
            return;
        }

        int id = v.getId();
        if(id == R.id.my_info_icon_container) {
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mPresenter.showMyInfoPannel();
                mMyInfoIconView.setMsgUnreadCnt(0);
            }
        } else if(id == R.id.gift_fast_sent_container) {
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mPresenter.onFastGiftClick();
            }
        } else if(id == R.id.gift_iv) {
            if(AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mPresenter.showGiftView();
            }
        } else if(id == R.id.barrage_btn_container) {
            if(AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mPortraitGameBarregeBtnView.optStatistics();
                mPresenter.showInputView();
            }
        }
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) WatchGameBottomEditView.this;
            }

            @Override
            public void tryBindAvatar() {
                mMyInfoIconView.tryBindAvatar();
            }

            @Override
            public void setFastGift(String widgetIcon, boolean needGiftIcon) {
                mGiftFastSendView.setImgPic(widgetIcon, needGiftIcon);
            }

            @Override
            public void startFastGiftPBarAnim() {
                mGiftFastSendView.start();
            }

            @Override
            public void onUpdateUnreadCount(int unReadCnt) {
                mMyInfoIconView.setMsgUnreadCnt(unReadCnt);
            }

            @Override
            public void hideGiftBtn() {
                mGiftIv.setVisibility(GONE);
            }

            @Override
            public void hideFastGfitBtn() {
                mGiftFastSendView.setVisibility(GONE);
            }
        }
        return new ComponentView();
    }

    @Override
    public void setPresenter(IPresenter iPresenter) {
        this.mPresenter = iPresenter;
    }

    public interface IPresenter {
        void showMyInfoPannel();

        void onFastGiftClick();

        void showGiftView();

        void showInputView();
    }

    public interface IView extends IViewProxy {
        void tryBindAvatar();

        void setFastGift(String widgetIcon, boolean needGiftIcon);

        void startFastGiftPBarAnim();

        void onUpdateUnreadCount(int unReadCnt);

        void hideGiftBtn();

        void hideFastGfitBtn();
    }
}
