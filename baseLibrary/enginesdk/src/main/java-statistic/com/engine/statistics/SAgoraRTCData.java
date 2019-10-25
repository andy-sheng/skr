package com.engine.statistics;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;



public class SAgoraRTCData
{

    private String mLinePrefix = "";
    private List<SAgoraRTCStats>  mRtcStatsList;
    private List<SAgoraLocalVideoStats> mLocalVList;
    private List<SAgoraRemoteAudioStats> mRemoteAList;
    private List<SAgoraRemoteVideoStats> mRemoteVList;

    private List<SAgoraNetworkQuality> mNetQualityList;
    private List<SAgoraRemoteAudioTransportStats>   mRemoteATransList;
    private List<SAgoraRemoteVideoTransportStat>    mRemoteVTransList;


    public SAgoraRTCData() {
        mRtcStatsList = new ArrayList<SAgoraRTCStats>();
        mLocalVList  = new ArrayList<SAgoraLocalVideoStats>();
        mRemoteAList = new ArrayList<SAgoraRemoteAudioStats>();
        mRemoteVList = new ArrayList<SAgoraRemoteVideoStats>();
        mNetQualityList= new ArrayList<SAgoraNetworkQuality>();
        mRemoteATransList= new ArrayList<SAgoraRemoteAudioTransportStats>();
        mRemoteVTransList= new ArrayList<SAgoraRemoteVideoTransportStat>();
    }

    public SAgoraRTCData setLinePrefix(String prefix) {
        if (null != prefix && 0 != prefix.length())
            mLinePrefix = prefix;

        return this;
    }


    //将数值转回为相应的语义字符串
    protected static String transNetQuality(int quality) {
        if (0 == quality) return "质量未知value(0)";
        else if (1 == quality) return "质量极好";
        else if (2 == quality) return "用户主观感觉和极好差不多，但码率可能略低于极好";
        else if (3 == quality) return "用户主观感受有瑕疵但不影响沟通";
        else if (4 == quality) return "勉强能沟通但不顺畅";
        else if (5 == quality) return "网络质量非常差，基本不能沟通";
        else if (6 == quality) return "网络连接断开，完全无法沟通";
        else return "声网没有定义的质量值value("+quality+")";
    }

    public static class SAgoraRTCStats extends IRtcEngineEventHandler.RtcStats {
        public long timeStamp;

        public String toString() {
            return SUtils.transTime(timeStamp)+" SAgoraRTCStats: totalDuration="+totalDuration+
                    ", txBytes=" + txBytes +
                    ", rxBytes=" + rxBytes +
                    ", txKBitRate=" + txKBitRate +
                    ", rxKBitRate=" + rxKBitRate +
                    ", txAudioKBitRate=" + txAudioKBitRate +
                    ", rxAudioKBitRate=" + rxAudioKBitRate +
                    ", txVideoKBitRate=" + txVideoKBitRate +
                    ", rxVideoKBitRate=" + rxVideoKBitRate +
                    ", users=" + users +
                    ", lastmileDelay=" + lastmileDelay +
                    ", txPacketLossRate=" + txPacketLossRate +
                    ", rxPacketLossRate=" + rxPacketLossRate +
                    ", cpuTotalUsage=" + cpuTotalUsage +
                    ", cpuAppUsage=" + cpuAppUsage + "\n";

        }
    }


    public static class SAgoraLocalVideoStats extends IRtcEngineEventHandler.LocalVideoStats {
        public long timeStamp;
        public String toString() {
            return SUtils.transTime(timeStamp)+" SAgoraLocalVideoStats: sentBitrate=" + sentBitrate +
                    ", sentFrameRate=" + sentFrameRate+
                    ", encoderOutputFrameRate=" + encoderOutputFrameRate+
                    ", rendererOutputFrameRate=" + rendererOutputFrameRate+
                    ", targetBitrate=" + targetBitrate+
                    ", targetFrameRate=" + targetFrameRate+
                    ", qualityAdaptIndication=" + qualityAdaptIndication+"\n";
        }
    }

    public static class SAgoraRemoteAudioStats extends IRtcEngineEventHandler.RemoteAudioStats {
        public long timeStamp;
        public String strQuality; //replace base class's "quality" for explicitly meanning

        public String toString() {

            return SUtils.transTime(timeStamp)+" SAgoraRemoteAudioStats: uid=" + uid +
                    ", quality=" + strQuality +
                    ", networkTransportDelay=" + networkTransportDelay+
                    ", jitterBufferDelay=" + jitterBufferDelay+
                    ", audioLossRate=" + audioLossRate+"\n";
        }
    }

