package com.mi.live.data.event;

import com.mi.live.data.repository.model.turntable.TurnTableConfigModel;

/**
 * Created by zhujianning on 18-4-17.
 */

public class TurnTableEvent {
    private TurnTableConfigModel data;

    public TurnTableEvent(TurnTableConfigModel data) {
        this.data = data;
    }

    public TurnTableConfigModel getData() {
        return data;
    }

    public void setData(TurnTableConfigModel data) {
        this.data = data;
    }
}
