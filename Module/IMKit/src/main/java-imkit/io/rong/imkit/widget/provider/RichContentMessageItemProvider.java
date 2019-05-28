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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import io.rong.imkit.R;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider.MessageProvider;
import io.rong.imlib.model.Message.MessageDirection;
import io.rong.message.RichContentMessage;

@ProviderTag(
        messageContent = RichContentMessage.class,
        showReadState = true
)
public class RichContentMessageItemProvider extends MessageProvider<RichContentMessage> {
    private static final String TAG = "RichContentMessageItemProvider";

    public RichContentMessageItemProvider() {
    }

    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_rich_content_message, (ViewGroup) null);
        io.rong.imkit.widget.provider.RichContentMessageItemProvider.ViewHolder holder = new io.rong.imkit.widget.provider.RichContentMessageItemProvider.ViewHolder();
        holder.title = (TextView) view.findViewById(R.id.rc_title);
        holder.content = (TextView) view.findViewById(R.id.rc_content);
        holder.img = (SimpleDraweeView) view.findViewById(R.id.rc_img);
        holder.mLayout = (RelativeLayout) view.findViewById(R.id.rc_layout);
        view.setTag(holder);
        return view;
    }

    public void onItemClick(View view, int position, RichContentMessage content, UIMessage message) {
        String action = "io.io.rong.imkit.intent.action.webview";
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("url", content.getUrl());
        intent.setPackage(view.getContext().getPackageName());
        view.getContext().startActivity(intent);
    }

    public void bindView(View v, int position, RichContentMessage content, UIMessage message) {
        io.rong.imkit.widget.provider.RichContentMessageItemProvider.ViewHolder holder = (io.rong.imkit.widget.provider.RichContentMessageItemProvider.ViewHolder) v.getTag();
        holder.title.setText(content.getTitle());
        holder.content.setText(content.getContent());
        if (content.getImgUrl() != null) {
            FrescoWorker.loadImage(holder.img, ImageFactory.newHttpImage(content.getImgUrl())
                    .setScaleType(ScalingUtils.ScaleType.FIT_XY).build());
        }

        if (message.getMessageDirection() == MessageDirection.SEND) {
            holder.mLayout.setBackgroundResource(R.drawable.rc_ic_bubble_right_file);
        } else {
            holder.mLayout.setBackgroundResource(R.drawable.rc_ic_bubble_left_file);
        }

    }

    public Spannable getContentSummary(RichContentMessage data) {
        return null;
    }

    public Spannable getContentSummary(Context context, RichContentMessage data) {
        String text = context.getResources().getString(R.string.rc_message_content_rich_text);
        return new SpannableString(text);
    }

    private static class ViewHolder {
        SimpleDraweeView img;
        TextView title;
        TextView content;
        RelativeLayout mLayout;

        private ViewHolder() {
        }
    }
}
