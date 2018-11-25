//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.rong.common.RLog;
import io.rong.imkit.R;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utilities.RongUtils;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.message.PublicServiceRichContentMessage;

@ProviderTag(
        messageContent = PublicServiceRichContentMessage.class,
        showPortrait = false,
        centerInHorizontal = true,
        showSummaryWithName = false
)
public class PublicServiceRichContentMessageProvider extends MessageProvider<PublicServiceRichContentMessage> {
  private final String TAG = this.getClass().getSimpleName();

  public PublicServiceRichContentMessageProvider() {
  }

  public View newView(Context context, ViewGroup group) {
    io.rong.imkit.widget.provider.PublicServiceRichContentMessageProvider.ViewHolder holder = new io.rong.imkit.widget.provider.PublicServiceRichContentMessageProvider.ViewHolder();
    View view = LayoutInflater.from(context).inflate(R.layout.rc_item_public_service_rich_content_message, (ViewGroup)null);
    holder.title = (TextView)view.findViewById(R.id.rc_title);
    holder.time = (TextView)view.findViewById(R.id.rc_time);
    holder.description = (TextView)view.findViewById(R.id.rc_content);
    holder.imageView = (AsyncImageView)view.findViewById(R.id.rc_img);
    int w = RongUtils.getScreenWidth() - RongUtils.dip2px(32.0F);
    view.setLayoutParams(new LayoutParams(w, -2));
    view.setTag(holder);
    return view;
  }

  public void bindView(View v, int position, PublicServiceRichContentMessage content, UIMessage message) {
    io.rong.imkit.widget.provider.PublicServiceRichContentMessageProvider.ViewHolder holder = (io.rong.imkit.widget.provider.PublicServiceRichContentMessageProvider.ViewHolder)v.getTag();
    PublicServiceRichContentMessage msg = (PublicServiceRichContentMessage)message.getContent();
    if (msg.getMessage() != null) {
      holder.title.setText(msg.getMessage().getTitle());
      holder.description.setText(msg.getMessage().getDigest());
      holder.imageView.setResource(msg.getMessage().getImageUrl(), 0);
    }

    String time = this.formatDate(message.getReceivedTime(), "MM月dd日 HH:mm");
    holder.time.setText(time);
  }

  private String formatDate(long timeMillis, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(new Date(timeMillis));
  }

  public Spannable getContentSummary(PublicServiceRichContentMessage data) {
    return null;
  }

  public Spannable getContentSummary(Context context, PublicServiceRichContentMessage data) {
    if (data != null && data.getMessage() != null) {
      return new SpannableString(data.getMessage().getTitle());
    } else {
      RLog.e(this.TAG, "The content of the message is null! Check your message content!");
      return new SpannableString("");
    }
  }

  public void onItemClick(View view, int position, PublicServiceRichContentMessage content, UIMessage message) {
    String url = content.getMessage().getUrl();
    String action = "io.io.rong.imkit.intent.action.webview";
    Intent intent = new Intent(action);
    intent.setPackage(view.getContext().getPackageName());
    intent.putExtra("url", url);
    view.getContext().startActivity(intent);
  }

  public static class ViewHolder {
    public TextView title;
    public AsyncImageView imageView;
    public TextView time;
    public TextView description;

    public ViewHolder() {
    }
  }
}
