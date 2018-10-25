package com.wali.live.modulewatch.barrage.event;

import com.wali.live.modulewatch.barrage.model.viewmodel.CommentModel;

import java.util.List;

public class RefreshGameLiveCommentEvent {
    public List<CommentModel> barrageMsgs;
    public CommentModel barrageMsg;
    public String token;

    public RefreshGameLiveCommentEvent(List<CommentModel> barrageMsgs, String token) {
        this.barrageMsgs = barrageMsgs;
        this.token = token;
    }

    public RefreshGameLiveCommentEvent(CommentModel barrageMsg, String token) {
        this.barrageMsg = barrageMsg;
        this.token = token;
    }
}
