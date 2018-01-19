package com.mi.live.data.push.model.contest;

import com.mi.live.data.push.model.BarrageMsgType;
import com.wali.live.proto.LiveSummitProto;

/**
 * Created by zyh on 2018/1/11.
 *
 * @module 冲顶答题push消息
 */
public class ContestAnswerMsgExt extends BaseContestMsgExt {
    private LastQuestionInfoModel mLastQuestionInfoModel;

    public ContestAnswerMsgExt() {
    }

    public ContestAnswerMsgExt(LiveSummitProto.ContestAnswerMsg msg) {
        this.contestId = msg.getContestId();
        this.questionInfoModel = new QuestionInfoModel(msg.getQuestionInfos());
        LiveSummitProto.LastQuestionInfo lastQuestionInfo = msg.getLastQuestionInfo();
        if (msg.hasLastQuestionInfo() && lastQuestionInfo != null) {
            this.mLastQuestionInfoModel = new LastQuestionInfoModel(lastQuestionInfo);
        }
        this.streamTs = msg.getStreamTs();
        this.answerNums = msg.getAnswerNum();
    }

    @Override
    public String toString() {
        return "ContestAnswerMsgExt{" +
                "contestId='" + contestId + '\'' +
                ", questionInfoModel=" + questionInfoModel +
                ", streamTs=" + streamTs +
                ", answerNums=" + answerNums +
                "mLastQuestionInfoModel=" + mLastQuestionInfoModel.toString() +
                '}';
    }

    public LastQuestionInfoModel getLastQuestionInfoModel() {
        return mLastQuestionInfoModel;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ContestAnswerMsgExt) {
            return ((ContestAnswerMsgExt) o).equals(questionInfoModel);
        }
        return super.equals(o);
    }

    @Override
    public int getMsgType() {
        return BarrageMsgType.B_MSG_TYPE_ANSWER;
    }
}
