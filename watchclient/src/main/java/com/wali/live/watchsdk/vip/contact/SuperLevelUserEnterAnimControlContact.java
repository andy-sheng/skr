package com.wali.live.watchsdk.vip.contact;

import com.mi.live.data.push.model.BarrageMsg;

/**
 * Created by zhujianning on 18-6-29.
 */

public class SuperLevelUserEnterAnimControlContact {

    public interface IView {
        void setAnchorId(long anchorId);

        void destory();

        void reset();

        void putBarrage(BarrageMsg enterLiveBarrage);
    }
}
