package useroperate.def;

import com.component.busilib.R;

import useroperate.inter.AbsRelationOperate;

public class DefaultFollowOperateStub extends AbsRelationOperate {

    @Override
    public int getFriendType() {
        return 1;
    }

    @Override
    public String getTitle() {
        return "关注";
    }

    public DefaultFollowOperateStub(String text, AbsRelationOperate.ClickListener onClickListener) {
        super(text, R.layout.operate_view_holder_item, onClickListener);
    }

    @Override
    public int getViewLayout() {
        return mLayoutId;
    }
}
