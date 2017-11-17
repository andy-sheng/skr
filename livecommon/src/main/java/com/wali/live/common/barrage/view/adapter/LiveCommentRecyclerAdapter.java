
package com.wali.live.common.barrage.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import java.util.HashMap;
import java.util.List;

import static com.mi.live.data.push.model.BarrageMsgType.B_MSG_TYPE_TEXT;

/**
 * @module 直播间评论弹幕
 * Created by lan on 15-11-26.
 */
public class LiveCommentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = LiveCommentRecyclerAdapter.class.getSimpleName();

    public static final int DEFAULT_SUB_NAME_LENGTH = 4;

    WeakReference<Context> mContext;

    private boolean mIsGameLive;

    private List<CommentModel> mCommentList = new ArrayList<>();

    private HashMap<String, Bitmap> mBitmapHashMap = new HashMap<>();

    private NameClickListener mLiveCommentNameClickListener = null;

    public void setLiveCommentNameClickListener(NameClickListener listener) {
        mLiveCommentNameClickListener = listener;
    }

    public LiveCommentRecyclerAdapter(Context activity) {
        mContext = new WeakReference<>(activity);
    }

    public void setIsGameLive(boolean isGameLive) {
        mIsGameLive = isGameLive;
    }

    public void setCommentList(List<CommentModel> dataList) {
        if (dataList != null) {
            mCommentList.clear();
            mCommentList.addAll(dataList);
            notifyDataSetChanged();
        }
    }

    private void setBackground(LiveCommentHolder liveCommentHolder, CommentModel liveComment) {
        if (null != liveComment) {
            int type = liveComment.getMsgType();
            if ((type == BarrageMsgType.B_MSG_TYPE_TEXT || type == BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE || type == BarrageMsgType.B_MSG_TYPE_GLOBAL_SYS_MSG)
                    && null != mContext.get()) {
                int drawableRes = liveComment.getBackGround();
                liveCommentHolder.itemView.setBackgroundResource(drawableRes == 0 ? R.drawable.live_bg_comment : drawableRes);
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) liveCommentHolder.itemView.getLayoutParams();
                lp.setMargins(0, 3, 0, 3);
            } else {
                liveCommentHolder.itemView.setBackground(null);
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) liveCommentHolder.itemView.getLayoutParams();
                lp.setMargins(0, 0, 0, 0);
            }
        } else {
            MyLog.w(TAG, "setBackground liveComment is null!");
        }
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
        liveCommentHolder.setFansMedalInfo();
    }

    private void bindNameView(LiveCommentHolder liveCommentHolder, CommentModel liveComment) {
        String name = liveComment.getName();
        if (mIsGameLive && !TextUtils.isEmpty(name)) {
            if (name.length() > DEFAULT_SUB_NAME_LENGTH) {
                name = name.substring(0, DEFAULT_SUB_NAME_LENGTH);
                name += "...";
            }
        }
        liveCommentHolder.setNameSpan(name, liveComment.getNameColor(), liveComment.getCertificationType(), liveComment.getMsgType() != B_MSG_TYPE_TEXT, liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_SELL);
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

            CommentModel liveComment = mCommentList.get(position);
            liveCommentHolder.setModel(liveComment);

            setBackground(liveCommentHolder, liveComment);

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
        return mCommentList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    /**
     * 这个回调是点击弹幕的姓名时的回调
     */
    public interface NameClickListener {
        void onClickName(long uid);
    }
}
