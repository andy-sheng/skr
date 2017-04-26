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
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.global.GlobalData;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.common.listener.OnItemClickListener;
import com.wali.live.common.listener.OnItemLongClickListener;
import com.wali.live.dao.RelationDaoAdapter;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.utils.relation.RelationUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.ranking.data.RankUserData;
import com.wali.live.watchsdk.ranking.holder.AnchorRankingTopHolder;
import com.wali.live.watchsdk.ranking.holder.AnchorRankingViewHolder;
import com.wali.live.watchsdk.ranking.holder.EmptyViewHolder;
import com.wali.live.watchsdk.ranking.holder.FooterViewHolder;
import com.wali.live.watchsdk.ranking.holder.RankingTotalHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
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

    private int mNameTextMaxWidth;
    private String type;
    private RxActivity mAct;
    private boolean hasFooter;//设置是否显示Footer
    private List<UserListData> mRankUserList = new ArrayList<>();
    private int mTotalNum;//总的星票数

    public RankRecyclerViewAdapter(RxActivity mAct, String type) {
        this.type = type;
        this.mAct = mAct;
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = null;
        if (viewType == TYPE_HEADER_TOP_3) {
            if (type.equals(TOTAL_RANK) && getBasicItemCount() > 3) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.anchor_rank_list_total_top_3, viewGroup, false);
                return new AnchorRankingTopHolder(view);
            }
        } else if (viewType == TYPE_HEADER_TOTAL_NUM) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.vote_ranking_total_item, viewGroup, false);
            return new RankingTotalHolder(view);
        } else if (viewType == TYPE_FOOTER) {    //底部 加载view
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_view_load_more, viewGroup, false);
            return new FooterViewHolder(view);
        } else if (viewType == TYPE_EMPTY) {
            view = LayoutInflater.from(GlobalData.app()).inflate(R.layout.empty_view, viewGroup, false);
            return new EmptyViewHolder(view);
        } else if (viewType == TYPE_ITEM_BIG_HEIGHT) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.anchor_rank_list_item_big, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.anchor_rank_list_item, viewGroup, false);
        }
        RecyclerView.ViewHolder holder = new AnchorRankingViewHolder(view);
        setMaxTextWidth((AnchorRankingViewHolder) holder);
        return holder;
    }

    private void setMaxTextWidth(final AnchorRankingViewHolder holder) {
        if (mNameTextMaxWidth == 0) {
            holder.itemView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    holder.itemView.getViewTreeObserver().removeOnPreDrawListener(this);
                    // 设置NAME area的宽度
                    int totalAreaWidth = holder.nameAreaRl.getWidth();
                    int otherWidth = holder.liveIcon.getWidth() + holder.levelTv.getWidth() + holder.imgGenderIv.getWidth() + DisplayUtils.dip2px(5.67f) + 6; // 5.67是margin， 多余的6px 是因为level标识大小不一样，不想每次都计算
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
        notifyDataSetChanged();
        mShowTopThreeHeader = mRankUserList.size() > 3;
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
        } else if (position == getBasicItemCount() && hasFooter) {
            return TYPE_FOOTER;
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
            return getBasicItemCount() + (mShowTotalNumHeader ? 1 : 0) + (hasFooter ? 1 : 0) - 2;// 2=3-1,把头3聚合成一条，相当于减少了2个Item
        } else {
            return getBasicItemCount() + (mShowTotalNumHeader ? 1 : 0) + (hasFooter ? 1 : 0);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof AnchorRankingViewHolder) {
            final AnchorRankingViewHolder holder = (AnchorRankingViewHolder) viewHolder;

            final UserListData item = getRankUser(position);
            if (null == item) {
                return;
            }

            holder.nameTv.setText(item.userNickname);

            AvatarUtils.loadAvatarByUidTs(holder.avatarDv, item.userId, item.avatar, true);
            int totalHeaderOffset = (mShowTotalNumHeader ? 1 : 0);
            int positionOffset = mShowTopThreeHeader ? 3 : 1;
            Resources mResources = holder.nameTv.getContext().getResources();
            holder.avatarBg.setBackgroundResource(0);
            holder.nameTv.setTextColor(mResources.getColor(R.color.color_black_trans_90));

            if (type.equals(CURRENT_RANK) && getBasicItemCount() >= 3) {
                if (position > 2 + totalHeaderOffset) {
                    holder.rankNum.setText(String.valueOf(position + (mShowTotalNumHeader ? 0 : 1)));
                } else {
                    holder.rankImg.setImageResource(mIconDrawableList[position - totalHeaderOffset]);
                    holder.avatarBg.setBackgroundResource(mIconBgList[position - totalHeaderOffset]);
                }
            } else {
                holder.rankNum.setText(String.valueOf(position + positionOffset - (mShowTotalNumHeader ? 1 : 0)));
            }

            GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(item.level);
            holder.levelTv.setText(String.valueOf(item.level + ""));
            holder.levelTv.setBackgroundDrawable(levelItem.drawableBG);

            if (item.certificationType > 0) {
                holder.imgBadge.setVisibility(View.GONE);
                holder.imgBadgeVip.setVisibility(View.VISIBLE);
                holder.imgBadgeVip.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(item.certificationType));
            } else {
                holder.imgBadge.setVisibility(View.GONE);
                holder.imgBadgeVip.setVisibility(View.GONE);
            }

            holder.imgGenderIv.setVisibility(View.VISIBLE);
            if (item.gender == User.GENDER_MAN) {
                holder.imgGenderIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_man));
            } else if (item.gender == User.GENDER_WOMAN) {
                holder.imgGenderIv.setImageDrawable(GlobalData.app().getResources().getDrawable(R.drawable.all_women));
            } else {
                holder.imgGenderIv.setVisibility(View.GONE);
            }


            if (UserAccountManager.getInstance().getUuidAsLong() == item.userId) {
                holder.followState.setVisibility(View.GONE);
                holder.clickArea.setVisibility(View.GONE);
            } else {
                holder.clickArea.setVisibility(View.VISIBLE);
                holder.followState.setVisibility(View.VISIBLE);
                if (item.isBothway) {
                    holder.clickArea.setEnabled(false);
                    holder.followState.setText(R.string.follow_both);
                } else {
                    if (item.isFollowing) {
                        holder.clickArea.setEnabled(false);
                        holder.followState.setText(R.string.already_followed);
                    } else {
                        holder.clickArea.setEnabled(true);
                        holder.followState.setText(R.string.follow);
                    }
                }
                holder.clickArea.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickRelation(item, holder.followState);
                    }
                });
            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (mClickListener != null) {
                        mClickListener.onItemClick(view, position);
                    }
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (mLongClickListener != null) {
                        return mLongClickListener.onItemLongClick(view, position);
                    }
                    return false;
                }
            });

            if (item instanceof RankUserData) {
                holder.voteTv.setText(((RankUserData) item).getRankInfo());
                highlightNum(holder.voteTv, ((RankUserData) item).getRankInfo());
                if (item.mIsShowing) {
                    holder.liveIcon.setVisibility(View.VISIBLE);
                } else {
                    holder.liveIcon.setVisibility(View.INVISIBLE);
                }
            } else {
                String ticketNumTip = com.base.global.GlobalData.app().getResources()
                        .getQuantityString(R.plurals.contribute_ticket_num, item.mTicketNum, item.mTicketNum);
                holder.voteTv.setText(ticketNumTip);
                highlightNum(holder.voteTv, ticketNumTip);
                holder.liveIcon.setVisibility(View.INVISIBLE);
            }
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
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean result) {
                        if (mAct == null || mAct.isFinishing()) {
                            return;
                        }
                        if (result) {
                            notifyDataSetChanged();
                        } else {
                            if (RelationUtils.sErrorCode == ErrorCode.CODE_RELATION_BLACK) {
                                ToastUtils.showToast(GlobalData.app(), GlobalData.app().getString(R.string.setting_black_follow_hint));
                            }
                        }
                    }
                });
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

            if (UserAccountManager.getInstance().getUuidAsLong() == userData.userId) {
                holder.txtFollowStates[i].setVisibility(View.GONE);
                holder.rlytClickAreas[i].setVisibility(View.GONE);
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

            if (userData instanceof RankUserData) {
                holder.txtVotes[i].setText(((RankUserData) userData).getRankInfo());
                highlightNum(holder.txtVotes[i], ((RankUserData) userData).getRankInfo());
            } else {
                String ticketNumTip = GlobalData.app().getResources()
                        .getQuantityString(R.plurals.contribute_ticket_num, userData.mTicketNum, userData.mTicketNum);
                holder.txtVotes[i].setText(ticketNumTip);
                highlightNum(holder.txtVotes[i], ticketNumTip);
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
