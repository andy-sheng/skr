package com.mi.live.data.gift.model;

/**
 * Created by chengsimin on 16/6/15.
 *
 * @module 礼物
 */
public class GiftType {
    public static final int NORMAL_GIFT = 0;
    public static final int BARRAGE_GIFT = 1;
    public static final int RED_ENVELOPE_GIFT_OLD = 2;
    public static final int ROOM_BACKGROUND_GIFT = 3;
    public static final int LIGHT_UP_GIFT = 4;
    public static final int GLOBAL_GIFT = 5;
    public static final int NORMAL_EFFECTS_GIFT = 6;
    public static final int HIGH_VALUE_GIFT = 7;
    public static final int PECK_OF_GIFT = 8;
    public static final int RED_ENVELOPE_GIFT = 9;
    public static final int BIG_PACK_OF_GIFT = 10;
    public static final int Mi_COIN_GIFT = 11;
    public static final int PRIVILEGE_GIFT = 12;

    /**
     * 门票也是一种礼物
     */
    public static final int TICKET_GIFT = 13;
    public static final int MAGIC_GIFT = 15;
    public static final int OPERATION_GIFT = 16;

    /**
     * @notice type=16  {@link com.mi.live.data.repository.GiftRepository#GIFT_CATEGORY_VFANS_PRIVILEGE}.
     */
}
