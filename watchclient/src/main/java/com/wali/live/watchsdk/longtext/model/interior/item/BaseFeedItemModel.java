package com.wali.live.watchsdk.longtext.model.interior.item;

import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

/**
 * Created by lan on 2017/9/20.
 */
public abstract class BaseFeedItemModel extends BaseViewModel {
    public final static int FEEDS_MULTI_MEDIA_TYPE_PIC = 1;
    public final static int FEEDS_MULTI_MEDIA_TYPE_VIDEO = 2;
    public final static int FEEDS_MULTI_MEDIA_TYPE_TEXT = 3;
    public final static int FEEDS_MULTI_MEDIA_TYPE_LINK = 4;
    public final static int FEEDS_MULTI_MEDIA_TYPE_USER_LINK = 5;

    public abstract int getUiType();
}
