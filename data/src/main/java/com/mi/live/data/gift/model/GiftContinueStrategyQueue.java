package com.mi.live.data.gift.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by chengsimin on 16/2/21.
 */
public class GiftContinueStrategyQueue {

    private static final int SIZE = 500;

    private LinkedHashMap<String, GiftRecvModel> mMapQueue = new LinkedHashMap<>();

    private LinkedHashMap<String, GiftRecvModel> mSelfMapQueue = new LinkedHashMap<>();

    private LinkedHashMap<String, GiftRecvModel> mBreakMapQueue = new LinkedHashMap<>();

    public  void offer(GiftRecvModel model) {
        if (model == null) {
            return;
        }
        if (model.isFromSelf()) {
            offer(mSelfMapQueue, model);
        } else {
            if (isFull()) {
                return;
            }
            offer(mMapQueue, model);
        }
    }

    public void offerToBreakQueue(GiftRecvModel model) {
        if(model!=null){
            offer(mBreakMapQueue, model);
        }
    }

    private synchronized void offer(LinkedHashMap<String, GiftRecvModel> mapQueue, GiftRecvModel model) {
        String key = model.getUserId() + "_" + model.getGiftId() + "_" + model.getContinueId();
        GiftRecvModel recv = mapQueue.get(key);
        if (recv == null) {
            mapQueue.put(key, model);
        } else {
            if (model.getEndNumber() > recv.getEndNumber()) {
                recv.setEndNumber(model.getEndNumber());
            } else if (model.getStartNumber() < recv.getStartNumber()) {
                recv.setStartNumber(model.getStartNumber());
            }
        }
    }

    /**
     * @return
     */
    public synchronized GiftRecvModel poll() {
        if (!mSelfMapQueue.isEmpty()) {
            return poll(mSelfMapQueue);
        }
        if(!mBreakMapQueue.isEmpty()){
            return poll(mBreakMapQueue);
        }
        if(!mMapQueue.isEmpty()){
            return poll(mMapQueue);
        }
        return null;
    }

    /**
     * @return
     */
    public synchronized GiftRecvModel poll(LinkedHashMap<String, GiftRecvModel> mapQueue) {
        Iterator iter = mapQueue.entrySet().iterator();
        String key = null;
        GiftRecvModel recv = null;
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            key = (String) entry.getKey();
            recv = (GiftRecvModel) entry.getValue();
            break;
        }
        if (recv == null) {
            return null;
        } else {
            mapQueue.remove(key);
            return recv;
        }
    }

    public synchronized void clear() {
        mMapQueue.clear();
        mSelfMapQueue.clear();
    }

    public synchronized boolean isFull() {
        return mMapQueue.size() > SIZE;
    }

    public synchronized int size() {
        return mMapQueue.size() + mSelfMapQueue.size();
    }

}
