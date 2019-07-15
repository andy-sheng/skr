package com.module.playways.doubleplay.pushEvent;

import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.ChoiceGameItemMsg;

import java.util.ArrayList;
import java.util.List;

public class DoubleChoiceGameItemEvent {
    public BasePushInfo mBasePushInfo;

    private int userID;

    private int panelSeq;

    private int itemID;

    List<Integer> itemIDs;

    public int getUserID() {
        return userID;
    }

    public int getPanelSeq() {
        return panelSeq;
    }

    public int getItemID() {
        return itemID;
    }

    public DoubleChoiceGameItemEvent(BasePushInfo basePushInfo, ChoiceGameItemMsg choiceGameItemMsg) {
        mBasePushInfo = basePushInfo;
        userID = choiceGameItemMsg.getUserID();
        panelSeq = choiceGameItemMsg.getPanelSeq();
        itemID = choiceGameItemMsg.getItemID();

        if (choiceGameItemMsg.hasItemIDsList()) {
            itemIDs = new ArrayList<>();
            for (Integer integer : choiceGameItemMsg.getItemIDsList()) {
                itemIDs.add(integer);
            }
        }
    }
}
