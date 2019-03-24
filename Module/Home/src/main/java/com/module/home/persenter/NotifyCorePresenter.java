package com.module.home.persenter;

import android.os.Handler;
import android.os.Message;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.scheme.event.GrabInviteFromSchemeEvent;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.floatwindow.FloatWindow;
import com.common.floatwindow.MoveType;
import com.common.floatwindow.Screen;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.notification.event.FollowNotifyEvent;
import com.common.notification.event.GrabInviteNotifyEvent;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.home.view.INotifyView;
import com.module.rank.IRankingModeService;
import com.zq.dialog.ConfirmDialog;
import com.zq.notification.GrabInviteNotifyView;
import com.zq.notification.FollowNotifyView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class NotifyCorePresenter extends RxLifeCyclePresenter {

    static final String TAG_INVITE_FOALT_WINDOW = "TAG_INVITE_FOALT_WINDOW";
    static final String TAG_RELATION_FOALT_WINDOW = "TAG_RELATION_FOALT_WINDOW";
    static final int MSG_DISMISS_INVITE_FLOAT_WINDOW = 2;
    static final int MSG_DISMISS_RELATION_FLOAT_WINDOW = 3;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_DISMISS_INVITE_FLOAT_WINDOW:
                    FloatWindow.destroy(TAG_INVITE_FOALT_WINDOW);
                    break;
                case MSG_DISMISS_RELATION_FLOAT_WINDOW:
                    FloatWindow.destroy(TAG_RELATION_FOALT_WINDOW);
                    break;
            }
        }
    };

    INotifyView iNotifyView;

    public NotifyCorePresenter(INotifyView iNotifyView) {
        this.iNotifyView = iNotifyView;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FollowNotifyEvent event) {
        iNotifyView.showMessageRedDot();
        showFollowFloatWindow(event.mUserInfoModel);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabInviteNotifyEvent event) {
        showGrabInviteFloatWindow(event.mUserInfoModel, event.roomID);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabInviteFromSchemeEvent event) {
        // TODO: 2019/3/20   一场到底邀请 口令
        if (event.ask == 1) {
            // 需要再次确认弹窗
            UserInfoManager.getInstance().getUserInfoByUuid(event.ownerId, new UserInfoManager.ResultCallback<UserInfoModel>() {
                @Override
                public boolean onGetLocalDB(UserInfoModel o) {
                    return false;
                }

                @Override
                public boolean onGetServer(UserInfoModel userInfoModel) {
                    if (userInfoModel != null) {
                        ConfirmDialog confirmDialog = new ConfirmDialog(U.getActivityUtils().getTopActivity()
                                , userInfoModel, ConfirmDialog.TYPE_INVITE_CONFIRM);
                        confirmDialog.setListener(new ConfirmDialog.Listener() {
                            @Override
                            public void onClickConfirm(UserInfoModel userInfoModel) {
                                tryGoGrabRoom(event.roomId);
                            }
                        });
                        confirmDialog.show();
                    }
                    return false;
                }
            });
        } else {
            // 不需要直接进
            tryGoGrabRoom(event.roomId);
        }
    }

    void tryGoGrabRoom(int roomID) {
        IRankingModeService iRankingModeService = (IRankingModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
        if (iRankingModeService != null) {
            iRankingModeService.tryGoGrabRoom(roomID);
        }
    }

    void showGrabInviteFloatWindow(UserInfoModel userInfoModel, int roomID) {
        mUiHandler.removeMessages(MSG_DISMISS_INVITE_FLOAT_WINDOW);
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_INVITE_FLOAT_WINDOW, 5000);
        GrabInviteNotifyView grabInviteNotifyView = new GrabInviteNotifyView(U.app());
        grabInviteNotifyView.bindData(userInfoModel);
        grabInviteNotifyView.setListener(new GrabInviteNotifyView.Listener() {
            @Override
            public void onIgnore() {
                mUiHandler.removeMessages(MSG_DISMISS_INVITE_FLOAT_WINDOW);
                FloatWindow.destroy(TAG_INVITE_FOALT_WINDOW);
            }

            @Override
            public void onAgree() {
                tryGoGrabRoom(roomID);
                mUiHandler.removeMessages(MSG_DISMISS_INVITE_FLOAT_WINDOW);
                FloatWindow.destroy(TAG_INVITE_FOALT_WINDOW);
            }
        });
        FloatWindow.with(U.app())
                .setView(grabInviteNotifyView)
                .setMoveType(MoveType.inactive)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
//                                .setX(100)                                   //设置控件初始位置
//                                .setY(Screen.height,0.3f)
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setViewStateListener(null)    //监听悬浮控件状态改变
                .setTag(TAG_INVITE_FOALT_WINDOW)
                .build();
    }


    void showFollowFloatWindow(UserInfoModel userInfoModel) {
        mUiHandler.removeMessages(MSG_DISMISS_RELATION_FLOAT_WINDOW);
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_RELATION_FLOAT_WINDOW, 5000);
        FollowNotifyView relationNotifyView = new FollowNotifyView(U.app());
        relationNotifyView.bindData(userInfoModel);
        relationNotifyView.setListener(new FollowNotifyView.Listener() {
            @Override
            public void onFollowBtnClick() {
                mUiHandler.removeMessages(MSG_DISMISS_RELATION_FLOAT_WINDOW);
                FloatWindow.destroy(TAG_RELATION_FOALT_WINDOW);
            }
        });
        FloatWindow.with(U.app())
                .setView(relationNotifyView)
                .setMoveType(MoveType.inactive)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
//                                .setX(100)                                   //设置控件初始位置
//                                .setY(Screen.height,0.3f)
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(true)
                .setViewStateListener(null)    //监听悬浮控件状态改变
                .setTag(TAG_RELATION_FOALT_WINDOW)
                .build();
    }

}
