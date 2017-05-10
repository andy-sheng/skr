package com.wali.live.livesdk.live.presenter.view;

import com.base.mvp.IRxView;
import com.wali.live.proto.LiveCommonProto;

/**
 * Created by lan on 17/4/5.
 */
public interface IRoomPrepareView extends IRxView {
    void setManagerCount(int count);

    void fillTitle(String title);

    void updateControlTitleArea(boolean isShow);

    void setDailyTaskUnit(LiveCommonProto.NewWidgetUnit unit);
}
