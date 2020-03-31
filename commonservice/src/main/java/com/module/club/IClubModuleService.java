package com.module.club;


import android.content.Context;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.module.common.ICallback;

public interface IClubModuleService extends IProvider {
    void tryGoClubHomePage(int clubID);

    IClubHomeView getClubHomeView(Context context);

    void getClubMembers(int clubID, ICallback callback);

    void getClubApplyCount(int clubID, ICallback callback);

    //发布家族作品完成
    void finishClubWorkUpload();

    //发布家族帖子完成
    void finishClubPostUpload();
}