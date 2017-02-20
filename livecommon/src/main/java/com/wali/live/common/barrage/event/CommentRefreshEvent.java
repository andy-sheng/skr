package com.wali.live.common.barrage.event;

import com.wali.live.common.model.CommentModel;

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