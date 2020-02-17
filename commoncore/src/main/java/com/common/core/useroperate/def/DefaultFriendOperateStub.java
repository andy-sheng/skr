package com.common.core.useroperate.def;

import com.common.core.useroperate.inter.AbsRelationOperate;

public class DefaultFriendOperateStub extends AbsRelationOperate {

    @Override
    public String getTitle() {
        return "好友";
    }

    public DefaultFriendOperateStub(String text, int layoutId, AbsRelationOperate.ClickListener onClickListener) {
        super(text, layoutId, onClickListener);
    }

    @Override
    public int getViewLayout() {
        return 0;
    }
}
