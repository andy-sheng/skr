package com.module.club;


import android.content.Context;

import com.alibaba.android.arouter.facade.template.IProvider;

public interface IClubModuleService extends IProvider {
    void tryGoClubHomePage(int clubID);

    IClubListView getPartyRoomView(Context context);
}