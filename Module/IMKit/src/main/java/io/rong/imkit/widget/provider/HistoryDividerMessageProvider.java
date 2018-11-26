//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.message.HistoryDividerMessage;

@ProviderTag(
        messageContent = HistoryDividerMessage.class,
        showPortrait = false,
        centerInHorizontal = true,
        showProgress = false,
        showSummaryWithName = false
)
public class HistoryDividerMessageProvider extends MessageProvider<HistoryDividerMessage> {
    public HistoryDividerMessageProvider() {
    }

    public void bindView(View view, int i, HistoryDividerMessage newMessageDivider, UIMessage uiMessage) {
        io.rong.imkit.widget.provider.HistoryDividerMessageProvider.ViewHolder viewHolder = (io.rong.imkit.widget.provider.HistoryDividerMessageProvider.ViewHolder) view.getTag();
        Context context = RongContext.getInstance();
        viewHolder.contentTextView.setText(newMessageDivider.getContent());
    }

    public Spannable getContentSummary(HistoryDividerMessage historyDividerMessage) {
        return null;
    }

    public void onItemClick(View view, int i, HistoryDividerMessage historyDividerMessage, UIMessage uiMessage) {
    }

    public void onItemLongClick(View view, int i, HistoryDividerMessage historyDividerMessage, UIMessage uiMessage) {
    }

    public View newView(Context context, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_new_message_divider, (ViewGroup) null);
        io.rong.imkit.widget.provider.HistoryDividerMessageProvider.ViewHolder viewHolder = new io.rong.imkit.widget.provider.HistoryDividerMessageProvider.ViewHolder();
        viewHolder.contentTextView = (TextView) view.findViewById(R.id.tv_divider_message);
        viewHolder.contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
        view.setTag(viewHolder);
        return view;
    }

    private static class ViewHolder {
        TextView contentTextView;

        private ViewHolder() {
        }
    }
}
