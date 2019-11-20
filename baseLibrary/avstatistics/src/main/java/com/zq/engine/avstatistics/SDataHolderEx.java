package com.zq.engine.avstatistics;


import com.zq.engine.avstatistics.datastruct.ILogItem;
import com.zq.engine.avstatistics.datastruct.SAgora;
import com.zq.engine.avstatistics.datastruct.SAgoraUserEvent;
import com.zq.engine.avstatistics.datastruct.Skr;
import com.zq.engine.avstatistics.logservice.SLogServiceBase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;

import static com.zq.engine.avstatistics.datastruct.SAgoraUserEvent.EVENT_TYPE_onAudioRouteChanged;
import static io.agora.rtc.Constants.AUDIO_ROUTE_HEADSET;
import static io.agora.rtc.Constants.AUDIO_ROUTE_HEADSETBLUETOOTH;
import static io.agora.rtc.Constants.AUDIO_ROUTE_SPEAKERPHONE;

public class SDataHolderEx
{
    private final static String TAG = "[SLS]SDataHolderEx";

    private String mLinePrefix = "";


    private List<ILogItem> mItemList = null;

    public SDataHolderEx() {
        mItemList = new ArrayList<ILogItem>();
    }

    public SDataHolderEx setLinePrefix(String prefix) {
        if (null != prefix && 0 != prefix.length())
            mLinePrefix = prefix;

        return this;
    }



    public void addRtcStats(IRtcEngineEventHandler.RtcStats s ) {
        SAgora.SRTCStats n = new SAgora.SRTCStats();
        n.timeStamp = System.currentTimeMillis();

        n.cpuAppUsage = s.cpuAppUsage;
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

        mItemList.add(n);
        return;
    }


