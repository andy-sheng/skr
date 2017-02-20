
package com.wali.live.common.barrage.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.image.ImageUtils;
import com.live.module.common.R;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.config.GetConfigManager;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.repository.GiftRepository;
import com.wali.live.common.barrage.view.holder.LiveCommentHolder;
import com.wali.live.common.heartview.HeartItemManager;
import com.wali.live.common.model.CommentModel;
import com.wali.live.dao.Gift;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @module 直播间评论弹幕
 * Created by lan on 15-11-26.
 */
public class LiveCommentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = LiveCommentRecyclerAdapter.class.getSimpleName();

    public static final int DEFAULT_SUB_NAME_LENGTH = 4;

    WeakReference<Context> mContext;

    private boolean mIsGameLive;

    private HashMap<String, Bitmap> mBitmapHashMap = new HashMap<>();

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

    public void setLiveCommentList(final List<CommentModel> dataList, final Runnable afterRefresh) {
        // 一直都关闭看看能不能缓解跳变问题
        if (null != dataList) {
            if (mSubscription != null && !mSubscription.isUnsubscribed()) {
                return;
            }
            mSubscription = Observable.create(new Observable.OnSubscribe<DiffUtil.DiffResult>() {
                @Override
                public void call(Subscriber<? super DiffUtil.DiffResult> subscriber) {
                    mCommentDiffUtils.setList(dataList);
                    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(mCommentDiffUtils, true);
                    // 把结果应用到 adapter
                    subscriber.onNext(diffResult);
                    subscriber.onCompleted();
                }
            })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<DiffUtil.DiffResult>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(DiffUtil.DiffResult diffResult) {
                            diffResult.dispatchUpdatesTo(LiveCommentRecyclerAdapter.this);
                            afterRefresh.run();
                        }
                    });

        }
    }


    /**
     * 这个回调是点击弹幕的姓名时的回调
     */
    public interface LiveCommentNameClickListener {
        void onClickName(long uid);
    }

    public void bindLevelView(LiveCommentHolder liveCommentHolder, CommentModel liveComment) {
        int level = liveComment.getLevel();
        if (liveComment.getLevel() != 0 && liveComment.getSenderId() == MyUserInfoManager.getInstance().getUser().getUid() && MyUserInfoManager.getInstance().getUser().getLevel() > liveComment.getLevel()) {
            level = MyUserInfoManager.getInstance().getUser().getLevel();
        }
        if (level <= 0) {
            liveCommentHolder.levelTv.setVisibility(View.GONE);
        } else {
            GetConfigManager.LevelItem levelItem = GetConfigManager.getInstance().getLevelItem(level);
            liveCommentHolder.levelTv.setText(String.valueOf(String.valueOf(level)));
            liveCommentHolder.levelTv.setBackgroundDrawable(levelItem.drawableBG);
            liveCommentHolder.levelTv.setCompoundDrawables(levelItem.drawableLevel, null, null, null);
            liveCommentHolder.levelTv.setVisibility(View.VISIBLE);
        }
    }

    private void bindNameView(LiveCommentHolder liveCommentHolder, CommentModel liveComment) {
        String name = liveComment.getName();
        if (mIsGameLive && !TextUtils.isEmpty(name)) {
            if (name.length() > DEFAULT_SUB_NAME_LENGTH) {
                name = name.substring(0, DEFAULT_SUB_NAME_LENGTH);
                name += "...";
            }
        }
        liveCommentHolder.setNameSpan(name, liveComment.getNameColor(), liveComment.getCertificationType(), liveComment.getMsgType() != BarrageMsgType.B_MSG_TYPE_TEXT, liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_SELL);
    }

    public void bindCommentView(LiveCommentHolder liveCommentHolder, CommentModel liveComment) {
        if ((liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_GIFT
                || liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_GLABAL_MSG) && mIsGameLive) {
            Gift gift = GiftRepository.findGiftById((int) liveComment.getGiftId());
            if (gift != null) {
                liveCommentHolder.giftIv.setVisibility(View.VISIBLE);
                liveCommentHolder.setComment(GlobalData.app().getString(R.string.game_give_one_gift, gift.getName()), R.color.color_7EEEFF, liveCommentHolder.getClickSpan());
                FrescoWorker.loadImage(liveCommentHolder.giftIv, ImageFactory.newHttpImage(gift.getPicture()).build());
            }
        } else {
            liveCommentHolder.giftIv.setVisibility(View.GONE);
            liveCommentHolder.setComment(liveComment.getBody(), liveComment.getCommentColor(), liveCommentHolder.getClickSpan());
        }
        if (liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_LIKE) {
            Drawable drawable = null;
            // 先找有没有特别的特效图片
            String path = liveComment.getLikePath();
            if (!TextUtils.isEmpty(path)) {
                Bitmap bitmap = null;
                if (!mBitmapHashMap.containsKey(path)) {
                    bitmap = ImageUtils.getLocalBitmap(path);
                    mBitmapHashMap.put(path, bitmap);
                } else {
                    bitmap = mBitmapHashMap.get(path);
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
        }
        return liveCommentHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LiveCommentHolder) {
            LiveCommentHolder liveCommentHolder = (LiveCommentHolder) holder;
            liveCommentHolder.reset();
            CommentModel liveComment = mCommentDiffUtils.getList().get(position);
            liveCommentHolder.setModel(liveComment);
            bindLevelView(liveCommentHolder, liveComment);

            bindNameView(liveCommentHolder, liveComment);

            bindCommentView(liveCommentHolder, liveComment);

            liveCommentHolder.setAll();
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
            List<CommentModel> reverseList = new ArrayList<>(list);
            Collections.reverse(reverseList);
            oldList.clear();
            oldList.addAll(newList);
            newList.clear();
            newList.addAll(reverseList);
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
