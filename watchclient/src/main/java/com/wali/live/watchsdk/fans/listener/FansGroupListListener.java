package com.wali.live.watchsdk.fans.listener;

import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;

/**
 * Created by lan on 2017/11/15.
 */
public interface FansGroupListListener {
    void createGroup(String name);

    void openPrivilege(FansGroupDetailModel detailModel);
}
