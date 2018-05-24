package com.mi.live.data.gift.mapper;

import com.base.log.MyLog;
import com.mi.live.data.gift.model.GiftType;
import com.mi.live.data.gift.model.giftEntity.BigAnimationGift;
import com.mi.live.data.gift.model.giftEntity.BigPackOfGift;
import com.mi.live.data.gift.model.giftEntity.ExpressionGift;
import com.mi.live.data.gift.model.giftEntity.LightUpGift;
import com.mi.live.data.gift.model.giftEntity.NormalEffectGift;
import com.mi.live.data.gift.model.giftEntity.NormalGift;
import com.mi.live.data.gift.model.giftEntity.PeckOfGift;
import com.mi.live.data.gift.model.giftEntity.RoomEffectGift;
import com.wali.live.dao.Gift;
import com.wali.live.proto.GiftProto;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by chengsimin on 16/7/2.
 *
 * @module 礼物
 */
public class GiftTypeMapper {
    private static final String TAG = "GiftTypeMapper";

    public static Gift loadExactGift(Gift baseGift) {
        Gift gift;
        MyLog.d(TAG, "loadExactGift");
        switch (baseGift.getCatagory()) {
            case GiftType.NORMAL_EFFECTS_GIFT: {
                gift = new NormalEffectGift();
            }
            break;
            case GiftType.ROOM_BACKGROUND_GIFT: {
                gift = new RoomEffectGift();
            }
            break;
            case GiftType.LIGHT_UP_GIFT: {
                gift = new LightUpGift();
            }
            break;
            case GiftType.GLOBAL_GIFT: {
                gift = new BigAnimationGift();
            }
            break;
            case GiftType.HIGH_VALUE_GIFT: {
                gift = new BigAnimationGift();
            }
            break;
            case GiftType.PECK_OF_GIFT: {
                gift = new PeckOfGift();
            }
            break;
            case GiftType.BIG_PACK_OF_GIFT: {
                gift = new BigPackOfGift();
            }
            break;
            case GiftType.MAGIC_GIFT:{
                gift = new ExpressionGift();
            }
            break;
            //特权礼物和米币礼物同样处理，getOriginGiftType是真正的type。
            case GiftType.Mi_COIN_GIFT:
            case GiftType.PRIVILEGE_GIFT: {
                switch (baseGift.getOriginGiftType()) {
                    case GiftType.NORMAL_EFFECTS_GIFT: {
                        gift = new NormalEffectGift();
                    }
                    break;
                    case GiftType.ROOM_BACKGROUND_GIFT: {
                        gift = new RoomEffectGift();
                    }
                    break;
                    case GiftType.LIGHT_UP_GIFT: {
                        gift = new LightUpGift();
                    }
                    break;
                    case GiftType.GLOBAL_GIFT: {
                        gift = new BigAnimationGift();
                    }
                    break;
                    case GiftType.HIGH_VALUE_GIFT: {
                        gift = new BigAnimationGift();
                    }
                    break;
                    case GiftType.PECK_OF_GIFT: {
                        gift = new PeckOfGift();
                    }
                    break;
                    case GiftType.BIG_PACK_OF_GIFT: {
                        gift = new BigPackOfGift();
                    }
                    break;
                    case GiftType.MAGIC_GIFT:{
                        gift = new ExpressionGift();
                    }
                    break;
                    default: {
                        gift = new NormalGift();
                    }
                    break;
                }
            }
            break;
            default: {
                gift = new NormalGift();
            }
            break;
        }
        gift.setGiftId(baseGift.getGiftId());
        gift.setSortId(baseGift.getSortId());
        gift.setName(baseGift.getName());
        gift.setPrice(baseGift.getPrice());
        gift.setEmpiricValue(baseGift.getEmpiricValue());
        gift.setPicture(baseGift.getPicture());
        gift.setCanContinuous(baseGift.getCanContinuous());
        gift.setResourceUrl(baseGift.getResourceUrl());
        gift.setCanSale(baseGift.getCanSale());
        gift.setCatagory(baseGift.getCatagory());
        gift.setLanguageStr(baseGift.getLanguageStr());
        gift.setOriginalPrice(baseGift.getOriginalPrice());
        gift.setIcon(baseGift.getIcon());
        gift.setComment(baseGift.getComment());
        gift.setGifUrl(baseGift.getGifUrl());
        gift.setLowerLimitLevel(baseGift.getLowerLimitLevel());
        gift.setOriginGiftType(baseGift.getOriginGiftType());
        gift.setBuyType(baseGift.getBuyType());
        gift.setDisplayInGiftArea(baseGift.getDisplayInGiftArea());
        gift.setDisplayInSubTitle(baseGift.getDisplayInSubTitle());
        gift.setCostType(baseGift.getCostType());
        return gift;
    }

