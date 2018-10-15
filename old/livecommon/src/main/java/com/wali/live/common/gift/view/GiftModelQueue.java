package com.wali.live.common.gift.view;


import com.mi.live.data.gift.model.GiftRecvModel;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by yangjiawei on 2017/8/7.
 */

public class GiftModelQueue implements IGiftModelQueue {

    private Map<String, GiftRecvModel> mSelfBatchMap = Collections.synchronizedMap(new LinkedHashMap<String, GiftRecvModel>());    //本地组合礼物

    private Map<String, GiftRecvModel> mOtherBatchMap = Collections.synchronizedMap(new LinkedHashMap<String, GiftRecvModel>());   //非本地组合礼物
    private Map<String, GiftRecvModel> mSelfMap = Collections.synchronizedMap(new LinkedHashMap<String, GiftRecvModel>());         //本地普通礼物
    private Map<String, GiftRecvModel> mOtherMap = Collections.synchronizedMap(new LinkedHashMap<String, GiftRecvModel>());        //非本地普通礼物


    @Override
    public synchronized GiftRecvModel tryNextModel(GiftRecvModel model) {
        if (model == null) {
            return null;
        }
        String key = model.getUserId() + "_" + model.getGiftId() + "_" + model.getContinueId();
        GiftRecvModel recvModel = mSelfBatchMap.get(key);
        Map<String, GiftRecvModel> map = mSelfBatchMap;
        if (recvModel == null) {
            recvModel = mOtherBatchMap.get(key);
            map = mOtherBatchMap;
        }
        if (recvModel == null) {
            recvModel = mSelfMap.get(key);
            map = mSelfMap;
        }
        if (recvModel == null) {
            recvModel = mOtherMap.get(key);
            map = mOtherMap;
        }
        if (recvModel != null) {
            GiftRecvModel copyModel = recvModel.copy();
            copyModel.setMainOrbitId(model.getMainOrbitId());
            recvModel.setStartNumber(recvModel.getStartNumber() + 1);
            if (recvModel.getStartNumber() > recvModel.getEndNumber()) {
                map.remove(key);
            }
            return copyModel;
        }
        return null;
    }

    @Override
    public int getBatchSize() {
        return mSelfBatchMap.size() + mOtherBatchMap.size();
    }

    @Override
    public synchronized void offer(GiftRecvModel model) {
        if (model.isBatchGift()) {
            if (model.isFromSelf()) {
                offer(model, mSelfBatchMap);
            } else {
                offer(model, mOtherBatchMap);
            }
        } else {
            if (model.isFromSelf()) {
                offer(model, mSelfMap);
            } else {
                offer(model, mOtherMap);
            }
        }
    }

    private synchronized void offer(GiftRecvModel model, Map<String, GiftRecvModel> map) {
        String key = model.getUserId() + "_" + model.getGiftId() + "_" + model.getContinueId();
        GiftRecvModel data = map.get(key);
        if (data != null) {
            if (model.getEndNumber() > data.getEndNumber()) {
                data.setEndNumber(model.getEndNumber());
            }
            if (model.getStartNumber() < data.getStartNumber()) {
                data.setStartNumber(model.getStartNumber());
            }
        } else {
            map.put(key, model);
        }
    }

    @Override
    public synchronized GiftRecvModel poll() {
        if (!mSelfBatchMap.isEmpty()) {
            return poll(mSelfBatchMap);
        } else if (!mOtherBatchMap.isEmpty()) {
            return poll(mOtherBatchMap);
        } else if (!mSelfMap.isEmpty()) {
            return poll(mSelfMap);
        } else if (!mOtherMap.isEmpty()) {
            return poll(mOtherMap);
        }
        return null;
    }

    private GiftRecvModel poll(Map<String, GiftRecvModel> map) {
        Iterator iterator = map.entrySet().iterator();
        GiftRecvModel model = null;
        String key = null;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            key = (String) entry.getKey();
            model = (GiftRecvModel) entry.getValue();
            break;
        }
        if (model != null) {
            GiftRecvModel recvModel = model.copy();
            if (model.getStartNumber() < model.getEndNumber()) {
                model.setStartNumber(model.getStartNumber() + 1);
            } else {
                map.remove(key);
            }
            return recvModel;
        }
        return null;
    }

    @Override
    public synchronized GiftRecvModel top() {
        if (!mSelfBatchMap.isEmpty()) {
            return top(mSelfBatchMap);
        } else if (!mOtherBatchMap.isEmpty()) {
            return top(mOtherBatchMap);
        } else if (!mSelfMap.isEmpty()) {
            return top(mSelfMap);
        } else if (!mOtherMap.isEmpty()) {
            return top(mOtherMap);
        }
        return null;
    }

    private GiftRecvModel top(Map<String, GiftRecvModel> map) {
        Iterator iterator = map.entrySet().iterator();
        GiftRecvModel model = null;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            model = (GiftRecvModel) entry.getValue();
            break;
        }
        return model;
    }

    @Override
    public synchronized void clear() {
        mSelfBatchMap.clear();
        mOtherBatchMap.clear();
        mSelfMap.clear();
        mOtherMap.clear();
    }

    @Override
    public int size() {
        return mSelfBatchMap.size() + mOtherBatchMap.size() + mSelfMap.size() + mOtherMap.size();
    }

    @Override
    public synchronized int batchGiftSize() {
        return mSelfBatchMap.size() + mOtherBatchMap.size();
    }

    //从queue中获取一个非本continueId的model
    @Override
    public synchronized GiftRecvModel nonThisModel(GiftRecvModel model) {
        GiftRecvModel data = nonThisModel(model, mSelfBatchMap);
        if (data == null) {
            data = nonThisModel(model, mOtherBatchMap);
        }
        if (data == null) {
            data = nonThisModel(model, mSelfMap);
        }
        if (data == null) {
            data = nonThisModel(model, mOtherMap);
        }
        return data;
    }

    private GiftRecvModel nonThisModel(GiftRecvModel model, Map<String, GiftRecvModel> map) {
        Iterator it = map.entrySet().iterator();
        GiftRecvModel data = null;
        String key = null;
        while (it.hasNext()) {
            Map.Entry<String, GiftRecvModel> entry = (Map.Entry<String, GiftRecvModel>) it.next();
            data = entry.getValue();
            key = entry.getKey();
            if (data.getContinueId() != model.getContinueId()) {
                break;
            } else {
                data = null;
            }
        }

        if (data != null) {
            GiftRecvModel copy = data.copy();
            if (data.getStartNumber() < data.getEndNumber()) {
                data.setStartNumber(data.getStartNumber() + 1);
            } else {
                map.remove(key);
            }
            return copy;
        }
        return null;
    }
}
