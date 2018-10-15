package com.wali.live.michannel;

import android.text.TextUtils;

import com.base.log.MyLog;

import java.io.Serializable;

/**
 * Created by lan on 16/8/26.
 *
 * @module 频道
 */
public class ChannelParam implements Serializable, Cloneable {
    private static final String TAG = ChannelParam.class.getSimpleName();

    private long mChannelId;
    private long mSubListId;
    private String mSectionTitle;

    private String mDownText1;
    private int mFrom;

    public ChannelParam(long channelId, long subListId) {
        setId(channelId, subListId);
    }

    public ChannelParam(long channelId, long subListId, int from) {
        setId(channelId, subListId);
        setFrom(from);
    }

    public void setId(long channelId, long subListId) {
        mChannelId = channelId;
        mSubListId = subListId;
    }

    public void setSectionTitle(String sectionTitle) {
        mSectionTitle = sectionTitle;
    }

    public long getChannelId() {
        return mChannelId;
    }

    public long getSubListId() {
        return mSubListId;
    }

    public String getSectionTitle() {
        if (TextUtils.isEmpty(mSectionTitle)) {
            return String.valueOf(0);
        }
        return mSectionTitle;
    }

    public void setSubListId(int subListId) {
        mSubListId = subListId;
    }

    public void setChannelId(long channelId) {
        mChannelId = channelId;
    }

    public int getFrom() {
        return mFrom;
    }

    public void setFrom(int from) {
        mFrom = from;
    }

    public String getDownText1() {
        return mDownText1;
    }

    public void setDownText1(String downText1) {
        mDownText1 = downText1;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static ChannelParam clone(ChannelParam param, String sectionTitle) {
        try {
            ChannelParam result = (ChannelParam) param.clone();
            result.setSectionTitle(sectionTitle);
            return result;
        } catch (CloneNotSupportedException e) {
            MyLog.d(TAG, e);
        } catch (Exception e) {
            MyLog.d(TAG, e);
        }
        return null;
    }
}
