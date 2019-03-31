package com.module.home.persenter;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.scheme.event.BothRelationFromSchemeEvent;
import com.common.core.scheme.event.GrabInviteFromSchemeEvent;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.floatwindow.FloatWindow;
import com.common.floatwindow.MoveType;
import com.common.floatwindow.Screen;
import com.common.floatwindow.ViewStateListener;
import com.common.floatwindow.ViewStateListenerAdapter;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.notification.event.FollowNotifyEvent;
import com.common.notification.event.GrabInviteNotifyEvent;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.component.busilib.manager.WeakRedDotManager;
import com.dialog.view.TipsDialogView;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.rank.IRankingModeService;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.dialog.ConfirmDialog;
import com.common.core.global.event.ShowDialogInHomeEvent;
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

    public NotifyCorePresenter() {
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
        showFollowFloatWindow(event.mUserInfoModel);
        if (event.mUserInfoModel.isFriend()) {
            // 好友
            U.getPreferenceUtils().setSettingInt(WeakRedDotManager.SP_KEY_NEW_FRIEND, 2);
            WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.FRIEND_RED_ROD_TYPE, 2);
        } else {
            // 粉丝
            U.getPreferenceUtils().setSettingInt(WeakRedDotManager.SP_KEY_NEW_FANS, 2);
            WeakRedDotManager.getInstance().updateWeakRedRot(WeakRedDotManager.FANS_RED_ROD_TYPE, 2);
        }
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
                                if (userInfoModel != null) {
                                    if (!userInfoModel.isFriend()) {
                                        MyLog.d(TAG, "同意邀请，强制成为好友" + userInfoModel);
                                        UserInfoManager.getInstance().beFriend(userInfoModel.getUserId());
                                    }
                                }
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

    DialogPlus mBeFriendDialog;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BothRelationFromSchemeEvent event) {
        // TODO: 2019/3/25 成为好友的的口令
        MyLog.d(TAG, "onEvent" + " event=" + event);
        UserInfoManager.getInstance().getUserInfoByUuid(event.useId, new UserInfoManager.ResultCallback<UserInfoModel>() {
            @Override
            public boolean onGetLocalDB(UserInfoModel o) {
                return false;
            }

            @Override
            public boolean onGetServer(UserInfoModel userInfoModel) {
                if (userInfoModel != null) {
                    SpannableStringBuilder stringBuilder = new SpanUtils()
                            .append("是否确定与").setForegroundColor(Color.parseColor("#7F7F7F"))
                            .append("" + userInfoModel.getNickname()).setForegroundColor(Color.parseColor("#F5A623"))
                            .append("成为好友？").setForegroundColor(Color.parseColor("#7F7F7F"))
                            .create();
                    TipsDialogView tipsDialogView = new TipsDialogView.Builder(U.getActivityUtils().getTopActivity())
                            .setMessageTip(stringBuilder)
                            .setConfirmTip("确定")
                            .setCancelTip("取消")
                            .setConfirmBtnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mBeFriendDialog != null) {
                                        mBeFriendDialog.dismiss(false);
                                    }
                                    beFriend(userInfoModel.getUserId());
                                }
                            })
                            .setCancelBtnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mBeFriendDialog != null) {
                                        mBeFriendDialog.dismiss(false);
                                    }
                                }
                            })
                            .build();
                    if (mBeFriendDialog == null) {
                        mBeFriendDialog = DialogPlus.newDialog(U.getActivityUtils().getTopActivity())
                                .setContentHolder(new ViewHolder(tipsDialogView))
                                .setGravity(Gravity.BOTTOM)
                                .setContentBackgroundResource(R.color.transparent)
                                .setOverlayBackgroundResource(R.color.black_trans_80)
                                .setExpanded(false)
                                .create();
                    }

                    EventBus.getDefault().post(new ShowDialogInHomeEvent(mBeFriendDialog, 30));
                }
                return false;
            }
        });

    }

    private void beFriend(int userId) {
        UserInfoManager.getInstance().beFriend(userId);
    }

    void tryGoGrabRoom(int roomID) {
        IRankingModeService iRankingModeService = (IRankingModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
        if (iRankingModeService != null) {
            iRankingModeService.tryGoGrabRoom(roomID);
        }
    }

    void resendGrabInviterFloatWindowDismissMsg() {
        mUiHandler.removeMessages(MSG_DISMISS_INVITE_FLOAT_WINDOW);
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_INVITE_FLOAT_WINDOW, 5000);
    }

    void showGrabInviteFloatWindow(UserInfoModel userInfoModel, int roomID) {
        resendGrabInviterFloatWindowDismissMsg();
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
                .setMoveType(MoveType.canRemove)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
                .setViewStateListener(new ViewStateListenerAdapter() {
                    @Override
                    public void onDismiss() {
                        //mUiHandler.removeMessages(MSG_DISMISS_INVITE_FLOAT_WINDOW);
                    }

                    @Override
                    public void onPositionUpdate(int x, int y) {
                        super.onPositionUpdate(x, y);
                        resendGrabInviterFloatWindowDismissMsg();
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(false)
                .setTag(TAG_INVITE_FOALT_WINDOW)
                .build();
    }

    void resendFollowFloatWindowDismissMsg() {
        mUiHandler.removeMessages(MSG_DISMISS_INVITE_FLOAT_WINDOW);
        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_INVITE_FLOAT_WINDOW, 5000);
    }

    void showFollowFloatWindow(UserInfoModel userInfoModel) {
        resendFollowFloatWindowDismissMsg();
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
                .setMoveType(MoveType.canRemove)
                .setWidth(Screen.width, 1f)                               //设置控件宽高
                .setHeight(Screen.height, 0.2f)
                .setViewStateListener(new ViewStateListenerAdapter() {
                    @Override
                    public void onDismiss() {
                        //mUiHandler.removeMessages(MSG_DISMISS_RELATION_FLOAT_WINDOW);
                    }

                    @Override
                    public void onPositionUpdate(int x, int y) {
                        super.onPositionUpdate(x, y);
                        resendFollowFloatWindowDismissMsg();
                    }
                })
                .setDesktopShow(false)                        //桌面显示
                .setCancelIfExist(false)
                .setReqPermissionIfNeed(true)
                .setTag(TAG_RELATION_FOALT_WINDOW)
                .build();
    }

}