    public void addLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats s) {
        SAgora.SLocalVideoStats n = new SAgora.SLocalVideoStats();

        n.timeStamp = System.currentTimeMillis();
        n.sentBitrate = s.sentBitrate;
        n.sentFrameRate = s.sentFrameRate;
        n.encoderOutputFrameRate = s.encoderOutputFrameRate;
        n.rendererOutputFrameRate = s.rendererOutputFrameRate;
        n.targetBitrate = s.targetBitrate;
        n.targetFrameRate = s.targetFrameRate;
        n.qualityAdaptIndication = s.qualityAdaptIndication;

        mItemList.add(n);
        return;
    }



    public void addRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats s) {
        SAgora.SRemoteAudioStats n = new SAgora.SRemoteAudioStats();

        n.timeStamp = System.currentTimeMillis();

        n.uid = s.uid;
        n.quality = s.quality;
//        n.strQuality =  transNetQuality(s.quality);
        n.networkTransportDelay = s.networkTransportDelay;
        n.jitterBufferDelay = s.jitterBufferDelay;
        n.audioLossRate = s.audioLossRate;

        mItemList.add(n);
        return;
    }



    public void addRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats s) {
        SAgora.SRemoteVideoStats n = new SAgora.SRemoteVideoStats();

        n.timeStamp = System.currentTimeMillis();
        n.uid = s.uid;
        n.width = s.width;
        n.height = s.height;
        n.receivedBitrate = s.receivedBitrate;
        n.decoderOutputFrameRate = s.decoderOutputFrameRate;
        n.rendererOutputFrameRate = s.rendererOutputFrameRate;
        n.rxStreamType = s.rxStreamType;

        mItemList.add(n);
        return;
    }

    public void addNetQualityStats(int uid, int txQuality, int rxQuality) {
        SAgora.SNetworkQuality n = new SAgora.SNetworkQuality();

        n.timeStamp = System.currentTimeMillis();
        n.uid = uid;
        n.txQuality = txQuality;
        n.rxQuality = rxQuality;

        mItemList.add(n);
        return;
    }


    public void addRemoteAudioTransStats(int uid, int delay, int lost, int rxKBitRate) {
        SAgora.SRemoteAudioTransportStats n = new SAgora.SRemoteAudioTransportStats();

        n.timeStamp = System.currentTimeMillis();
        n.uid = uid;
        n.delay = delay;
        n.lost = lost;
        n.rxKBitRate = rxKBitRate;

        mItemList.add(n);
        return;
    }

    public void addRemoteVideoTransStata(int uid, int delay, int lost, int rxKBitRate) {
        SAgora.SRemoteVideoTransportStat n = new SAgora.SRemoteVideoTransportStat();

        n.timeStamp = System.currentTimeMillis();
        n.uid = uid;
        n.delay = delay;
        n.lost = lost;
        n.rxKBitRate = rxKBitRate;

        mItemList.add(n);
        return;
    }

    public void addAudioSamplingInfo(SAgora.SAudioSamplingInfo o, long curTs) {
        if (null == o) return;

        SAgora.SAudioSamplingInfo n = new SAgora.SAudioSamplingInfo();

        n.timeStamp = System.currentTimeMillis();


        n.meanAbsPCM = o.meanAbsPCM;
        n.maxAbsPCM = o.maxAbsPCM;
        n.smpCnt = o.smpCnt;
        n.chCnt = o.chCnt;
        n.smpRate = o.smpRate;
        n.pcmDuration = o.pcmDuration;
        n.statisticSpan = o.statisticSpan;

        mItemList.add(n);

        return;
    }


    public void addPlayerInfo(final int uid, final String filePath, final String midiPath,
                              final long mixMusicBeginOffset, final boolean loopback,
                              final boolean replace, final int cycle) {
        SAgora.SPlayerInfo n = new SAgora.SPlayerInfo();
        n.ts = System.currentTimeMillis();
        n.uid = uid;
        n.filePath = filePath;
        n.midiPath = midiPath;
        n.mixMusicBeginOffset = mixMusicBeginOffset;
        n.loopback = loopback;
        n.replace = replace;
        n.cycle = cycle;
        mItemList.add(n);
        return;
    }


    //for user event
    public void addUserEvent(SAgoraUserEvent e) {
        if (null == e) return;
        mItemList.add(e);
        return;
    }

    public void addPingInfo(Skr.PingInfo e) {
        if (null == e) return;

        e.ts = System.currentTimeMillis();
        mItemList.add(e);
        return;
    }

    public void addNetworkInfo(Skr.NetworkInfo e) {
        if (null == e) return;
        e.ts = System.currentTimeMillis();
        mItemList.add(e);
        return;
    }

    public final static int AR_PHONE_SPEAKER = 1;
    public final static int AR_BLUETOOTH = 2;
    public final static int AR_HEADSET = 3;
    public void addAudioRoutine(int type) {//服用SAgoraUserEvent，一起统计
        SAgoraUserEvent n = new SAgoraUserEvent();
        n.ts = System.currentTimeMillis();
        n.type = EVENT_TYPE_onAudioRouteChanged;
        n.uid = -1;

        switch (type) {
            case AR_HEADSET:
                n.event = new SAgoraUserEvent.AudioRouting(AUDIO_ROUTE_HEADSET);
                break;
            case AR_BLUETOOTH:
                n.event = new SAgoraUserEvent.AudioRouting(AUDIO_ROUTE_HEADSETBLUETOOTH);
                break;
            case AR_PHONE_SPEAKER:
                n.event = new SAgoraUserEvent.AudioRouting(AUDIO_ROUTE_SPEAKERPHONE);
                break;
            default:
                return;
        }

        mItemList.add(n);

    }

    public void addJoinChannelAction(int joinRet) {
        SAgora.SJoinChannelAction n = new SAgora.SJoinChannelAction();
        n.ts = System.currentTimeMillis();
        n.ret = joinRet;

        mItemList.add(n);
    }


    public List<ILogItem> getItemList() {
        return mItemList;
    }

    public SDataHolderEx reset() {
        mItemList.clear();
        return this;
    }


    private final static int LIST_COUNT_THRESHOLD = 50;
    public boolean need2Flush() {
        if (mItemList.size() >= LIST_COUNT_THRESHOLD )
            return true;
        else
            return false;
    }



}