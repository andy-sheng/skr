package com.module.playways.room.msg.event;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.room.msg.BasePushInfo;
import com.component.live.proto.Room.CommentMsg;

import java.util.List;

// 处理用户真实的信息
// TODO: 2019/5/14 注意区分PretendCommentMsgEvent和CommentMsgEvent的区别
public class CommentMsgEvent {

    public final static int MSG_TYPE_SEND = 0;
    public final static int MSG_TYPE_RECE = 1;

    public int type = MSG_TYPE_RECE;
    public String text;
    public List<UserInfoModel> mUserInfoModelList;
    public BasePushInfo info;

    public CommentMsgEvent(BasePushInfo info, int type, String text) {
        this.info = info;
        this.type = type;
        this.text = text;
    }

    public CommentMsgEvent(BasePushInfo info, int type, CommentMsg commentMsg) {
        this.info = info;
        this.type = type;
        this.text = commentMsg.getText();
        mUserInfoModelList = UserInfoModel.parseFromPB(commentMsg.getReceiverList());
    }
}
