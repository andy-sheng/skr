package com.wali.live.watchsdk.fans.model.item.special;

import com.wali.live.watchsdk.fans.model.item.ViewType;
import com.wali.live.watchsdk.fans.model.type.BaseTypeModel;

/**
 * Created by lan on 2017/11/8.
 */
public class HintMemGroupModel extends BaseTypeModel {
    @Override
    protected int defaultType() {
        return ViewType.TYPE_HINT_MEM_GROUP;
    }
}
