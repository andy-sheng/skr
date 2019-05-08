package com.module.playways.room.gift.inter;

import com.module.playways.room.gift.model.BaseGift;

import java.util.HashMap;
import java.util.List;

public interface IGiftView {
    void showGift(HashMap<Integer, List<BaseGift>> baseGiftCollection);
}
