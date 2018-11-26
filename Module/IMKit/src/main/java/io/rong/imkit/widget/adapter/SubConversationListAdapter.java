//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.model.UIConversation.UnreadRemindType;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imkit.widget.ProviderContainerView;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Conversation.ConversationType;

public class SubConversationListAdapter extends ConversationListAdapter {
    LayoutInflater mInflater;
    Context mContext;

    public long getItemId(int position) {
        UIConversation conversation = (UIConversation) this.getItem(position);
        return conversation == null ? 0L : (long) conversation.hashCode();
    }

    public SubConversationListAdapter(Context context) {
        super(context);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
    }

    protected View newView(Context context, int position, ViewGroup group) {
        View result = this.mInflater.inflate(R.layout.rc_item_conversation, group, false);
        io.rong.imkit.widget.adapter.SubConversationListAdapter.ViewHolder holder = new io.rong.imkit.widget.adapter.SubConversationListAdapter.ViewHolder();
        holder.layout = this.findViewById(result, R.id.rc_item_conversation);
        holder.leftImageLayout = this.findViewById(result, R.id.rc_item1);
        holder.rightImageLayout = this.findViewById(result, R.id.rc_item2);
        holder.leftUnReadView = this.findViewById(result, R.id.rc_unread_view_left);
        holder.rightUnReadView = this.findViewById(result, R.id.rc_unread_view_right);
        holder.leftImageView = (AsyncImageView) this.findViewById(result, R.id.rc_left);
        holder.rightImageView = (AsyncImageView) this.findViewById(result, R.id.rc_right);
        holder.contentView = (ProviderContainerView) this.findViewById(result, R.id.rc_content);
        holder.unReadMsgCount = (TextView) this.findViewById(result, R.id.rc_unread_message);
        holder.unReadMsgCountRight = (TextView) this.findViewById(result, R.id.rc_unread_message_right);
        holder.unReadMsgCountIcon = (ImageView) this.findViewById(result, R.id.rc_unread_message_icon);
        holder.unReadMsgCountRightIcon = (ImageView) this.findViewById(result, R.id.rc_unread_message_icon_right);
        result.setTag(holder);
        return result;
    }

    protected void bindView(View v, int position, UIConversation data) {
        io.rong.imkit.widget.adapter.SubConversationListAdapter.ViewHolder holder = (io.rong.imkit.widget.adapter.SubConversationListAdapter.ViewHolder) v.getTag();
        IContainerItemProvider provider = RongContext.getInstance().getConversationTemplate(data.getConversationType().getName());
        View view = holder.contentView.inflate(provider);
        provider.bindView(view, position, data);
        if (data.isTop()) {
            holder.layout.setBackgroundColor(this.mContext.getResources().getColor(R.color.rc_conversation_top_bg));
        } else {
            holder.layout.setBackgroundColor(this.mContext.getResources().getColor(R.color.rc_text_color_primary_inverse));
        }

        ConversationProviderTag tag = RongContext.getInstance().getConversationProviderTag(data.getConversationType().getName());
        int defaultId;
        if (data.getConversationType() == ConversationType.GROUP) {
            defaultId = R.drawable.rc_default_group_portrait;
        } else if (data.getConversationType() == ConversationType.DISCUSSION) {
            defaultId = R.drawable.rc_default_discussion_portrait;
        } else {
            defaultId = R.drawable.rc_default_portrait;
        }

        if (tag.portraitPosition() == 1) {
            holder.leftImageLayout.setVisibility(View.VISIBLE);
            if (data.getIconUrl() != null) {
                holder.leftImageView.setAvatar(data.getIconUrl().toString(), defaultId);
            } else {
                holder.leftImageView.setAvatar((String) null, defaultId);
            }

            if (data.getUnReadMessageCount() > 0) {
                holder.unReadMsgCountIcon.setVisibility(View.VISIBLE);
                this.setUnReadViewLayoutParams(holder.leftUnReadView, data.getUnReadType());
                if (data.getUnReadType().equals(UnreadRemindType.REMIND_WITH_COUNTING)) {
                    holder.unReadMsgCount.setVisibility(View.VISIBLE);
                    if (data.getUnReadMessageCount() > 99) {
                        holder.unReadMsgCount.setText(this.mContext.getResources().getString(R.string.rc_message_unread_count));
                    } else {
                        holder.unReadMsgCount.setText(Integer.toString(data.getUnReadMessageCount()));
                    }

                    holder.unReadMsgCountIcon.setImageResource(R.drawable.rc_unread_count_bg);
                } else {
                    holder.unReadMsgCount.setVisibility(View.GONE);
                    holder.unReadMsgCountIcon.setImageResource(R.drawable.rc_unread_remind_without_count);
                }
            } else {
                holder.unReadMsgCountIcon.setVisibility(View.GONE);
                holder.unReadMsgCount.setVisibility(View.GONE);
            }

            holder.rightImageLayout.setVisibility(View.GONE);
        } else if (tag.portraitPosition() == 2) {
            holder.rightImageLayout.setVisibility(View.VISIBLE);
            if (data.getIconUrl() != null) {
                holder.rightImageView.setAvatar(data.getIconUrl().toString(), defaultId);
            } else {
                holder.rightImageView.setAvatar((String) null, defaultId);
            }

            if (data.getUnReadMessageCount() > 0) {
                holder.unReadMsgCountRight.setVisibility(View.VISIBLE);
                holder.unReadMsgCountIcon.setVisibility(View.VISIBLE);
                this.setUnReadViewLayoutParams(holder.rightUnReadView, data.getUnReadType());
                if (data.getUnReadType().equals(UnreadRemindType.REMIND_WITH_COUNTING)) {
                    if (data.getUnReadMessageCount() > 99) {
                        holder.unReadMsgCountRight.setText(this.mContext.getResources().getString(R.string.rc_message_unread_count));
                    } else {
                        holder.unReadMsgCountRight.setText(Integer.toString(data.getUnReadMessageCount()));
                    }

                    holder.unReadMsgCountIcon.setImageResource(R.drawable.rc_unread_count_bg);
                } else {
                    holder.unReadMsgCountIcon.setImageResource(R.drawable.rc_unread_remind_without_count);
                }
            }

            holder.leftImageLayout.setVisibility(View.GONE);
        } else {
            if (tag.portraitPosition() != 3) {
                throw new IllegalArgumentException("the portrait position is wrong!");
            }

            holder.rightImageLayout.setVisibility(View.GONE);
            holder.leftImageLayout.setVisibility(View.GONE);
        }

    }

    class ViewHolder {
        View layout;
        View leftImageLayout;
        View rightImageLayout;
        View leftUnReadView;
        View rightUnReadView;
        AsyncImageView leftImageView;
        AsyncImageView rightImageView;
        ProviderContainerView contentView;
        TextView unReadMsgCount;
        TextView unReadMsgCountRight;
        ImageView unReadMsgCountRightIcon;
        ImageView unReadMsgCountIcon;

        ViewHolder() {
        }
    }
}
