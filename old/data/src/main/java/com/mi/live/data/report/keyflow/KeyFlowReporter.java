package com.mi.live.data.report.keyflow;

import android.text.TextUtils;

import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.report.IReporter;
import com.base.log.MyLog;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.StatReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by yangli on 16-7-19.
 */
public class KeyFlowReporter implements IReporter {
    private static final String TAG = "KeyFlowReporter";

    private static final String SPLITTER = " ";

    private void addElemToJsonObj(JSONObject jsonObject, String key, Object val) throws JSONException{
        if (jsonObject == null) {
            return;
        }
        jsonObject.put(key, val);
    }

    public boolean reportFile(File file) {
        byte[] data = parseFile(file);
        if (data != null) {
            return reportByMiLink(data);
        } else {
            return false;
        }
    }

    private String getReportType(String filename) {
        if (!TextUtils.isEmpty(filename)) {
            if (filename.contains(KeyFlowReportManager.TYPE_LIVE)) {
                return KeyFlowProtocol.KEY_PUSH;
            } else if (filename.contains(KeyFlowReportManager.TYPE_WATCH)) {
                return KeyFlowProtocol.KEY_PULL;
            } else if (filename.contains(KeyFlowReportManager.TYPE_REPLAY)) {
                return KeyFlowProtocol.KEY_PLAYBACK;
            }
        }
        return "";
    }

    private byte[] parseFile(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        StatReport.StatInfo.Builder builder = StatReport.StatInfo.newBuilder();
        builder.setName(getReportType(file.getName()));
        JSONArray infoJsonArray = new JSONArray();
        JSONArray stutterJsonArray = new JSONArray();
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            JSONObject infoJsonObject = null;
            JSONObject stutterJsonObject = null;
            boolean hasErrNo = false;
            while (line != null) {
                if (TextUtils.isEmpty(line)) {
                    line = bufferedReader.readLine();
                    continue;
                }
                String[] result = line.split(SPLITTER);
                switch (result[0]) {
                    /** info 字段 */
                    case KeyFlowProtocol.KEY_IP:
                        if (infoJsonObject != null && infoJsonObject.optLong(KeyFlowProtocol.KEY_END_TIME, 0) == 0) {
                            addElemToJsonObj(infoJsonObject, KeyFlowProtocol.KEY_END_TIME, result[3]);
                        }
                        infoJsonObject = new JSONObject();
                        addElemToJsonObj(infoJsonObject, KeyFlowProtocol.KEY_IP, result[1]);
                        addElemToJsonObj(infoJsonObject, KeyFlowProtocol.KEY_FLAG, result[2]);
                        addElemToJsonObj(infoJsonObject, KeyFlowProtocol.KEY_BEGIN_TIME, result[3]);
                        addElemToJsonObj(infoJsonObject, KeyFlowProtocol.KEY_END_TIME, 0);
                        addElemToJsonObj(infoJsonObject, KeyFlowProtocol.KEY_STATUS, 1); // status默认为失败，值为1
                        infoJsonArray.put(infoJsonObject);
                        break;
                    // case KeyFlowProtocol.KEY_IP_BEGIN:
                    //     addElemToJsonObj(infoJsonObject, KeyFlowProtocol.KEY_BEGIN_TIME, result[1]);
                    //     break;
                    // case KeyFlowProtocol.KEY_IP_END:
                    //     addElemToJsonObj(infoJsonObject, KeyFlowProtocol.KEY_END_TIME, result[1]);
                    //    break;
                    case KeyFlowProtocol.KEY_STATUS:
                        addElemToJsonObj(infoJsonObject, KeyFlowProtocol.KEY_STATUS, result[1]);
                        break;
                    /** 非info中需要特殊处理的字段，这些字段在写文件时进行了自定义的处理 */
                    case KeyFlowProtocol.KEY_STUTTER_BEGIN:
                        stutterJsonObject = new JSONObject();
                        addElemToJsonObj(stutterJsonObject, KeyFlowProtocol.KEY_BEGIN_TIME, result[1]);
                        stutterJsonArray.put(stutterJsonObject);
                        break;
                    case KeyFlowProtocol.KEY_STUTTER_END:
                        addElemToJsonObj(stutterJsonObject, KeyFlowProtocol.KEY_END_TIME, result[1]);
                        break;
                    case KeyFlowProtocol.KEY_ERRNO:
                        hasErrNo = true;
                        builder.addItem(StatReport.StatItem.newBuilder().setKey(KeyFlowProtocol.KEY_ERRNO).setVal(result[1]));
                        builder.addItem(StatReport.StatItem.newBuilder().setKey(KeyFlowProtocol.KEY_ERROR).setVal(result.length > 2 ? result[2] : ""));
                        break;
                    case KeyFlowProtocol.KEY_END_TIME:
                        MyLog.w(TAG, result[0] + " " + result[1]);
                        if (infoJsonObject != null && infoJsonObject.optLong(KeyFlowProtocol.KEY_END_TIME, 0) == 0) {
                            addElemToJsonObj(infoJsonObject, KeyFlowProtocol.KEY_END_TIME, result[1]);
                        }
                        builder.addItem(StatReport.StatItem.newBuilder().setKey(result[0]).setVal(result[1]));
                        break;
                    case KeyFlowProtocol.KEY_BEGIN_TIME: // fall through
                    case KeyFlowProtocol.KEY_CREATE_ROOM:
                    case KeyFlowProtocol.KEY_ENGINE_INIT:
                    case KeyFlowProtocol.KEY_DNS_PARSE:
                        MyLog.w(TAG, result[0] + " " + result[1]);
                    default:
                        builder.addItem(StatReport.StatItem.newBuilder().setKey(result[0]).setVal(result[1]));
                        break;
                }
                line = bufferedReader.readLine();
            }
            if (!hasErrNo) {
                builder.addItem(StatReport.StatItem.newBuilder().setKey(KeyFlowProtocol.KEY_ERRNO).setVal("" + KeyFlowProtocol.ERR_CODE_UNKNOWN));
                builder.addItem(StatReport.StatItem.newBuilder().setKey(KeyFlowProtocol.KEY_ERROR).setVal("msg_unknown_error"));
            }
            builder.addItem(StatReport.StatItem.newBuilder().setKey(KeyFlowProtocol.KEY_INFO).setVal(infoJsonArray.toString()));
            builder.addItem(StatReport.StatItem.newBuilder().setKey(KeyFlowProtocol.KEY_STUTTER).setVal(stutterJsonArray.toString()));
            StatReport.StatInfo statInfo = builder.build();
            MyLog.d(TAG, "parseFile statInfo=" + statInfo.toString());
            return statInfo.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public boolean reportByMiLink(byte[] data) {
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_STAT_REPORT);
        packetData.setData(data);
        PacketData response = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        return response != null && response.getMnsCode() == 0;
    }

}
