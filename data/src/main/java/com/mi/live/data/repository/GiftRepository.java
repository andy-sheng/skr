package com.mi.live.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.IOUtils;
import com.base.utils.MD5;
import com.base.utils.image.ImageUrlDNSManager;
import com.base.utils.network.Network;
import com.base.utils.network.NetworkUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gift.manager.GiftPushMsgProcesser;
import com.mi.live.data.gift.mapper.GiftTypeMapper;
import com.mi.live.data.gift.model.GiftInfoForEnterRoom;
import com.mi.live.data.gift.model.GiftRecvModel;
import com.mi.live.data.gift.model.GiftType;
import com.mi.live.data.gift.model.giftEntity.BigPackOfGift;
import com.mi.live.data.location.Location;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.repository.datasource.GiftLocalStore;
import com.mi.milink.sdk.aidl.PacketData;
import com.mi.milink.sdk.debug.MiLinkMonitor;
import com.wali.live.dao.Gift;
import com.wali.live.proto.EffectProto;
import com.wali.live.proto.GiftProto;
import com.wali.live.proto.LiveProto;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by zjn on 16-11-28.
 *
 * @module 礼物
 */
public class GiftRepository {
    public static String GIFT_MONITOR_CMD = "gift.download";
    public static final int GIFT_DOWNLOAD_FAILED = 1;
    public static final int GIFT_DOWNLOAD_SUCCESS = 0;
    private static String PREFIX_GIFT = "gift_";
    public static String PREF_LIVE_GIFT_CONFIG = "pref_gift_config";
    public static String KEY_PULL_GIFTLIST_TIMESTAMP = "key_pull_giftlist_timestamp";
    public static String TAG = "GiftRepository";

    //资源已经存在的礼物
    private static HashMap<Integer, Gift> mExistedGift = new HashMap<>();
    //绿色通道，无论那种都要下载
    private static HashSet<Integer> mGreenChannelGift = new HashSet<>();
    /**
     * 专门用于下载礼物动画资源
     */
    private static PublishSubject<Gift> mDownloadAnimationRes = PublishSubject.create();

