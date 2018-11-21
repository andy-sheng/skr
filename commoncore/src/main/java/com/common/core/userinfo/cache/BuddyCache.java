package com.common.core.userinfo.cache;


/**
 * 用户资料的cache
 * 包含关注的好友资料,还包括陌生人资料
 */
public class BuddyCache {

//    private final static String TAG = "BuddyCache";
//    /**
//     * 缓存
//     */
//    private LruCache<Long, BuddyCacheEntry> mLruCache = null;
//
//    private volatile static BuddyCache sInstance = null;
//
//    private final int MAX_SIZE = 1000;
//
//    private BuddyCache() {
//        mLruCache = new LruCache<>(MAX_SIZE);
//    }
//
//    public static synchronized BuddyCache getInstance() {
//        if (sInstance == null) {
//            sInstance = new BuddyCache();
//        }
//
//        return sInstance;
//    }
//
//    /**
//     * 获取buddy
//     *
//     * @param uuid
//     * @param queryIfNotExist 是否去服务器查询
//     * @return
//     */
//    public BuddyCacheEntry getBuddyNormal(final long uuid, boolean queryIfNotExist) {
//        BuddyCacheEntry result = mLruCache.get(uuid);
//        if (result != null) {
//            return result;
//        } else {
//            // 本地数据库
//            syncBuddyInfoFromDB(uuid);
//            if (mLruCache.get(uuid) != null) {
//                return mLruCache.get(uuid);
//            }
//            if (queryIfNotExist) {
//                // 网络查询
//                syncBuddyInfoFromServer(uuid);
//                result = mLruCache.get(uuid);
//            }
//        }
//        return result;
//    }
//
//    /**
//     * 从DB里面去找
//     *
//     * @param uuid
//     */
//    public void syncBuddyInfoFromDB(final long uuid) {
//        Observable.create(new ObservableOnSubscribe<Relation>() {
//            @Override
//            public void subscribe(ObservableEmitter<Relation> emitter) throws Exception {
//                Relation relation = RelationManager.getInstance().getRelationFromDB(uuid);
//                if (relation != null) {
//                    BuddyCacheEntry buddyCacheEntry = new BuddyCacheEntry(relation.getUserId(), relation.getUserNickname(), relation.getAvatar());
//                    putBuddy(buddyCacheEntry);
//                }
//                emitter.onComplete();
//            }
//        }).subscribeOn(Schedulers.io())
//                .subscribe();
//
//    }
//
//    /**
//     * 从服务器里面查
//     *
//     * @param uuid
//     */
//    public void syncBuddyInfoFromServer(final long uuid) {
//
//    }
//
//    /**
//     * 加入缓存
//     *
//     * @param buddyCacheEntryList
//     */
//    public void putBuddyList(final List<BuddyCacheEntry> buddyCacheEntryList) {
//        if (null != buddyCacheEntryList && buddyCacheEntryList.size() > 0) {
//            for (BuddyCacheEntry buddyCacheEntry : buddyCacheEntryList) {
//                mLruCache.put(buddyCacheEntry.uuid, buddyCacheEntry);
//            }
//            if (mLruCache.size() > MAX_SIZE) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    mLruCache.trimToSize(MAX_SIZE / 2);
//                }
//            }
//        }
//    }
//
//    public void putBuddy(BuddyCacheEntry buddyCacheEntry) {
//        if (buddyCacheEntry == null) {
//            MyLog.w(TAG + " put entry == null");
//            return;
//        }
//
//        BuddyCacheEntry entry1 = getBuddyNormal(buddyCacheEntry.uuid, false);
//        if (buddyCacheEntry.same(entry1)) {
//            // 已经存在了,不做操作
//            return;
//        }
//
//        mLruCache.put(entry1.uuid, buddyCacheEntry);
//    }
//
//
//    /**
//     * cache entry, 字段较少,主要缓存常用的字段
//     */
//    public static class BuddyCacheEntry {
//
//        private long uuid;
//        private String nickname;
//        private long avatarTimestamp;
//
//        public String getNickName() {
//            return nickname;
//        }
//
//        public long getUid() {
//            return uuid;
//        }
//
//        public long getAvatarTimestamp() {
//            return avatarTimestamp;
//        }
//
//        public BuddyCacheEntry(long uid) {
//            uuid = uid;
//        }
//
//        public BuddyCacheEntry(long uid, String name, long avatar) {
//            uuid = uid;
//            nickname = name;
//            avatarTimestamp = avatar;
//        }
//
//        public BuddyCacheEntry(Relation relation) {
//            uuid = relation.getUserId();
//            nickname = relation.getUserNickname();
//            avatarTimestamp = relation.getAvatar();
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (o == this) {
//                return true;
//            }
//
//            if (!(o instanceof BuddyCacheEntry)) {
//                return false;
//            }
//
//            BuddyCacheEntry data = (BuddyCacheEntry) o;
//            return this.uuid == data.uuid;
//        }
//
//        @Override
//        public String toString() {
//            return "BuddyCacheEntry{" +
//                    "uuid=" + uuid +
//                    ", nickname='" + nickname + '\'' +
//                    ", avatarTimestamp=" + avatarTimestamp +
//                    '}';
//        }
//
//        @Override
//        public int hashCode() {
//            int result = 17;
//            result = 31 * result + (int) (this.uuid ^ (this.uuid >>> 32));
//            return result;
//        }
//
//        public boolean same(BuddyCacheEntry entry1) {
//            if (entry1 != null
//                    && entry1.uuid == uuid
//                    && entry1.avatarTimestamp == avatarTimestamp
//                    && entry1.nickname.equals(nickname)) {
//                return true;
//            }
//            return false;
//        }
//    }

}
