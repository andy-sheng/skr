package com.wali.live.watchsdk.fans.task.model;

import android.support.annotation.Nullable;

import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.type.BaseTypeModel;

/**
 * Created by lan on 2017/11/13.
 */
public class GroupJobHeaderModel extends BaseTypeModel {
    private int mVipLevel;

    public GroupJobHeaderModel(@Nullable FansGroupDetailModel model) {
        if (model != null) {
            mVipLevel = model.getVipLevel();
        }
    }

    public int getVipLevel() {
        return mVipLevel;
    }

    @Override
    protected int defaultType() {
        return TaskViewType.TYPE_GROUP_HEADER;
    }
}