    static {
        mDownloadAnimationRes
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<Gift>() {
                    @Override
                    public void onCompleted() {
                        MyLog.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, "download onError msg:" + e);
                    }

                    @Override
                    public void onNext(Gift gift) {
                        MyLog.d(TAG, "Thread name:" + Thread.currentThread());
                        downloadAndUnzip(gift);
                    }
                });
    }

    // 记录下载失败的礼物
    private static HashSet<Gift> mDownLoadFailedGifts = null;

    private static Subscription mTimerSubscription = null;

    // 记录正在下载的礼物
    private static HashSet<Integer> mDownloadingGiftSet = new HashSet<Integer>();

    private static void addToDownloadFailedGifts(Gift g) {
        if (mDownLoadFailedGifts == null) {
            mDownLoadFailedGifts = new HashSet<>();
        }
        mDownLoadFailedGifts.add(g);
        if (mTimerSubscription != null && !mTimerSubscription.isUnsubscribed()) {
            return;
        }
        mTimerSubscription = Observable.timer(60, TimeUnit.SECONDS)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (mDownLoadFailedGifts != null) {
                            Iterator<Gift> it = mDownLoadFailedGifts.iterator();
                            while (it.hasNext()) {
                                Gift gift = it.next();
                                checkOneAnimationRes(gift);
                            }
                        }
                    }
                });
    }

    private static void removeFromDownloadFailedGifts(Gift gift) {
        if (mDownLoadFailedGifts != null) {
            mDownLoadFailedGifts.remove(gift);
        }
    }

    private static Vector<Gift> mCache = new Vector<Gift>();

    /**
     * 拉取该房间的特效
     */
    public static Observable<GiftInfoForEnterRoom> getRoomEnterGiftInfo(final String roomid, final long uuid, final long zuid, final Location location) {
        return Observable.create(
                new Observable.OnSubscribe<EffectProto.GetRoomEffectsResponse>() {
                    @Override
                    public void call(Subscriber<? super EffectProto.GetRoomEffectsResponse> subscriber) {

                        EffectProto.GetRoomEffectsRequest.Builder reqBuilder = EffectProto
                                .GetRoomEffectsRequest
                                .newBuilder()
                                .setRoomId(roomid)
                                .setUserId(uuid)
                                .setAnchorId(zuid)
                                .setPlatform(EffectProto.Platform.ANDROID);
                        if (location != null) {
                            EffectProto.Location.Builder locationBuilder = EffectProto.Location.newBuilder();
                            String country = location.getCountry();
                            if (!TextUtils.isEmpty(country)) {
                                locationBuilder.setCountry(country);
                            }
                            String province = location.getProvince();
                            if (!TextUtils.isEmpty(province)) {
                                locationBuilder.setProvince(province);
                            }
                            reqBuilder.setLocation(locationBuilder);
                        }

                        EffectProto.GetRoomEffectsRequest req = reqBuilder.build();
                        PacketData data = new PacketData();
                        data.setCommand(MiLinkCommand.COMMAND_EFFECT_GET);
                        data.setData(req.toByteArray());
                        data.setNeedCached(true);
                        MyLog.w(TAG, "getRoomEffect request:" + req.toString());
                        PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
                        EffectProto.GetRoomEffectsResponse rsp = null;
                        try {
                            rsp = EffectProto.GetRoomEffectsResponse.parseFrom(response.getData());
                            MyLog.w(TAG, "getRoomEffect response:" + rsp);
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                        subscriber.onNext(rsp);
                        subscriber.onCompleted();
                    }
                }).map(new Func1<EffectProto.GetRoomEffectsResponse, GiftInfoForEnterRoom>() {

            @Override
            public GiftInfoForEnterRoom call(EffectProto.GetRoomEffectsResponse getRoomEffectsResponse) {
                return GiftInfoForEnterRoom.loadFromPB(getRoomEffectsResponse);
            }
        })
                .subscribeOn(Schedulers.io());
    }

    /**
     * 拉取该房间运营位信息
     */
    public static Observable<LiveProto.GetRoomAttachmentRsp> getRoomAttachment(final String roomid, final long zuid, final boolean isGet, final int roomType, final boolean isGetIconConfig) {
        return Observable.create(
                new Observable.OnSubscribe<LiveProto.GetRoomAttachmentRsp>() {
                    @Override
                    public void call(Subscriber<? super LiveProto.GetRoomAttachmentRsp> subscriber) {
                        LiveProto.GetRoomAttachmentReq req = LiveProto.GetRoomAttachmentReq
                                .newBuilder()
                                .setIsGetWidget(isGet)
                                .setLiveid(roomid)
                                .setZuid(zuid)
                                .setIsGetAnimation(true)
                                .setRoomType(roomType)
                                .setIsGetIconConfig(isGetIconConfig)
                                .build();
                        PacketData data = new PacketData();
                        data.setCommand(MiLinkCommand.COMMAND_ROOM_ATTACHMENT);
                        data.setData(req.toByteArray());
                        data.setNeedCached(true);
                        MyLog.w(TAG, "getRoomAttachment request:" + req.toString());
                        try {
                            PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
                            LiveProto.GetRoomAttachmentRsp rsp = LiveProto.GetRoomAttachmentRsp.parseFrom(response.getData());
                            MyLog.w(TAG, "getRoomAttachment response:" + rsp);
                            if (rsp != null && rsp.getRetCode() == 0) {
                                subscriber.onNext(rsp);
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(new Throwable("getRoomAttachment retCode != 0"));
                            }
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                    }
                }).subscribeOn(Schedulers.io());
    }

    /**
     * 点击运营位计数
     */
    public static Observable<LiveProto.WidgetClickRsp> clickCounter(final int attachementId, final long zuid, final String roomId) {
        return Observable.create(
                new Observable.OnSubscribe<LiveProto.WidgetClickRsp>() {
                    @Override
                    public void call(Subscriber<? super LiveProto.WidgetClickRsp> subscriber) {
                        LiveProto.WidgetClickReq req = LiveProto.WidgetClickReq
                                .newBuilder().setWidgetID(attachementId).setZuid(zuid).setLiveid(roomId)
                                .build();
                        PacketData data = new PacketData();
                        data.setCommand(MiLinkCommand.COMMAND_ROOM_ATTACHMENT_CLICK);
                        data.setData(req.toByteArray());
                        data.setNeedCached(true);
                        MyLog.w(TAG, "clickCounter request:" + req.toString());
                        try {
                            PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
                            LiveProto.WidgetClickRsp rsp = LiveProto.WidgetClickRsp.parseFrom(response.getData());
                            MyLog.w(TAG, "clickCounter response:" + rsp);
                            if (rsp != null) {
                                subscriber.onNext(rsp);
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(new Throwable("clickCounter rsp is null"));
                            }

                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                    }
                }).subscribeOn(Schedulers.io());
    }

    /**
     * 同步购买礼物
     *
     * @param gift
     * @param receiveId
     * @param roomid
     * @param continueCount
     * @param timestamp
     * @return
     */
    public static GiftProto.BuyGiftRsp bugGiftSync(Gift gift, long receiveId, String roomid, int continueCount, long timestamp,
                                                   long continueId, String msgBody, int roomType, boolean useGiftCard, boolean isFromGameRoom) {
        GiftProto.BuyGiftReq.Builder reqBuilder = GiftProto.BuyGiftReq.newBuilder()
                .setUserId(MyUserInfoManager.getInstance().getUser().getUid())
                .setReceiverId(receiveId)
                .setRoomId(roomid)
                .setGiftId(gift.getGiftId())
                .setTimestamp(timestamp)
                .setCount(continueCount)
                .setContinueId(continueId)
                .setRoomType(roomType)
                .setUseGiftCard(useGiftCard)
                .setPlatform(GiftProto.Platform.ANDROID);
        if (!TextUtils.isEmpty(msgBody)) {
            reqBuilder.setMsgBody(msgBody);
        }
        GiftProto.BuyGiftReq req = reqBuilder.build();
        PacketData data = new PacketData();
        if (isFromGameRoom) {
            data.setCommand(MiLinkCommand.COMMAND_GIFT_BUY_GAME_ROOM);
        } else {
            data.setCommand(MiLinkCommand.COMMAND_GIFT_BUY);
        }
        data.setData(req.toByteArray());
        data.setNeedCached(false);
        MyLog.w(TAG, "bugGiftSync request:" + req.toString());
        PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
        GiftProto.BuyGiftRsp buyGiftRsp = null;
        try {
            buyGiftRsp = GiftProto.BuyGiftRsp.parseFrom(response.getData());
            MyLog.w(TAG, "bugGiftSync response:" + buyGiftRsp);
        } catch (Exception e) {
        }
        return buyGiftRsp;
    }

    public static void loadGiftListFromDB() {
        List<Gift> temp = GiftLocalStore.getInstance().getGiftList();
        replaceCache(temp);
    }

    /**
     * 替换掉现在的缓存队列
     *
     * @param newData
     */
    private static void replaceCache(List<Gift> newData) {
        if (newData != null && !newData.isEmpty()) {
            synchronized (mCache) {
                MyLog.d(TAG, "update gift cache:" + newData);
                mCache.clear();
                mCache.addAll(newData);
                EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_CACHE_CHANGE));
                checkAnimationResOfCache();
            }
        }
    }

    /**
     * 网络发生变化重新请求
     */
    public static void onChangeNetState() {
        MyLog.w(TAG, "onChangeNetState begin");
        checkAnimationResOfCache();
    }

    /**
     * 检查所有gift的动画资源是否准备妥当
     */
    private static void checkAnimationResOfCache() {
        MyLog.d(TAG, "checkAnimationResOfCache begin");
        synchronized (mCache) {
            if (mCache.size() > 0) {
                for (Gift gift : mCache) {
                    /**
                     * 1.如果在绿色通道里，就必须下载，
                     * 2.如果没有，(1)wifi下 && (2)橱窗可见的情况下下载
                     */
                    if (mGreenChannelGift.contains(gift.getGiftId())
                            || (NetworkUtils.isWifi(GlobalData.app())
                            && gift.getCanSale())) {
                        String jsonFilePath = checkOneAnimationRes(gift);
                        if (!TextUtils.isEmpty(jsonFilePath)) {
                            gift.completeGiftInfo(jsonFilePath);
                        }
                    }

                }
            }
        }
        MyLog.d(TAG, "checkAnimationResOfCache over");
    }

    /**
     * 检查一个礼物动画的资源是否存在
     * 如果已经有资源了，返回配置文件路径，如果没有资源或者不需要资源都返回null
     */
    public static String checkOneAnimationRes(Gift gift) {
        MyLog.d(TAG, "checkOneAnimationRes");
        if (gift.needDownResource()) {
            String url = gift.getResourceUrl();
            String fileNameMd5 = MD5.MD5_32(url);
            File animationResFile = new File(GlobalData.app().getFilesDir(), PREFIX_GIFT + fileNameMd5);
            String jsonFilePath = getConfigJsonPathIfResExist(animationResFile, gift.getConfigJsonFileName());
            if (TextUtils.isEmpty(jsonFilePath)) {
                // 路径还未存在，说明下载-》解压这一步还未完成，如果路径存在，但是文件不存在，说明文件被删除了，重新下载--》解压。
                MyLog.w(TAG, "giftName:" + gift.getName() + " resource not exist,post to download queue");
                // 下载成功后，填充信息
                mDownloadAnimationRes.onNext(gift);
                return null;
            } else {
                MyLog.w(TAG, "giftName:" + gift.getName() + " resource exist,go on!");
                if (!TextUtils.isEmpty(jsonFilePath)) {
                    gift.completeGiftInfo(jsonFilePath);
                }
                addGiftToExistedSet(gift);
                return jsonFilePath;
            }
        } else {
            return null;
        }
    }

    /**
     * 当执行完checkOneAnimationRes()后用这个方法得到一个礼物的资源是否已经存在
     */
    public static Gift checkExistedGiftRes(int giftId) {
        MyLog.d(TAG, "checkOneAnimationRes");
        synchronized (mExistedGift) {
            return mExistedGift.get(giftId);
        }
    }

    /**
     * 判断这个礼物的大动画资源是否存在
     *
     * @param animationResFile
     * @return
     */
    private static String getConfigJsonPathIfResExist(File animationResFile, String jsonFileName) {
        if (!animationResFile.exists()) {
            //文件夹都不存在，肯定没有资源
            return null;
        }
        // 文件夹存在
        String animationJsonPath = findConfigPath(animationResFile, jsonFileName);
        if (TextUtils.isEmpty(animationJsonPath)) {
            return null;
        }
        // 且能找到json文件
        return animationJsonPath;
    }

    /**
     * 下载和解压动画资源文件
     *
     * @param gift
     * @return
     */
    private static boolean downloadAndUnzip(Gift gift) {
        if (mDownloadingGiftSet.contains(gift.getGiftId())) {
            MyLog.w(TAG, gift + " already downloading");
            return false;
        }
        mDownloadingGiftSet.add(gift.getGiftId());
        MyLog.w(TAG, "downloadAndUnzip:" + gift);
        String zipUrl = gift.getResourceUrl();
        //创建好保存zip的file
        File parentFile = GlobalData.app().getFilesDir();
        File zipFile = new File(parentFile, PREFIX_GIFT + gift.getGiftId() + ".zip");
        // 不存在则创建
        if (!zipFile.exists()) {
            try {
                zipFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        boolean downloadResult = false;
        long downloadBeginTime = System.currentTimeMillis();
        try {
            downloadResult = Network.downloadFile(ImageUrlDNSManager.getAvailableUrl(zipUrl), Uri.parse(zipUrl).getHost(), new FileOutputStream(zipFile), false, null);
        } catch (FileNotFoundException e) {
            MyLog.e(TAG, e);
        }
        if (!downloadResult) {
            try {
                downloadResult = Network.downloadFile(zipUrl, new FileOutputStream(zipFile));
            } catch (FileNotFoundException e) {
                MyLog.e(TAG, e);
            }
        }
        boolean unZipResult = false;
        // 下载成功
        if (downloadResult) {
            MyLog.w(TAG, "download success unzip");
            MiLinkMonitor.getInstance().trace("", 0, GIFT_MONITOR_CMD,
                    GIFT_DOWNLOAD_SUCCESS, downloadBeginTime,
                    System.currentTimeMillis(), 0, 0, 0);
            // 准备存放解压文件的文件夹
            String fileNameMd5 = MD5.MD5_32(zipUrl);
            // 还在存在file的文件夹，不存在cache，存在cache会被清掉
            File unZipFile = new File(GlobalData.app().getFilesDir(), PREFIX_GIFT + fileNameMd5);
            unZipFile.mkdirs();
            unZipResult = IOUtils.unZip(zipFile.getAbsolutePath(), unZipFile.getAbsolutePath());
            // 解压成功
            if (unZipResult) {
                // 将路径存到gift内存中
//                String jsonConifgPath = findConfigPath(unZipFile);
                // 将压缩文件删除了
                MyLog.d(TAG, "unzip success");
                zipFile.delete();
                // 填充该礼物的剩余属性信息
                String jsonFilePath = getConfigJsonPathIfResExist(unZipFile, gift.getConfigJsonFileName());
                gift.completeGiftInfo(jsonFilePath);
                notifyGiftDownloadSuccess(gift);
                addGiftToExistedSet(gift);
            }
        } else {
            MyLog.w(TAG, "download gift resource failed");
            MiLinkMonitor.getInstance().trace("", 0, GIFT_MONITOR_CMD,
                    GIFT_DOWNLOAD_FAILED, downloadBeginTime,
                    System.currentTimeMillis(), 0, 0, 0);
        }
        mDownloadingGiftSet.remove(gift.getGiftId());
        if (downloadResult && unZipResult) {
            // 下载成功
            // 移除
            removeFromDownloadFailedGifts(gift);
            return true;
        } else {
            addToDownloadFailedGifts(gift);
            return false;
        }
    }

    private static String findConfigPath(File file, String fileName) {
        if (file.isFile()) {
            if (file.getName().endsWith(fileName)) {
                return file.getAbsolutePath();
            } else {
                return null;
            }
        }
        if (file.isDirectory()) {
            for (File temp : file.listFiles()) {
                String re = findConfigPath(temp, fileName);
                if (re != null) {
                    return re;
                }
            }
        }
        return null;
    }

    private static void notifyGiftDownloadSuccess(Gift gift) {
        MyLog.w(TAG, "notifyGiftDownloadSuccess gift=" + gift.toString());
        EventBus.getDefault().post(new GiftEventClass.GiftDownloadSuc(gift));
    }

    private static void addGiftToExistedSet(Gift gift) {
        synchronized (mExistedGift) {
            mExistedGift.put(gift.getGiftId(), gift);
        }
    }


    private static Handler mainHandler = new Handler(Looper.getMainLooper());
    private static boolean isLoading = false;
    private static Runnable resetLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            isLoading = false;
        }
    };

    /**
     * 从服务器拉取礼物列表
     *
     * @param timestamp
     */
    public static void pullGiftListFromServer(long timestamp) {
        if (isLoading) {
            MyLog.i(TAG, "pullGiftListFromServer isLoading already,cancel this");
            return;
        }
        isLoading = true;
        mainHandler.postDelayed(resetLoadingRunnable, 5000);
        GiftProto.GetGiftListReq req = GiftProto.GetGiftListReq
                .newBuilder()
                .setTimestamp(timestamp)
                .setVersion(2)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GIFT_GET_LIST);
        data.setData(req.toByteArray());
        MyLog.v(TAG, "pullGiftListFromServer request:" + req.toString());
        MiLinkClientAdapter.getsInstance().sendAsync(data, 0);
    }

    /**
     * 同步礼物列表，如果数据为空，时间戳传0.
     */
    public static void syncGiftList() {
        MyLog.w(TAG, "syncGiftList");
        loadGiftListFromDB();
        if (mCache.isEmpty()) {
            MyLog.d(TAG, "mCache.isEmpty()");
            pullGiftListFromServer(0);
        } else {
            MyLog.d(TAG, "！mCache.isEmpty()");
            SharedPreferences settingPreferences = GlobalData.app().getSharedPreferences(
                    PREF_LIVE_GIFT_CONFIG, Context.MODE_PRIVATE);
            pullGiftListFromServer(settingPreferences.getLong(KEY_PULL_GIFTLIST_TIMESTAMP, 0));
        }
    }

    /**
     * 处理礼物列表的服务器返回信息
     *
     * @param response
     */
    public static void process(GiftProto.GetGiftListRsp response) {
        mainHandler.removeCallbacks(resetLoadingRunnable);
        resetLoadingRunnable.run();
        if (response == null) {
            return;
        }
        ArrayList<Gift> newList = new ArrayList<>();
        GiftProto.GiftList list = response.getGiftList();
        for (int i = 0; i < list.getGiftInfosCount(); i++) {
            newList.add(GiftTypeMapper.loadFromPB(list.getGiftInfos(i)));
        }
        // 有新数据下来
        if (!newList.isEmpty()) {
            // 更新数据库，更新水位
            GiftLocalStore.getInstance().deleteAll();
            if (GiftLocalStore.getInstance().insertGiftList(newList)) {
                // 更新数据成功
                // 写入时间戳
                long timestamp = response.getTimestamp();
                SharedPreferences settingPreferences = GlobalData.app().getSharedPreferences(
                        PREF_LIVE_GIFT_CONFIG, Context.MODE_PRIVATE);
                PreferenceUtils.setSettingLong(settingPreferences, KEY_PULL_GIFTLIST_TIMESTAMP, timestamp);
            }
            replaceCache(newList);
        }
    }

    public static List<Gift> getGiftListCache() {
        synchronized (mCache) {
            if (mCache.isEmpty()) {
                syncGiftList();
            }
            return mCache;
        }
    }

    @Nullable
    public static Gift findGiftById(int giftId) {
        synchronized (mCache) {
            for (Gift gift : mCache) {
                if (gift.getGiftId() == giftId) {
                    return gift;
                }
            }
        }
        return null;
    }

    /**
     * 目前用于查询门票礼物的id列表
     */
    @NonNull
    public static List<Integer> getGiftIdListByCategory(int category) {
        List<Integer> giftIdList = new ArrayList<>();
        synchronized (mCache) {
            for (Gift gift : mCache) {
                if (gift.getCatagory() == category) {
                    giftIdList.add(gift.getGiftId());
                }
            }
        }
        return giftIdList;
    }

    /**
     * @param category 礼物类型
     * @return 返指定类型按价格排序的列表
     */
    @NonNull
    public static List<Pair<Integer, Integer>> getGiftPriceByCategory(int category) {
        List<Pair<Integer, Integer>> priceList = new ArrayList<>();
        synchronized (mCache) {
            for (Gift gift : mCache) {
                if (gift.getCatagory() == category) {
                    priceList.add(new Pair<Integer, Integer>(gift.getGiftId(), gift.getPrice()));
                }
            }
        }
        Collections.sort(priceList, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> lhs, Pair<Integer, Integer> rhs) {
                return lhs.second.compareTo(rhs.second);
            }
        });
        return priceList;
    }

    /**
     * 填充礼物图片和动画资源路径
     *
     * @param model
     * @return
     */
    public static void fillGiftEntityById(GiftRecvModel model) {
        //添加必须需要下载的礼物，以防mCache里没有重新syncGiftList()，再去下载
        synchronized (mGreenChannelGift) {
            mGreenChannelGift.add(model.getGiftId());
        }
        //这里有些耗时
        synchronized (mCache) {
            for (Gift gift : mCache) {
                if (gift.getGiftId() == model.getGiftId()) {
                    MyLog.d(TAG, "Match gift:" + gift + ",class:" + gift.getClass());
                    model.setGift(gift);
                    // 再检查资源包在不在
                    checkOneAnimationRes(gift);
                    return;
                }
            }
            MyLog.w(TAG, "fillGiftEntityById no find gift id=" + model.getGiftId());
            // 一个都没命中，说明本地列表旧了，重新拉
            syncGiftList();
        }
        return;
    }

    public static BarrageMsg createGiftBarrageMessage(int giftId, String giftName, int giftType, String content, int count,
                                                      int zhuboAsset, long zhuboAssetTs, long continueId, String roomId, String ownerId,
                                                      String redEnvelopeId, String giftSenderName, long senderAvatarTimestamp, boolean isPrivilegeGift) {
        // 直接丢到队列
        BarrageMsg msg = new BarrageMsg();
        msg.setRoomId(roomId);
        MyLog.d(TAG, "ownerId" + ownerId);
        msg.setAnchorId(Long.parseLong(ownerId));
        // 根据礼物类型填充消息类型
        switch (giftType) {
            case GiftType.NORMAL_GIFT: {
                msg.setMsgType(BarrageMsgType.B_MSG_TYPE_GIFT);
            }
            break;
            case GiftType.NORMAL_EFFECTS_GIFT: {
                msg.setMsgType(BarrageMsgType.B_MSG_TYPE_GIFT);
            }
            break;
            case GiftType.HIGH_VALUE_GIFT: {
                msg.setMsgType(BarrageMsgType.B_MSG_TYPE_GIFT);
            }
            break;
            case GiftType.GLOBAL_GIFT: {
                msg.setMsgType(BarrageMsgType.B_MSG_TYPE_GLABAL_MSG);
            }
            break;
            case GiftType.ROOM_BACKGROUND_GIFT: {
                msg.setMsgType(BarrageMsgType.B_MSG_TYPE_ROOM_BACKGROUND_GIFT);
            }
            break;
            case GiftType.LIGHT_UP_GIFT: {
                msg.setMsgType(BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT);
            }
            break;
            case GiftType.BARRAGE_GIFT: {
            }
            break;
            case GiftType.RED_ENVELOPE_GIFT: {
                msg.setMsgType(BarrageMsgType.B_MSG_TYPE_RED_ENVELOPE);
            }
            break;
            default: {
                msg.setMsgType(BarrageMsgType.B_MSG_TYPE_GIFT);
            }
            break;
        }

        //大礼包弹幕添加
        String senderName;
        if (TextUtils.isEmpty(giftSenderName)) {
            msg.setSender(MyUserInfoManager.getInstance().getUser().getUid());
            senderName = MyUserInfoManager.getInstance().getUser().getNickname();
        } else {
            msg.setSender(Long.parseLong(ownerId));
            senderName = giftSenderName;
        }
        msg.setSenderName(senderName);
        msg.setSenderLevel(MyUserInfoManager.getInstance().getUser().getLevel());
        msg.setCertificationType(MyUserInfoManager.getInstance().getUser().getCertificationType());
        msg.setSentTime(System.currentTimeMillis());
        msg.setBody(content);
        msg.setRedName(MyUserInfoManager.getInstance().getUser().isRedName());
        BarrageMsg.GiftMsgExt ext = new BarrageMsg.GiftMsgExt();
        ext.giftId = giftId;
        ext.giftName = giftName;
        ext.giftCount = count;
        ext.zhuboAsset = zhuboAsset;
        ext.zhuboAssetTs = zhuboAssetTs;
        ext.continueId = continueId;
        ext.msgBody = content;
        ext.isPrivilegeGift = isPrivilegeGift;
        if (senderAvatarTimestamp > 0) {
            ext.avatarTimestamp = senderAvatarTimestamp;
        } else {
            ext.avatarTimestamp = MyUserInfoManager.getInstance().getUser().getAvatar();
        }
        ext.redEnvelopeId = redEnvelopeId;
        // 不填，订单号为空时默认放行
//        ext.orderId = String.valueOf(System.currentTimeMillis());
        msg.setMsgExt(ext);
        return msg;
    }

    public static List<BarrageMsg> getBigPackOfGiftBarrageMsgList(BigPackOfGift bigPackOfGift, BarrageMsg msg) {

        List<BigPackOfGift.PackOfGiftInfo> packOfGiftInfoList = bigPackOfGift.getPackOfGiftInfoList();
        List<BarrageMsg> pushMsgList = new ArrayList<>();
        BarrageMsg.GiftMsgExt msgExt = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
        String senderName = msg.getSenderName();
        long senderAvatarTimestamp = msgExt.getAvatarTimestamp();
        if (packOfGiftInfoList == null || packOfGiftInfoList.isEmpty()) {
            return null;
        }

        for (int i = 0; i < packOfGiftInfoList.size(); i++) {
            BigPackOfGift.PackOfGiftInfo packOfGiftInfo = packOfGiftInfoList.get(i);
            int giftId = packOfGiftInfo.getGiftId();
            Gift gift = findGiftById(giftId);
            //TODO test
//            if(gift.getCatagory()==GiftType.HIGH_VALUE_GIFT){
//                continue;
//            }
//            if(giftId == 2001) {
//                continue;
//            }
            long continueId = System.currentTimeMillis();
            int continueNum = packOfGiftInfo.getGiftSendNum();
            if (gift == null || continueNum < 1) {
                return null;
            }

            for (int continueSednNum = 0; continueSednNum < continueNum; continueSednNum++) {
                BarrageMsg pushMsg = createGiftBarrageMessage(gift.getGiftId(), gift.getName(), gift.getCatagory(),
                        gift.getSendDescribe(), continueSednNum + 1, msgExt.getZhuboAsset(), msgExt.getZhuboAssetTs(), continueId,
                        msg.getRoomId(), String.valueOf(msg.getSender()), msgExt.getRedEnvelopeId(), senderName, senderAvatarTimestamp, false);
                pushMsgList.add(pushMsg);
            }
        }

        return pushMsgList;
    }

    public static BarrageMsg getPrivilegeGiftBarrage(Gift gift, BarrageMsg msg) {
        BarrageMsg.GiftMsgExt msgExt = (BarrageMsg.GiftMsgExt) msg.getMsgExt();

        BarrageMsg barrageMsg = createGiftBarrageMessage(gift.getGiftId(), gift.getName(), gift.getOriginGiftType(), gift.getSendDescribe(),
                msgExt.giftCount, msgExt.getZhuboAsset(), msgExt.getZhuboAssetTs(), msgExt.getContinueId(), msg.getRoomId(),
                String.valueOf(msg.getSender()), msgExt.getRedEnvelopeId(), msg.getSenderName(), msgExt.getAvatarTimestamp(), true);
        return barrageMsg;
    }

    public static void processRedEnvelopeMsgByPushWay(BarrageMsg.RedEnvelopMsgExt ext) {
        GiftPushMsgProcesser.processRedEnvelopGiftPush(ext);
    }

    public static void processGiftMsgByPushWay(BarrageMsg msg, BarrageMsg.GiftMsgExt ext, String roomId) {
        GiftPushMsgProcesser.processGiftPush(msg, ext, roomId);
    }

    public static void processRoomEffectGiftMsgFromEnterRoom(GiftRecvModel effectGiftModel) {
        GiftPushMsgProcesser.processGiftMsg(effectGiftModel, true, null);
    }

