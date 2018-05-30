package com.wali.live.watchsdk.ranking.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.common.barrage.view.utils.FansInfoUtils;
import com.wali.live.common.listener.OnItemClickListener;
import com.wali.live.common.listener.OnItemLongClickListener;
import com.wali.live.dao.RelationDaoAdapter;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.utils.relation.RelationUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.rank.data.RankFansData;
import com.wali.live.watchsdk.ranking.data.RankUserData;
import com.wali.live.watchsdk.view.EmptyView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zhaomin on 16-10-17.
 */
public class RankRecyclerViewAdapter extends RecyclerView.Adapter {
    public static final String TAG = RankRecyclerViewAdapter.class.getSimpleName();

    protected static int[] mIconDrawableList = {R.drawable.anchor_rank_list_item_icon_1, R.drawable.anchor_rank_list_item_icon_2, R.drawable.anchor_rank_list_item_icon_3};
    protected static int[] mIconBgList = {R.drawable.anchor_rank_round_bg_top1, R.drawable.anchor_rank_round_bg_top2, R.drawable.anchor_rank_round_bg_top3};

    private static final int TYPE_EMPTY = 9999;
    private static final int TYPE_HEADER_TOP_3 = -1;
    private static final int TYPE_HEADER_TOTAL_NUM = -2;
    private static final int TYPE_FOOTER = Integer.MIN_VALUE;
    private static final int TYPE_ITEM_BIG_HEIGHT = 0;
    private static final int TYPE_ITEM = 1;

    public final static String TOTAL_RANK = "total";
    public final static String CURRENT_RANK = "current";

    private int mNameTextMaxWidth = DisplayUtils.dip2px(93.3f); // 最大长度 有直播中
    private int mNameTextMaxWidthNoLiveIcon = mNameTextMaxWidth + DisplayUtils.dip2px(30); //没live Icon 的最大长度
    private int mFansNameTextMaxWidth = DisplayUtils.dip2px(129f); // 宠爱团排行榜 昵称的长度

    private String type;
    private RxActivity mAct;

    private boolean mEnableFollow = true;

    private List<UserListData> mRankUserList = new ArrayList<>();

    private int mTotalNum;      // 总的星票数
    private String mEmptyHint;  // 设置空页面提示文案

    public RankRecyclerViewAdapter(RxActivity mAct, String type) {
        this.mAct = mAct;
        this.type = type;
    }

    private boolean mShowTopThreeHeader; // top 3 header
    private boolean mShowTotalNumHeader;  // 总共多少星票 header

