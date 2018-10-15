package com.mi.live.data.push.model.contest;

import com.google.protobuf.ByteString;
import com.mi.live.data.push.model.BarrageMsg;

/**
 * Created by zyh on 2018/1/15.
 */

public abstract class BaseContestMsgExt implements BarrageMsg.MsgExt {
    protected String contestId;                      //场次号
    protected QuestionInfoModel questionInfoModel;   //题目信息
    protected long streamTs;                         //当前视频流的时间戳
    protected long answerNums;                       //答题人数

    abstract public int getMsgType();

    public String getContestId() {
        return contestId;
    }

    public QuestionInfoModel getQuestionInfoModel() {
        return questionInfoModel;
    }

    public long getStreamTs() {
        return streamTs;
    }

    public long getAnswerNums() {
        return answerNums;
    }

    @Override
    public ByteString toByteString() {
        return null;
    }
}
