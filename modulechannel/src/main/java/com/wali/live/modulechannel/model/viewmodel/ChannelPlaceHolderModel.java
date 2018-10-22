package com.wali.live.modulechannel.model.viewmodel;

import com.squareup.wire.Message;

/**
 * Created by vera on 2018/6/27.
 */

public class ChannelPlaceHolderModel extends ChannelViewModel {
    int mViewHeight = 0;

    public ChannelPlaceHolderModel(int viewHeight) {
        mViewHeight = viewHeight;
        mUiType = ChannelUiType.TYPE_PLACEHOLDER;
    }

    @Override
    protected void parseTemplate(Message protoItem) throws Exception {

    }

    @Override
    public boolean isNeedRemove() {
        return false;
    }

    public int getViewHeight() {
        return mViewHeight;
    }

    public void setViewHeight(int mViewHeight) {
        this.mViewHeight = mViewHeight;
    }

}
