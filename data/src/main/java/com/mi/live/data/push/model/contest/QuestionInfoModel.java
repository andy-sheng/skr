package com.mi.live.data.push.model.contest;

import com.wali.live.proto.LiveSummitProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyh on 2018/1/11.
 *
 * @module 下发的问题的数据结构
 */

public class QuestionInfoModel {
    private String seq;                                                          //题目号（从1开始，例：第12题，seq=2018011101_12）
    private ArrayList<QuestionInfoItem> questionInfoItems = new ArrayList<>();   //问题选项
    private String questionContent;                                              //题目信息
    private boolean isLastQuestion;                                               //是否最后一题
    private long delayTime;                                                       //超时时间
    private long questionShowId;                                                  //题目显示的顺序号（从1开始 例：1, 2, 3, ...）

    public QuestionInfoModel(String seq, ArrayList<QuestionInfoItem> questionInfoItems,
                             String questionContent, boolean isLastQuestion,
                             long delayTime, long questionShowId) {
        this.seq = seq;
        this.questionInfoItems = questionInfoItems;
        this.questionContent = questionContent;
        this.isLastQuestion = isLastQuestion;
        this.delayTime = delayTime;
        this.questionShowId = questionShowId;
    }

    public QuestionInfoModel(LiveSummitProto.QuestionInfo questionInfo) {
        this.seq = questionInfo.getSeq();
        List<LiveSummitProto.QuestionInfoItem> list = questionInfo.getQuestionInfosList();
        if (list != null) {
            for (LiveSummitProto.QuestionInfoItem item : list) {
                this.questionInfoItems.add(new QuestionInfoItem(item));
            }
        }
        this.questionContent = questionInfo.getQuestion();
        this.isLastQuestion = questionInfo.getIsLast();
        this.delayTime = questionInfo.hasDelayTime() ? questionInfo.getDelayTime() : 0;
        this.questionShowId = questionInfo.getSeqId();
    }

    public String getSeq() {
        return seq;
    }

    public ArrayList<QuestionInfoItem> getQuestionInfoItems() {
        return questionInfoItems;
    }

    public boolean isLastQuestion() {
        return isLastQuestion;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public long getQuestionShowId() {
        return questionShowId;
    }

    @Override
    public String toString() {
        String items = "";
        for (QuestionInfoItem item : questionInfoItems) {
            items += item.toString() + " ";
        }
        return "QuestionInfoModel{" +
                "seq='" + seq + '\'' +
                ", questionInfoItems=" + items +
                ", questionContent='" + questionContent + '\'' +
                ", isLastQuestion=" + isLastQuestion +
                ", delayTime=" + delayTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof QuestionInfoModel) {
            return ((QuestionInfoModel) o).getSeq() == this.seq;
        }
        return super.equals(o);
    }

    public String getQuestionContent() {
        return questionContent;
    }

    //每一个单一的选项只有在QuestionInfo中存在，所以定义成QuestionInfo的内部类了。
    // 多个类应用的话，可以提出去。
    public static class QuestionInfoItem {
        private String id;
        private String text;
        private boolean isAnswer;
        private int num;

        public QuestionInfoItem(String id, String text, boolean isAnswer, int num) {
            this.id = id;
            this.text = text;
            this.isAnswer = isAnswer;
            this.num = num;
        }

        public QuestionInfoItem(LiveSummitProto.QuestionInfoItem questionInfoItem) {
            this.id = questionInfoItem.getId();
            this.text = questionInfoItem.getText();
            this.isAnswer = questionInfoItem.getIsAnswer();
            this.num = questionInfoItem.getNum();
        }

        public String getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public boolean isAnswer() {
            return isAnswer;
        }

        public int getNum() {
            return num;
        }

        @Override
        public String toString() {
            return "QuestionInfoItem{" +
                    "id=" + id +
                    ", text='" + text + '\'' +
                    ", isAnswer=" + isAnswer +
                    ", num=" + num +
                    '}';
        }
    }
}
