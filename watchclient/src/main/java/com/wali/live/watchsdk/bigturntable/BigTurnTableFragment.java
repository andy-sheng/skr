package com.wali.live.watchsdk.bigturntable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.activity.BaseActivity;
import com.base.event.KeyboardEvent;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.view.BackTitleBar;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.repository.model.turntable.TurnTableConfigModel;
import com.wali.live.proto.BigTurnTableProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.bigturntable.contact.BigTurnTableContact;
import com.wali.live.watchsdk.bigturntable.presenter.BigTurnTablePresenter;
import com.wali.live.watchsdk.bigturntable.view.LiveBigTurnTableContainer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by zhujianning on 18-7-10.
 */

public class BigTurnTableFragment extends RxFragment {
    private static final String TAG = "BigTurnTableFragment";
    private static final String EXTRAL_ROOM_ID = "extral_room_id";
    private static final String EXTRAL_LIVE_USER_ID = "extral_live_user_id";

    //data
    private String mRoomId;
    private long mZUid;

    //presenter
    private BigTurnTablePresenter mPresenter;

    //ui
    private View mTopView;
    private BackTitleBar mTitleBar;
    private LiveBigTurnTableContainer mBigTurnTablePanelView;
//    private BigTurnTableRuleView mBigTurnTableRuleView;

    @Override
    public int getRequestCode() {
        return mRequestCode;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.frag_big_turn_table, container, false);
    }

    @Override
    protected void bindView() {
        EventBus.getDefault().register(this);
        mTitleBar = (BackTitleBar) mRootView.findViewById(R.id.title_bar);
        mTitleBar.getBackBtn().setText(GlobalData.app().getResources().getString(R.string.back));
        mTitleBar.getRightTextBtn().setText(GlobalData.app().getResources().getString(R.string.rule));
        mBigTurnTablePanelView = (LiveBigTurnTableContainer) mRootView.findViewById(R.id.big_turn_table_panel_view);
        mTopView = mRootView.findViewById(R.id.top_iv);
        initParams();
        initListener();
        initPresenter();
        initData();
    }

    private void initParams() {
        Bundle bundle = getArguments();
        if(bundle != null) {
            mRoomId = bundle.getString(EXTRAL_ROOM_ID);
            mZUid = bundle.getLong(EXTRAL_LIVE_USER_ID);
        }

        MyLog.d(TAG, "zuid :" + mZUid + ", roomId:" + mRoomId);
    }

    private void initListener() {
        RxView.clicks(mTitleBar.getBackBtn())
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        FragmentNaviUtils.popFragmentFromStack(getActivity());
                    }
                });

        mBigTurnTablePanelView.setOnLiveTurnTableListener(new LiveBigTurnTableContainer.OnLiveTurnTableListener() {
            @Override
            public void open(BigTurnTableProto.TurntableType mode, String inputTxt, boolean needCloseOtherMode) {
                if(needCloseOtherMode) {
                    if(mode == BigTurnTableProto.TurntableType.TYPE_128) {
                        mPresenter.close(mZUid, mRoomId, BigTurnTableProto.TurntableType.TYPE_500, inputTxt, true);
                    } else {
                        mPresenter.close(mZUid, mRoomId, BigTurnTableProto.TurntableType.TYPE_128, inputTxt,  true);
                    }
                } else {
                    mPresenter.open(mZUid, mRoomId, mode, inputTxt);
                }
            }

            @Override
            public void close(BigTurnTableProto.TurntableType mode) {
                mPresenter.close(mZUid, mRoomId, mode, "",false);
            }

            @Override
            public void switchMode(BigTurnTableProto.TurntableType type) {
                MyLog.d(TAG, "switchMode");
                mPresenter.setType(type);
                mPresenter.switchMode(type);
            }
        });

        RxView.clicks(mTitleBar.getRightTextBtn())
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        showRuleTips();
                    }
                });
    }

    private void initPresenter() {
        mPresenter = new BigTurnTablePresenter(mBigTurnTableContact);
    }

    private void initData() {
        mPresenter.loadTurnTableDataByType(mZUid, mRoomId);
    }

    private void showRuleTips() {
//        if(mBigTurnTableRuleView == null) {
//            mBigTurnTableRuleView = new BigTurnTableRuleView(getActivity());
//        }
//        mBigTurnTableRuleView.show(mRootView);
    }

    private BigTurnTableContact.IView mBigTurnTableContact = new BigTurnTableContact.IView() {

        @Override
        public void loadDataSuccess(TurnTableConfigModel data) {
            MyLog.d(TAG, "loadDataSuccess");
            mBigTurnTablePanelView.setDatas(data);
        }

        @Override
        public void notifyOpenStatus(BigTurnTableProto.TurntableType type) {
            mBigTurnTablePanelView.notifyOpenStatus(type);
        }

        @Override
        public void loadDataFail() {

        }

        @Override
        public void openSuccess(BigTurnTableProto.TurntableType type) {
            mBigTurnTablePanelView.openOptSuccess(type);
            KeyboardUtils.hideKeyboard(getActivity());
            mBigTurnTablePanelView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FragmentNaviUtils.popFragmentFromStack(getActivity());
                }
            }, 1000);
        }

        @Override
        public void openFail() {

        }

        @Override
        public void closeSuccess(BigTurnTableProto.TurntableType type, String input, boolean needOpenOtherMode) {
            if(needOpenOtherMode) {
                if(type == BigTurnTableProto.TurntableType.TYPE_128) {
                    mPresenter.open(mZUid, mRoomId, BigTurnTableProto.TurntableType.TYPE_500, input);
                } else {
                    mPresenter.open(mZUid, mRoomId, BigTurnTableProto.TurntableType.TYPE_128, input);
                }
            } else {
                mBigTurnTablePanelView.closeOptSuccess(type);
            }
        }

        @Override
        public void closeFail() {

        }
    };

    @Override
    public boolean isStatusBarDark() {
        return true;
    }
    @Override
    public boolean isOverrideStatusBar() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false, priority = 1)
    public void onEventMainThread(KeyboardEvent event) {
        MyLog.d(TAG, "KeyboardEvent");
        int start = DisplayUtils.dip2px(66.67f);
        int height = KeyboardUtils.getKeyboardHeight(getActivity());
        int trans = 0;
        int y = KeyboardUtils.getScreenHeight(getActivity()) - mBigTurnTablePanelView.getBottom();
        if (y < height) {
            trans = start - height + y;
        } else {
            trans = start;
        }
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE:
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTopView.getLayoutParams();
                params.topMargin = trans;
                mTopView.setLayoutParams(params);
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                RelativeLayout.LayoutParams paramsl = (RelativeLayout.LayoutParams) mTopView.getLayoutParams();
                paramsl.topMargin = start;
                mTopView.setLayoutParams(paramsl);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mBigTurnTablePanelView != null) {
            mBigTurnTablePanelView.onDestory();
        }

        if (mPresenter != null) {
            mPresenter.destroy();
        }
    }

    public static void openFragment(BaseActivity activity, String roomId, long uid) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRAL_ROOM_ID, roomId);
        bundle.putLong(EXTRAL_LIVE_USER_ID, uid);
        FragmentNaviUtils.addFragment(activity,  R.id.main_act_container, BigTurnTableFragment.class, bundle, true, false, true);
    }
}
