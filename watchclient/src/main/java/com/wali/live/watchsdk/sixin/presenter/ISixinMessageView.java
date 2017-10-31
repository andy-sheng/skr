package com.wali.live.watchsdk.sixin.presenter;

import com.base.mvp.IRxView;
import com.wali.live.watchsdk.sixin.message.SixinMessageModel;

import java.util.List;

/**
 * Created by lan on 2017/10/29.
 */
public interface ISixinMessageView extends IRxView {
    void loadData(List<SixinMessageModel> messageModelList);

    void addData(List<SixinMessageModel> messageModelList);
}
