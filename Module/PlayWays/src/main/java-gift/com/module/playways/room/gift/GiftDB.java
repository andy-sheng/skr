package com.module.playways.room.gift;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity(
        indexes = {
                @Index(value = "giftId DESC", unique = true)
        }
)
public class GiftDB implements Serializable {
    private static final long serialVersionUID = -5809782578272913999L;

    @Id
    private Integer giftId;

@Generated(hash = 2057978490)
public GiftDB(Integer giftId) {
    this.giftId = giftId;
}

@Generated(hash = 2046579016)
public GiftDB() {
}

public Integer getGiftId() {
    return this.giftId;
}

public void setGiftId(Integer giftId) {
    this.giftId = giftId;
}

}

