package com.module.playways.room.gift.inter;

import com.module.playways.room.gift.model.BaseGift;

public interface IContinueSendView {
    void buySuccess(BaseGift baseGift, int continueCount);

    void buyFaild(int erroCode, String errorMsg);
}
