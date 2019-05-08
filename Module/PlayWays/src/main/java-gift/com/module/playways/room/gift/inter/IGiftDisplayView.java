package com.module.playways.room.gift.inter;

import com.module.playways.room.gift.model.BaseGift;

import java.util.HashMap;
import java.util.List;

public interface IGiftDisplayView {
    void showGift(HashMap<Integer, List<BaseGift>> baseGiftCollection);

    void getGiftListFaild();
}
