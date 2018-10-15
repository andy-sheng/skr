package com.common.milink.callback;

import android.os.Message;
import android.os.Process;

import com.common.log.MyLog;
import com.mi.milink.sdk.aidl.PacketData;
import com.mi.milink.sdk.base.CustomHandlerThread;
import com.mi.milink.sdk.client.IPacketListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * reconstruct by chengsimin on 01/07/16
 */
public class MiLinkPacketDispatcher extends CustomHandlerThread implements IPacketListener {
    private static final String TAG = MiLinkPacketDispatcher.class.getSimpleName();

    private static final int MESSAGE_ADD_PACKET_DATA_HANDLER = 0;
    private static final int MESSAGE_REMOVE_PACKET_DATA_HANDLER = 1;
    private static final int MESSAGE_PROCESS_PACKET_DATA = 2;

    private HashMap<String, Set<PacketDataHandler>> mPacketDataHandlerMap = new HashMap<>();

    public MiLinkPacketDispatcher() {
        super(TAG, Process.THREAD_PRIORITY_URGENT_AUDIO);
    }

    public void addPacketDataHandler(PacketDataHandler handler) {
        if (handler != null) {
            Message msg = obtainMessage();
            msg.what = MESSAGE_ADD_PACKET_DATA_HANDLER;
            msg.obj = handler;
            sendMessage(msg);
        }
    }

    public void removePacketDataHandler(PacketDataHandler handler) {
        if (handler != null) {
            Message msg = obtainMessage();
            msg.what = MESSAGE_REMOVE_PACKET_DATA_HANDLER;
            msg.obj = handler;
            sendMessage(msg);
        }
    }

    private void notifyAllPacketDataHandler(PacketData data) {
        Set<PacketDataHandler> set = mPacketDataHandlerMap.get(data.getCommand());
        if (set != null) {
            for (PacketDataHandler handler : set) {
                handler.processPacketData(data);
            }
        }
    }

    //add by mk
    @Override
    protected void processMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_ADD_PACKET_DATA_HANDLER: {
                PacketDataHandler packetDataHandler = (PacketDataHandler) msg.obj;
                if (packetDataHandler != null) {
                    for (String command : packetDataHandler.getAcceptCommand()) {
                        if (mPacketDataHandlerMap.containsKey(command)) {
                            Set set = mPacketDataHandlerMap.get(command);
                            set.add(packetDataHandler);
                        } else {
                            Set set = new HashSet<>();
                            set.add(packetDataHandler);
                            mPacketDataHandlerMap.put(command, set);
                        }
                    }
                }
            }
            break;
            case MESSAGE_REMOVE_PACKET_DATA_HANDLER: {
                PacketDataHandler packetDataHandler = (PacketDataHandler) msg.obj;
                if (packetDataHandler != null) {
                    for (String command : mPacketDataHandlerMap.keySet()) {
                        Set set = mPacketDataHandlerMap.get(command);
                        set.remove(packetDataHandler);
                    }
                }
            }
            break;
            case MESSAGE_PROCESS_PACKET_DATA: {
                List<PacketData> dataList = (List<PacketData>) msg.obj;
                for (PacketData data : dataList) {
                    notifyAllPacketDataHandler(data);
                }
            }
            break;
        }
    }

    public void processReceivePacket(final List<PacketData> dataList) {
        if (null != dataList) {
            MyLog.v(TAG + "  processReceivePacket dataList.size=" + dataList.size());
            Message msg = obtainMessage();
            msg.what = MESSAGE_PROCESS_PACKET_DATA;
            msg.obj = dataList;
            sendMessage(msg);
        } else {
            MyLog.v(TAG + " processReceivePacket dataList is null");
        }
    }


    public interface PacketDataHandler {
        boolean processPacketData(final PacketData data);

        // added by csm
        String[] getAcceptCommand();
    }

    @Override
    public void onReceive(ArrayList<PacketData> arrayList) {
        processReceivePacket(arrayList);
    }
}
