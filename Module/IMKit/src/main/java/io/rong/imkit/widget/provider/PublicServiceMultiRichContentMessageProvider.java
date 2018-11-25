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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.R;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utilities.RongUtils;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.message.PublicServiceMultiRichContentMessage;
import io.rong.message.RichContentItem;

@ProviderTag(
        messageContent = PublicServiceMultiRichContentMessage.class,
        showPortrait = false,
        centerInHorizontal = true,
        showSummaryWithName = false
)
public class PublicServiceMultiRichContentMessageProvider extends MessageProvider<PublicServiceMultiRichContentMessage> {
  public PublicServiceMultiRichContentMessageProvider() {
  }

  public void bindView(final View v, int position, PublicServiceMultiRichContentMessage content, UIMessage message) {
    io.rong.imkit.widget.provider.PublicServiceMultiRichContentMessageProvider.ViewHolder vh = (io.rong.imkit.widget.provider.PublicServiceMultiRichContentMessageProvider.ViewHolder)v.getTag();
    final ArrayList<RichContentItem> msgList = content.getMessages();
    if (msgList.size() > 0) {
      vh.tv.setText(((RichContentItem)msgList.get(0)).getTitle());
      vh.iv.setResource(((RichContentItem)msgList.get(0)).getImageUrl(), 0);
    }

    LayoutParams params = v.getLayoutParams();
    io.rong.imkit.widget.provider.PublicServiceMultiRichContentMessageProvider.PublicAccountMsgAdapter mAdapter = new io.rong.imkit.widget.provider.PublicServiceMultiRichContentMessageProvider.PublicAccountMsgAdapter(v.getContext(), msgList);
    vh.lv.setAdapter(mAdapter);
    vh.lv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RichContentItem item = (RichContentItem)msgList.get(position + 1);
        String url = item.getUrl();
        String action = "io.io.rong.imkit.intent.action.webview";
        Intent intent = new Intent(action);
        intent.setPackage(v.getContext().getPackageName());
        intent.putExtra("url", url);
        v.getContext().startActivity(intent);
      }
    });
    int height = this.getListViewHeight(vh.lv) + vh.height;
    params.height = height;
    params.width = RongUtils.getScreenWidth() - RongUtils.dip2px(32.0F);
    v.setLayoutParams(params);
    v.requestLayout();
  }

  private int getListViewHeight(ListView list) {
    int totalHeight = 0;
    ListAdapter adapter = list.getAdapter();

    for(int i = 0; i < adapter.getCount(); ++i) {
      View item = adapter.getView(i, (View)null, list);
      totalHeight += item.getLayoutParams().height;
    }

    return totalHeight;
  }

  public Spannable getContentSummary(PublicServiceMultiRichContentMessage data) {
    return null;
  }

  public Spannable getContentSummary(Context context, PublicServiceMultiRichContentMessage data) {
    List<RichContentItem> list = data.getMessages();
    return list.size() > 0 ? new SpannableString(((RichContentItem)data.getMessages().get(0)).getTitle()) : null;
  }

  public void onItemClick(View view, int position, PublicServiceMultiRichContentMessage content, UIMessage message) {
    if (content.getMessages().size() != 0) {
      String url = ((RichContentItem)content.getMessages().get(0)).getUrl();
      String action = "io.io.rong.imkit.intent.action.webview";
      Context context = view.getContext();
      Intent intent = new Intent(action);
      intent.setPackage(context.getPackageName());
      intent.putExtra("url", url);
      context.startActivity(intent);
    }
  }

  public View newView(Context context, ViewGroup group) {
    io.rong.imkit.widget.provider.PublicServiceMultiRichContentMessageProvider.ViewHolder holder = new io.rong.imkit.widget.provider.PublicServiceMultiRichContentMessageProvider.ViewHolder();
    View view = LayoutInflater.from(context).inflate(R.layout.rc_item_public_service_multi_rich_content_message, (ViewGroup)null);
    holder.lv = (ListView)view.findViewById(R.id.rc_list);
    holder.iv = (AsyncImageView)view.findViewById(R.id.rc_img);
    holder.tv = (TextView)view.findViewById(R.id.rc_txt);
    view.measure(0, 0);
    holder.height = view.getMeasuredHeight();
    view.setTag(holder);
    return view;
  }

  private static class PublicAccountMsgAdapter extends BaseAdapter {
    LayoutInflater inflater;
    ArrayList<RichContentItem> itemList;
    int itemCount;

    public PublicAccountMsgAdapter(Context context, ArrayList<RichContentItem> msgList) {
      this.inflater = LayoutInflater.from(context);
      this.itemList = new ArrayList();
      this.itemList.addAll(msgList);
      this.itemCount = msgList.size() - 1;
    }

    public int getCount() {
      return this.itemCount;
    }

    public RichContentItem getItem(int position) {
      return this.itemList.size() == 0 ? null : (RichContentItem)this.itemList.get(position + 1);
    }

    public long getItemId(int position) {
      return 0L;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      convertView = this.inflater.inflate(R.layout.rc_item_public_service_message, parent, false);
      AsyncImageView iv = (AsyncImageView)convertView.findViewById(R.id.rc_img);
      TextView tv = (TextView)convertView.findViewById(R.id.rc_txt);
      View divider = convertView.findViewById(R.id.rc_divider);
      if (this.itemList.size() == 0) {
        return null;
      } else {
        String title = ((RichContentItem)this.itemList.get(position + 1)).getTitle();
        if (title != null) {
          tv.setText(title);
        }

        iv.setResource(((RichContentItem)this.itemList.get(position + 1)).getImageUrl(), 0);
        if (position == this.getCount() - 1) {
          divider.setVisibility(View.GONE);
        } else {
          divider.setVisibility(View.VISIBLE);
        }

        return convertView;
      }
    }
  }

  protected static class ViewHolder {
    public int height;
    public TextView tv;
    public AsyncImageView iv;
    public View divider;
    public ListView lv;

    protected ViewHolder() {
    }
  }
}
