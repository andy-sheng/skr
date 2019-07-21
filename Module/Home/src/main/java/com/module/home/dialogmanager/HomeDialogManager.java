package com.module.home.dialogmanager;

import android.support.annotation.NonNull;
import android.view.View;

import com.common.anim.ObjectPlayControlTemplate;
import com.common.log.MyLog;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.common.core.global.event.ShowDialogInHomeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 首页弹窗管理，保证首页有N个弹窗要显示时，能有序显示
 */
public class HomeDialogManager {
    public final String TAG = "HomeDialogManager";

    private boolean mHasShowDialog = false;
    ObjectPlayControlTemplate<ExDialogData, HomeDialogManager> mObjectPlayControlTemplate;

    public HomeDialogManager() {
        mObjectPlayControlTemplate = new ObjectPlayControlTemplate<ExDialogData, HomeDialogManager>() {

            @Override
            protected HomeDialogManager accept(ExDialogData cur) {
                if (mHasShowDialog) {
                    return null;
                } else {
                    DialogPlus dialogPlus = cur.mDialogPlus;
                    if (dialogPlus != null) {
                        if (dialogPlus.isDependActivityExit()) {
                            MyLog.d(TAG, "accept" + "isDependActivityExit cur=" + cur);
//                            OnDismissListener oriListener = dialogPlus.onDismissListener;
//                            dialogPlus.onDismissListener = new OnDismissListener() {
//                                @Override
//                                public void onDismiss(@NonNull DialogPlus dialog) {
//                                    MyLog.d(TAG, "onDismiss" + " dialog=" + dialog);
//                                    if (oriListener != null) {
//                                        oriListener.onDismiss(dialog);
//                                    }
//                                    if (mHasShowDialog) {
//                                        mHasShowDialog = false;
//                                        mObjectPlayControlTemplate.endCurrent(cur);
//                                    }
//                                }
//                            };
                            dialogPlus.mOnAttachStateChangeListener = new View.OnAttachStateChangeListener() {
                                @Override
                                public void onViewAttachedToWindow(View v) {

                                }

                                @Override
                                public void onViewDetachedFromWindow(View v) {
                                    // 被移除了
                                    if (mHasShowDialog) {
                                        mHasShowDialog = false;
                                        mObjectPlayControlTemplate.endCurrent(cur);
                                    }
                                }
                            };
                            mHasShowDialog = true;
                            return HomeDialogManager.this;
                        } else {
                            MyLog.d(TAG, "accept" + "isDependActivityNotExit cur=" + cur);
                            return HomeDialogManager.this;
                        }
                    } else {
                        return null;
                    }
                }
            }

            @Override
            public void onStart(ExDialogData exDialogData, HomeDialogManager manager) {
                if (exDialogData.mDialogPlus != null && exDialogData.mDialogPlus.isDependActivityExit()) {
                    exDialogData.mDialogPlus.show();
                }
            }

            @Override
            protected void onEnd(ExDialogData exDialogData) {

            }
        };
    }

    public void register() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void destroy() {
        EventBus.getDefault().unregister(this);
        mObjectPlayControlTemplate.destroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ShowDialogInHomeEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        mObjectPlayControlTemplate.add(new ExDialogData(event.mDialogPlus), true);
    }
}
