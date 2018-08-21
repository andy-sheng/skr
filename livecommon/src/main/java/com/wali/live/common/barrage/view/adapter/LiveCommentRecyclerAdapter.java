
package com.wali.live.common.barrage.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.image.ImageUtils;
import com.live.module.common.R;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.repository.GiftRepository;
import com.wali.live.common.barrage.view.holder.LiveCommentHolder;
import com.wali.live.common.barrage.view.utils.CommentLevelOrLikeCache;
import com.wali.live.common.heartview.HeartItemManager;
import com.wali.live.common.model.CommentModel;
import com.wali.live.dao.Gift;
import com.wali.live.utils.ItemDataFormatUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Subscription;

import static com.mi.live.data.push.model.BarrageMsgType.B_MSG_TYPE_TEXT;

/**
 * @module 直播间评论弹幕
 * Created by lan on 15-11-26.
 */
public class LiveCommentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = LiveCommentRecyclerAdapter.class.getSimpleName();

    public static final int DEFAULT_SUB_NAME_LENGTH = 4;


    private final static int BACKGROUD_MARGIN = DisplayUtils.dip2px(1);

    public static final int PAGE_MAX_SHOW_SIZE = 6;

    private WeakReference<Context> mContext;

    private boolean mIsGameLive;

    private LiveCommentNameClickListener mLiveCommentNameClickListener = null;

    public void setLiveCommentNameClickListener(LiveCommentNameClickListener listener) {
        mLiveCommentNameClickListener = listener;
    }

    public LiveCommentRecyclerAdapter(Context activity) {
        mContext = new WeakReference<>(activity);
    }

    public void setIsGameLive(boolean isGameLive) {
        mIsGameLive = isGameLive;
    }

    CommentDiffUtils mCommentDiffUtils = new CommentDiffUtils();

    Subscription mSubscription;

    public void setLiveCommentList(List<CommentModel> dataList, Runnable afterRefresh) {
        // 一直都关闭看看能不能缓解跳变问题
        if (null != dataList) {
            // 防止多个线程同时刷新引起的状态不一致，按理这里只应该主线程才会调用。

            if (mSubscription != null && !mSubscription.isUnsubscribed()) {
                return;
            }
            mCommentDiffUtils.setList(dataList);
            notifyDataSetChanged();
//            mSubscription = Observable.create(new Observable.OnSubscribe<DiffUtil.DiffResult>() {
//                @Override
//                public void call(Subscriber<? super DiffUtil.DiffResult> subscriber) {
//                    int ps1 = MyLog.ps("setLiveCommentList calculateDiff dataListSize=" + dataList.size() + ",ts=" + System.currentTimeMillis());
//                    mCommentDiffUtils.setList(dataList);
//                    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(mCommentDiffUtils, true);
//                    // 把结果应用到 adapter
//                    MyLog.pe(ps1);
//                    subscriber.onNext(diffResult);
//                    subscriber.onCompleted();
//                }
//            })
//                    .subscribeOn(Schedulers.computation())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<DiffUtil.DiffResult>() {
//                        @Override
//                        public void onCompleted() {
//
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//
//                        }
//
//                        @Override
//                        public void onNext(DiffUtil.DiffResult diffResult) {
//                            if (dataList.size() > PAGE_MAX_SHOW_SIZE) {
//                                diffResult.dispatchUpdatesTo(LiveCommentRecyclerAdapter.this);
//                            } else {
//                                notifyDataSetChanged();
//                            }
//                            afterRefresh.run();
//                            MyLog.pe(ps);
//                        }
//                    });

        }
    }


    /**
     * 这个回调是点击弹幕的姓名时的回调
     */
    public interface LiveCommentNameClickListener {
        void onClickName(long uid);

        void onClickComment(String schemaUrl);
    }

    private void bindLevelView(LiveCommentHolder liveCommentHolder, CommentModel liveComment) {
        //产品策略 这里等级显示关系是贵族>vip>粉丝团  但是贵族和vip不共存，优先显示贵族
        int level = liveComment.getLevel();
        if (liveComment.isNoble() && level > 0) {
            liveCommentHolder.setNobleMedalInfo();
        } else {
            if (level != 0
                    && liveComment.getSenderId() == MyUserInfoManager.getInstance().getUuid()
                    && MyUserInfoManager.getInstance().getLevel() > liveComment.getLevel()) {
                level = MyUserInfoManager.getInstance().getLevel();
            }
            if (level > 0) {
                GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(level);
                liveCommentHolder.levelTv.setText(String.valueOf(String.valueOf(level)));
                liveCommentHolder.levelTv.setBackgroundDrawable(levelItem.drawableBG);
                liveCommentHolder.setLevelInfo();
            }
        }
        MyLog.d(TAG, "bindLevelView isNobel=" + liveComment.isNoble() + " level=" + liveComment.getLevel());
        liveCommentHolder.setVfansMedalInfo();
    }

    private void bindNameAndCommentView(LiveCommentHolder liveCommentHolder, CommentModel liveComment) {
        List<Integer> innerGlobalRoomMessageTypeList = liveComment.getInnerGlobalRoomMessageTypeList();
        // todo 和娱乐直播保持一致
//        String name = liveComment.getName();
//        if (mIsGameLive && !TextUtils.isEmpty(name)) {
//            if (name.length() > DEFAULT_SUB_NAME_LENGTH) {
//                name = name.substring(0, DEFAULT_SUB_NAME_LENGTH);
//                name += "...";
//            }
//        }

        if (innerGlobalRoomMessageTypeList != null && !innerGlobalRoomMessageTypeList.isEmpty()) {
            List<String> beforeNickNameConfigList = new ArrayList<>();
            List<String> afterNickNameConfigList = new ArrayList<>();
            List<String> beforeContentConfigList = new ArrayList<>();
            List<String> afterContentConfigList = new ArrayList<>();
            String schemeUrl = null;
            for (int i = 0; i < innerGlobalRoomMessageTypeList.size(); i++) {
                int type = innerGlobalRoomMessageTypeList.get(i);
                if (type == BarrageMsg.INNER_GLOBAL_MEDAL_TYPE) {
                    beforeNickNameConfigList = liveComment.getBeforeNickNameConfigList();
                    afterNickNameConfigList = liveComment.getAfterNickNameConfigList();
                    beforeContentConfigList = liveComment.getBeforeContentConfigList();
                    afterContentConfigList = liveComment.getAfterContentConfigList();
                }

                if (type == BarrageMsg.INNER_GLOBAL_SCHEME_TYPE) {
                    schemeUrl = liveComment.getInnerGlobalRoomMessageSchemaUrl();
                }
            }

            liveCommentHolder.setNameSpan(beforeNickNameConfigList, afterNickNameConfigList, liveComment.getName(), liveComment.getNameColor(), liveComment.getCertificationType(),
                    liveComment.getMsgType() != BarrageMsgType.B_MSG_TYPE_TEXT && liveComment.getMsgType() != BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE, liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_SELL,
                    liveCommentHolder.getClickSpan(true), liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_LIKE || liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_JOIN);

            bindCommentView(schemeUrl, beforeContentConfigList, afterContentConfigList, liveCommentHolder, liveComment);
        } else {
            liveCommentHolder.setNameSpan(null, null, liveComment.getName(), liveComment.getNameColor(), liveComment.getCertificationType(),
                    liveComment.getMsgType() != BarrageMsgType.B_MSG_TYPE_TEXT && liveComment.getMsgType() != BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE, liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_SELL,
                    liveCommentHolder.getClickSpan(true), liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_LIKE || liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_JOIN);

            bindCommentView("", null, null, liveCommentHolder, liveComment);
        }
    }

    /**
     * @param schemeUrl         转移到{@linkplain CommentModel#innerGlobalRoomMessageSchemaUrl}
     * @param bConfigList
     * @param aConfigList
     * @param liveCommentHolder
     * @param liveComment
     * @see LiveCommentHolder#getClickSpan(boolean)
     */
    private void bindCommentView(String schemeUrl, List<String> bConfigList, List<String> aConfigList, LiveCommentHolder liveCommentHolder, CommentModel liveComment) {
//        MyLog.d(TAG, "schema:" + schemeUrl);
        if ((liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_GIFT
                || liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_GLABAL_MSG) && mIsGameLive) {
            Gift gift = GiftRepository.findGiftById((int) liveComment.getGiftId());
            if (gift != null) {
                liveCommentHolder.giftIv.setVisibility(View.VISIBLE);
                int count = liveComment.getGiftCount();
                if (count <= 1) {
                    liveCommentHolder.setComment(bConfigList, aConfigList,
                            GlobalData.app().getString(R.string.game_give_one_gift, gift.getName()),
                            R.color.color_7EEEFF, liveCommentHolder.getClickSpan(false));
                } else {
                    liveCommentHolder.setComment(bConfigList, aConfigList,
                            GlobalData.app().getResources().getQuantityString(R.plurals.game_give_n_gift, count, count, gift.getName()),
                            R.color.color_7EEEFF, liveCommentHolder.getClickSpan(false));
                }

                FrescoWorker.loadImage(liveCommentHolder.giftIv, ImageFactory.newHttpImage(gift.getPicture()).build());
            }
        } else {
            liveCommentHolder.giftIv.setVisibility(View.GONE);
            liveCommentHolder.setComment(bConfigList, aConfigList, liveComment.getBody(), liveComment.getCommentColor(), liveCommentHolder.getClickSpan(false));
        }

        if (liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_LIKE) {
            Drawable drawable = null;
            // 先找有没有特别的特效图片
            String path = liveComment.getLikePath();
            if (!TextUtils.isEmpty(path)) {
                Bitmap bitmap = null;
                if (null == CommentLevelOrLikeCache.getLevelOrLike(path)) {
                    bitmap = ImageUtils.getLocalBitmap(path);
                    CommentLevelOrLikeCache.setLevelOrLike(path, bitmap);
                } else {
                    bitmap = CommentLevelOrLikeCache.getLevelOrLike(path);
                }
                if (bitmap != null) {
                    drawable = new BitmapDrawable(bitmap);
                }
            }
            // 如果没有，用星星
            if (drawable == null) {
                int resId = HeartItemManager.getResId(liveComment.getLikeId() - 1);
                if (resId != 0) {
                    drawable = GlobalData.app().getResources().getDrawable(resId);
                }
            }
            if (drawable != null) {
                drawable.setBounds(0, 0, DisplayUtils.dip2px(16f), DisplayUtils.dip2px(16f));
                liveCommentHolder.setLikeDrawable(drawable);
            }
        }
    }


    private void setBackground(LiveCommentHolder liveCommentHolder, CommentModel liveComment) {
        if (null != liveComment) {
            int type = liveComment.getMsgType();
            if ((type == BarrageMsgType.B_MSG_TYPE_TEXT
                    || type == BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE
                    || type == BarrageMsgType.B_MSG_TYPE_GLOBAL_SYS_MSG)
                    ) {
                if (mIsGameLive) {
                    liveCommentHolder.itemView.setBackground(null);
                } else {
                    if (liveComment.getBackGround() == 0) {
                        liveCommentHolder.itemView.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.live_bg_comment));
                    } else {
                        liveCommentHolder.itemView.setBackground(GlobalData.app().getResources().getDrawable(liveComment.getBackGround()));
                    }
                }
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) liveCommentHolder.itemView.getLayoutParams();
                lp.setMargins(0, BACKGROUD_MARGIN, 0, BACKGROUD_MARGIN);
            } else {
                liveCommentHolder.itemView.setBackground(null);
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) liveCommentHolder.itemView.getLayoutParams();
                lp.setMargins(0, 0, 0, 0);
            }
        } else {
            MyLog.w(TAG, "setBackground liveComment is null!");
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyLog.d(TAG, "onCreateViewHolder");
//            View footView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_footerview, null, false);
//            return new FootViewHolder(footView);
        View footView = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_comment_item_left, parent, false);
        LiveCommentHolder liveCommentHolder = new LiveCommentHolder(footView);
        liveCommentHolder.setContext(mContext);
        liveCommentHolder.setNameClickListener(mLiveCommentNameClickListener);
        if (mIsGameLive) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) liveCommentHolder.barrageTv.getLayoutParams();
            params.rightMargin = DisplayUtils.dip2px(3.33f);
            params.topMargin = DisplayUtils.dip2px(8.33f);
        }
        return liveCommentHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        position = mCommentDiffUtils.getNewListSize() - position - 1;
        CommentModel liveComment = mCommentDiffUtils.getList().get(position);
        if (holder instanceof LiveCommentHolder && liveComment != null) {
            LiveCommentHolder liveCommentHolder = (LiveCommentHolder) holder;
            liveCommentHolder.reset();
            liveCommentHolder.barrageTv.setText("");
            liveCommentHolder.setModel(liveComment);
            if (mIsGameLive) {
                liveCommentHolder.barrageTv.setShadowLayer(0, 0, 0, 0);
                liveCommentHolder.barrageTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13.33f);
            }

            setBackground(liveCommentHolder, liveComment);

            bindLevelView(liveCommentHolder, liveComment);

            bindNameAndCommentView(liveCommentHolder, liveComment);

//            bindCommentView(liveCommentHolder, liveComment);

            liveCommentHolder.setAll();

            liveCommentHolder.barrageTv.setLongClickable(true);
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mCommentDiffUtils.getList().size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    static class CommentDiffUtils extends DiffUtil.Callback {

        private List<CommentModel> oldList = new ArrayList<>();
        private List<CommentModel> newList = new ArrayList<>();

        public List<CommentModel> getList() {
            return newList;
        }

        public void setList(List<CommentModel> list) {
//            List<CommentModel> reverseList = new ArrayList<>(list);
//            Collections.reverse(reverseList);
//            oldList.clear();
//            oldList.addAll(newList);
            newList.clear();
            newList.addAll(list);
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            CommentModel oldCommentModel = oldList.get(oldItemPosition);
            CommentModel newCommentModel = newList.get(newItemPosition);
            return oldCommentModel == newCommentModel;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return true;
        }
    }


}
