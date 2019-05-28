package com.module.playways.room.room.gift.model;

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
