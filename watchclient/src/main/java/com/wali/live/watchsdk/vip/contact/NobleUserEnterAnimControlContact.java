package com.wali.live.watchsdk.vip.contact;

import com.mi.live.data.push.model.BarrageMsg;

/**
 * Created by zhujianning on 18-6-30.
 */

public class NobleUserEnterAnimControlContact {
    public interface IView {
        void putBarrage(BarrageMsg enterLiveBarrage);

        void setAnchorId(long anchorId);

        void destory();
    }

    public interface IPresenter {

    }
}
