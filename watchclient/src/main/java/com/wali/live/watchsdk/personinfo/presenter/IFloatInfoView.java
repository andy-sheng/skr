package com.wali.live.watchsdk.personinfo.presenter;

import com.base.mvp.IRxView;
import com.mi.live.data.user.User;
import com.wali.live.proto.RankProto;

/**
 * Created by wangmengjie on 17-8-24.
 */

public interface IFloatInfoView extends IRxView {
    void refreshAllViews(User mUser, RankProto.RankUser mTopOneUser, boolean enableFollow);

    void refreshUserInfo();

    void popUnFollowDialog();

    void onOrientation(boolean isLandscape);

}
