package com.common.log.screenlog;

import android.util.Log;

import com.common.log.MyFlattener;
import com.common.utils.U;
import com.elvishew.xlog.LogLevel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class LogListContainer {

    static class Node {
        LogModel mLogModel;
        Node next;
    }

    public static final int MAX_COUNT = 2000;

    HashMap<String, Integer> mTagNumMap = new HashMap<>();

    Node mHead;// 队列头

    Node mTail;// 队列尾

    Node mLastInput;//下一次改输出的位置

    int mLength = 1;

    Listener mListener;

    long mLastNotifyTs = 0;

    public LogListContainer() {
        mHead = new Node();
        mHead.mLogModel = new LogModel();
        mHead.mLogModel.level = LogLevel.WARN;
        mHead.mLogModel.tag = "SKER";
        mHead.mLogModel.msg = "当前时间:" + U.getDateTimeUtils().formatTimeStringForDate(System.currentTimeMillis());
        mTail = mHead;
        mLength = 1;
        mLastInput = mTail;
    }

    public void addLog(LogModel logModel) {
        synchronized (this) {
            if (mTagNumMap.containsKey(logModel.tag)) {
                int n = mTagNumMap.get(logModel.tag);
                mTagNumMap.put(logModel.tag, n + 1);
            } else {
                mTagNumMap.put(logModel.tag, 1);
            }
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
                        LogModel l = mLastInput.mLogModel;
                        if (mListener.accept(l.tag)) {
                            sb.append(flatten(l)).append("\n");
                        }
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
                    LogModel l = mLastInput.mLogModel;
                    sb.append(flatten(l)).append("\n");
                }
                mLastInput = mLastInput.next;
            }
        }
        return sb.toString();
    }

    public CharSequence flatten(LogModel l) {
        String formattedNow = U.getDateTimeUtils().formatTimeStringForDate(l.ts);
        return formattedNow
                + '|' + Thread.currentThread().getId()
                + '|' + LogLevel.getShortLevelName(l.level)
                + '|' + l.tag
                + "| " + l.msg;
    }

    public HashMap<String, Integer> getTagMap() {
        return mTagNumMap;
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public interface Listener {
        boolean accept(String tag);

        int getNotifyInterval();

        void notifyLogUpdate(String logs);
    }
}
