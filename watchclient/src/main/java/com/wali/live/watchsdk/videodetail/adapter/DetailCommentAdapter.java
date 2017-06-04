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

import com.base.utils.display.DisplayUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.config.GetConfigManager;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.adapter.ClickItemAdapter;

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
                return new CommentHolder(view, mCommentSpan);
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
        private long commentId;
        private int fromUserLevel;
        private long fromUid;
        private String fromNickName;
        private long toUid;
        private String toNickName;
        private String content;

        public CommentItem(long commentId, int fromUserLevel, long fromUid, String fromNickName,
                           long toUid, String toNickName, String content) {
            super(ITEM_TYPE_COMMENT);
            this.commentId = commentId;
            this.fromUserLevel = fromUserLevel;
            this.fromUid = fromUid;
            this.fromNickName = fromNickName;
            this.toUid = toUid;
            this.toNickName = toNickName;
            this.content = content;
        }
    }

    protected static class LabelHolder extends ClickItemAdapter.BaseHolder<
            LabelItem, View.OnClickListener> {
        private TextView mLabelTv;

        public LabelHolder(View view) {
            super(view);
            mLabelTv = $(R.id.comment_label);
        }

        @Override
        public void bindView(LabelItem item, View.OnClickListener onClickListener) {
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

    protected static class CommentHolder extends ClickItemAdapter.BaseHolder<
            CommentItem, ICommentClickListener> {
        private TextView mCommentTv;
        private TextView mLevelTv;
        private DetailCommentAdapter.ICommentClickListener mListener;
        private SpannableStringBuilder mCommentSpan;

        public CommentHolder(View view, SpannableStringBuilder commentSpan) {
            super(view);
            mCommentSpan = commentSpan;
            mCommentTv = $(R.id.comment_tv);
            mLevelTv = $(R.id.level_tv);
            mCommentTv.setTextColor(getResources().getColor(R.color.color_black_trans_90));
        }

        @Override
        public void bindView(final CommentItem item, DetailCommentAdapter.ICommentClickListener listener) {
            mListener = listener;
            bindLevelView(item);
            bindComment(item);
            if (mListener != null) {
                itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        mListener.onItemLongClick(item);
                    }
                });
            }
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
            mLevelTv.setBackgroundDrawable(levelItem.drawableBG);
            mLevelTv.setCompoundDrawables(levelItem.drawableLevel, null, null, null);
        }

        private void appendTextWithSpan(String content, @ColorRes int colorId, ClickableSpan clickableSpan) {
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

        private void bindComment(final CommentItem item) {
            mCommentSpan.clear();
            mCommentSpan.clearSpans();
            // 评论者
            String name = item.fromNickName;
            if (TextUtils.isEmpty(name)) {
                name = String.valueOf(item.fromUid);
            }
            appendTextWithSpan(name, R.color.color_5191d2, mListener == null ? null : new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    mListener.onClickName(item.fromUid);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setUnderlineText(false);
                }
            });
            // 评论对象
            if (item.toUid > 0) {
                if (TextUtils.isEmpty(item.toNickName)) {
                    item.toNickName = String.valueOf(item.toUid);
                }
                mCommentSpan.append(" " + getResources().getString(R.string.recomment_text) + " "); // 回复两个字
                appendTextWithSpan(item.toNickName, R.color.color_5191d2, mListener == null ? null : new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        mListener.onClickName(item.toUid);
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        ds.setUnderlineText(false);
                    }
                });
            }
            mCommentSpan.append(": ");
            // 评论内容
            appendTextWithSpan(item.content, 0, mListener == null ? null : new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    mListener.onItemClick(item);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setUnderlineText(false);
                }
            });
            mCommentSpan.setSpan(new LeadingMarginSpan.Standard(DisplayUtils.dip2px(31), 0),
                    0, mCommentSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mCommentTv.setText(mCommentSpan);
        }
    }

    public interface ICommentClickListener {
        void onClickName(long uid);

        void onItemClick(CommentItem item);

        void onItemLongClick(CommentItem item);
    }

}
