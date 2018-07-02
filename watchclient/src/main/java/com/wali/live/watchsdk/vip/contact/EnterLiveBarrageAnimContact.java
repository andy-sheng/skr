package com.wali.live.watchsdk.vip.contact;

import android.graphics.drawable.Drawable;

import com.wali.live.watchsdk.vip.model.OperationAnimation;

import java.util.List;

/**
 * Created by zhujianning on 18-6-30.
 */

public class EnterLiveBarrageAnimContact {

    public interface Iview {
        void onNoRes();

        void getExistedAnimResSuccess(OperationAnimation animation);

        void transformFileToDrawableSuccess(List<Drawable> drawables);

        void updateVipEnterRoomEffectSwitchEvent(long anchorId, boolean enableEffect);
    }

    public interface Ipresenter {
        void getExistedAnimRes(int animId);

        void tryLoadAnimRes();

        void transformFileToDrawable(List<String> path);
    }
}
