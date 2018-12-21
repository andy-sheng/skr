package com.common.log.screenlog;

import com.common.utils.U;

import java.util.HashSet;

public class LogListContainer {

    static class Node {
        LogModel mLogModel;
        Node next;
    }

    public static final int MAX_COUNT = 10000;

    Node mHead;// 队列头

    Node mTail;// 队列尾

    Node mLastInput;//下一次改输出的位置

    int mLength = 1;

    Listener mListener;

    long mLastNotifyTs = 0;

    public LogListContainer() {
        mHead = new Node();
        mHead.mLogModel = new LogModel();
        mHead.mLogModel.tag = "链表头";
        mHead.mLogModel.msg = "当前时间:" + U.getDateTimeUtils().formatTimeStringForDate(System.currentTimeMillis());
        mTail = mHead;
        mLength = 1;
        mLastInput = mTail;
    }


    public void addLog(LogModel logModel) {
        synchronized (this) {
            Node node = new Node();
            node.mLogModel = logModel;
            mTail.next = node;
            mTail = node;
            if (mLastInput == null) {
                mLastInput = mTail;
            }
            mLength++;
            if (mLength > MAX_COUNT) {
                mLength = MAX_COUNT;
                Node t = mHead;
                mHead = mHead.next;
                t.next = null;
            }
            if (mListener != null) {
                long now = System.currentTimeMillis();
                if (now - mLastNotifyTs > mListener.getNotifyInterval()) {
                    mLastNotifyTs = System.currentTimeMillis();
                    StringBuilder sb = new StringBuilder();
                    while (mLastInput != null) {
                        sb.append(mLastInput.mLogModel.tag + ":" + mLastInput.mLogModel.msg).append("\n");
                        mLastInput = mLastInput.next;
                    }
                    // 通知更新
                    mListener.notifyLogUpdate(sb.toString());
                }
            }
        }
    }

    public String getLogByTag(HashSet<String> set) {
        StringBuilder sb = new StringBuilder();
        synchronized (this) {
            mLastInput = mHead;
            while (mLastInput != null) {
                if (set == null || set.contains(mLastInput.mLogModel.tag)) {
                    sb.append(mLastInput.mLogModel.tag + ":" + mLastInput.mLogModel.msg).append("\n");
                }
                mLastInput = mLastInput.next;
            }
        }
        return sb.toString();
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public interface Listener {
        int getNotifyInterval();

        void notifyLogUpdate(String logs);
    }
}
