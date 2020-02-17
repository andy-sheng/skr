package com.zq.engine.avstatistics.datastruct;


import com.zq.engine.avstatistics.SUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;

import static io.agora.rtc.Constants.ADAPT_DOWN_BANDWIDTH;
import static io.agora.rtc.Constants.ADAPT_NONE;
import static io.agora.rtc.Constants.ADAPT_UP_BANDWIDTH;


public class SAgora //all struct related to Agora is defined here!
{
    public static String TAG = "[SDM]";

    //将数值转回为相应的语义字符串
    protected static String transNetQuality(int quality) {
        if (0 == quality) return "未知(0)";
        else if (1 == quality) return "极好";
        else if (2 == quality) return "主观极好，码率略低";
        else if (3 == quality) return "主观有瑕疵，但不影响沟通";
        else if (4 == quality) return "勉强沟通";
        else if (5 == quality) return "质量很差，沟通困难";
        else if (6 == quality) return "网络断开";
        else if (8 == quality) return "检测网络中(8)";
        else return "声网没有定义的质量值value("+quality+")";
    }

    private final static int ASI_GROUP_MAX_CNT = 10;
    public static class SAudioSamplingInfoGroup implements ILogItem {

        public long ts = 0; //统计间隔结束点的时间戳
        public List<SAudioSamplingInfo> infoList = new ArrayList<SAudioSamplingInfo>() ;

        public SAudioSamplingInfoGroup(long ts) {
            this.ts = ts;
        }


        public boolean isFull() {
            if (infoList.size() >= ASI_GROUP_MAX_CNT)
                return true;
            else
                return false;
        }

        public boolean hasData() {
            if (infoList.size() > 0)
                return true;
            else
                return false;
        }


        public void addInfo(SAudioSamplingInfo info) {
            infoList.add(info);
        }

        @Override
        public String toString(){

            int infoCnt = infoList.size();
            if (infoList.size() <= 0)
                return "";

            StringBuilder sb = new StringBuilder();
            SAudioSamplingInfo e = infoList.get(0);


            sb.append(SUtils.transTime(ts));
            sb.append(" SAudioSamplingInfoGroup: infoCnt=").append(infoCnt);
            sb.append(", smpRate=").append(e.smpRate);
            sb.append(", chCnt=").append(e.chCnt).append("\n");

            e = null;
            for (int i=0; i<infoCnt; i++) {
                e = infoList.get(i);
                if (null == e) continue;

                sb.append(TAG).append(i).append("# ");
                sb.append(" smpCnt=").append(e.smpCnt);
                sb.append(", pcmDur=").append(e.pcmDuration);
                sb.append(", maxPCM=").append(e.maxAbsPCM);
                sb.append(", meanPCM=").append(e.meanAbsPCM);
                sb.append(", wSpan=").append(e.statisticSpan);
                sb.append("\n");
            }

            return sb.toString();
        }

        @Override
        public JSONObject toJSONObject() {
            int infoCnt = infoList.size();

            JSONObject jsObj = new JSONObject();

            try {
                jsObj.put("tsStr", SUtils.transTime(ts));
                jsObj.put("tsValue", ts);


                if (infoCnt > 0) {
                    SAudioSamplingInfo info = infoList.get(0);
                    jsObj.put("smpRate", info.smpRate);
                    jsObj.put("chCnt", info.chCnt);
                    jsObj.put("infoCnt", infoCnt);
                }

                JSONArray jsArray = new JSONArray();
                for (int i=0; i<infoCnt; i++) {
                    JSONObject elem = infoList.get(i).toJONSObject4Group();
                    jsArray.put(elem);
                }
                jsObj.put("infos", jsArray);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsObj;
        }


        @Override
        public String getKey() {
            return "SAudioSamplingInfoGroup";
        }


        public void reset() { //TODO: reset可能有用，回头再看

        }
    }

    public static class SAudioSamplingInfo implements ILogItem {
        public long timeStamp; //统计间隔结束点的时间戳

        public long smpRate; //采样率
        public long chCnt; //声道数
        public long smpCnt; //统计间隔内，获取到的总的采样数
        public long pcmDuration; //时间间隔内，总的数据时长
        public long statisticSpan; //实际的统计间隔，与STATISTIC_SPAN_SETTTING会有偏差

