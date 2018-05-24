package com.mi.live.data.repository.model.turntable;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.wali.live.proto.BigTurnTableProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhujianning on 18-4-16.
 * 转盘信息model
 */

public class TurnTablePreConfigModel {
    private BigTurnTableProto.TurntableType type;
    private int startCost;
    private int drawCost;
    List<PrizeItemModel> prizeItems;

    public TurnTablePreConfigModel(BigTurnTableProto.TurntablePreConfig data) {
        if(data == null) {
            return;
        }

        this.type = data.getType();
        this.startCost = data.getStartCost();
        this.drawCost = data.getDrawCost();

        List<BigTurnTableProto.PrizeItem> prizeItemList = data.getPrizeItemList();
        if(prizeItemList != null && !prizeItemList.isEmpty()) {
            for(BigTurnTableProto.PrizeItem item : prizeItemList) {
                addPrizeItem(new PrizeItemModel(item));
            }
        }
    }

    public TurnTablePreConfigModel() {

    }

    public void rotateBmp() {
        if(prizeItems != null && !prizeItems.isEmpty()) {
            for(int i = 0; i < prizeItems.size(); i++) {

                Bitmap drawable = prizeItems.get(i).getDrawable();
                if(drawable != null) {
                    int ww = drawable.getWidth();
                    int hh = drawable.getHeight();
                    Matrix matrix = new Matrix();
                    matrix.postScale(1f, 1f);
                    matrix.postRotate(360 / prizeItems.size() * i + 10);
                    drawable = Bitmap.createBitmap(drawable, 0, 0, ww, hh,
                            matrix, true);
                    prizeItems.get(i).setBmp(drawable);
                }
            }
        }
    }


    public BigTurnTableProto.TurntableType getType() {
        return type;
    }

    public void setType(BigTurnTableProto.TurntableType type) {
        this.type = type;
    }

    public int getStartCost() {
        return startCost;
    }

    public void setStartCost(int startCost) {
        this.startCost = startCost;
    }

    public int getDrawCost() {
        return drawCost;
    }

    public void setDrawCost(int drawCost) {
        this.drawCost = drawCost;
    }

    public List<PrizeItemModel> getPrizeItems() {
        return prizeItems;
    }

    public void setPrizeItems(List<PrizeItemModel> prizeItems) {
        this.prizeItems = prizeItems;
    }

    public void addPrizeItem(PrizeItemModel prizeItem) {
        if(prizeItem == null) {
            return;
        }

        if(prizeItems == null) {
            prizeItems = new ArrayList<>();
        }

        prizeItems.add(prizeItem);
    }

    @Override
    public String toString() {
        return "TurnTablePreConfigModel{" +
                "type=" + type +
                ", startCost=" + startCost +
                ", drawCost=" + drawCost +
                ", prizeItems=" + prizeItems +
                '}';
    }
}
