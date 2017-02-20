package com.mi.live.data.gift.model;

import android.util.SparseArray;

import com.base.log.MyLog;
import com.wali.live.proto.EffectProto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by chengsimin on 16/7/27.
 */
public class GiftInfoForThisRoom {
    private static final String TAG = "GiftInfoForThisRoom";
    // 是否奏效
    private boolean mEnable = false;

    private HashSet<Integer> mDisplayGiftSet = new HashSet<>();

    private long updateGiftCardTs = 0;

    private SparseArray<GiftCard> mGiftCardMap = new SparseArray();

    public boolean enable() {
        return mEnable;
    }

    public GiftCard getGiftCardById(int id) {
        return mGiftCardMap.get(id);
    }

    public static GiftInfoForThisRoom loadFromPB(EffectProto.GetRoomEffectsResponse rsp) {
        GiftInfoForThisRoom giftInfoForThisRoom = new GiftInfoForThisRoom();
        giftInfoForThisRoom.mEnable = rsp.getIsSpecialGiftList();
        {
            List<EffectProto.GiftObj> list = rsp.getGiftListList();
            for (EffectProto.GiftObj obj : list) {
                giftInfoForThisRoom.mDisplayGiftSet.add(obj.getGiftId());
            }
        }
        {
            List<GiftCard> temp = new ArrayList<>();
            List<EffectProto.GiftCard> list = rsp.getGiftCardListList();
            for (EffectProto.GiftCard card : list) {
                temp.add(GiftCard.loadFromPB(card));
            }
            giftInfoForThisRoom.updateGiftCard(temp,rsp.getTimestamp());
        }
        return giftInfoForThisRoom;
    }

    public boolean  needShow(int giftId) {
        return mDisplayGiftSet.contains(giftId);
    }

    // 不改变引用
    private void put(int id,GiftCard newcard){
        MyLog.d(TAG,"put id:"+id+",newcard:"+newcard);
        if(newcard==null){
            return;
        }
        GiftCard card = mGiftCardMap.get(id);
        if(card==null){
            mGiftCardMap.put(id,newcard);
        }else{
            GiftCard.copy(newcard,card);
        }
    }

    public void updateGiftCard(List<GiftCard> giftCardsList,long ts) {
        MyLog.d(TAG,"updateGiftCard:"+ts);
        if(giftCardsList != null){
            if(ts>updateGiftCardTs){
                for(GiftCard card:giftCardsList){
                    put(card.getGiftId(),card);
                }
                updateGiftCardTs = ts;
            }
        }
    }
}
