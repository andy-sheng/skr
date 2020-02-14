package com.module.msg.custom.club;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.AutoLinkTextView;
import io.rong.imkit.widget.ILinkClickListener;
import io.rong.imkit.widget.LinkTextViewMovementMethod;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imkit.widget.provider.TextMessageItemProvider;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

@ProviderTag(
        messageContent = ClubHandleMsg.class,
        showReadState = true
)
public class ClubHandleMessageItemProvider extends MessageProvider<ClubHandleMsg> {
    private static final String TAG = "ClubAgreeMessageItemProvider";

    public ClubHandleMessageItemProvider() {
    }

    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_clubhandle_message, (ViewGroup)null);
        ClubHandleMessageItemProvider.ViewHolder holder = new ClubHandleMessageItemProvider.ViewHolder(view);
        holder.mContentTv = (AutoLinkTextView)view.findViewById(R.id.content_tv);
        view.setTag(holder);
        return view;
    }

    public void onItemClick(View view, int position, ClubHandleMsg content, UIMessage message) {
    }

    public void bindView(final View v, int position, ClubHandleMsg content, final UIMessage data) {
        ClubHandleMessageItemProvider.ViewHolder holder = (ClubHandleMessageItemProvider.ViewHolder)v.getTag();
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
    public Spannable getContentSummary(ClubHandleMsg clubHandleMsg) {
        return new SpannableString(clubHandleMsg.getContent());
    }

    private static class ViewHolder {
        AutoLinkTextView mContentTv;

        private ViewHolder(View rootView) {
            mContentTv = (AutoLinkTextView)rootView.findViewById(R.id.content_tv);
        }
    }

}