    public static class SAgoraRemoteVideoStats extends IRtcEngineEventHandler.RemoteVideoStats {
        public long timeStamp;
        public String toString() {
            return SUtils.transTime(timeStamp)+" SAgoraRemoteVideoStats: uid="+ uid +
                    ", width=" +width+
                    ", height=" +height+
                    ", receivedBitrate=" +receivedBitrate+
                    ", decoderOutputFrameRate=" +decoderOutputFrameRate+
                    ", rendererOutputFrameRate=" +rendererOutputFrameRate+
                    ", rxStreamType=" +rxStreamType+"\n";
        }
    }


    public static class SAgoraRemoteVideoTransportStat{
        public long timeStamp;
        public int 	uid;
        public int 	delay;
        public int 	lost;
        public int 	rxKBitRate;

        public String toString() {
            return SUtils.transTime(timeStamp)+" SAgoraRemoteVideoTransportStat: uid="+uid+"， delay="+delay+"， lost="+lost+"， rxKBitRate="+rxKBitRate + "\n";
        }
    }


    public static class SAgoraRemoteAudioTransportStats{
        public long timeStamp;
        public int 	uid;
        public int 	delay;
        public int 	lost;
        public int 	rxKBitRate;

        public String toString(){
            return SUtils.transTime(timeStamp)+" SAgoraRemoteAudioTransportStats: uid="+uid+"， delay="+delay+"， lost="+lost+"， rxKBitRate="+rxKBitRate + "\n";
        }
    }

    public static class SAgoraNetworkQuality {
        public long timeStamp;
        public int  uid = -1;
        public int 	txQuality=0;
        public int 	rxQuality=0;



        public String toString(){
            return SUtils.transTime(timeStamp)+" SAgoraNetworkQuality: uid="+uid+", txQuality="+transNetQuality(txQuality)+", rxQuality="+transNetQuality(rxQuality) + "\n";
        }
    } //end of class SAgoraNetworkQuality


    public synchronized void addRtcStats(IRtcEngineEventHandler.RtcStats s ) {
        SAgoraRTCStats n = new SAgoraRTCStats();
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
        SAgoraLocalVideoStats n = new SAgoraLocalVideoStats();

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
        SAgoraRemoteAudioStats n = new SAgoraRemoteAudioStats();

        n.timeStamp = System.currentTimeMillis();

        n.uid = s.uid;
        n.strQuality =  transNetQuality(s.quality);
        n.networkTransportDelay = s.networkTransportDelay;
        n.jitterBufferDelay = s.jitterBufferDelay;
        n.audioLossRate = s.audioLossRate;

        mRemoteAList.add(n);
        return;
    }


    public synchronized void addRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats s) {
        SAgoraRemoteVideoStats n = new SAgoraRemoteVideoStats();

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
        SAgoraNetworkQuality n = new SAgoraNetworkQuality();

        n.timeStamp = System.currentTimeMillis();
        n.uid = uid;
        n.txQuality = txQuality;
        n.rxQuality = rxQuality;

        mNetQualityList.add(n);
        return;
    }


    public synchronized void addRemoteAudioTransStats(int uid, int delay, int lost, int rxKBitRate) {
        SAgoraRemoteAudioTransportStats n = new SAgoraRemoteAudioTransportStats();

        n.timeStamp = System.currentTimeMillis();
        n.uid = uid;
        n.delay = delay;
        n.lost = lost;
        n.rxKBitRate = rxKBitRate;

        mRemoteATransList.add(n);
        return;
    }

    public synchronized void addRemoteVideoTransStata(int uid, int delay, int lost, int rxKBitRate) {
        SAgoraRemoteVideoTransportStat n = new SAgoraRemoteVideoTransportStat();

        n.timeStamp = System.currentTimeMillis();
        n.uid = uid;
        n.delay = delay;
        n.lost = lost;
        n.rxKBitRate = rxKBitRate;

        mRemoteVTransList.add(n);
        return;
    }


    //SAgoraRTCData's to String
    public synchronized String toString() {

        String retStr = "";

        if (mRtcStatsList.size() > 0) {
            for (SAgoraRTCStats e : mRtcStatsList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        if (mLocalVList.size() > 0) {
            for (SAgoraLocalVideoStats e : mLocalVList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        if (mRemoteAList.size() > 0) {
            for (SAgoraRemoteAudioStats e : mRemoteAList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        if (mRemoteVList.size() > 0) {
            for (SAgoraRemoteVideoStats e : mRemoteVList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        if (mNetQualityList.size() > 0) {
            for (SAgoraNetworkQuality e : mNetQualityList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        if (mRemoteATransList.size() > 0) {
            for (SAgoraRemoteAudioTransportStats e : mRemoteATransList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        if (mRemoteVTransList.size() > 0) {
            for (SAgoraRemoteVideoTransportStat e: mRemoteVTransList) {
                retStr += (mLinePrefix + e.toString());
            }
        }

        return retStr;
    }


    public synchronized SAgoraRTCData reset() {

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