package com.engine.statistics;


import com.engine.statistics.datadef.AD;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;



public class SAgoraDataHolder
{

    private String mLinePrefix = "";
    private List<AD.SAgoraRTCStats>  mRtcStatsList;
    private List<AD.SAgoraLocalVideoStats> mLocalVList;
    private List<AD.SAgoraRemoteAudioStats> mRemoteAList;
    private List<AD.SAgoraRemoteVideoStats> mRemoteVList;

    private List<AD.SAgoraNetworkQuality> mNetQualityList;
    private List<AD.SAgoraRemoteAudioTransportStats>   mRemoteATransList;
    private List<AD.SAgoraRemoteVideoTransportStat>    mRemoteVTransList;


    public SAgoraDataHolder() {
        mRtcStatsList = new ArrayList<AD.SAgoraRTCStats>();
        mLocalVList  = new ArrayList<AD.SAgoraLocalVideoStats>();
        mRemoteAList = new ArrayList<AD.SAgoraRemoteAudioStats>();
        mRemoteVList = new ArrayList<AD.SAgoraRemoteVideoStats>();
        mNetQualityList= new ArrayList<AD.SAgoraNetworkQuality>();
        mRemoteATransList= new ArrayList<AD.SAgoraRemoteAudioTransportStats>();
        mRemoteVTransList= new ArrayList<AD.SAgoraRemoteVideoTransportStat>();
    }

    public SAgoraDataHolder setLinePrefix(String prefix) {
        if (null != prefix && 0 != prefix.length())
            mLinePrefix = prefix;

        return this;
    }





    public synchronized void addRtcStats(IRtcEngineEventHandler.RtcStats s ) {
        AD.SAgoraRTCStats n = new AD.SAgoraRTCStats();
        n.timeStamp     = System.currentTimeMillis();

        n.cpuAppUsage   = s.cpuAppUsage;
        n.cpuTotalUsage = s.cpuTotalUsage;
        n.totalDuration = s.totalDuration;
        n.txBytes = s.txBytes;
        n.rxBytes = s.rxBytes;
        n.txKBitRate = s.txKBitRate;
        n.rxKBitRate = s.rxKBitRate;
        n.txAudioKBitRate = s.txAudioKBitRate;
        n.rxAudioKBitRate = s.rxAudioKBitRate;
        n.users = s.users;
        n.lastmileDelay = s.lastmileDelay;
        n.txPacketLossRate = s.txPacketLossRate;
        n.rxPacketLossRate = s.rxPacketLossRate;

        mRtcStatsList.add(n);
        return;
    }

    public synchronized void addLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats s) {
        AD.SAgoraLocalVideoStats n = new AD.SAgoraLocalVideoStats();

        n.timeStamp = System.currentTimeMillis();
        n.sentBitrate = s.sentBitrate;
        n.sentFrameRate = s.sentFrameRate;
        n.encoderOutputFrameRate = s.encoderOutputFrameRate;
        n.rendererOutputFrameRate= s.rendererOutputFrameRate;
        n.targetBitrate = s.targetBitrate;
        n.targetFrameRate = s.targetFrameRate;
        n.qualityAdaptIndication = s.qualityAdaptIndication;

        mLocalVList.add(n);
        return;
    }

    public synchronized void addRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats s) {
        AD.SAgoraRemoteAudioStats n = new AD.SAgoraRemoteAudioStats();

        n.timeStamp = System.currentTimeMillis();

        n.uid = s.uid;
        n.quality = s.quality;
//        n.strQuality =  transNetQuality(s.quality);
        n.networkTransportDelay = s.networkTransportDelay;
        n.jitterBufferDelay = s.jitterBufferDelay;
        n.audioLossRate = s.audioLossRate;

        mRemoteAList.add(n);
        return;
    }


    public synchronized void addRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats s) {
        AD.SAgoraRemoteVideoStats n = new AD.SAgoraRemoteVideoStats();

        n.timeStamp = System.currentTimeMillis();
        n.uid = s.uid;
        n.width = s.width;
        n.height= s.height;
        n.receivedBitrate = s.receivedBitrate;
        n.decoderOutputFrameRate = s.decoderOutputFrameRate;
        n.rendererOutputFrameRate= s.rendererOutputFrameRate;
        n.rxStreamType = s.rxStreamType;

        mRemoteVList.add(n);
        return;
    }

    public synchronized void addNetQualityStats(int uid, int txQuality,int rxQuality) {
        AD.SAgoraNetworkQuality n = new AD.SAgoraNetworkQuality();

        n.timeStamp = System.currentTimeMillis();
        n.uid = uid;
        n.txQuality = txQuality;
        n.rxQuality = rxQuality;

        mNetQualityList.add(n);
        return;
    }


    public synchronized void addRemoteAudioTransStats(int uid, int delay, int lost, int rxKBitRate) {
        AD.SAgoraRemoteAudioTransportStats n = new AD.SAgoraRemoteAudioTransportStats();

        n.timeStamp = System.currentTimeMillis();
        n.uid = uid;
        n.delay = delay;
        n.lost = lost;
        n.rxKBitRate = rxKBitRate;

        mRemoteATransList.add(n);
        return;
    }

    public synchronized void addRemoteVideoTransStata(int uid, int delay, int lost, int rxKBitRate) {
        AD.SAgoraRemoteVideoTransportStat n = new AD.SAgoraRemoteVideoTransportStat();

        n.timeStamp = System.currentTimeMillis();
        n.uid = uid;
        n.delay = delay;
        n.lost = lost;
        n.rxKBitRate = rxKBitRate;

        mRemoteVTransList.add(n);
        return;
    }


    //SAgoraDataHolder's to String
    public synchronized String toString() {

        String retStr = "";

        if (mRtcStatsList.size() > 0) {
            for (AD.SAgoraRTCStats e : mRtcStatsList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        if (mLocalVList.size() > 0) {
            for (AD.SAgoraLocalVideoStats e : mLocalVList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        if (mRemoteAList.size() > 0) {
            for (AD.SAgoraRemoteAudioStats e : mRemoteAList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        if (mRemoteVList.size() > 0) {
            for (AD.SAgoraRemoteVideoStats e : mRemoteVList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        if (mNetQualityList.size() > 0) {
            for (AD.SAgoraNetworkQuality e : mNetQualityList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        if (mRemoteATransList.size() > 0) {
            for (AD.SAgoraRemoteAudioTransportStats e : mRemoteATransList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        if (mRemoteVTransList.size() > 0) {
            for (AD.SAgoraRemoteVideoTransportStat e: mRemoteVTransList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        return retStr;
    }


    public synchronized SAgoraDataHolder reset() {

        mRtcStatsList.clear();
        mLocalVList.clear();
        mRemoteAList.clear();
        mRemoteVList.clear();
        mNetQualityList.clear();
        mRemoteATransList.clear();
        mRemoteVTransList.clear();

        return this;
    }

    //临时实验性API:
    public boolean need2Flush(){
        int recPerListLimit = 3;

        int nowTotalRecords = mRtcStatsList.size() + mLocalVList.size()+ mRemoteAList.size() +
                mRemoteVList.size() +  mNetQualityList.size() + mRemoteATransList.size() +  mRemoteVTransList.size();

        if (nowTotalRecords >= recPerListLimit * 7)
            return true;
        else
            return false;

    }

}