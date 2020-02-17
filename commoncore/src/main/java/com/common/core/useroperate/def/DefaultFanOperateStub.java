package com.common.core.useroperate.def;

import com.common.core.useroperate.inter.AbsRelationOperate;

//只是layout和title不一样
public class DefaultFanOperateStub extends AbsRelationOperate {

    @Override
    public String getTitle() {
        return "粉丝";
    }

    public DefaultFanOperateStub(String text, int layoutId, AbsRelationOperate.ClickListener onClickListener) {
        super(text, layoutId, onClickListener);
    }

    @Override
    public int getViewLayout() {
        return 0;
    }
}
