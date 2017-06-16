package com.wali.live.watchsdk.videodetail.adapter;

import android.support.annotation.ColorRes;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.span.LinkMovementClickMethod;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.config.GetConfigManager;
import com.wali.live.common.smiley.SmileyParser;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.adapter.ClickItemAdapter;

import java.util.Collection;
import java.util.Comparator;

/**
 * Created by yangli on 2017/6/2.
 */
public class DetailCommentAdapter extends ClickItemAdapter<DetailCommentAdapter.LabelItem,
        ClickItemAdapter.BaseHolder, DetailCommentAdapter.ICommentClickListener> {
    private static final String TAG = "DetailCommentAdapter";

    protected static final int ITEM_TYPE_HOT_LABEL = 0;
    protected static final int ITEM_TYPE_ALL_LABEL = 1;
    protected static final int ITEM_TYPE_COMMENT = 2;

    private final SpannableStringBuilder mCommentSpan = new SpannableStringBuilder();
    private long mLastClickTs = 0;

    private final LabelItem mHotLabel = new LabelItem(ITEM_TYPE_HOT_LABEL);
    private final LabelItem mAllLabel = new LabelItem(ITEM_TYPE_ALL_LABEL);

    private final boolean canProcessClick() {
        if (System.currentTimeMillis() - mLastClickTs < 500) {
            return false;
        }
        mLastClickTs = System.currentTimeMillis();
        return true;
    }

    public final boolean isEmpty() {
        return mItems.isEmpty();
    }

    public void setItemData(Collection<CommentItem> hotList, Collection<CommentItem> allList, boolean isReverse) {
        mItems.clear();
        if (isReverse) {
            if (!allList.isEmpty()) {
                mItems.addAll(allList);
                mItems.add(mAllLabel);
            }
            if (!hotList.isEmpty()) {
                mItems.addAll(hotList);
                mItems.add(mHotLabel);
            }
        } else {
            if (!hotList.isEmpty()) {
                mItems.add(mHotLabel);
                mItems.addAll(hotList);
            }
            if (!allList.isEmpty()) {
                mItems.add(mAllLabel);
                mItems.addAll(allList);
            }
        }
        notifyDataSetChanged();
    }

    public void addSendItem(CommentItem commentItem) {
        mItems.add(0, commentItem);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).type;
    }

    @Override
    public ClickItemAdapter.BaseHolder newViewHolder(int viewType) {
        switch (viewType) {
            case ITEM_TYPE_HOT_LABEL:
            case ITEM_TYPE_ALL_LABEL: { // fall through
                View view = mInflater.inflate(R.layout.detail_label_item, null);
                return new LabelHolder(view);
            }
            case ITEM_TYPE_COMMENT: {
                View view = mInflater.inflate(R.layout.detail_comment_item, null);
                return new CommentHolder(view);
            }
            default:
                break;
        }
        return null;
    }

    public static class LabelItem extends ClickItemAdapter.BaseItem {
        private int type;

        public LabelItem(int type) {
            this.type = type;
        }
    }

    public static class CommentItem extends LabelItem {
        public long commentId;
        public int fromUserLevel;
        public long fromUid;
        public String fromNickName;
        public long toUid;
        public String toNickName;
        public long createTime;
        public CharSequence content;

        public CommentItem(long commentId, int fromUserLevel, long fromUid, String fromNickName,
                           long toUid, String toNickName, String content) {
            super(ITEM_TYPE_COMMENT);
            this.commentId = commentId;
            this.fromUserLevel = fromUserLevel;
            this.fromUid = fromUid;
            this.fromNickName = fromNickName;
            this.toUid = toUid;
            this.toNickName = toNickName;
            if (!TextUtils.isEmpty(content)) {
                this.content = SmileyParser.getInstance().addSmileySpans(GlobalData.app(), content,
                        DisplayUtils.dip2px(16), true, false, true);
            }
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o) || commentId == ((CommentItem) o).commentId;
        }
    }

    protected static class LabelHolder extends ClickItemAdapter.BaseHolder<
            LabelItem, ICommentClickListener> {
        private TextView mLabelTv;

        public LabelHolder(View view) {
            super(view);
            mLabelTv = $(R.id.comment_label);
        }

        @Override
        public void bindView(LabelItem item, ICommentClickListener listener) {
            switch (item.type) {
                case ITEM_TYPE_HOT_LABEL:
                    mLabelTv.setText(getResources().getString(R.string.feeds_hot_comment));
                    break;
                case ITEM_TYPE_ALL_LABEL:
                    mLabelTv.setText(getResources().getString(R.string.feeds_hot_all));
                    break;
                default:
                    break;
            }
        }
    }

    protected class CommentHolder extends ClickItemAdapter.BaseHolder<CommentItem, Object>
            implements View.OnClickListener, View.OnCreateContextMenuListener {
        private TextView mCommentTv;
        private TextView mLevelTv;

        private CommentItem mItem;

        private final ClickableSpan mFromNameClickListener = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                MyLog.d(TAG, "itemView onClickFromName");
                if (mListener != null && canProcessClick()) {
                    mListener.onClickName(mItem.fromUid);
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };

        private final ClickableSpan mToNameClickListener = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                MyLog.d(TAG, "itemView onClickToName");
                if (mListener != null && canProcessClick()) {
                    mListener.onClickName(mItem.toUid);
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };

        @Override
        public void onClick(View v) {
            MyLog.d(TAG, "itemView onItemClick");
            if (mListener != null && canProcessClick()) {
                mListener.onItemClick(mItem);
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MyLog.d(TAG, "itemView onItemLongClick");
            if (mListener != null && canProcessClick()) {
                mListener.onItemLongClick(mItem);
            }
        }

        public CommentHolder(View view) {
            super(view);
            mCommentTv = $(R.id.comment_tv);
            mLevelTv = $(R.id.level_tv);
            mCommentTv.setTextColor(getResources().getColor(R.color.color_black_trans_90));
        }

        @Override
        public void bindView(CommentItem item, Object listener) {
            mItem = item;
            bindLevelView(mItem);
            bindComment(mItem);
            mCommentTv.setOnCreateContextMenuListener(this);
            mCommentTv.setOnClickListener(this);
        }

        private void bindLevelView(CommentItem item) {
            int level = item.fromUserLevel;
            if (item.fromUid == MyUserInfoManager.getInstance().getUuid() &&
                    MyUserInfoManager.getInstance().getLevel() > level) {
                level = MyUserInfoManager.getInstance().getLevel();
            }
            if (level <= 0) {
                level = 1;
            }
            GetConfigManager.LevelItem levelItem = ItemDataFormatUtils.getLevelItem(level);
            mLevelTv.setText(String.valueOf(String.valueOf(level)));
            mLevelTv.setBackground(levelItem.drawableBG);
            mLevelTv.setCompoundDrawables(levelItem.drawableLevel, null, null, null);
        }

        private void appendTextWithSpan(CharSequence content, @ColorRes int colorId, ClickableSpan clickableSpan) {
            int start = mCommentSpan.length(), end = start + content.length();
            mCommentSpan.append(content);
            if (colorId != 0) {
                mCommentSpan.setSpan(new ForegroundColorSpan(getResources().getColor(colorId)),
                        start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (clickableSpan != null) {
                mCommentSpan.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        private void bindComment(CommentItem item) {
            mCommentSpan.clear();
            mCommentSpan.clearSpans();
            // 添加 评论者 并设置点击事件
            String fromNickName = !TextUtils.isEmpty(item.fromNickName) ? item.fromNickName :
                    String.valueOf(item.fromUid);
            appendTextWithSpan(fromNickName, R.color.color_5191d2, mFromNameClickListener);
            // 添加 评论对象 并设置点击事件
            if (item.toUid > 0) {
                mCommentSpan.append(" ").append(getResources().getString(R.string.recomment_text))
                        .append(" "); // 回复两个字
                String toNickName = !TextUtils.isEmpty(item.toNickName) ? item.toNickName :
                        String.valueOf(item.toUid);
                appendTextWithSpan(toNickName, R.color.color_5191d2, mToNameClickListener);
            }
            // 添加 评论内容
            mCommentSpan.append(": ").append(item.content);
            mCommentSpan.setSpan(new LeadingMarginSpan.Standard(DisplayUtils.dip2px(31), 0),
                    0, mCommentSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mCommentTv.setText(mCommentSpan);
            mCommentTv.setMovementMethod(LinkMovementClickMethod.getInstance());
        }
    }

    public interface ICommentClickListener {
        void onClickName(long uid);

        void onItemClick(CommentItem item);

        void onItemLongClick(CommentItem item);
    }

}
