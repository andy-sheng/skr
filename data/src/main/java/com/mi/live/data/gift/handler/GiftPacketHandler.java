package com.mi.live.data.gift.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.repository.GiftRepository;
import com.base.log.MyLog;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.callback.MiLinkPacketDispatcher;
import com.wali.live.proto.GiftProto;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.PayProto;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chengsimin on 16/2/22.
 */
public class GiftPacketHandler implements MiLinkPacketDispatcher.PacketDataHandler {
    @Override
    public boolean processPacketData(PacketData data) {
        if (data == null) {
            return false;
        }
        switch (data.getCommand()) {
            case MiLinkCommand.COMMAND_GIFT_GET_LIST: {
                GiftProto.GetGiftListRsp response = null;
                try {
                    response = GiftProto.GetGiftListRsp.parseFrom(data.getData());
                    MyLog.d("GiftPackageHandler", "GetGiftListRsp:" + response);
                } catch (InvalidProtocolBufferException e) {
                }
                GiftRepository.process(response);
            }
            break;
            case MiLinkCommand.COMMAND_GIFT_CARD_PUSH: {
                PayProto.GiftCardPush response = null;
                try {
                    response = PayProto.GiftCardPush.parseFrom(data.getData());
                    MyLog.w("GiftPackageHandler", "GiftCardPush:" + response);
                } catch (InvalidProtocolBufferException e) {
                }
                EventBus.getDefault().post(new GiftEventClass.GiftCardPush(response));
            }
            break;
        }
        return true;
    }

    @Override
    public String[] getAcceptCommand() {
        return new String[]{
                MiLinkCommand.COMMAND_GIFT_GET_LIST,
                MiLinkCommand.COMMAND_GIFT_CARD_PUSH
        };
    }
}
