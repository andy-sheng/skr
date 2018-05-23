package com.wali.live.common.gift.bean;

import com.base.log.MyLog;
import com.wali.live.common.gift.presenter.GiftMallPresenter;
import com.wali.live.dao.Gift;

import java.util.Iterator;
import java.util.List;

/**
 * Created by lan on 2017/12/21.
 */
public class GiftMallBean {
    private static final String TAG = GiftMallBean.class.getSimpleName();

    //竖屏正常礼物
    private List<List<GiftMallPresenter.GiftWithCard>> mNormalGiftPortraitList;
    //竖屏包裹礼物
    private List<List<GiftMallPresenter.GiftWithCard>> mPktGiftPortraitList;
    //横屏正常礼物
    private List<GiftMallPresenter.GiftWithCard> mNormalGiftLandscapeList;
    //横屏包裹礼物
    private List<GiftMallPresenter.GiftWithCard> mPktGiftLandscapeList;

    public void remove(GiftMallPresenter.GiftWithCard giftWithCard) {
        MyLog.d(TAG, "giftWithCard giftId=" + giftWithCard.gift.getGiftId());

        if (mPktGiftLandscapeList != null) {
            MyLog.d(TAG, "mPktGiftLandscapeList size before=" + mPktGiftLandscapeList.size());
            removeGift(mPktGiftLandscapeList, giftWithCard);
            MyLog.d(TAG, "mPktGiftLandscapeList size after=" + mPktGiftLandscapeList.size());
        }

        if (mPktGiftPortraitList != null) {
            for (List<GiftMallPresenter.GiftWithCard> list : mPktGiftPortraitList) {
                if (list != null) {
                    MyLog.d(TAG, "mPktGiftPortraitList innerList size before=" + list.size());
                    if (removeGift(list, giftWithCard)) {
                        MyLog.d(TAG, "mPktGiftPortraitList innerList size after=" + list.size());
                        break;
                    }
                }
            }
        }
    }

    private boolean removeGift(List<GiftMallPresenter.GiftWithCard> list, GiftMallPresenter.GiftWithCard giftWithCard) {
        Iterator<GiftMallPresenter.GiftWithCard> iterator = list.iterator();
        while (iterator.hasNext()) {
            GiftMallPresenter.GiftWithCard giftWithCard1 = iterator.next();
            if (giftWithCard1.gift.getGiftId() == giftWithCard.gift.getGiftId()) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public List<List<GiftMallPresenter.GiftWithCard>> getGiftPortraitList(boolean isMallGift) {
        return isMallGift ? mNormalGiftPortraitList : mPktGiftPortraitList;
    }

    public List<GiftMallPresenter.GiftWithCard> getGiftLandscapeList(boolean isMallGift) {
        return isMallGift ? mNormalGiftLandscapeList : mPktGiftLandscapeList;
    }

    public void setNormalGiftPortraitList(List<List<GiftMallPresenter.GiftWithCard>> normalGiftPortraitList) {
        mNormalGiftPortraitList = normalGiftPortraitList;
    }

    public void setPktGiftPortraitList(List<List<GiftMallPresenter.GiftWithCard>> pktGiftPortraitList) {
        mPktGiftPortraitList = pktGiftPortraitList;
    }

    public void setNormalGiftLandscapeList(List<GiftMallPresenter.GiftWithCard> normalGiftLandscapeList) {
        mNormalGiftLandscapeList = normalGiftLandscapeList;
    }

    public void setPktGiftLandscapeList(List<GiftMallPresenter.GiftWithCard> pktGiftLandscapeList) {
        mPktGiftLandscapeList = pktGiftLandscapeList;
    }

    public int[] getGiftIndex(Gift gift, boolean isLandscape, boolean isPacket) {
        //第几页和第几个位置
        int[] pp = new int[]{-1, -1};
        if (!isLandscape) {
            List<List<GiftMallPresenter.GiftWithCard>> list;
            if (isPacket) {
                list = mPktGiftPortraitList;
            } else {
                list = mNormalGiftPortraitList;
            }
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < list.get(i).size(); j++) {
                    if (list.get(i).get(j).gift.getGiftId() == gift.getGiftId()) {
                        pp[0] = i;
                        pp[1] = j;
                    }
                }
            }
        } else {
            List<GiftMallPresenter.GiftWithCard> list;
            if (isPacket) {
                list = mPktGiftLandscapeList;
            } else {
                list = mNormalGiftLandscapeList;
            }

            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).gift.getGiftId() == gift.getGiftId()) {
                    pp[0] = 0;
                    pp[1] = j;
                }
            }
        }

        return pp;
    }
}
