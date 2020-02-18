package useroperate.def;

import com.component.busilib.R;

import useroperate.inter.AbsRelationOperate;

//只是layout和title不一样
public class DefaultFansOperateStub extends AbsRelationOperate {

    @Override
    public int getFriendType() {
        return 2;
    }

    @Override
    public String getTitle() {
        return "粉丝";
    }

    public DefaultFansOperateStub(String text, AbsRelationOperate.ClickListener onClickListener) {
        super(text, R.layout.operate_view_holder_item, onClickListener);
    }

    @Override
    public int getViewLayout() {
        return mLayoutId;
    }
}
