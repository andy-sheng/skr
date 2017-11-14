package com.wali.live.watchsdk.fans.presenter;

import com.base.mvp.IRxView;
import com.wali.live.watchsdk.fans.task.model.GroupJobListModel;

/**
 * Created by lan on 2017/11/13.
 */
public interface IFansTaskView extends IRxView {
    void setGroupTaskList(GroupJobListModel model);
}