//    public static void popupGlobalGiftMsgWindow(BarrageMsg msg, BarrageMsg.GiftMsgExt msgExt, String sCurrentRoomId) {
//        MyLog.w(TAG, "processGlobalGiftMsg msg:" + msg);
//        // 已经在当前房间了，不弹窗
//        if (msg.getRoomId().equals(sCurrentRoomId)) {
//            MyLog.w(TAG, "processGlobalGiftMsg but already in room");
//            return;
//        }
//        // 提前取出来，因为以后会变
//        String msgbody = msg.getBody();
//        // 大金龙弹窗
//        Observable.create(new Observable.OnSubscribe<LiveShow>() {
//            @Override
//            public void call(Subscriber<? super LiveShow> subscriber) {
////                User anchor = UserInfoManager.getUserInfoByUuid(msg.getAnchorId(), true);
////                LiveShow liveShow = UserInfoManager.getLiveShowByUserId(msg.getAnchorId());
////                liveShow.setNickname(anchor.getNickname());
//                LiveShow liveShow = new LiveShow();
//                if (TextUtils.isEmpty(msgExt.liveStreamUrl)) {
//                    liveShow = UserInfoManager.getLiveShowByUserId(msg.getAnchorId());
//                } else {
//                    liveShow.setUrl(msgExt.liveStreamUrl);
//                }
//                subscriber.onNext(liveShow);
//                subscriber.onCompleted();
//            }
//        })
//                .filter(user -> user != null)
//                .subscribeOn(Schedulers.io())
//                .subscribeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<LiveShow>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onNext(LiveShow liveShow) {
//                        Context context = GlobalData.app();
//                        Intent intent = JumpActivity.getJumpLiveShowIntent(context,
//                                msg.getAnchorId(),
//                                liveShow.getAvatar(),
//                                msg.getRoomId(),
//                                liveShow.getUrl(),
//                                liveShow.getLocation(),
//                                liveShow.getNickname(),
//                                liveShow.getLiveType());
//                        intent.putExtra(JumpActivity.IS_FROM_GLOBAL_MSG, true);
//                        if (CommonUtils.isAppForeground(context) && !CommonUtils.isScreenLocked()) {
//                            GlobalData.globalUIHandler.post(() -> FloatNotification.getInstance().showGoToLiveTips(msgbody, 6, intent));
//                        }
//                    }
//                });
//    }
}