        public long maxAbsPCM = -1;
        public long meanAbsPCM= 0;
        public String extraInfo;


        public long totalAbsPCM =0; //用于保留计算的中间值，非log输出

        public final static long STATISTIC_SPAN_SETTTING = 1000; //ms 统计时间间隔的设定值

        public String toString() {

            String additionInfo = null;

            if (null == extraInfo) {
                additionInfo = "";
            }
            else {
                additionInfo = ", extraInfo="+extraInfo;
            }

            return SUtils.transTime(timeStamp) + " SAudioSamplingInfo: smpRate=" + smpRate +
                    ", chCnt=" + chCnt +
                    ", smpCnt=" + smpCnt +
                    ", maxPCM=" + maxAbsPCM +
                    ", meanPCM=" + meanAbsPCM +
                    ", pcmDur=" + pcmDuration +
                    ", wSpan=" + statisticSpan + additionInfo +"\n";
        }


        @Override
        public JSONObject toJSONObject() {
            JSONObject jsObj = new JSONObject();
            try {

                jsObj.put("tsStr", SUtils.transTime(timeStamp));
                jsObj.put("tsValue", timeStamp);

                jsObj.put("smpRate",smpRate);
                jsObj.put("chCnt", chCnt);
                jsObj.put("smpCnt", smpCnt);
                jsObj.put("pcmDur", pcmDuration);
                jsObj.put("wSpan", statisticSpan);
                jsObj.put("maxPCM", maxAbsPCM);
                jsObj.put("meanPCM", meanAbsPCM);
                jsObj.put("extraInfo", extraInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsObj;
        }

        public JSONObject toJONSObject4Group() {
            JSONObject jsObj = new JSONObject();

            try {
                jsObj.put("smpCnt", smpCnt);
                jsObj.put("pcmDur", pcmDuration);
                jsObj.put("wSpan", statisticSpan);
                jsObj.put("maxPCM", maxAbsPCM);
                jsObj.put("meanPCM", meanAbsPCM);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return jsObj;
        }

        @Override
        public String getKey() {
            return "SAudioSamplingInfo";
        }

        public void reset() {
            this.timeStamp = 0;
            this.smpRate = 0;
            this.chCnt = 0;
            this.smpCnt = 0;
            this.pcmDuration = 0;
            this.statisticSpan = 0;
            this.meanAbsPCM = 0;
            this.maxAbsPCM = 0;
            this.totalAbsPCM = 0;
        }

        public SAudioSamplingInfo clone() {
            SAudioSamplingInfo n = new SAudioSamplingInfo();
            n.timeStamp = this.timeStamp;
            n.smpRate = this.smpRate;
            n.chCnt = this.chCnt;
            n.smpCnt = this.smpCnt;
            n.pcmDuration = this.pcmDuration;
            n.statisticSpan = this.statisticSpan;
            n.meanAbsPCM = this.meanAbsPCM;
            n.maxAbsPCM = this.maxAbsPCM;
            n.totalAbsPCM = this.totalAbsPCM;

            return n;
        }

    }


    public static class SRTCStats extends IRtcEngineEventHandler.RtcStats implements ILogItem {
        public long timeStamp;

        public String toString() {
            return SUtils.transTime(timeStamp) + " SRTCStats: totalDuration=" + totalDuration +
                    ", txBytes=" + txBytes +
                    ", rxBytes=" + rxBytes +
                    ", txKBR=" + txKBitRate +
                    ", rxKBR=" + rxKBitRate +
                    ", txAKBR=" + txAudioKBitRate +
                    ", rxAKBR=" + rxAudioKBitRate +
                    ", txVKBR=" + txVideoKBitRate +
                    ", rxVKBR=" + rxVideoKBitRate +
                    ", users=" + users +
                    ", lastmileDelay=" + lastmileDelay +
                    ", txPacketLossRate=" + txPacketLossRate +
                    ", rxPacketLossRate=" + rxPacketLossRate +
                    ", cpuTotalUsage=" + cpuTotalUsage +
                    ", cpuAppUsage=" + cpuAppUsage + "\n";

        }

        @Override
        public JSONObject toJSONObject() {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put("tsStr", SUtils.transTime(timeStamp));
                jsObj.put("tsValue", timeStamp);

                jsObj.put("totalDuration", totalDuration);
                jsObj.put("txBytes", txBytes);
                jsObj.put("rxBytes", rxBytes);
                jsObj.put("txKBR", txKBitRate);
                jsObj.put("rxKBR", rxKBitRate);
                jsObj.put("txAKBR", txAudioKBitRate);
                jsObj.put("rxAKBR", rxAudioKBitRate);
                jsObj.put("txVKBR", txVideoKBitRate);
                jsObj.put("rxVKBR", rxVideoKBitRate);
                jsObj.put("users", users);
                jsObj.put("lastmileDelay", lastmileDelay);
                jsObj.put("txPacketLossRate", rxPacketLossRate);
                jsObj.put("rxPacketLossRate", rxPacketLossRate);
                jsObj.put("cpuTotalUsage", cpuTotalUsage);
                jsObj.put("cpuAppUsage", cpuAppUsage);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsObj;
        }

        @Override
        public String getKey() {
            return "SRTCStats";
        }
    }


    public static class SLocalVideoStats extends IRtcEngineEventHandler.LocalVideoStats implements ILogItem {
        public long timeStamp;

        private String transQualityAdaptIndication(int q) {
            String retStr = "";
            switch (q) {
                case ADAPT_NONE:
                    retStr = "质量不变";
                    break;
                case ADAPT_UP_BANDWIDTH:
                    retStr = "质量改善";
                    break;
                case ADAPT_DOWN_BANDWIDTH:
                    retStr = "质量变差";
                    break;
                default:
                    retStr = "Unknow indication("+q+")";
                    break;
            }
            return retStr;
        }

        public String toString() {
            return SUtils.transTime(timeStamp)+" SLocalVideoStats: sentBR=" + sentBitrate +
                    ", sentFPS=" + sentFrameRate+
                    ", encOutFPS=" + encoderOutputFrameRate+
                    ", rendererOutFPS=" + rendererOutputFrameRate+
                    ", targetBR=" + targetBitrate+
                    ", targetFPS=" + targetFrameRate+
                    ", quality=" + transQualityAdaptIndication(qualityAdaptIndication)+"\n";
        }

        @Override
        public JSONObject toJSONObject() {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put("tsStr", SUtils.transTime(timeStamp));
                jsObj.put("tsValue", timeStamp);

                jsObj.put("sentBR", sentBitrate);
                jsObj.put("sentFPS", sentFrameRate);
                jsObj.put("encOutFPS", encoderOutputFrameRate);
                jsObj.put("rendererOutFPS", rendererOutputFrameRate);
                jsObj.put("targetBR", targetBitrate);
                jsObj.put("targetFPS", targetFrameRate);
                jsObj.put("qltID", qualityAdaptIndication);
                jsObj.put("qltStr", transQualityAdaptIndication(qualityAdaptIndication));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsObj;
        }

        @Override
        public String getKey() {
            return "SLocalVideoStats'";
        }
    }

    public static class SRemoteAudioStats extends IRtcEngineEventHandler.RemoteAudioStats implements ILogItem {
        public long timeStamp;
//        public String strQuality; //replace base class's "quality" for explicitly meanning

        public String toString() {

            return SUtils.transTime(timeStamp)+" SRemoteAudioStats: uid=" + uid +
                    ", qlt=" + transNetQuality(quality) +
                    ", networkDelay=" + networkTransportDelay+
                    ", JBDelay=" + jitterBufferDelay+
                    ", ALossRate=" + audioLossRate+"\n";
        }

        @Override
        public JSONObject toJSONObject() {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put("tsStr", SUtils.transTime(timeStamp));
                jsObj.put("tsValue", timeStamp);

                jsObj.put("uid", uid);
                jsObj.put("qltID", quality);
                jsObj.put("qltStr", transNetQuality(quality));
                jsObj.put("networkDelay", networkTransportDelay);
                jsObj.put("JBDelay", jitterBufferDelay);
                jsObj.put("ALossRate", audioLossRate);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsObj;
        }

        @Override
        public String getKey() {
            return "SRemoteAudioStats";
        }
    }

    public static class SRemoteVideoStats extends IRtcEngineEventHandler.RemoteVideoStats implements ILogItem {
        public long timeStamp;
        public String toString() {
            return SUtils.transTime(timeStamp)+" SRemoteVideoStats: uid="+ uid +
                    ", w=" +width+
                    ", h=" +height+
                    ", receivedBR=" +receivedBitrate+
                    ", decOutFPS=" +decoderOutputFrameRate+
                    ", rendererOutFPS=" +rendererOutputFrameRate+
                    ", rxStreamType=" +rxStreamType+"\n";
        }

        @Override
        public JSONObject toJSONObject() {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put("tsStr", SUtils.transTime(timeStamp));
                jsObj.put("tsValue", timeStamp);

                jsObj.put("uid", uid);
                jsObj.put("w", width);
                jsObj.put("h", height);
                jsObj.put("receivedBR", receivedBitrate);
                jsObj.put("decOutFPS", decoderOutputFrameRate);
                jsObj.put("rendererOutFPS", rendererOutputFrameRate);
                jsObj.put("rxStreamType", rxStreamType);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsObj;
        }

        @Override
        public String getKey() {
            return "SRemoteVideoStats";
        }
    }


    public static class SRemoteVideoTransportStat implements ILogItem {
        public long timeStamp;
        public int 	uid;
        public int 	delay;
        public int 	lost;
        public int 	rxKBitRate;

        public String toString() {
            return SUtils.transTime(timeStamp)+" SRemoteVideoTransportStat: uid="+uid+"， delay="+delay+"， lost="+lost+"， rxKBitRate="+rxKBitRate + "\n";
        }

        @Override
        public JSONObject toJSONObject() {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put("tsStr", SUtils.transTime(timeStamp));
                jsObj.put("tsValue", timeStamp);

                jsObj.put("uid", uid);
                jsObj.put("delay", delay);
                jsObj.put("lost", lost);
                jsObj.put("rxKBR", rxKBitRate);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsObj;
        }

        @Override
        public String getKey() {
            return "SRemoteVideoTransportStat";
        }
    }


    public static class SRemoteAudioTransportStats implements ILogItem {
        public long timeStamp;
        public int 	uid;
        public int 	delay;
        public int 	lost;
        public int 	rxKBitRate;

        public String toString(){
            return SUtils.transTime(timeStamp)+" SRemoteAudioTransportStats: uid="+uid+"， delay="+delay+"， lost="+lost+"， rxKBitRate="+rxKBitRate + "\n";
        }

        @Override
        public JSONObject toJSONObject() {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put("tsStr", SUtils.transTime(timeStamp));
                jsObj.put("tsValue", timeStamp);

                jsObj.put("uid", uid);
                jsObj.put("delay", delay);
                jsObj.put("lost", lost);
                jsObj.put("rxKBR", rxKBitRate);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsObj;
        }

        @Override
        public String getKey() {
            return "SRemoteAudioTransportStats";
        }
    }

    public static class SNetworkQuality implements ILogItem {
        public long timeStamp;
        public int  uid = -1;
        public int 	txQuality=0;
        public int 	rxQuality=0;



        public String toString(){

            return SUtils.transTime(timeStamp)+" SNetworkQuality: uid="+uid+", txQuality="+transNetQuality(txQuality)+", rxQuality="+transNetQuality(rxQuality) + "\n";
        }

        @Override
        public JSONObject toJSONObject() {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put("tsStr", SUtils.transTime(timeStamp));
                jsObj.put("tsValue", timeStamp);

                jsObj.put("uid", uid);
                jsObj.put("txQlt", txQuality);
                jsObj.put("rxQlt", rxQuality);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsObj;
        }

        @Override
        public String getKey() {
            return "SNetworkQuality";
        }
    } //end of class SAgora.SNetworkQuality


    public static class SJoinChannelAction implements ILogItem{
        public long ts = 0;
        public long ret= 0;
        public long hasServerConfig = 0;
        public long isExternalAudio = 0;
        public long isOpenSL = 0;
        public long audioPreview = 0;

        public String toString(){

            return SUtils.transTime(ts)+" SJoinChannelAction: ret="+ret+"\n";
        }

        @Override
        public JSONObject toJSONObject() {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put("tsStr", SUtils.transTime(ts));
                jsObj.put("tsValue", ts);

                jsObj.put("ret", ret);

                jsObj.put("hasServerConf", hasServerConfig);
                jsObj.put("extAudio", isExternalAudio);
                jsObj.put("openSL", isOpenSL);
                jsObj.put("audioPreview", audioPreview);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsObj;
        }

        @Override
        public String getKey() {
            return "SJoinChannelAction";
        }
    }



}
