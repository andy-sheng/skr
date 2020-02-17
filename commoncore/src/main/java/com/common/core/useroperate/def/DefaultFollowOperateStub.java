package com.common.core.useroperate.def;

import com.common.core.useroperate.inter.AbsRelationOperate;

public class DefaultFollowOperateStub extends AbsRelationOperate {

    @Override
    public String getTitle() {
        return "关注";
    }

    public DefaultFollowOperateStub(String text, int layoutId, AbsRelationOperate.ClickListener onClickListener) {
        super(text, layoutId, onClickListener);
    }

    @Override
    public int getViewLayout() {
        return 0;
    }
}
