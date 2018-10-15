package com.wali.live.watchsdk.fans.task.model;

import com.wali.live.watchsdk.fans.model.type.BaseTypeModel;

/**
 * Created by lan on 2017/11/13.
 */
public class LimitJobHeaderModel extends BaseTypeModel {
    @Override
    protected int defaultType() {
        return TaskViewType.TYPE_LIMIT_HEADER;
    }
}
