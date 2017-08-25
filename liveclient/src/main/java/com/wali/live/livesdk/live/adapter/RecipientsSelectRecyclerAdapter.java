package com.wali.live.livesdk.live.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.fragment.RxFragment;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.pinyin.PinyinUtils;
import com.base.utils.CommonUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.FragmentEvent;
import com.wali.live.dao.Relation;

import com.wali.live.dao.RelationDaoAdapter;
import com.wali.live.event.EventClass;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.fragment.RecipientsSelectFragment;
import com.wali.live.livesdk.live.view.IndexableRecyclerView;
import com.wali.live.livesdk.live.view.UserSectionIndexer;
import com.wali.live.proto.RelationProto;
import com.wali.live.utils.AsyncTaskUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.utils.relation.RelationUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yurui on 3/7/16.
 * @module 选人
 */
public class RecipientsSelectRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = RecipientsSelectRecyclerAdapter.class.getSimpleName();
    public static final int PAGE_COUNT = 15;
    private long userId = UserAccountManager.getInstance().getUuidAsLong();
    private String roomId;

    private List<Long> alreadyList;
    //搜索相关 处于搜索模式不会loadmore
    private boolean searchMode = false;
    private String mKeyword = "";

    public static final int ITEM_TYPE_FOOTER = 101;    //列表的footer  展示loadmore正在加载 和 列表为空的状态

    public static final int ITEM_TYPE_FOLLOWING = 0;   //关注的item

    public static final int ITEM_TYPE_MANAGER = 1;     //管理员

    public static final int ITEM_TYPE_ROOM_FRIENDS = 2;     //本房间的关注好友

    private boolean hasMoreItem = true;

    private int selMaxCnt;

    private int itemNormalType = ITEM_TYPE_FOLLOWING;

    private int mode = RecipientsSelectFragment.SELECT_MODE_SINGLE_CLICK;

    private List<Object> mDataList = new ArrayList<>();

    private List<Object> mDataListSearch = new ArrayList<>();

    private List<Long> selList = new ArrayList<>();

    private Object selectItem;

    private View mFooter, mCover;
    private RecipientsSelectFragment mFragment;
    private int mOffset = 0;
    public UserSectionIndexer mUserSectionIndexer;
    //是否正在加载数据
    private boolean mIsLoading = false;
    private boolean mIsDbLoading = false;
    private boolean mNeedSearch = false;
    List<Relation> relationListFromDb;

    public Object getSelectItem() {
        return selectItem;
    }

    public void setMaxSelectCount(int count) {
        selMaxCnt = count;
    }

    private boolean showLvlSx = true;

    public void setShowLevelSex(boolean b) {
        showLvlSx = b;
    }

    private boolean bothWay = true;//是否只显示双向关注

    private boolean mShowIndex = true;

    public void setBothWay(boolean b) {
        bothWay = b;
    }

    public void setCoverView(View v) {
        mCover = v;
    }

    public void setShowIndex(boolean showIndex) {
        mShowIndex = showIndex;
    }

    public EditText editText;
    IndexableRecyclerView recyclerView;

    private boolean mIsPrivateLive = false;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_FOOTER:
                if (mFooter == null) {
                    mFooter = LayoutInflater.from(GlobalData.app()).inflate(R.layout.private_live_invite_people_loading, parent, false);
                }
                return new UserListDataHolder(mFooter);
            case ITEM_TYPE_MANAGER:
            case ITEM_TYPE_FOLLOWING:
            case ITEM_TYPE_ROOM_FRIENDS:
                break;
        }
        View view = LayoutInflater.from(GlobalData.app()).inflate(R.layout.user_list_cell, parent, false);

        return new UserListDataHolder(view);
    }

    public RecipientsSelectRecyclerAdapter(RecipientsSelectFragment frag, IndexableRecyclerView recyclerView) {
        mFragment = frag;
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!ViewCompat.canScrollVertically(recyclerView, 1)) {
                    if (!mIsLoading) {
                        loadMoreData();
                    }
                }
            }
        });
        mUserSectionIndexer = new UserSectionIndexer();
        this.recyclerView = recyclerView;
    }

    public List<Long> getResultList() {
        if (alreadyList != null) {
            selList.removeAll(alreadyList);
        }
        return selList;
    }

    public void setItemTypeAndLoadData(int type, int mode, String roomId) {
        itemNormalType = type;
        this.mode = mode;
        this.roomId = roomId;
        loadData();
    }

    //第一次初始化 需要加载数据
    private void loadData() {
        if (itemNormalType != ITEM_TYPE_ROOM_FRIENDS) {
            loadRelationDbData(bothWay && itemNormalType == ITEM_TYPE_FOLLOWING);
        }
        if (getDataSize() == 0) {
            loadDataFromServer(userId, PAGE_COUNT, mOffset);
        } else {
            notifyDataSetChanged();
            changeCover();
        }
    }

    private void loadMoreData() {
        if (hasMore()) {
            loadDataFromServer(userId, PAGE_COUNT, mOffset);
        }
    }

    public void loadRelationDbData(final boolean bothWay) {

        if (mIsDbLoading) {
            return;
        }
        AsyncTaskUtils.exeIOTask(new AsyncTask<Object, Object, List<Object>>() {

            @Override
            protected List<Object> doInBackground(Object... params) {
                List<Object> list = new ArrayList<>();
                relationListFromDb = RelationDaoAdapter.getInstance().getRelationList();
                if (relationListFromDb != null && relationListFromDb.size() > 0) {
                    for (Relation relation : relationListFromDb) {

                        if (itemNormalType == ITEM_TYPE_MANAGER) {
                            if (LiveRoomCharacterManager.getInstance().isManager(relation.getUserId())) {
                                continue;
                            }
                        }
                        if (bothWay) {
                            if (relation.getIsBothway()) {
                                list.add(new UserListData(relation));
                            }
                        } else {
                            if (relation.getIsFollowing()) {
                                list.add(new UserListData(relation));
                            }
                        }
                    }
                }
                list = sortFollowingData(list);
                return list;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mIsDbLoading = true;
                changeCover();
            }

            @Override
            protected void onPostExecute(List<Object> result) {
                super.onPostExecute(result);
                mIsDbLoading = false;
                if (result != null && result.size() > 0) {
                    setData(result);
                    changeCover();
                }

            }

        });
    }


    private boolean hasMore() {
        return hasMoreItem && !searchMode;
    }

    public List<Object> sortFollowingData(List<Object> list) {

        if (itemNormalType == ITEM_TYPE_FOLLOWING || itemNormalType == ITEM_TYPE_MANAGER
                || itemNormalType == ITEM_TYPE_ROOM_FRIENDS) {
            List<UserListData> mDataListTemp = new ArrayList<UserListData>();
            for (Object object : list) {
                mDataListTemp.add((UserListData) object);
            }

            // 按照字母排序的
            Collections.sort(mDataListTemp, new Comparator<UserListData>() {
                @Override
                public int compare(UserListData lb, UserListData rb) {
                    return CommonUtils.sortName(lb.userNickname, rb.userNickname);

                }
            });
            list.clear();
            list.addAll(mDataListTemp);
        }
        return list;

    }

    public void setData(List<Object> list) {
        if (null != list) {
            //mDataList = sortFollowingData(list);
            mDataList = list;
            mUserSectionIndexer.setDataList(mDataList);
            if (mShowIndex) {
                recyclerView.setSectionIndexer(mUserSectionIndexer);
                if (list.size() > 0) {
                    recyclerView.showIndexBar();
                    recyclerView.enableScrollListener(true);
                } else {
                    recyclerView.hideIndexBar();
                }
            }
            notifyDataSetChanged();
            if (getDataSize() == 0) {
                loadMoreData();
            }
        }
    }

    public void addData(List<Object> list) {
        if (null != list) {
            List<Object> mDataListCopy = new ArrayList<>(mDataList);
            mDataListCopy.addAll(list);
            //sortFollowingData(mDataListCopy);
            mDataList = mDataListCopy;
            mUserSectionIndexer.setDataList(mDataList);
            notifyDataSetChanged();
            if (getDataSize() == 0) {
                loadMoreData();
            }
        }
    }

    public Object getData(int position) {
        if (position >= 0 && position < getDataSize()) {
            if (!searchMode) {
                return mDataList.get(position);
            } else {
                return mDataListSearch.get(position);
            }
        }
        return null;
    }

    private int getDataSize() {
        if (!searchMode) {
            return mDataList.size();
        } else {
            return mDataListSearch.size();
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (hasMore() && getDataSize() > 0) {
            count++;
        }
        if (!searchMode) {
            return getDataSize() + count;
        } else {
            return getDataSize();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getDataSize()) {
            return ITEM_TYPE_FOOTER;
        }
        return itemNormalType;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (getItemViewType(position) == ITEM_TYPE_FOOTER) {
            return;
        }

        if (!(viewHolder instanceof UserListDataHolder)) {
            return;
        }
        final UserListDataHolder holder = (UserListDataHolder) viewHolder;
        final UserListData item = (UserListData) getData(position);
        if (item == null) {
            MyLog.v("getData null");
            return;
        }
        AvatarUtils.loadAvatarByUidTs(holder.avatarIv, item.userId, item.avatar, true);
        holder.userNameTv.setText(!TextUtils.isEmpty(item.userNickname) ? item.userNickname : String.valueOf(item.userId));
//            if (!TextUtils.isEmpty(item.signature)) {
//                holder.signTv.setText(item.signature);
//            } else {
        holder.signTv.setVisibility(View.GONE);
//            }
        if (showLvlSx) {
            GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(item.level);
            holder.levelTv.setText(String.valueOf(item.level + ""));
            holder.levelTv.setBackgroundDrawable(levelItem.drawableBG);
            holder.levelTv.setCompoundDrawables(levelItem.drawableLevel, null, null, null);

            holder.imgGenderIv.setVisibility(View.VISIBLE);
            if (item.gender == User.GENDER_MAN) {
                holder.imgGenderIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_man));
            } else if (item.gender == User.GENDER_WOMAN) {
                holder.imgGenderIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_women));
            } else {
                holder.imgGenderIv.setVisibility(View.GONE);
            }
            if (item.certificationType > 0) {
                holder.badgeIv.setVisibility(View.GONE);
                holder.badgeVipIv.setVisibility(View.VISIBLE);
                holder.badgeVipIv.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(item.certificationType));
            } else {
                holder.badgeIv.setVisibility(View.GONE);
                holder.badgeVipIv.setVisibility(View.GONE);
            }
        } else {
            holder.levelTv.setVisibility(View.GONE);
            holder.imgGenderIv.setVisibility(View.GONE);
            if (item.certificationType > 0) {
                holder.badgeIv.setVisibility(View.GONE);
                holder.badgeVipIv.setVisibility(View.VISIBLE);
                holder.badgeVipIv.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(item.certificationType));
            } else {
                holder.badgeIv.setVisibility(View.VISIBLE);
                holder.badgeVipIv.setVisibility(View.GONE);
                holder.badgeIv.setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(item.level));
            }
        }
        switch (itemNormalType) {
            case ITEM_TYPE_FOLLOWING:
            case ITEM_TYPE_MANAGER:
            case ITEM_TYPE_ROOM_FRIENDS:
                if (mode == RecipientsSelectFragment.SELECT_MODE_SINGLE_CLICK) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.putExtra(RecipientsSelectFragment.RESULT_SINGLE_OBJECT, new User(item.userId, item.userNickname, item.level, item.avatar, item.certificationType));
                            if (editText != null) {
                                KeyboardUtils.hideKeyboard(mFragment.getActivity(), editText);
                            }
                            EventBus.getDefault().post(new EventClass.OnActivityResultEvent(mFragment.requestCode, Activity.RESULT_OK, intent));
                        }
                    });
                } else if (mode == RecipientsSelectFragment.SELECT_MODE_MULTI) {
                    holder.checkbox.setVisibility(View.VISIBLE);
                    if (alreadyList.contains(item.userId)) {
                        holder.itemView.setEnabled(false);
                        // TODO 变成灰色的对勾
                        holder.checkbox.setSelected(true);
                        return;
                    } else {
                        holder.itemView.setEnabled(true);
                        holder.checkbox.setSelected(false);
                    }
                    holder.checkbox.setChecked(selList.contains(item.userId));
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mFragment.getActivity() != null) {
                                KeyboardUtils.hideKeyboard(mFragment.getActivity());
                            }
                            if (selList.contains(item.userId)) {
                                EventBus.getDefault().post(new EventClass.ChangeBottomInvitee(item.userId, item.avatar, false));
                                selList.remove(item.userId);
                                holder.checkbox.setChecked(false);
                            } else {
                                if (selList.size() < selMaxCnt) {
                                    EventBus.getDefault().post(new EventClass.ChangeBottomInvitee(item.userId, item.avatar, true));
                                    selList.add(item.userId);
                                    holder.checkbox.setChecked(true);
                                } else {
                                    ToastUtils.showToast(GlobalData.app(), mFragment.getActivity().getResources().getString(R.string.sel_count_hint, selMaxCnt));
                                }
                            }

                            if (mFragment != null) {
                                mFragment.mTitleBar.getRightTextBtn().setEnabled(selList.size() > 0);
                                mFragment.mTitleBar.getRightTextBtn().setText(mFragment.getActivity().getResources().getString(R.string.match_ok_btn, selList.size(), selMaxCnt));
                            }
                        }
                    });

                } else if (mode == RecipientsSelectFragment.SELECT_MODE_SINGLE_SELECT) {
                    if (itemNormalType == ITEM_TYPE_ROOM_FRIENDS) {
                        holder.checkbox.setVisibility(View.VISIBLE);
                        if (selectItem != null && item.userId == ((UserListData) selectItem).userId) {
                            holder.checkbox.setChecked(true);
                        } else {
                            holder.checkbox.setChecked(false);
                        }
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (selectItem == null || item.userId != ((UserListData) selectItem).userId) {
                                    selectItem = item;
                                }
                                notifyDataSetChanged();
                                if (mFragment != null) {
                                    mFragment.mTitleBar.getRightTextBtn().setEnabled(true);
                                }
                            }
                        });
                    } else {
                        holder.checkbox.setVisibility(View.VISIBLE);
                        if (selectItem != null && item.userId == ((UserListData) selectItem).userId) {
                            holder.checkbox.setChecked(true);
                        } else {
                            holder.checkbox.setChecked(false);
                        }
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (selectItem == null || item.userId != ((UserListData) selectItem).userId) {
                                    selectItem = item;
                                }
                                notifyDataSetChanged();
                                if (mFragment != null) {
                                    mFragment.mTitleBar.getRightTextBtn().setEnabled(true);
                                }
                            }
                        });
                    }
                }
                break;
        }

    }

    class UserListDataHolder extends RecyclerView.ViewHolder {
        BaseImageView avatarIv;
        TextView userNameTv;
        TextView signTv;
        TextView levelTv;
        CheckBox checkbox;
        ImageView stateBtn;
        ImageView badgeIv;
        ImageView badgeVipIv;
        ImageView imgGenderIv;
        View clickArea;

        UserListDataHolder(View view) {
            super(view);
            if (mFooter == view) {
                return;
            }
            avatarIv = (BaseImageView)view.findViewById(R.id.user_list_avatar);
            userNameTv = (TextView)view.findViewById(R.id.txt_username);
            signTv = (TextView)view.findViewById(R.id.txt_tip);
            levelTv = (TextView)view.findViewById(R.id.level_tv);
            checkbox = (CheckBox)view.findViewById(R.id.checkbox);
            stateBtn = (ImageView)view.findViewById(R.id.img_follow_state);
            badgeIv = (ImageView)view.findViewById(R.id.img_badge);
            badgeVipIv = (ImageView)view.findViewById(R.id.img_badge_vip);
            imgGenderIv = (ImageView)view.findViewById(R.id.img_gender);
            clickArea = view.findViewById(R.id.btn_area);
        }
    }

    private int total = -1;

    public void loadDataFromServer(final long uuid, final int pageCount, final int offset) {
        if (!mIsLoading) {
            mIsLoading = true;
            changeCover();
            Observable.just("")
                    .observeOn(Schedulers.io())
                    .flatMap(new Func1<String, Observable<List<Object>>>() {
                        @Override
                        public Observable<List<Object>> call(String item) {

                            switch (itemNormalType) {
                                case ITEM_TYPE_FOLLOWING: {
                                    List<Object> dataList = new ArrayList<Object>();
                                    total = RelationUtils.loadFollowingData(uuid, RelationUtils.LOADING_FOLLOWING_PAGE_COUNT, offset, dataList, bothWay);
                                    dataList = sortFollowingData(dataList);
                                    return Observable.just(dataList);
                                }

                                case ITEM_TYPE_MANAGER: {
                                    List<Object> dataList = new ArrayList<Object>();
                                    total = RelationUtils.loadFollowingData(uuid, RelationUtils.LOADING_FOLLOWING_PAGE_COUNT, offset, dataList, false);
                                    dataList = sortFollowingData(dataList);
                                    return Observable.just(dataList);
                                }
                                case ITEM_TYPE_ROOM_FRIENDS: {
                                    RelationProto.MicUserListResponse response = RelationUtils.getMICUserListResponse(uuid, roomId);
                                    if (response != null && response.getCode() == ErrorCode.CODE_SUCCESS) {
                                        return Observable.just(UserListData.parseUserList(response));
                                    }
                                }

                            }
                            return Observable.just(null);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(((RxFragment) mFragment).bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {
                            mIsLoading = false;
                            if (mFragment == null || mFragment.getActivity() == null || mFragment.getActivity().isFinishing()) {
                                return;
                            }

                            if (o != null) {
                                //hasMoreItem = offset + pageCount < total && itemNormalType != ITEM_TYPE_MANAGER;
                                List<Object> dataList = (List<Object>)o;
                                hasMoreItem = dataList.size() < total;
                                mOffset = dataList.size();

                                if (itemNormalType == ITEM_TYPE_MANAGER) {
                                    List<Object> list = new ArrayList<>();
                                    for (Object obj : dataList) {
                                        UserListData item = (UserListData) obj;
                                        if (LiveRoomCharacterManager.getInstance().isManager(item.userId)) {
                                            list.add(obj);
                                        }
                                    }
                                    for (Object obj : list) {
                                        dataList.remove(obj);
                                    }
                                }
                                if (offset == 0) {
                                    setData(dataList);
                                } else {
                                    addData(dataList);
                                }
                            }
                            changeCover();
                            if (mNeedSearch) {
                                mNeedSearch = false;
                                doSearch();
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            mIsLoading = false;
                        }
                    }, new Action0() {
                        @Override
                        public void call() {
                            mIsLoading = false;
                        }
                    });

        }
    }

    private void doSearch() {
        if (!mIsLoading) {
            if (mDataList != null && mDataList.size() > 0) {
                mIsLoading = true;
                Observable.just("")
                        .observeOn(Schedulers.io())
                        .flatMap(new Func1<String, Observable<List<Object>>>() {
                            @Override
                            public Observable<List<Object>> call(String item) {
                                List<Object> searchDataSource = new ArrayList<>();
                                for (Object o : mDataList) {
                                    UserListData u = (UserListData) o;
                                    if ((u.userId + "").contains(mKeyword) || u.userNickname.contains(mKeyword) || PinyinUtils.hanziToPinyin(u.userNickname).contains(mKeyword)) {
                                        searchDataSource.add(o);
                                    }
                                }
                                return Observable.just(searchDataSource);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(((RxActivity) mFragment.getActivity()).bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                mIsLoading = false;
                                if (mFragment == null || mFragment.getActivity() == null || mFragment.getActivity().isFinishing()) {
                                    return;
                                }
                                mIsLoading = false;
                                if (mNeedSearch) {
                                    doSearch();
                                } else {
                                    mDataListSearch = (List<Object>)o;
                                    notifyDataSetChanged();
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                mIsLoading = false;
                            }
                        }, new Action0() {
                            @Override
                            public void call() {
                                mIsLoading = false;
                            }
                        });
            } else {
                changeCover();
            }
        } else {
            mNeedSearch = true;
        }
    }

    public void search(String key) {
        mKeyword = key;
        if (!TextUtils.isEmpty(mKeyword)) {
            searchMode = true;
            doSearch();
        } else {
            searchMode = false;
            mNeedSearch = false;
            notifyDataSetChanged();
        }
    }

    private void changeCover() {
        boolean showLoading = mIsLoading && getDataSize() == 0;
        boolean showEmpty = !mIsLoading && getDataSize() == 0;
        if (mCover != null) {
            View loadingView = mCover.findViewById(R.id.loading);
            View emptyView = mCover.findViewById(R.id.empty);
            if (loadingView != null) {
                if (showLoading) {
                    loadingView.setVisibility(View.VISIBLE);
                } else {
                    loadingView.setVisibility(View.INVISIBLE);
                }
            }
            if (emptyView != null) {
                if (showEmpty) {
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public void setCancelBootom(long id) {
        selList.remove(id);
        notifyDataSetChanged();
    }

}
