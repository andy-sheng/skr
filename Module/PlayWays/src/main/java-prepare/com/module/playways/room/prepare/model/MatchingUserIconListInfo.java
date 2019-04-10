package com.module.playways.room.prepare.model;

import java.io.Serializable;
import java.util.List;

public class MatchingUserIconListInfo implements Serializable {

    private List<MatchIconModel> players;

    public List<MatchIconModel> getPlayers() {
        return players;
    }

    public void setPlayers(List<MatchIconModel> players) {
        this.players = players;
    }
}
