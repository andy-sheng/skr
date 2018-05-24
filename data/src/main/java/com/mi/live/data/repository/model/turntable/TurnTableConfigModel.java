package com.mi.live.data.repository.model.turntable;

import com.wali.live.proto.BigTurnTableProto;

/**
 * Created by zhujianning on 18-4-16.
 * 观众端的model
 */

public class TurnTableConfigModel {
    private TurnTablePreConfigModel turnTablePreConfigModel;
    private int status;
    private BigTurnTableProto.TurntableType type;

    public TurnTableConfigModel(BigTurnTableProto.TurntableConfig data) {
        if(data == null) {
            return;
        }

        this.status = data.getStatus();
        this.turnTablePreConfigModel = new TurnTablePreConfigModel(data.getPreConfig());
        this.type = turnTablePreConfigModel.getType();
    }

    public TurnTableConfigModel() {

    }


    public TurnTablePreConfigModel getTurnTablePreConfigModel() {
        return turnTablePreConfigModel;
    }

    public void setTurnTablePreConfigModel(TurnTablePreConfigModel turnTablePreConfigModel) {
        this.turnTablePreConfigModel = turnTablePreConfigModel;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public BigTurnTableProto.TurntableType getType() {
        return type;
    }

    public void setType(BigTurnTableProto.TurntableType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TurnTableConfigModel{" +
                "turnTablePreConfigModel=" + turnTablePreConfigModel +
                ", status=" + status +
                ", type=" + type +
                '}';
    }
}
