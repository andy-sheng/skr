package com.mi.live.data.event;

/**
 * Created by zjn on 16-11-30.
 */
public class GiftEventClass {

    public static class GiftMallEvent {
        public int eventType;

        public static final int EVENT_TYPE_GIFT_CACHE_CHANGE = 2;

        public static final int EVENT_TYPE_GIFT_SHOW_MALL_LIST = 3;

        public static final int EVENT_TYPE_GIFT_HIDE_MALL_LIST = 5;


        public static final int EVENT_TYPE_GIFT_PLAY_COMPLETE = 7;

        public static final int EVENT_TYPE_GIFT_GO_RECHARGE = 8;

        public static final int EVENT_TYPE_GIFT_PLAY_BREAK = 9;

        public static final int EVENT_TYPE_CLICK_SELECT_GIFT = 10;

        public GiftMallEvent(int type) {
            this.eventType = type;
        }

        public Object obj1;
        public Object obj2;

        public GiftMallEvent(int type, Object obj1, Object obj2) {
            this.eventType = type;
            this.obj1 = obj1;
            this.obj2 = obj2;
        }
    }

    public static class GiftAttrMessage {
        public static class Normal {
            public Object obj1;

            public Normal(Object obj1) {
                this.obj1 = obj1;
            }
        }

        public static class Big {
            public Object obj1;

            public Big(Object obj1) {
                this.obj1 = obj1;
            }
        }

        public static class LightUp {
            public Object obj1;

            public LightUp(Object obj1) {
                this.obj1 = obj1;
            }
        }

        public static class RoomBackGround {
            public Object obj1;

            public RoomBackGround(Object obj1) {
                this.obj1 = obj1;
            }
        }
        public static class FlyBarrage {
            public Object obj1;

            public FlyBarrage(Object obj1) {
                this.obj1 = obj1;
            }
        }
    }

    public static class GiftCardPush {
        public Object obj1;

        public GiftCardPush(Object obj1) {
            this.obj1 = obj1;
        }
    }
}