    public static Gift loadFromPB(GiftProto.GiftInfo giftInfo) {
        Gift gift;
        MyLog.d(TAG, "loadfromPB");
        switch (giftInfo.getCatagory()) {
            case GiftType.NORMAL_EFFECTS_GIFT: {
                gift = new NormalEffectGift();
            }
            break;
            case GiftType.ROOM_BACKGROUND_GIFT: {
                gift = new RoomEffectGift();
            }
            break;
            case GiftType.LIGHT_UP_GIFT: {
                gift = new LightUpGift();
            }
            break;
            case GiftType.GLOBAL_GIFT: {
                gift = new BigAnimationGift();
            }
            break;
            case GiftType.HIGH_VALUE_GIFT: {
                gift = new BigAnimationGift();
            }
            break;
            case GiftType.PECK_OF_GIFT: {
                gift = new PeckOfGift();
            }
            break;
            case GiftType.BIG_PACK_OF_GIFT: {
                gift = new BigPackOfGift();
            }
            break;
            case GiftType.MAGIC_GIFT:{
                gift = new ExpressionGift();
            }
            break;
            //特权礼物和米币礼物同样处理，getOriginGiftType是真正的type。
            case GiftType.Mi_COIN_GIFT:
            case GiftType.PRIVILEGE_GIFT: {
                switch (giftInfo.getOriginGiftType()) {
                    case GiftType.NORMAL_EFFECTS_GIFT: {
                        gift = new NormalEffectGift();
                    }
                    break;
                    case GiftType.ROOM_BACKGROUND_GIFT: {
                        gift = new RoomEffectGift();
                    }
                    break;
                    case GiftType.LIGHT_UP_GIFT: {
                        gift = new LightUpGift();
                    }
                    break;
                    case GiftType.GLOBAL_GIFT: {
                        gift = new BigAnimationGift();
                    }
                    break;
                    case GiftType.HIGH_VALUE_GIFT: {
                        gift = new BigAnimationGift();
                    }
                    break;
                    case GiftType.PECK_OF_GIFT: {
                        gift = new PeckOfGift();
                    }
                    break;
                    case GiftType.BIG_PACK_OF_GIFT: {
                        gift = new BigPackOfGift();
                    }
                    break;
                    case GiftType.MAGIC_GIFT: {
                        gift = new ExpressionGift();
                    }
                    break;
                    default: {
                        gift = new NormalGift();
                    }
                    break;
                }
            }
            break;
            default: {
                gift = new NormalGift();
            }
            break;
        }
        gift.setGiftId(giftInfo.getGiftId());
        gift.setSortId(giftInfo.getSortId());
        gift.setName(giftInfo.getName());
        gift.setPrice(giftInfo.getPrice());
        gift.setEmpiricValue(giftInfo.getEmpiricValue());
        gift.setPicture(giftInfo.getPicture());
        gift.setCanContinuous(giftInfo.getCanContinuous());
        gift.setResourceUrl(giftInfo.getDetail());
        boolean canSale = giftInfo.getAndSale() == 1 ? true : false;
        gift.setCanSale(canSale);
        gift.setCatagory(giftInfo.getCatagory());
        gift.setOriginalPrice(giftInfo.getOriginalPrice());
        gift.setIcon(giftInfo.getIcon());
        gift.setComment(giftInfo.getComment());
        gift.setGifUrl(giftInfo.getGifUrl());
        gift.setLowerLimitLevel(giftInfo.getLowerLimitLevel());
        gift.setOriginGiftType(giftInfo.getOriginGiftType());
        gift.setBuyType(giftInfo.getBuyType());
        gift.setDisplayInGiftArea(giftInfo.getDisplayInGiftArea());
        gift.setDisplayInSubTitle(giftInfo.getDisplayInSubtitleArea());
        List<GiftProto.Language> list = giftInfo.getMultiLanguageList();
        JSONArray languageModels = new JSONArray();
        if (list != null) {
            for (GiftProto.Language l : list) {
                Gift.GiftLanguageModel gl = Gift.GiftLanguageModel.toGiftLanguageModel(l);
                languageModels.put(gl.toJson());
            }
        }
        gift.setLanguageStr(languageModels.toString());
        gift.setCostType(giftInfo.getCostType());
        return gift;
    }

}