    protected OnItemClickListener mClickListener;
    protected OnItemLongClickListener mLongClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongClickListener = listener;
    }

    public void setShowTotalNumHeader(boolean mShowTotalNumHeader) {
        this.mShowTotalNumHeader = mShowTotalNumHeader;
    }

    public void setEmptyHint(String mEmptyHint) {
        this.mEmptyHint = mEmptyHint;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        if (viewType == TYPE_HEADER_TOP_3) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.anchor_rank_list_total_top_3, viewGroup, false);
            return new AnchorRankingTopHolder(view);
        } else if (viewType == TYPE_HEADER_TOTAL_NUM) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.vote_ranking_total_item, viewGroup, false);
            return new RankingTotalHolder(view);
        } else if (viewType == TYPE_EMPTY) {
            view = LayoutInflater.from(GlobalData.app()).inflate(R.layout.empty_view, viewGroup, false);
            return new EmptyViewHolder(view);
        } else if (viewType == TYPE_ITEM_BIG_HEIGHT) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.anchor_rank_list_item_big, viewGroup, false);
            return new AnchorRankingViewHolder(view);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.anchor_rank_list_item, viewGroup, false);
            return new AnchorRankingViewHolder(view);
        }
    }

    private void setMaxTextWidth(final AnchorRankingViewHolder holder) {
        if (mNameTextMaxWidth == 0) {
            holder.itemView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    holder.itemView.getViewTreeObserver().removeOnPreDrawListener(this);
                    // 设置NAME area的宽度
                    int totalAreaWidth = holder.nameAreaRl.getWidth();
                    int otherWidth = holder.liveIcon.getWidth() + holder.levelTv.getWidth() + holder.imgGenderIv.getWidth() + DisplayUtils.dip2px(5.67f) + 10; // 5.67是margin， 多余的10px 是因为level标识大小不一样，不想每次都计算
                    holder.nameTv.setMaxWidth(totalAreaWidth - otherWidth);
                    mNameTextMaxWidth = totalAreaWidth - otherWidth;
                    return true;
                }
            });

        } else {
            holder.nameTv.setMaxWidth(mNameTextMaxWidth);
        }
    }

    public UserListData getRankUser(int position) {
        position = position - (mShowTotalNumHeader ? 1 : 0);
        if (type.equals(TOTAL_RANK) && getBasicItemCount() > 3) {
            position += 2;
        }
        if (position < 0 || position >= mRankUserList.size()) {
            return null;
        }
        return mRankUserList.get(position);
    }

    public List<UserListData> getDatalist() {
        return mRankUserList;
    }

    public void refreshFocus(long uid, boolean focus) {
        for (int i = 0; i < mRankUserList.size(); i++) {
            if (mRankUserList.get(i).userId == uid) {
                mRankUserList.get(i).isFollowing = focus;
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void setRankList(List<? extends UserListData> rankUserList) {
        if (rankUserList == null) {
            return;
        }
        mRankUserList.clear();
        mRankUserList.addAll(rankUserList);
        mShowTopThreeHeader = mRankUserList.size() > 3;
        notifyDataSetChanged();
    }

    public void isShowFollowBtn(boolean enableFollow) {
        mEnableFollow = enableFollow;
    }

    public void setTotalNum(int totalNum) {
        mTotalNum = totalNum;
        notifyItemChanged(0);
    }

    public void appendRankList(List<? extends UserListData> rankUserList) {
        if (rankUserList == null) {
            return;
        }
        mRankUserList.addAll(rankUserList);
        notifyDataSetChanged();
    }

    public void setType(String type) {
        this.type = type;
    }

    protected int getBasicItemCount() {
        return mRankUserList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mShowTotalNumHeader) {
            if (position == 0) {
                return TYPE_HEADER_TOTAL_NUM;
            }
            position--;
        }
        if (position == 0 && mShowTopThreeHeader && type.equals(TOTAL_RANK)) {
            return TYPE_HEADER_TOP_3;
        } else if (position >= 0 && position < 3 && type.equals(CURRENT_RANK) && getBasicItemCount() >= 3) {
            return TYPE_ITEM_BIG_HEIGHT;
        } else if (getBasicItemCount() == 0) {
            return TYPE_EMPTY;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        if (getBasicItemCount() == 0) {
            return 1;
        } else if (type.equals(TOTAL_RANK) && getBasicItemCount() > 3) {
            return getBasicItemCount() + (mShowTotalNumHeader ? 1 : 0) - 2;// 2=3-1,把头3聚合成一条，相当于减少了2个Item
        } else {
            return getBasicItemCount() + (mShowTotalNumHeader ? 1 : 0);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof AnchorRankingViewHolder) {
            ((AnchorRankingViewHolder) viewHolder).bind(getRankUser(position), position);
        } else if (viewHolder instanceof AnchorRankingTopHolder) {
            final AnchorRankingTopHolder holder = (AnchorRankingTopHolder) viewHolder;
            setTopThreeData(holder);
        } else if (viewHolder instanceof RankingTotalHolder) {
            RankingTotalHolder holder = (RankingTotalHolder) viewHolder;
            if (mTotalNum < 0) {
                mTotalNum = 0;
                for (int i = 0; i < mRankUserList.size(); i++) {
                    mTotalNum += mRankUserList.get(i).mTicketNum;
                }
            }
            holder.voteTotalTv.setText(String.valueOf(mTotalNum));
        } else if (viewHolder instanceof EmptyViewHolder) {
            if (!TextUtils.isEmpty(mEmptyHint)) {
                ((EmptyViewHolder) viewHolder).emptyView.setEmptyTips(mEmptyHint);
            }
        }
    }

    public void clickRelation(UserListData item, TextView followStateIv) {
        Observable.just(item)
                .observeOn(Schedulers.io())
                .flatMap(new Func1<UserListData, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(UserListData item) {
                        if (!item.isFollowing) {
                            int result = RelationUtils.follow(UserAccountManager.getInstance().getUuidAsLong(), item.userId, null, FollowOrUnfollowEvent.FOLLOW_FROM_RANK_PAGE);
                            item.isBothway = result == RelationUtils.FOLLOW_STATE_BOTH_WAY;
                            boolean resultBool = result >= RelationUtils.FOLLOW_STATE_SUCCESS;
                            if (resultBool) {
                                item.isFollowing = true;
                                RelationDaoAdapter.getInstance().insertRelation(item.toRelation());
                            }
                            return Observable.just(resultBool);
                        } else {
                            boolean result = RelationUtils.unFollow(UserAccountManager.getInstance().getUuidAsLong(), item.userId);
                            if (result) {
                                RelationDaoAdapter.getInstance().deleteRelation(item.userId);
                                item.isBothway = false;
                                item.isFollowing = false;
                            }
                            return Observable.just(result);
                        }

                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mAct.<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        if (mAct == null || mAct.isFinishing()) {
                            return;
                        }
                        if (result) {
                            notifyDataSetChanged();
                        } else {
                            if (RelationUtils.sErrorCode == RelationUtils.ERROR_CODE_BLACK) {
                                ToastUtils.showToast(R.string.setting_black_follow_hint);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    private static class RankingTotalHolder extends RecyclerView.ViewHolder {
        public TextView voteTotalTv;

        public RankingTotalHolder(View itemView) {
            super(itemView);
            voteTotalTv = (TextView) itemView.findViewById(R.id.voteTotalTv);
        }
    }

    private class AnchorRankingTopHolder extends RecyclerView.ViewHolder {
        private static final int CARD_NUM = 3;

        public BaseImageView imgFirst;
        public BaseImageView imgSecond;
        public BaseImageView imgThird;

        public LinearLayout[] infoContents;

        public TextView[] txtNames;
        public ImageView[] imgGenders;
        public TextView[] txtLevels;
        public TextView[] txtVotes;
        public TextView[] txtFollowStates;
        public RelativeLayout[] rlytRoots;
        public RelativeLayout[] rlytClickAreas;
        public View[] iconAreas;
        public TextView[] expTitleTvs;

        public AnchorRankingTopHolder(View itemView) {
            super(itemView);

            infoContents = new LinearLayout[CARD_NUM];
            txtNames = new TextView[CARD_NUM];
            imgGenders = new ImageView[CARD_NUM];
            txtLevels = new TextView[CARD_NUM];
            txtVotes = new TextView[CARD_NUM];
            txtFollowStates = new TextView[CARD_NUM];
            rlytRoots = new RelativeLayout[CARD_NUM];
            rlytClickAreas = new RelativeLayout[CARD_NUM];
            iconAreas = new View[CARD_NUM];
            expTitleTvs = new TextView[CARD_NUM];

            imgFirst = (BaseImageView) itemView.findViewById(R.id.current_rank_avatar_imgFirst);
            imgSecond = (BaseImageView) itemView.findViewById(R.id.current_rank_avatar_imgSecond);
            imgThird = (BaseImageView) itemView.findViewById(R.id.current_rank_avatar_imgThird);

            infoContents[0] = (LinearLayout) itemView.findViewById(R.id.single_card_bottom_info_1);
            infoContents[1] = (LinearLayout) itemView.findViewById(R.id.single_card_bottom_info_2);
            infoContents[2] = (LinearLayout) itemView.findViewById(R.id.single_card_bottom_info_3);

            rlytRoots[0] = (RelativeLayout) (itemView.findViewById(R.id.current_rank_rlytFirstRoot));
            rlytRoots[1] = (RelativeLayout) (itemView.findViewById(R.id.current_rank_rlytSecondRoot));
            rlytRoots[2] = (RelativeLayout) (itemView.findViewById(R.id.current_rank_rlytThirdRoot));

            for (int i = 0; i < CARD_NUM; i++) {
                txtNames[i] = (TextView) (infoContents[i].findViewById(R.id.current_rank_txtUsername));
                imgGenders[i] = (ImageView) (infoContents[i].findViewById(R.id.current_rank_imgGender));
                txtLevels[i] = (TextView) (infoContents[i].findViewById(R.id.current_rank_txtLevel));
                txtVotes[i] = (TextView) (infoContents[i].findViewById(R.id.current_rank_txtVote));
                txtFollowStates[i] = (TextView) (infoContents[i].findViewById(R.id.current_rank_txtFollowState));
                rlytClickAreas[i] = (RelativeLayout) (infoContents[i].findViewById(R.id.current_rank_rlytClickArea));
                iconAreas[i] = (infoContents[i].findViewById(R.id.live_icon));
                expTitleTvs[i] = (TextView) infoContents[i].findViewById(R.id.my_exp_title);

                if (!mEnableFollow) {
                    rlytClickAreas[i].setVisibility(View.GONE);
                }
            }

        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {

        EmptyView emptyView;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            emptyView = (EmptyView) itemView.findViewById(R.id.empty_view);
        }
    }

    public class AnchorRankingViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout rankingRootLayout;

        public BaseImageView avatarDv;

        public ImageView rankImg;

        public TextView nameTv;

        public TextView voteTv;

        public TextView rankNum;

        public View clickArea;

        public TextView followState;

        public ImageView imgBadge;

        public ImageView imgBadgeVip;

        public ImageView imgGenderIv;

        public TextView levelTv;

        public FrameLayout avatarBg;

        public View liveIcon;

        public RelativeLayout nameAreaRl;

        public TextView expTitleTv;

        public AnchorRankingViewHolder(View itemView) {
            super(itemView);
            rankingRootLayout = (RelativeLayout) itemView.findViewById(R.id.rankingRootLayout);
            avatarDv = (BaseImageView) itemView.findViewById(R.id.rank_avatar);
            rankImg = (ImageView) itemView.findViewById(R.id.rankImg);
            nameTv = (TextView) itemView.findViewById(R.id.txt_username);
            voteTv = (TextView) itemView.findViewById(R.id.voteTv);
            rankNum = (TextView) itemView.findViewById(R.id.rankNum);
            clickArea = (View) itemView.findViewById(R.id.btn_area);
            followState = (TextView) itemView.findViewById(R.id.tv_follow_state);
            imgBadge = (ImageView) itemView.findViewById(R.id.img_badge);
            imgBadgeVip = (ImageView) itemView.findViewById(R.id.img_badge_vip);
            imgGenderIv = (ImageView) itemView.findViewById(R.id.img_gender);
            levelTv = (TextView) itemView.findViewById(R.id.level_tv);
            avatarBg = (FrameLayout) itemView.findViewById(R.id.rank_avatar_bg);
            liveIcon = (View) itemView.findViewById(R.id.live_icon);
            nameAreaRl = (RelativeLayout) itemView.findViewById(R.id.name_gender_level);
            expTitleTv = (TextView) itemView.findViewById(R.id.my_exp_title);

            if (!mEnableFollow) {
                clickArea.setVisibility(View.GONE);
            }

        }

        public void bind(final UserListData item, final int position) {
            if (null == item) {
                return;
            }

            nameTv.setText(item.userNickname);
            AvatarUtils.loadAvatarByUidTs(avatarDv, item.userId, item.avatar, true);
            int totalHeaderOffset = (mShowTotalNumHeader ? 1 : 0);
            int positionOffset = mShowTopThreeHeader ? 3 : 1;
            Resources mResources = nameTv.getContext().getResources();
            avatarBg.setBackgroundResource(0);
            nameTv.setTextColor(mResources.getColor(R.color.color_black_trans_90));

            if (type.equals(CURRENT_RANK) && getBasicItemCount() >= 3) {
                if (position > 2 + totalHeaderOffset) {
                    rankNum.setText(String.valueOf(position + (mShowTotalNumHeader ? 0 : 1)));
                } else {
                    rankImg.setImageResource(mIconDrawableList[position - totalHeaderOffset]);
                    avatarBg.setBackgroundResource(mIconBgList[position - totalHeaderOffset]);
                }
            } else {
                rankNum.setText(String.valueOf(position + positionOffset - (mShowTotalNumHeader ? 1 : 0)));
            }

            if (mEnableFollow) {
                if (UserAccountManager.getInstance().getUuidAsLong() == item.userId) {
                    followState.setVisibility(View.GONE);
                    clickArea.setVisibility(View.GONE);
                } else {
                    clickArea.setVisibility(View.VISIBLE);
                    followState.setVisibility(View.VISIBLE);
                    if (item.isBothway) {
                        clickArea.setEnabled(false);
                        followState.setText(R.string.follow_both);
                    } else {
                        if (item.isFollowing) {
                            clickArea.setEnabled(false);
                            followState.setText(R.string.already_followed);
                        } else {
                            clickArea.setEnabled(true);
                            followState.setText(R.string.follow);
                        }
                    }
                    clickArea.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            clickRelation(item, followState);
                        }
                    });
                }
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mClickListener != null) {
                        mClickListener.onItemClick(view, position);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (mLongClickListener != null) {
                        return mLongClickListener.onItemLongClick(view, position);
                    }
                    return false;
                }
            });

            if (item instanceof RankUserData) {
                voteTv.setText(((RankUserData) item).getRankInfo());
                highlightNum(voteTv, ((RankUserData) item).getRankInfo());
                if (item.mIsShowing) {
                    liveIcon.setVisibility(View.VISIBLE);
                    nameTv.setMaxWidth(mNameTextMaxWidth);
                } else {
                    liveIcon.setVisibility(View.GONE);
                    nameTv.setMaxWidth(mNameTextMaxWidthNoLiveIcon);
                }
            } else if (item instanceof RankFansData) {

                String numTip = "";
                if (((RankFansData) item).isGroupRank()) {
                    numTip = GlobalData.app().getResources().getString(R.string.meili_value) + ":" + ((RankFansData) item).getExp();
                    expTitleTv.setBackgroundResource(FansInfoUtils.getImageResourcesByCharmLevelValue(((RankFansData) item).getLevel()));
                    expTitleTv.setText("");
                } else {
                    expTitleTv.setBackgroundResource(FansInfoUtils.getGroupMemberLevelDrawable(((RankFansData) item).getLevel()));
                    numTip = String.format(GlobalData.app().getResources().getString(R.string.vfans_friendliness_value), ((RankFansData) item).getExp());
                    expTitleTv.setText(((RankFansData) item).getMedalName());
                }
                expTitleTv.setVisibility(View.VISIBLE);
                voteTv.setText(numTip);
                nameTv.setMaxWidth(mFansNameTextMaxWidth);
                levelTv.setVisibility(View.GONE);
                imgGenderIv.setVisibility(View.GONE);
                liveIcon.setVisibility(View.GONE);
                return;
            } else {
                String ticketNumTip = GlobalData.app().getResources()
                        .getQuantityString(R.plurals.contribute_ticket_num, item.mTicketNum, item.mTicketNum);
                voteTv.setText(ticketNumTip);
                highlightNum(voteTv, ticketNumTip);
                liveIcon.setVisibility(View.GONE);
            }
            GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(item.level);
            levelTv.setText(String.valueOf(item.level + ""));
            levelTv.setBackgroundDrawable(levelItem.drawableBG);

            if (item.certificationType > 0) {
                imgBadge.setVisibility(View.GONE);
                imgBadgeVip.setVisibility(View.VISIBLE);
                imgBadgeVip.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(item.certificationType));
            } else {
                imgBadge.setVisibility(View.GONE);
                imgBadgeVip.setVisibility(View.GONE);
            }

            imgGenderIv.setVisibility(View.VISIBLE);
            if (item.gender == User.GENDER_MAN) {
                imgGenderIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_man));
            } else if (item.gender == User.GENDER_WOMAN) {
                imgGenderIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_women));
            } else {
                imgGenderIv.setVisibility(View.GONE);
            }
        }
    }

    private void setTopThreeData(final AnchorRankingTopHolder holder) {
        if (getBasicItemCount() < 3) {
            return;
        }
        UserListData listDataFirst = mRankUserList.get(0);
        UserListData listDataSecond = mRankUserList.get(1);
        UserListData listDataThird = mRankUserList.get(2);
        if (null == listDataFirst || null == listDataSecond || null == listDataThird) {
            return;
        }
        for (int i = 0; i < AnchorRankingTopHolder.CARD_NUM; i++) {
            final UserListData userData = mRankUserList.get(i);
            final int finalI = i;
            holder.txtNames[i].setText(userData.userNickname);

            if (mEnableFollow) {
                if (UserAccountManager.getInstance().getUuidAsLong() == userData.userId) {
                    holder.txtFollowStates[i].setVisibility(View.INVISIBLE);
                    holder.rlytClickAreas[i].setVisibility(View.INVISIBLE);
                } else {
                    holder.rlytClickAreas[i].setVisibility(View.VISIBLE);
                    holder.txtFollowStates[i].setVisibility(View.VISIBLE);
                    if (userData.isBothway) {
                        holder.rlytClickAreas[i].setEnabled(false);
                        holder.txtFollowStates[i].setText(R.string.follow_both);
                    } else {
                        if (userData.isFollowing) {
                            holder.rlytClickAreas[i].setEnabled(false);
                            holder.txtFollowStates[i].setText(R.string.already_followed);
                        } else {
                            holder.rlytClickAreas[i].setEnabled(true);
                            holder.txtFollowStates[i].setText(R.string.follow);
                        }
                    }
                    holder.rlytClickAreas[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            clickRelation(userData, holder.txtFollowStates[finalI]);
                        }
                    });
                }
            }

            if (userData.mIsShowing) {
                holder.iconAreas[i].setVisibility(View.VISIBLE);
            } else {
                holder.iconAreas[i].setVisibility(View.GONE);
            }

            holder.rlytRoots[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (mClickListener != null) {
                        mClickListener.onItemClick(view, finalI - 2 + (mShowTotalNumHeader ? 1 : 0));
                    }
                }
            });
            if (userData instanceof RankUserData) {
                holder.txtVotes[i].setText(((RankUserData) userData).getRankInfo());
                highlightNum(holder.txtVotes[i], ((RankUserData) userData).getRankInfo());
            } else if (userData instanceof RankFansData) {
                String numTip = "";
                if (((RankFansData) userData).isGroupRank()) {
                    numTip = GlobalData.app().getString(R.string.meili_value) + ":" + ((RankFansData) userData).getExp();
                    holder.expTitleTvs[i].setBackgroundResource(FansInfoUtils.getImageResourcesByCharmLevelValue(((RankFansData) userData).getLevel()));
                    holder.expTitleTvs[i].setText("");
                } else {
                    holder.expTitleTvs[i].setBackgroundResource(FansInfoUtils.getGroupMemberLevelDrawable(((RankFansData) userData).getLevel()));
                    numTip = GlobalData.app().getString(R.string.vfans_friendliness_value, ((RankFansData) userData).getExp());
                    holder.expTitleTvs[i].setText(((RankFansData) userData).getMedalName());
                }
                holder.expTitleTvs[i].setVisibility(View.VISIBLE);
                holder.txtVotes[i].setText(numTip);
                holder.txtNames[i].setMaxWidth(mFansNameTextMaxWidth);
                holder.txtLevels[i].setVisibility(View.GONE);
                holder.imgGenders[i].setVisibility(View.GONE);
                holder.iconAreas[i].setVisibility(View.GONE);
                continue;
            } else {
                String ticketNumTip = GlobalData.app().getResources()
                        .getQuantityString(R.plurals.contribute_ticket_num, userData.mTicketNum, userData.mTicketNum);
                holder.txtVotes[i].setText(ticketNumTip);
                highlightNum(holder.txtVotes[i], ticketNumTip);
            }
            GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(userData.level);
            holder.txtLevels[i].setText(String.valueOf(userData.level + ""));
            holder.txtLevels[i].setBackgroundDrawable(levelItem.drawableBG);

            holder.imgGenders[i].setVisibility(View.VISIBLE);
            if (userData.gender == User.GENDER_MAN) {
                holder.imgGenders[i].setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_man));
            } else if (userData.gender == User.GENDER_WOMAN) {
                holder.imgGenders[i].setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_women));
            } else {
                holder.imgGenders[i].setVisibility(View.GONE);
            }
        }
        AvatarUtils.loadAvatarByUidTs(holder.imgFirst, listDataFirst.userId, listDataFirst.avatar, true);
        AvatarUtils.loadAvatarByUidTs(holder.imgSecond, listDataSecond.userId, listDataSecond.avatar, true);
        AvatarUtils.loadAvatarByUidTs(holder.imgThird, listDataThird.userId, listDataThird.avatar, true);
    }

    private void highlightNum(TextView tv, String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        Pattern pattern = Pattern.compile("[^0-9]");
        Matcher matcher = pattern.matcher(text);
        String des = matcher.replaceAll("");
        SpannableString s = new SpannableString(text);
        Pattern p = Pattern.compile(des);
        Matcher m = p.matcher(text);
        if (m.find()) {
            int start = m.start();
            int end = m.end();
            s.setSpan(new ForegroundColorSpan(tv.getResources().getColor(R.color.color_ff2966)), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(s);
        }
    }
}
