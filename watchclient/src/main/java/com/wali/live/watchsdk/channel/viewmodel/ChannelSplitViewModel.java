package com.wali.live.watchsdk.channel.viewmodel;

import com.wali.live.proto.CommonChannelProto.ChannelItem;
import com.wali.live.proto.CommonChannelProto.UiTemplateSeparator;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道分割线数据模型
 */
public class ChannelSplitViewModel extends ChannelViewModel<ChannelItem> {
    private static final String TAG = ChannelSplitViewModel.class.getSimpleName();

    // 1：灰色；2：白色
    protected int mColor;
    // 1：15；2：8
    protected int mHeight;

    protected String mTitle;

    public ChannelSplitViewModel(ChannelItem protoItem) throws Exception {
        super(protoItem);
    }

    @Override
    protected void parseTemplate(ChannelItem protoItem) throws Exception {
        mUiType = protoItem.getUiType();
        mSectionId = protoItem.getSectionId();
        parseUI(UiTemplateSeparator.parseFrom(protoItem.getUiData()));

        if (mColor != 1 && mColor != 2) {
            throw new Exception("ChannelSplitViewModel not supported color=" + mColor);
        }
    }

    private void parseUI(UiTemplateSeparator protoItem) {
        mColor = protoItem.getColor();
        mHeight = protoItem.getHeight();
        mTitle = protoItem.getTitle();
    }

    public int getColor() {
        return mColor;
    }

    public int getHeight() {
        return mHeight;
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public boolean isNeedRemove() {
        return false;
    }
}
