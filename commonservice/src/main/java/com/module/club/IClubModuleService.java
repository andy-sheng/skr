package com.module.club;


import android.content.Context;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.module.common.ICallback;

public interface IClubModuleService extends IProvider {
    void tryGoClubHomePage(int clubID);

    IClubHomeView getClubHomeView(Context context);

    void getClubMembers(int clubID, ICallback callback);
}