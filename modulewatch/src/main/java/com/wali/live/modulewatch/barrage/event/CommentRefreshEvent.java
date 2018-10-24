package com.wali.live.modulewatch.barrage.event;


import com.wali.live.modulewatch.barrage.model.viewmodel.CommentModel;

import java.util.List;

/**
 * Created by chengsimin on 2016/11/30.
 */

public class CommentRefreshEvent {
    public List<CommentModel> barrageMsgs;
    public boolean needManualMoveToLast;
    public String token;

    public CommentRefreshEvent(List<CommentModel> barrageMsgs, boolean needManualMoveToLast, String token) {
        this.barrageMsgs = barrageMsgs;
        this.needManualMoveToLast = needManualMoveToLast;
        this.token = token;
    }
}