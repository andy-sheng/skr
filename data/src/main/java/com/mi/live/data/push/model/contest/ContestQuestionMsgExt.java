package com.mi.live.data.push.model.contest;

import com.google.protobuf.ByteString;
import com.mi.live.data.push.model.BarrageMsgType;
import com.wali.live.proto.LiveSummitProto;

/**
 * Created by zyh on 2018/1/11.
 *
 * @module 冲顶问题消息
 */
public class ContestQuestionMsgExt extends BaseContestMsgExt {

    public ContestQuestionMsgExt() {
    }

    public ContestQuestionMsgExt(LiveSummitProto.ContestQuestionMsg msg) {
        this.contestId = msg.getContestId();
        this.questionInfoModel = new QuestionInfoModel(msg.getQuestionInfos());
        this.streamTs = msg.getStreamTs();
        this.answerNums = msg.getAnswerNum();
    }

    @Override
    public ByteString toByteString() {
        return null;
    }

    public void setQuestionInfoModel(QuestionInfoModel questionInfoModel) {
        this.questionInfoModel = questionInfoModel;
    }

    @Override
    public String toString() {
        return "ContestQuestionMsgExt{" +
                "contestId='" + contestId + '\'' +
                ", questionInfoModel=" + questionInfoModel.toString() +
                ", streamTs=" + streamTs +
                ", answerNums=" + answerNums +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ContestQuestionMsgExt) {
            return ((ContestQuestionMsgExt) o).getQuestionInfoModel().equals(questionInfoModel);
        }
        return super.equals(o);
    }

    @Override
    public int getMsgType() {
        return BarrageMsgType.B_MSG_TYPE_QUESTION;
    }
}
