package useroperate.def;

import com.component.busilib.R;

import useroperate.inter.AbsRelationOperate;

public class DefaultFriendOperateStub extends AbsRelationOperate {
    @Override
    public int getFriendType() {
        return 0;
    }

    @Override
    public String getTitle() {
        return "好友";
    }

    public DefaultFriendOperateStub(String text, AbsRelationOperate.ClickListener onClickListener) {
        super(text, R.layout.operate_view_holder_item, onClickListener);
    }

    @Override
    public int getViewLayout() {
        return mLayoutId;
    }
}
