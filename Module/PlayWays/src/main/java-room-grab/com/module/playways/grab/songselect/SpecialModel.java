package com.module.playways.grab.songselect;

import java.io.Serializable;

public class SpecialModel implements Serializable {
    int id;
    String specialName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSpecialName() {
        return specialName;
    }

    public void setSpecialName(String specialName) {
        this.specialName = specialName;
    }
}
