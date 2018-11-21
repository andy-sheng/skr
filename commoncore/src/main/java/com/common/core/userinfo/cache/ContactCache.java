package com.common.core.userinfo.cache;


/**
 * <p>
 * 增量拉去-服务器逻辑是根据水位去服务器拉去数据，如果当前水位拉去到数据服务器返回的是全量数据
 * <p>
 * 客户端处理的逻辑是
 */
public class ContactCache {
//    private static final String TAG = "ContactsStore";
//
//    public static final int LOADING_FOLLOWING_PAGE_COUNT = 1000;
//
//    /**
//     * 双向关注的人
//     */
//    private ConcurrentHashMap<Long, BuddyCache.BuddyCacheEntry> mContactsCache = new ConcurrentHashMap<>();
//
//    /**
//     * 排过序的双向关注的人
//     */
//    private List<BuddyCache.BuddyCacheEntry> mSortedContactsList = Collections.synchronizedList(new ArrayList<BuddyCache.BuddyCacheEntry>());
//
//    /**
//     * 我关注的人(单向关注)
//     */
//    private ConcurrentHashMap<Long, BuddyCache.BuddyCacheEntry> mMyFollowingCache = new ConcurrentHashMap<>();
//
//    /**
//     * 关注我的人(单向关注)
//     */
//    private ConcurrentHashMap<Long, BuddyCache.BuddyCacheEntry> mMyFollowerCache = new ConcurrentHashMap<>();
//
//    private static ContactCache mInstance = null;
//
//    public static ContactCache getInstance() {
//        if (mInstance == null) {
//            synchronized (ContactCache.class) {
//                if (mInstance == null) {
//                    mInstance = new ContactCache();
//                }
//            }
//        }
//        return mInstance;
//    }
//
//    /**
//     * 从缓存中取 联系人
//     * @param uid
//     * @return
//     */
//    public BuddyCache.BuddyCacheEntry getFollowBothWay(long uid) {
//        return mContactsCache.get(uid);
//    }
//
//    /**
//     * 从缓存中取 我关注的
//     * @param uid
//     * @return
//     */
//    public BuddyCache.BuddyCacheEntry getMyFollowing(long uid) {
//        return mMyFollowingCache.get(uid);
//    }
//
//    /**
//     * 从缓存中取 关注我的
//     * @param uid
//     * @return
//     */
//    public BuddyCache.BuddyCacheEntry getMyFollower(long uid) {
//        return mMyFollowerCache.get(uid);
//    }
//
//    /**
//     * 全量 拉我关注的人(包括双向关注)
//     *
//     * @return
//     */
//    private void syncContacts() {
//        long uid = MyUserInfoManager.getInstance().getUid();
//        if (uid <= 0) {
//            MyLog.d(TAG, "invlide uid=" + uid);
//            return;
//        }
//
//        List<Relation> relations = RelationManager.getInstance().syncFollowingFromServer(uid, LOADING_FOLLOWING_PAGE_COUNT, 0, true, true);
//
//        if (relations != null && !relations.isEmpty() && relations.size() > 0) {
//            //因为每次都是从服务器全量返回，所以要清空
//            mContactsCache.clear();
//            mMyFollowingCache.clear();
//
//            List<BuddyCache.BuddyCacheEntry> contactList = new ArrayList<>();
//            List<BuddyCache.BuddyCacheEntry> buddyCacheEntries = new ArrayList<>();
//            for (Relation relation : relations) {
//                BuddyCache.BuddyCacheEntry entry = new BuddyCache.BuddyCacheEntry(relation);
//                if (relation.getRelative() == RelationManager.MY_FOLLOWING) {
//                    mMyFollowingCache.put(entry.getUid(), entry);
//                } else if (relation.getRelative() == RelationManager.BOTH_FOLLOWED) {
//                    if (!mContactsCache.contains(entry)) {
//                        contactList.add(entry);
//                    }
//                }
//                buddyCacheEntries.add(entry);
//            }
//
//            addToContactCache(contactList);
//            BuddyCache.getInstance().putBuddyList(buddyCacheEntries);
//        } else {
//            //TODO 服务端的total可能因为资料拿不到 total 一直小于 size。这个逻辑去掉
////                if(mContactsCache.size()<rsp.getTotal()){
////                    //说明数据出现异常了，可能本地数据没拉全，但是时间戳却更新了，这种情况，重新拉一下,更新时间戳
////                    PreferenceUtils.setSettingLong(GlobalData.app(), KEY_CONTACT_UPDATE_TS, 0);
////                    // 做好次数保护，防止服务器出异常 搞死客户端
////                    syncContactsByPage(offset,times+1);
////                }else{
//            // 因为每次都是从服务器全量返回，所以要清空
//            mContactsCache.clear();
//            mMyFollowingCache.clear();
//        }
//    }
//
//
//    private void syncFollowers() {
//        long uid = MyUserInfoManager.getInstance().getUid();
//        if (uid <= 0) {
//            MyLog.d(TAG, "invlide uid=" + uid);
//            return;
//        }
//        // 拉关注我的人(粉丝)
//        List<Relation> relations = RelationManager.getInstance().syncFollowerListFromServer(uid, LOADING_FOLLOWING_PAGE_COUNT, 0);
//        if (relations != null && !relations.isEmpty() && relations.size() > 0) {
//            //因为每次都是从服务器全量返回，所以要清空
//            mMyFollowerCache.clear();
//
//            List<BuddyCache.BuddyCacheEntry> contactList = new ArrayList<>();
//            List<BuddyCache.BuddyCacheEntry> buddyCacheEntries = new ArrayList<>();
//            for (Relation relation : relations) {
//                BuddyCache.BuddyCacheEntry entry = new BuddyCache.BuddyCacheEntry(relation);
//                if (relation.getRelative() == RelationManager.MY_FOLLOWER) {
//                    mMyFollowerCache.put(entry.getUid(), entry);
//                } else if (relation.getRelative() == RelationManager.BOTH_FOLLOWED) {
//                    if (!mContactsCache.contains(entry)) {
//                        contactList.add(entry);
//                    }
//                }
//                buddyCacheEntries.add(entry);
//            }
//
//            addToContactCache(contactList);
//            BuddyCache.getInstance().putBuddyList(buddyCacheEntries);
//        } else {
//            //TODO 服务端的total可能因为资料拿不到 total 一直小于 size。这个逻辑去掉
////                if(mContactsCache.size()<rsp.getTotal()){
////                    //说明数据出现异常了，可能本地数据没拉全，但是时间戳却更新了，这种情况，重新拉一下,更新时间戳
////                    PreferenceUtils.setSettingLong(GlobalData.app(), KEY_CONTACT_UPDATE_TS, 0);
////                    // 做好次数保护，防止服务器出异常 搞死客户端
////                    syncContactsByPage(offset,times+1);
////                }else{
//            //因为每次都是从服务器全量返回，所以要清空
//            mMyFollowerCache.clear();
//        }
//    }
//
//    /**
//     * 添加到联系人cache
//     *
//     * @param list
//     */
//    private void addToContactCache(List<BuddyCache.BuddyCacheEntry> list) {
//        if (list != null) {
//            MyLog.d(TAG, "addToContactCache list.size:" + list.size());
//        }
//
//        // 为空也继续触发刷新
//        for (BuddyCache.BuddyCacheEntry entry : list) {
//            mContactsCache.put(entry.getUid(), entry);
//        }
//
//        List<SortT> sortList = new ArrayList<>();
//        List<BuddyCache.BuddyCacheEntry> list2 = new ArrayList<>(mContactsCache.values());
//        for (BuddyCache.BuddyCacheEntry entry : list2) {
//            sortList.add(new SortT(entry, entry.getNickName()));
//        }
//
//        Collections.sort(sortList, new Comparator<SortT>() {
//            @Override
//            public int compare(SortT a, SortT b) {
//                return 1;
//            }
//        });
//
//        mSortedContactsList.clear();
//        for (SortT entry : sortList) {
//            mSortedContactsList.add(entry.mEntry);
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.POSTING)
//    public void onEvent(MiLinkEvent.StatusLogined event) {//sync
//        if (event != null) {
//            if (MiLinkClientAdapter.getInstance().isMiLinkLogined()
//                    && UserAccountManager.getInstance().hasAccount()) {
//                MyLog.w(TAG, "MiLinkEvent.StatusLogined and postTask");
//                syncContacts();
//                syncFollowers();
//            }
//        }
//    }
//
//    /**
//     * 为了防止 getDisplayName 一开始没有备注名，排序的过程中获得备注，导致的排序不满足传递性 导致的崩溃
//     * 所以用一个类 这个 sortname 一旦确定就不会变
//     */
//    static class SortT {
//        public BuddyCache.BuddyCacheEntry mEntry;
//        public String sortName;
//
//        public SortT(BuddyCache.BuddyCacheEntry entry, String sortName) {
//            mEntry = entry;
//            this.sortName = sortName;
//        }
//    }
}
