package com.wali.live.watchsdk.vip.contact;

import com.wali.live.watchsdk.vip.model.OperationAnimation;

/**
 * Created by zhujianning on 18-6-30.
 */

public class NobleUserEnterBigAnimContact {

    public interface IView{

        void updateVipEnterRoomEffectSwitchEvent(long anchorId, boolean enableEffect);

        void getExistedAnimResSuccess(OperationAnimation operationAnimation);

        void getExistedAnimResFail();
    }

    public interface IPresenter {
        void getExistedAnimRes(int animId);

        void loadAnimRes();
    }
}
