package com.module.home.dialogmanager;

import android.support.annotation.NonNull;

import com.common.anim.ObjectPlayControlTemplate;
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
                        OnDismissListener oriListener = dialogPlus.onDismissListener;
                        dialogPlus.onDismissListener = new OnDismissListener() {
                            @Override
                            public void onDismiss(@NonNull DialogPlus dialog) {
                                if (oriListener != null) {
                                    oriListener.onDismiss(dialog);
                                }
                                mHasShowDialog = false;
                                mObjectPlayControlTemplate.endCurrent(cur);
                            }
                        };
                        return HomeDialogManager.this;
                    } else {
                        return null;
                    }
                }
            }

            @Override
            public void onStart(ExDialogData exDialogData, HomeDialogManager manager) {
                if (exDialogData.mDialogPlus != null) {
                    exDialogData.mDialogPlus.show();
                    mHasShowDialog = true;
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
        mObjectPlayControlTemplate.add(new ExDialogData(event.mDialogPlus), true);
    }
}
