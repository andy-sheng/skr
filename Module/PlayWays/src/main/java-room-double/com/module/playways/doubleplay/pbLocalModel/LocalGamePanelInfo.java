package com.module.playways.doubleplay.pbLocalModel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zq.live.proto.Common.GameItemInfo;
import com.zq.live.proto.Common.GamePanelInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LocalGamePanelInfo implements Serializable {
    private int panelSeq;

    private List<LocalGameItemInfo> items;

    public int getPanelSeq() {
        return panelSeq;
    }

    public List<LocalGameItemInfo> getItems() {
        return items;
    }

    public static LocalGamePanelInfo pb2LocalModel(GamePanelInfo gamePanelInfo) {
        LocalGamePanelInfo localGamePanelInfo = new LocalGamePanelInfo();
        if (gamePanelInfo.hasPanelSeq()) {
            localGamePanelInfo.panelSeq = gamePanelInfo.getPanelSeq();
        }

        if (gamePanelInfo.hasItemsList()) {
            localGamePanelInfo.items = new ArrayList<>();
            for (GameItemInfo gameItemInfo : gamePanelInfo.getItemsList()) {
                localGamePanelInfo.items.add(new LocalGameItemInfo(gameItemInfo.getItemID(), gameItemInfo.getItemDesc()));
            }
        }

        return localGamePanelInfo;
    }

    public static LocalGamePanelInfo json2LocalModel(JSONObject jsonObject) {
        LocalGamePanelInfo localGamePanelInfo = new LocalGamePanelInfo();
        localGamePanelInfo.panelSeq = jsonObject.getIntValue("panelSeq");
        List<LocalGameItemInfo> list = JSON.parseArray(jsonObject.getString("items"), LocalGameItemInfo.class);
        localGamePanelInfo.items = list;

        return localGamePanelInfo;
    }

    @Override
    public String toString() {
        return "LocalGamePanelInfo{" +
                "panelSeq=" + panelSeq +
                ", items=" + items +
                '}';
    }
}
