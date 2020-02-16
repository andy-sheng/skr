package com.module.msg.custom.relation;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.rong.imkit.R;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.AutoLinkTextView;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.model.Message;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

@ProviderTag(
        messageContent = RelationHandleMsg.class,
        showReadState = true
)
public class RelationHandleMessageItemProvider extends MessageProvider<RelationHandleMsg> {
    private static final String TAG = "RelationHandleMessageItemProvider";

    public RelationHandleMessageItemProvider() {
    }

    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_relation_handle_message, (ViewGroup)null);
        RelationHandleMessageItemProvider.ViewHolder holder = new RelationHandleMessageItemProvider.ViewHolder(view);
        holder.mContentTv = (AutoLinkTextView)view.findViewById(R.id.content_tv);
        view.setTag(holder);
        return view;
    }

    public void onItemClick(View view, int position, RelationHandleMsg content, UIMessage message) {
    }

    public void bindView(final View v, int position, RelationHandleMsg content, final UIMessage data) {
        RelationHandleMessageItemProvider.ViewHolder holder = (RelationHandleMessageItemProvider.ViewHolder)v.getTag();
        if (data.getMessageDirection() == Message.MessageDirection.SEND) {
            holder.mContentTv.setBackgroundResource(R.drawable.rc_ic_bubble_right);
        } else {
            holder.mContentTv.setBackgroundResource(R.drawable.rc_ic_bubble_left);
        }

        final AutoLinkTextView textView = holder.mContentTv;
        textView.setText(content.getContent());
        textView.stripUnderlines();
    }

    @Override
    public Spannable getContentSummary(RelationHandleMsg clubHandleMsg) {
        return new SpannableString(clubHandleMsg.getContent());
    }

    private static class ViewHolder {
        AutoLinkTextView mContentTv;

        private ViewHolder(View rootView) {
            mContentTv = (AutoLinkTextView)rootView.findViewById(R.id.content_tv);
        }
    }

}
