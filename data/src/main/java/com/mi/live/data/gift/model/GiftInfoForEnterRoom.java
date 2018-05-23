package com.mi.live.data.gift.model;

import com.wali.live.proto.EffectProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin on 16-7-26.
 *
 * @module 礼物
 */
public class GiftInfoForEnterRoom {

    private List<Long> enterRoomTicketTop10lList = new ArrayList<>();

    private List<GiftRecvModel> enterRoomGiftRecvModelList = new ArrayList<>();

    private int giftUsableGmCount;

    private int initStarStickCount;

    private List<Integer> mPktGiftIds = new ArrayList<>();

    public int getInitStarStickCount() {
        return initStarStickCount;
    }

    public List<GiftRecvModel> getEnterRoomGiftRecvModelList() {
        return enterRoomGiftRecvModelList;
    }

    public List<Long> getEnterRoomTicketTop10lList() {
        return enterRoomTicketTop10lList;
    }

    public int getGiftUsableGmCount() {
        return giftUsableGmCount;
    }

    public GiftInfoForThisRoom getmGiftInfoForThisRoom() {
        return mGiftInfoForThisRoom;
    }

    public List<Integer> getPktGiftId(){
        return mPktGiftIds;
    }

    /**
     * 该房间礼物信息
     */
    private GiftInfoForThisRoom mGiftInfoForThisRoom;


    public static GiftInfoForEnterRoom loadFromPB(EffectProto.GetRoomEffectsResponse rsp){
        GiftInfoForEnterRoom giftInfoForEnterRoom = new GiftInfoForEnterRoom();
        giftInfoForEnterRoom.enterRoomGiftRecvModelList.addAll(parseList(rsp.getBgEffectsList()));
        giftInfoForEnterRoom.enterRoomGiftRecvModelList.addAll(parseList(rsp.getLightEffectsList()));
        giftInfoForEnterRoom.enterRoomGiftRecvModelList.addAll(parseList(rsp.getGlobalEffectsList()));

        giftInfoForEnterRoom.mGiftInfoForThisRoom = GiftInfoForThisRoom.loadFromPB(rsp);

        giftInfoForEnterRoom.giftUsableGmCount = rsp.getUsableGemCnt();

        giftInfoForEnterRoom.initStarStickCount = rsp.getRoomOpenedTicketCnt();

        giftInfoForEnterRoom.mPktGiftIds.addAll(parsePktList(rsp.getUseableGiftCardListList()));

        List<EffectProto.RankItem> rank10List = rsp.getTop10ItemsList();
        if (rank10List != null) {
            for(EffectProto.RankItem item : rank10List){
                giftInfoForEnterRoom.enterRoomTicketTop10lList.add(item.getUuid());
            }
        }
        return giftInfoForEnterRoom;
    }


    private static List<GiftRecvModel> parseList(List<EffectProto.GiftEffect> list) {
        List<GiftRecvModel> resultList = new ArrayList<>();
        if (list != null) {
            for (EffectProto.GiftEffect effect : list) {
                resultList.add(GiftRecvModel.loadFromPB(effect));
            }
        }
        return resultList;
    }

    private static List<Integer> parsePktList(List<EffectProto.GiftObj> list) {
        List<Integer> resultList = new ArrayList<>();
        if (list != null) {
            for (EffectProto.GiftObj giftObj : list) {
                resultList.add(giftObj.getGiftId());
            }
        }
        return resultList;
    }

}
