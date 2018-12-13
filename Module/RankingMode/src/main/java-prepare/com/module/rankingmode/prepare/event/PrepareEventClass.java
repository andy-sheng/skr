package com.module.rankingmode.prepare.event;

import com.module.rankingmode.prepare.model.PlayerInfo;

import java.util.List;

public class PrepareEventClass {
    public static class PlayerInfoListEvent {
        private List<PlayerInfo> playerInfoList;

        public PlayerInfoListEvent(List<PlayerInfo> playerInfoList) {
            this.playerInfoList = playerInfoList;
        }

        public List<PlayerInfo> getPlayerInfoList() {
            return playerInfoList;
        }
    }
}
