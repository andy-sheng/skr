package com.wali.live.modulewatch.watch.model.roominfo;

import com.wali.live.proto.LiveCommon.MsgRule;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by wuxiaoshan on 16-7-8.
 */
public class MessageRule implements Serializable {

    private MessageRuleType mMessageRuleType;//本次发言规则的类型

    //不能重复，默认false，不设置或者​false：可以重复发言，true：不能重复发言
    private boolean unrepeatable;
    //发言频率周期,单位s,不设置或者0代表没有限制
    private int speakPeriod;

    public boolean isUnrepeatable() {
        return unrepeatable;
    }

    public void setUnrepeatable(boolean unrepeatable) {
        this.unrepeatable = unrepeatable;
    }

    public int getSpeakPeriod() {
        return speakPeriod;
    }

    public void setSpeakPeriod(int speakPeriod) {
        this.speakPeriod = speakPeriod;
    }

    public MessageRuleType getMessageRuleType() {
        return mMessageRuleType;
    }

    public void setMessageRuleType(MessageRuleType mMessageRuleType) {
        this.mMessageRuleType = mMessageRuleType;
    }

    public MessageRule() {
        unrepeatable = false;
        speakPeriod = 0;
        mMessageRuleType = MessageRuleType.NORMAL;
    }

    public MessageRule(MsgRule msgRule) {
        if (msgRule != null) {
            if (msgRule.hasSpeakPeriod()) {
                speakPeriod = msgRule.getSpeakPeriod();
            } else {
                speakPeriod = 0;
            }
            if (msgRule.hasUnrepeatable()) {
                unrepeatable = msgRule.getUnrepeatable();
            } else {
                unrepeatable = false;
            }
        } else {
            speakPeriod = 0;
            unrepeatable = false;
        }
        mMessageRuleType = MessageRuleType.NORMAL;
    }


    public MessageRule(boolean isRedName, int speakperid) {
        if (isRedName) {

            if (speakperid > 0) {
                speakPeriod = speakperid;
                unrepeatable = false;
            }

            if (speakperid == -1) {
                speakPeriod = Integer.MAX_VALUE;
                unrepeatable = true;
            }

        }
        mMessageRuleType = MessageRuleType.REDNAME;
    }


    public void merge(MessageRule messageRule) {
        if (messageRule != null) {
            if (this.speakPeriod < messageRule.speakPeriod) {
                this.speakPeriod = messageRule.speakPeriod;
            }

            if (!this.unrepeatable && this.unrepeatable != messageRule.unrepeatable) {
                this.unrepeatable = messageRule.unrepeatable;
            }
        }
    }

    @Override
    public String toString() {
        return "unrepeatable=" + unrepeatable + " speakPeriod=" + speakPeriod;
    }

    public static enum MessageRuleType {
        NORMAL, REDNAME
    }


    public static MessageRule mergeMessageRule(Map<MessageRuleType, MessageRule> messageRule) {
        if (messageRule != null && messageRule.size() > 0) {
            MessageRule result = null;
            Set<MessageRuleType> keys = messageRule.keySet();
            for (MessageRuleType key : keys) {
                if (result == null) {
                    result = messageRule.get(key);
                } else {
                    result.merge(messageRule.get(key));
                }
            }
            return result;
        }
        return null;
    }
}

