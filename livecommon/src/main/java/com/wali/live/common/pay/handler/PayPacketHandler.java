package com.wali.live.common.pay.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.milink.sdk.aidl.PacketData;
import com.base.log.MyLog;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.callback.MiLinkPacketDispatcher;
import com.wali.live.common.pay.manager.PayManager;
import com.wali.live.proto.PayProto;

/**
 * Created by chengsimin on 16/2/22.
 */
public class PayPacketHandler implements MiLinkPacketDispatcher.PacketDataHandler {
    @Override
    public boolean processPacketData(PacketData data) {
        if (data == null) {
            return false;
        }
        switch (data.getCommand()) {
            case MiLinkCommand.COMMAND_PAY_PRICE_LIST: {
                PayProto.GetGemPriceResponse response = null;
                try {
                    response = PayProto.GetGemPriceResponse.parseFrom(data.getData());
                } catch (InvalidProtocolBufferException e) {
                }
                MyLog.d("PayPacketHandler", "response:" + response);
                PayManager.process(response);
            }
            break;
            case MiLinkCommand.COMMAND_PAY_GET_RED_ICON: {
                PayProto.GetRedPointConfigResponse response = null;
                try {
                    response = PayProto.GetRedPointConfigResponse.parseFrom(data.getData());
                } catch (InvalidProtocolBufferException e) {
                }
                MyLog.d("PayPacketHandler", "response:" + response);
                PayManager.processRedPoint(response);
            }
            break;
        }
        return true;
    }

    @Override
    public String[] getAcceptCommand() {
        return new String[]{
                MiLinkCommand.COMMAND_PAY_PRICE_LIST,
                MiLinkCommand.COMMAND_PAY_GET_RED_ICON
        };
    }
}
