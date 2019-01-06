package com.module.playways.rank.room.gift.model;

import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.zq.live.proto.Room.PlayerInfo;

public class GiftDataUtils {

    public static boolean sameContinueId(GiftPlayModel model1, GiftPlayModel model2) {
        if (model1 != null && model2 != null) {
            if (model1.getContinueId() == model2.getContinueId()
                    && model1.getSender().getUserId() == model2.getSender().getUserId()) {
                return true;
            }
        }
        return false;
    }
}
