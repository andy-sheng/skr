package com.wali.live.watchsdk.channel.util;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.CommonUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.BannerProto;
import com.wali.live.utils.AsyncTaskUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理广告banner
 * Created by yaojian on 16-3-11.
 */
public class BannerManger {
    private final static String TAG = "BannerManager";

    //广告更新时间戳
    private final static String BANNER_LAST_UPDATE_TIMESTAMP = "bannerLastUpdateTs";

    //广告
    private static ConcurrentHashMap<Integer, List<BannerItem>> mBannerListMap = new ConcurrentHashMap<>();

    /**
     * 广告位
     */
    public static class BannerItem implements Parcelable {
        public String picUrl;       //图片地址
        public String skipUrl;      //图片跳转地址
        public long lastUpdateTs;   //修改时间
        public int bannerId;        //id
        public String shareIconUrl; //分享时用得图片的url
        public String shareTitle;   //分享时用得标题
        public String shareDesc;    //分享时用的摘要
        public int channelId;       //对应的频道，1为热门

        protected BannerItem(Parcel in) {
            picUrl = in.readString();
            skipUrl = in.readString();
            lastUpdateTs = in.readLong();
            bannerId = in.readInt();
            shareIconUrl = in.readString();
            shareTitle = in.readString();
            shareDesc = in.readString();
            channelId = in.readInt();
        }

        public BannerItem() {
        }

        public static final Creator<BannerItem> CREATOR = new Creator<BannerItem>() {
            @Override
            public BannerItem createFromParcel(Parcel in) {
                return new BannerItem(in);
            }

            @Override
            public BannerItem[] newArray(int size) {
                return new BannerItem[size];
            }
        };

        @Override
        public String toString() {
            return "BannerItem{" +
                    "picUrl='" + picUrl + '\'' +
                    ", skipUrl='" + skipUrl + '\'' +
                    ", lastUpdateTs=" + lastUpdateTs +
                    ", bannerId=" + bannerId +
                    ", shareIconUrl='" + shareIconUrl + '\'' +
                    ", shareTitle='" + shareTitle + '\'' +
                    ", shareDesc='" + shareDesc + '\'' +
                    ", channelId=" + channelId +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(picUrl);
            dest.writeString(skipUrl);
            dest.writeLong(lastUpdateTs);
            dest.writeInt(bannerId);
            dest.writeString(shareIconUrl);
            dest.writeString(shareTitle);
            dest.writeString(shareDesc);
            dest.writeInt(channelId);
        }
    }

    /**
     * 得到所有的广告
     */
    public static List<BannerItem> getAllBannersByChannelId(int channelId) {
        List<BannerItem> l = mBannerListMap.get(channelId);
        List<BannerItem> re = new ArrayList<>();
        if (l != null) {
            re.addAll(l);
        }
        MyLog.d(TAG, "getAllBannersByChannelId id=" + channelId + ",list=" + re);
        return re;
    }

    /**
     * 从server拉取广告. WARNNING : 有网络请求, 在异步线程里调用
     */
    public static void fetchBannerFromServer() {
        // 中文环境不拉banner
        MyLog.d(TAG, "fetchBannerFromServer isChinese = " + CommonUtils.isChinese());
        if (CommonUtils.isChinese()) {
            return;
        }
        BannerProto.SyncBannerReq req = BannerProto.SyncBannerReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setLastUpdateTs(0).build();

        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_BANNER_SYNC);
        data.setData(req.toByteArray());
        MyLog.d(TAG + " request : \n" + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);

        if (rspData == null) {
            MyLog.v(TAG + " fetchBannerFromServer rspData == null");
            return;
        }
        try {
            BannerProto.SyncBannerRsp rsp = BannerProto.SyncBannerRsp.parseFrom(rspData.getData());
            MyLog.v(TAG + " fetchBannerFromServer rsp == " + rsp.toString());
            if (rsp.getRetCode() == 0) {  //0表示成功
                List<BannerProto.Banner> returnList = rsp.getBannerList();

                synchronized (BannerManger.class) {
                    //清空本地
                    mBannerListMap.clear();

                    if (returnList != null && returnList.size() > 0) {
                        for (int i = 0; i < returnList.size(); ++i) {

                            BannerProto.Banner tmp = returnList.get(i);
                            if (tmp != null) {
                                BannerItem banner = new BannerItem();
                                banner.picUrl = tmp.getPicUrl();
                                banner.skipUrl = tmp.getSkipUrl();
                                banner.lastUpdateTs = tmp.getLastUpdateTs();
                                banner.bannerId = tmp.getId();

                                banner.shareIconUrl = tmp.getShareIconUrl();
                                banner.shareTitle = tmp.getShareTitle();
                                banner.shareDesc = tmp.getShareDesc();

                                banner.channelId = tmp.getChannelId();
                                if (mBannerListMap.get(banner.channelId) == null) {
                                    mBannerListMap.put(banner.channelId, new ArrayList<BannerItem>());
                                }
                                List l = mBannerListMap.get(banner.channelId);
                                if (l != null) {
                                    l.add(banner);
                                }
                            }
                        }

                        for (int key : mBannerListMap.keySet()) {
                            List l = mBannerListMap.get(key);
                            if (null != l) {
                                MyLog.d(TAG, "mBannerListMap key:" + key + ",size:" + l.size());
                            }
                        }

                    } else {

                    }
                }
                long time = rsp.getLastUpdateTs();
                PreferenceUtils.setSettingLong(BANNER_LAST_UPDATE_TIMESTAMP, time);
            } else {
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * 异步的拉取banner
     */
    public static void fetchBannerFromServerAsync() {
        AsyncTaskUtils.exeNetWorkTask(new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                fetchBannerFromServer();
                return null;
            }
        });
    }
}
